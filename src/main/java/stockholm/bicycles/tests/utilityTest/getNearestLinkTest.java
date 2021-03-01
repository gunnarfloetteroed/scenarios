package stockholm.bicycles.tests.utilityTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.scenario.ScenarioUtils;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.imprtGPS.GPSReader;
import stockholm.bicycles.mapmatching.GPSPoint;
import stockholm.bicycles.mapmatching.GPSSequence;
import stockholm.bicycles.routing.TravelDisutilityBicycle;

public class getNearestLinkTest {

	public static void main(String[] args) throws IOException, CsvException {
		// TODO Auto-generated method stub
		// read GPS data
		String inputGPSFileName="//vti.se/root/RUCY/GPS data/cykel_filtered_oneTrip.csv";
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/network_NVDB.xml";
		GPSReader reader = new GPSReader(inputGPSFileName);
		List<GPSSequence> GPSSequences = reader.read(30);
		for (GPSSequence GPSSequence: GPSSequences) {
			// GPSSequence.printInfo();
		}
		// load network
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		(new MatsimNetworkReader(scenario.getNetwork())).readFile(inputNetworkFileName);
		Network network = scenario.getNetwork();

		TravelDisutility travelDisutility = new TravelDisutilityBicycle("generalizedCost");
		long startTime = System.nanoTime();
		
		// test the method to get nearest link
		GPSSequence oneSequence=GPSSequences.get(0);
		List<GPSPoint> points = oneSequence.getGPSPoints();
		
		List<Node> nodes=new ArrayList<Node>();
		
		Node node1 = NetworkUtils.getNearestNode(network, points.get(0).getCoord());
		System.out.println(node1.getId());
		Node node2 = NetworkUtils.getNearestNode(network, points.get(0).getCoord());
		nodes.add(node1);
		if (nodes.contains(node2)) {
			System.out.println("Nodes contained.");
		}
		
		Node node3 = NetworkUtils.getNearestNode(network, points.get(100).getCoord());
		System.out.println(node3.getId());
		Node node4 = NetworkUtils.getNearestNode(network, points.get(0).getCoord());
		nodes.add(node3);
		node3=node4;
		for (Node node : nodes) {
			System.out.println(node.getId());
		}
//		int counter=1;
//		for (GPSPoint point: points) {
//			Link link = NetworkUtils.getNearestLink(network, point.getCoord());
//			// System.out.println("No."+ counter+": link ID: "+link.getId());
//			Link linkExactly = NetworkUtils.getNearestLinkExactly(network, point.getCoord());
//			// System.out.println("No."+ counter+": link exactly ID: "+link.getId());
//			if (!link.getId().equals(linkExactly.getId())) {
//				System.out.println("No."+ counter+": link and link exactly do not have the same ID: "+"link id: "+link.getId()+ ". link exactly id: "+linkExactly.getId());
//			}
//			counter++;
//		}
		
		

	}

}
