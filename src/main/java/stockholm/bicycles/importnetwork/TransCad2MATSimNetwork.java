package stockholm.bicycles.importnetwork;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import com.google.common.collect.Table;
import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.utility.CsvReaderToIteratable;

public class TransCad2MATSimNetwork {
	
	private static final Logger log = Logger.getLogger( TransCad2MATSimNetwork.class ) ;
	private final String tcNodesFileName;

	private final String tcLinksFileName;


	private final String matsimPlainNetworkFileName;

	private final String matsimFullFileName;




	public TransCad2MATSimNetwork(String tcNodesFileName, String tcLinksFileName,
			String matsimPlainNetworkFileName, String matsimFullFileName) {
		super();
		this.tcNodesFileName = tcNodesFileName;
		this.tcLinksFileName = tcLinksFileName;
		this.matsimPlainNetworkFileName = matsimPlainNetworkFileName;
		this.matsimFullFileName = matsimFullFileName;
	} // end constructor

	
	
	public void runGenerateNetwork() throws IOException, CsvException {
        
		Network matsimNetwork=this.generateNetwork();
		
		NetworkWriter plainNetworkWriter = new NetworkWriter(matsimNetwork);
		plainNetworkWriter.write(matsimPlainNetworkFileName);
		
		//----------------------------------
		
//		// (4) write the node and link attribute files
//		final ObjectAttributesXmlWriter linkAttributesWriter = new ObjectAttributesXmlWriter(linkAttributes);
//		linkAttributesWriter.writeFile(this.linkAttributesFileName);
//		
//		final ObjectAttributesXmlWriter nodeAttributesWriter = new ObjectAttributesXmlWriter(nodeAttributes);
//		nodeAttributesWriter.writeFile(this.nodeAttributesFileName);
		
	} // end run()
	
	public void runNetworkWithTurnPentalty() throws IOException, CsvException {
		Network matsimNetwork=this.generateNetwork();
		
		//to do:  recode matsimNetwork to generate a new network with turn Penalty.
		
		//1. loop each node
		//2. for each node we check the incoming and outcoming links and as long as there are more than 2 nodes connected we define this node as intersection.
		
		
		NetworkWriter plainNetworkWriter = new NetworkWriter(matsimNetwork);
		plainNetworkWriter.write(matsimPlainNetworkFileName);
	}
	
	private static double checkLength(double linkLengthMeterFromData, Link matsimABLink) {
		double outputLinklength=linkLengthMeterFromData;
		Node matsimFromNode = matsimABLink.getFromNode();
		Node matsimToNode = matsimABLink.getToNode();
		Coord fromNodeCoord = matsimFromNode.getCoord();
		Coord toNideCoord = matsimToNode.getCoord();
		double beelineDistance = Math.sqrt(Math.pow((fromNodeCoord.getX() - toNideCoord.getX()),2)+Math.pow((fromNodeCoord.getY() - toNideCoord.getY()),2));
		if (linkLengthMeterFromData<beelineDistance) {
			outputLinklength=beelineDistance;
			log.warn("Link ID: "+matsimABLink.getId().toString()+" has SHAPE_LEN: "+linkLengthMeterFromData + ". The beeline distance is: "+beelineDistance);
		}
		return outputLinklength;
		
	}
	
	public Network generateNetwork() throws IOException, CsvException {
		final Network matsimNetwork = NetworkUtils.createNetwork();
		final NetworkFactory matsimNetworkFactory = matsimNetwork.getFactory();
		
		CsvReaderToIteratable nodeReader = new CsvReaderToIteratable(this.tcNodesFileName,',');
		Table<String, String, String> nodeTable = nodeReader.readTableWithUniqueID("ID");

		final CoordinateTransformation coordinateTransform = StockholmTransformationFactory.getCoordinateTransformation(
				StockholmTransformationFactory.WGS84, StockholmTransformationFactory.WGS84_SWEREF99);
        
		// (1) Save the nodes into Matsim nodes
		//-----------------------------------
		Set<String> TransCadNodeIDSet=nodeTable.rowKeySet();
		for (String TransCadNodeID: TransCadNodeIDSet) {
			Map<String, String> ANode = nodeTable.row(TransCadNodeID); 
			// transform each node into Matsim node object
			double NodeY= Double.parseDouble(ANode.get("Latitude"));
			double NodeX= Double.parseDouble(ANode.get("Longitude"));		
			// Coord coord = new Coord(NodeX,NodeY);
			
			final Coord coord = coordinateTransform.transform(new Coord(1e-6*NodeX, 1e-6*NodeY));
			final Node matsimNode = matsimNetworkFactory.createNode(Id.create(TransCadNodeID, Node.class),coord);
			
			
			String NodeAltitude= ANode.get("Z");	
			String CentroidID= ANode.get("Grid_centroid_ID");	
			// nodeAttributes.putAttribute(TransCadNodeID, "Altitude",NodeAltitude);
			// nodeAttributes.putAttribute(TransCadNodeID, "CentroidID",CentroidID);
			
			
			
			matsimNode.getAttributes().putAttribute("Altitude",NodeAltitude);
			matsimNode.getAttributes().putAttribute("CentroidID",CentroidID);
			matsimNetwork.addNode(matsimNode);
			System.out.println("Node added: "+TransCadNodeID);
		}
		
		nodeTable=null;
		//----------------------------------
		
		// (2) Save the links into Matsim links
		//----------------------------------
		// to create matsim links you need 3 elements, ID (string) fromNode (Node) and toNode(Node)
		Set<String> allowedModes = new HashSet<>(Arrays.asList("bike"));
		
		CsvReaderToIteratable linkReader = new CsvReaderToIteratable(this.tcLinksFileName,',');
		Table<String, String, String> linkTable = linkReader.readTableWithUniqueID("ID");
		Set<String> TransCadLinkIDSet=linkTable.rowKeySet();
		for (String TransCadLinkID: TransCadLinkIDSet) {
			Map<String, String> ALink = linkTable.row(TransCadLinkID); 
			String FromNode= ALink.get("From ID");
			String ToNode= ALink.get("To ID");	
			
			
			final Node matsimFromNode = matsimNetwork.getNodes().get(Id.create(FromNode, Node.class));
			final Node matsimToNode = matsimNetwork.getNodes().get(Id.create(ToNode, Node.class));	
			
			// create a AB link
			String TransCadLinkID_AB=TransCadLinkID+"_AB";
			final Link matsimABLink = matsimNetworkFactory.createLink(Id.create(TransCadLinkID_AB, Link.class),
					matsimFromNode, matsimToNode);
			// set link length and speed as default attribute to links
			double linkLengthFromDataMeter= Double.parseDouble(ALink.get("SHAPE_LEN"));
			double linkLength = checkLength(linkLengthFromDataMeter,matsimABLink);
			
			double linkTravelTimeMin_AB= Double.parseDouble(ALink.get("AB_cykelrestid"));
			double bicycleSpeedM_S_AB= Double.parseDouble(ALink.get("AB_cykelspeed"))/3.6;// change back to: double bicycleSpeedM_S= Double.parseDouble(ALink.get("bicycleSpeed")) * Units.M_S_PER_KM_H;
			matsimABLink.setLength(linkLength); // change back to: matsimLink.setLength(LinkLengthKM * Units.M_PER_KM);
			matsimABLink.setFreespeed(bicycleSpeedM_S_AB);  
			matsimABLink.setAllowedModes(allowedModes);
			
			
			
			// specify which other attributes you want to save as link attributes
			double bicycleGeneralizedCost_AB= Double.parseDouble(ALink.get("AB_GK_Broach"));
			String linkType= ALink.get("link_type");	
			String lutning_AB= ALink.get("AB_slope");	
			String connector= ALink.get("Skaft");	
			
//			linkAttributes.putAttribute(TransCadLinkID, "bicycleSpeed_M_S",bicycleSpeedM_S);
//			linkAttributes.putAttribute(TransCadLinkID, "generalizedCost",bicycleGeneralizedCost);
//			linkAttributes.putAttribute(TransCadLinkID, "linkType",linkType);
//			linkAttributes.putAttribute(TransCadLinkID, "slope",lutning);
//			linkAttributes.putAttribute(TransCadLinkID, "connector",connector);
			
			// put link attributes
			matsimABLink.getAttributes().putAttribute("SHAPE_LEN",linkLengthFromDataMeter);
			matsimABLink.getAttributes().putAttribute("bicycleSpeed_M_S",bicycleSpeedM_S_AB);
			matsimABLink.getAttributes().putAttribute("generalizedCost",bicycleGeneralizedCost_AB);
			matsimABLink.getAttributes().putAttribute("linkType",linkType);
			matsimABLink.getAttributes().putAttribute("slope",lutning_AB);
			matsimABLink.getAttributes().putAttribute("connector",connector);
			matsimNetwork.addLink(matsimABLink);
			System.out.println("AB_Link added: "+TransCadLinkID);
			
			
			// add a BA_link
			String TransCadLinkID_BA=TransCadLinkID+"_BA";
			final Link matsimBALink = matsimNetworkFactory.createLink(Id.create(TransCadLinkID_BA, Link.class),
					 matsimToNode,matsimFromNode);
			double linkTravelTimeMin_BA= Double.parseDouble(ALink.get("BA_cykelrestid"));
			double bicycleSpeedM_S_BA= Double.parseDouble(ALink.get("BA_cykelspeed"))/3.6;// change back to: double bicycleSpeedM_S= Double.parseDouble(ALink.get("bicycleSpeed")) * Units.M_S_PER_KM_H;
			
			
			matsimBALink.setLength(linkLength); // change back to: matsimLink.setLength(LinkLengthKM * Units.M_PER_KM);
			matsimBALink.setFreespeed(bicycleSpeedM_S_BA); 
			matsimBALink.setAllowedModes(allowedModes);
			
			
			double bicycleGeneralizedCost_BA= Double.parseDouble(ALink.get("BA_GK_Broach"));
			String lutning_BA= ALink.get("BA_slope");
			
			matsimBALink.getAttributes().putAttribute("SHAPE_LEN",linkLengthFromDataMeter);
			matsimBALink.getAttributes().putAttribute("bicycleSpeed_M_S",bicycleSpeedM_S_BA);
			matsimBALink.getAttributes().putAttribute("generalizedCost",bicycleGeneralizedCost_BA);
			matsimBALink.getAttributes().putAttribute("linkType",linkType);
			matsimBALink.getAttributes().putAttribute("slope",lutning_BA);
			matsimBALink.getAttributes().putAttribute("connector",connector);
			matsimNetwork.addLink(matsimBALink);
			System.out.println("BA_Link added: "+TransCadLinkID);
		}
		
		linkTable=null;
		
		
		// (3) write matsim network
		NetworkWriter networkWriter = new NetworkWriter(matsimNetwork);
		networkWriter.write(this.matsimFullFileName);
		System.out.println();
		System.out.println("------------------------------------------------------------");
		System.out.println("RAW MATSIM NETWORK STATISTICS");
		System.out.println("(This network is saved as " + this.matsimFullFileName + ".)");
		System.out.println("Number of nodes: " + matsimNetwork.getNodes().size());
		System.out.println("Number of links: " + matsimNetwork.getLinks().size());
		System.out.println("------------------------------------------------------------");
		System.out.println();
		
		NetworkCleaner cleaner = new NetworkCleaner();
		cleaner.run(matsimNetwork);

		System.out.println();
		System.out.println("------------------------------------------------------------");
		System.out.println("MATSIM NETWORK STATISTICS AFTER NETWORK CLEANING");
		System.out.println("(This network is not saved to file.)");
		System.out.println("Number of nodes: " + matsimNetwork.getNodes().size());
		System.out.println("Number of links: " + matsimNetwork.getLinks().size());
		System.out.println("------------------------------------------------------------");
		System.out.println();
		return matsimNetwork;
		
	}


	










}
