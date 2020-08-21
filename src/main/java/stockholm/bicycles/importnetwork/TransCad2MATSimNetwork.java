package stockholm.bicycles.importnetwork;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import org.matsim.utils.objectattributes.ObjectAttributes;

import com.google.common.collect.Table;
import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.utility.CsvReaderToIteratable;

public class TransCad2MATSimNetwork {
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
		
		//to do:  recode matsimNetwork to generate a new network with Penalty.
		
		//1. loop each node
		//2. for each node we check the incoming and outcoming links and as long as there are more than 2 nodes connected we define this node as intersection.
		
		
		NetworkWriter plainNetworkWriter = new NetworkWriter(matsimNetwork);
		plainNetworkWriter.write(matsimPlainNetworkFileName);
	}
	
	public Network generateNetwork() throws IOException, CsvException {
		final Network matsimNetwork = NetworkUtils.createNetwork();
		final NetworkFactory matsimNetworkFactory = matsimNetwork.getFactory();
		final ObjectAttributes linkAttributes = new ObjectAttributes();
		final ObjectAttributes nodeAttributes = new ObjectAttributes();
		
		CsvReaderToIteratable nodeReader = new CsvReaderToIteratable(this.tcNodesFileName,';');
		Table<String, String, String> nodeTable = nodeReader.readTableWithUniqueID("TransCadID");

		final CoordinateTransformation coordinateTransform = StockholmTransformationFactory.getCoordinateTransformation(
				StockholmTransformationFactory.WGS84, StockholmTransformationFactory.WGS84_SWEREF99);
        
		// (1) Save the nodes into Matsim nodes
		//-----------------------------------
		Set<String> TransCadNodeIDSet=nodeTable.rowKeySet();
		for (String TransCadNodeID: TransCadNodeIDSet) {
			Map<String, String> ANode = nodeTable.row(TransCadNodeID); 
			// transform each node into Matsim node object
			double NodeLatitude= Double.parseDouble(ANode.get("latitude"));
			double Nodelongtitude= Double.parseDouble(ANode.get("longtitude"));		
			
			final Coord coord = coordinateTransform.transform(new Coord(1e-6*Nodelongtitude, 1e-6*NodeLatitude));
			final Node matsimNode = matsimNetworkFactory.createNode(Id.create(TransCadNodeID, Node.class),coord);
			
			
			String NodeAltitude= ANode.get("altitude");	
			String CentroidID= ANode.get("CentroidID");	
			nodeAttributes.putAttribute(TransCadNodeID, "Altitude",NodeAltitude);
			nodeAttributes.putAttribute(TransCadNodeID, "CentroidID",CentroidID);
			
			
			
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
		
		CsvReaderToIteratable linkReader = new CsvReaderToIteratable(this.tcLinksFileName,';');
		Table<String, String, String> linkTable = linkReader.readTableWithUniqueID("TransCadID");
		Set<String> TransCadLinkIDSet=linkTable.rowKeySet();
		for (String TransCadLinkID: TransCadLinkIDSet) {
			Map<String, String> ALink = linkTable.row(TransCadLinkID); 
			String FromNode= ALink.get("fromNode");
			String ToNode= ALink.get("toNode");	
			
			// create a link
			final Node matsimFromNode = matsimNetwork.getNodes().get(Id.create(FromNode, Node.class));
			final Node matsimToNode = matsimNetwork.getNodes().get(Id.create(ToNode, Node.class));		
			final Link matsimLink = matsimNetworkFactory.createLink(Id.create(TransCadLinkID, Link.class),
					matsimFromNode, matsimToNode);
			// set link length and speed as default attribute to links
			double LinkLengthKM= Double.parseDouble(ALink.get("length"));
			double LinkTravelTimeMin= Double.parseDouble(ALink.get("bicycleTravelTime"));
			matsimLink.setLength(LinkLengthKM*1000); // change back to: matsimLink.setLength(LinkLengthKM * Units.M_PER_KM);
			matsimLink.setFreespeed(LinkLengthKM/(LinkTravelTimeMin/60)/3.6);   // change back to: matsimLink.setFreespeed(LinkFreeSpeedKM_H * Units.M_S_PER_KM_H);  when GunnarRepo is updated.
			matsimLink.setAllowedModes(allowedModes);
			
			
			// specify which other attributes you want to save as link attributes
			double bicycleSpeedM_S= Double.parseDouble(ALink.get("bicycleSpeed"))/3.6;// change back to: double bicycleSpeedM_S= Double.parseDouble(ALink.get("bicycleSpeed")) * Units.M_S_PER_KM_H;
			double bicycleGeneralizedCost= Double.parseDouble(ALink.get("generalizedCost"));
			String linkType= ALink.get("linkType");	
			String lutning= ALink.get("lutning");	
			String connector= ALink.get("skaft");	
			
			linkAttributes.putAttribute(TransCadLinkID, "bicycleSpeed_M_S",bicycleSpeedM_S);
			linkAttributes.putAttribute(TransCadLinkID, "generalizedCost",bicycleGeneralizedCost);
			linkAttributes.putAttribute(TransCadLinkID, "linkType",linkType);
			linkAttributes.putAttribute(TransCadLinkID, "slope",lutning);
			linkAttributes.putAttribute(TransCadLinkID, "connector",connector);
			
			// put link attributes
			matsimLink.getAttributes().putAttribute("bicycleSpeed_M_S",bicycleSpeedM_S);
			matsimLink.getAttributes().putAttribute("generalizedCost",bicycleGeneralizedCost);
			matsimLink.getAttributes().putAttribute("linkType",linkType);
			matsimLink.getAttributes().putAttribute("slope",lutning);
			matsimLink.getAttributes().putAttribute("connector",connector);
			matsimNetwork.addLink(matsimLink);
			System.out.println("Link added: "+TransCadLinkID);
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
