package stockholm.bicycles.tests.matmatchingTest;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.misc.OptionalTime;


import stockholm.bicycles.mapmatching.GPSPoint;
import stockholm.bicycles.mapmatching.GPSSequence;
import stockholm.bicycles.mapmatching.MySpatialTemporalGPSSequenceMapMatcher;
import stockholm.bicycles.mapmatching.ShortestPathGPSSequenceMapMatcher;
import stockholm.bicycles.mapmatching.SpatialTemporalGPSSequenceMapMatcher;
import stockholm.bicycles.routing.MatsimDijkstra;
import stockholm.bicycles.routing.TravelDisutilityBicycle;

public class mapMatchingSimpleNetworkTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		// final String inputPath = "./ihop2/network-input/";
		String inputPath = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\";
		inputPath = inputPath.replaceAll("\\\\", "/");
		final String networkSaveFile = inputPath + "RouteChoiceTestData/TestMapMatchningNetwork.xml";
		final String GPSnetworkSaveFile = inputPath + "RouteChoiceTestData/TestGPSPoints.xml";
		final String networkSaveFile1 = inputPath + "RouteChoiceTestData/TestMapMatchningNetwork_withMatchedLinks.xml";
		Network network = createSimpleNetwork(networkSaveFile);
		GPSSequence GPSSequence = createSimpleGPSdata(); 
		
		// plot the GPS points
		Network net = NetworkUtils.createNetwork();
		NetworkFactory fac = net.getFactory();
		List<GPSPoint> GPSpoints = GPSSequence.getGPSPoints();
		// create nodes
		int counter=1;
		for (GPSPoint GPSpoint : GPSpoints) {
			String nodeID="N_"+counter;
			Node node = fac.createNode(Id.createNodeId(nodeID), GPSpoint.getCoord());
			net.addNode(node);
			counter++;
		}
		NetworkWriter plainNetworkWriter = new NetworkWriter(net);
		plainNetworkWriter.write(GPSnetworkSaveFile);
		TravelDisutility travelDisutility = new TravelDisutilityBicycle("generalizedCost");
		
//		Map<Id<Node>, ? extends Node> nodes = network.getNodes();
//		MatsimDijkstra dijkstraRouter = new MatsimDijkstra(network, travelDisutility, null);
//		
//		Path testPath1 = dijkstraRouter.calcLeastCostPath(nodes.get(Id.createNodeId("N_50_0")), nodes.get(Id.createNodeId("N_40_30")), 0, null, null);
		
		
		// call the mapmatching method
		long startTime = System.nanoTime();
//		MatsimDijkstra dijkstraRouter = new MatsimDijkstra(network, travelDisutility, null);
//		for( Node node:network.getNodes().values()) {
//			Path path=dijkstraRouter.calcLeastCostPath(node, node, 0, null, null);
//			List<Link> links = path.links;
//			List<Node> nodes = path.nodes;
//			System.out.println(links.toString());
//			break;
//		}
		
		MySpatialTemporalGPSSequenceMapMatcher matcher = new MySpatialTemporalGPSSequenceMapMatcher(network,GPSSequence,travelDisutility);
		// ShortestPathGPSSequenceMapMatcher matcher = new ShortestPathGPSSequenceMapMatcher(network,GPSSequence,travelDisutility);
		Path testPath = matcher.mapMatching();
		long endTime   = System.nanoTime();
		System.out.println("Total run time: "+(endTime-startTime)/10e9);
		System.out.println(testPath.travelCost);
		System.out.println(testPath.travelTime);
		List<Link> linksInPath = testPath.links;
		
		Map<Id<Link>, ? extends Link> networkLinks = network.getLinks();
		for (Link link : linksInPath) {
			System.out.println("link: "+link.getId());
			networkLinks.get(link.getId()).getAttributes().putAttribute("matched",1);
		}
		List<Node> nodeInPath = testPath.nodes;
		for (Node node :nodeInPath) {
			System.out.println("node: "+node.getId());
		}
		
		NetworkWriter plainNetworkWriter1 = new NetworkWriter(network);
		plainNetworkWriter1.write(networkSaveFile1);

	}

	public static Network createSimpleNetwork(String matsimPlainNetworkFileName) throws IOException {
		// create an empty network
		Network net = NetworkUtils.createNetwork();
		NetworkFactory fac = net.getFactory();
		Set<String> allowedModes = new HashSet<>(Arrays.asList("bike"));
		// create nodes
		for (int y=0;y<=5;y++) {
			for (int x=0;x<=10;x++) {
				String nodeID="N_"+Integer.toString(10*x)+"_"+Integer.toString(10*y);
				Node node = fac.createNode(Id.createNodeId(nodeID), new Coord(10*x, 10*y));
				net.addNode(node);
			}
		}

		// create links X direction
		Map<Id<Node>, ? extends Node> nodes = net.getNodes();
	    for (int y=0;y<=5;y++) {
			for (int x=0;x<=9;x++) {
				String nodeID_1="N_"+Integer.toString(10*x)+"_"+Integer.toString(10*y);
				String nodeID_2="N_"+Integer.toString(10*(x+1))+"_"+Integer.toString(10*y);
				Node node_1 = nodes.get(Id.createNodeId(nodeID_1));
				Node node_2 = nodes.get(Id.createNodeId(nodeID_2));
				
				String linkID="LX_"+nodeID_1+"_to_"+nodeID_2;
				Link l = fac.createLink(Id.createLinkId(linkID), node_1, node_2);
				l.setLength(10);
				l.setFreespeed(5);  
				l.setAllowedModes(allowedModes);
				l.getAttributes().putAttribute("generalizedCost",10.0);
				l.getAttributes().putAttribute("matched",0);
				net.addLink(l);

			}
		}
	 // create links Y direction
		for (int x=0;x<=10;x++) {
			for (int y=0;y<=4;y++) {
				String nodeID_1="N_"+Integer.toString(10*x)+"_"+Integer.toString(10*y);
				String nodeID_2="N_"+Integer.toString(10*x)+"_"+Integer.toString(10*(y+1));
				Node node_1 = nodes.get(Id.createNodeId(nodeID_1));
				Node node_2 = nodes.get(Id.createNodeId(nodeID_2));
				String linkID="LY_"+nodeID_1+"_to_"+nodeID_2;
				Link l = fac.createLink(Id.createLinkId(linkID), node_1, node_2);
				l.setLength(10);
				l.setFreespeed(5);  
				l.setAllowedModes(allowedModes);
				l.getAttributes().putAttribute("generalizedCost",10.0*(y+1));
				l.getAttributes().putAttribute("matched",0);
				net.addLink(l);
			}
		}

		//save the network
		// NetworkWriter plainNetworkWriter = new NetworkWriter(net);
		// plainNetworkWriter.write(matsimPlainNetworkFileName);

		return net;
	}

	public static GPSSequence createSimpleGPSdata() {
		List<GPSPoint> gpsPoints = new ArrayList<GPSPoint>();
		for (int i=0;i<=400;i++) {

			Coord coord= new Coord(0.25*i, 0.1*i);
			OptionalTime time = OptionalTime.defined(10*i);
			double searchRadius =10;
			if (i==3) {
				searchRadius =10;
			}
			GPSPoint point = new GPSPoint(coord,time,searchRadius);
			gpsPoints.add(point);

		}
		Id<Person> personID = Id.create("P_1", Person.class);
		GPSSequence TestGPSSequence = new GPSSequence(personID,gpsPoints);
		return TestGPSSequence;
	}
	




}
