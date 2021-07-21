package stockholm.bicycles.importnetwork;

import static org.junit.Assert.*;

import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

public class TransCad2MATSimNetwork_RUCYTest {

	@Test
	public void test() {
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/MatsimNetwork/network_NVDB.xml";
		// load network
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		(new MatsimNetworkReader(scenario.getNetwork())).readFile(inputNetworkFileName);
		Network network = scenario.getNetwork();
		Link link = network.getLinks().get(Id.create("223932_AB", Link.class));
		System.out.println("fromNodeID: "+link.getFromNode().getId());  // should be 89135
		System.out.println("toNodeID: "+link.getToNode().getId()); // should be 89344
		System.out.println("length: "+link.getLength()); // should be 240
		
		
//		// need to add one link that is missing in the network.
//		Node fromNode = network.getNodes().get(Id.create("89135", Node.class));
//		Node toNode=network.getNodes().get(Id.create("89344", Node.class));
//		final Link matsimABLink = network.getFactory().createLink(Id.create("223932_AB", Link.class),fromNode, toNode);
//		matsimABLink.setLength(240);
//		matsimABLink.setFreespeed(17/3.6);  
//		matsimABLink.getAttributes().putAttribute("generalizedCost",240);
//		final Link matsimBALink = network.getFactory().createLink(Id.create("223932_BA", Link.class), toNode,fromNode);
//		matsimBALink.setLength(240);
//		matsimBALink.setFreespeed(17/3.6);  
//		matsimBALink.getAttributes().putAttribute("generalizedCost",240);
//		network.addLink(matsimABLink);
//		network.addLink(matsimBALink);
//		
//
//		NetworkWriter plainNetworkWriter = new NetworkWriter(network);
//		plainNetworkWriter.write(inputNetworkFileName);

	}

}
