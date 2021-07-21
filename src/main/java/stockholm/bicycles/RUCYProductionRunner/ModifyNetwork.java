package stockholm.bicycles.RUCYProductionRunner;

import java.io.IOException;

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

import com.opencsv.exceptions.CsvException;



public class ModifyNetwork {

	public static void main(String[] args) throws IOException, CsvException {
		// TODO Auto-generated method stub
		// final String inputPath = "./ihop2/network-input/";

        
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/MatsimNetwork/network_NVDB.xml";
		// load network
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		(new MatsimNetworkReader(scenario.getNetwork())).readFile(inputNetworkFileName);
		Network network = scenario.getNetwork();

		
//		// need to add one link that is missing in the network.
//		Node fromNode = network.getNodes().get(Id.create("89135", Node.class));
//		Node toNode=network.getNodes().get(Id.create("89344", Node.class));
//		final Link matsimABLink = network.getFactory().createLink(Id.create("223932_AB", Link.class),fromNode, toNode);
//		matsimABLink.setLength(240.0);
//		matsimABLink.setFreespeed(17/3.6);  
//		matsimABLink.getAttributes().putAttribute("generalizedCost",240.0);
//		final Link matsimBALink = network.getFactory().createLink(Id.create("223932_BA", Link.class), toNode,fromNode);
//		matsimBALink.setLength(240.0);
//		matsimBALink.setFreespeed(17/3.6);  
//		matsimBALink.getAttributes().putAttribute("generalizedCost",240.0);
//		network.addLink(matsimABLink);
//		network.addLink(matsimBALink);
		
		Link link223932_AB = network.getLinks().get(Id.create("223932_AB", Link.class));
		link223932_AB.setLength(240.0);
		link223932_AB.getAttributes().putAttribute("generalizedCost",240.0);
		 
		Link link223932_BA = network.getLinks().get(Id.create("223932_BA", Link.class));
		link223932_BA.setLength(240.0);
		link223932_BA.getAttributes().putAttribute("generalizedCost",240.0);
		
		
        // already overwite the existing network, doing do it again!!!!
		NetworkWriter plainNetworkWriter = new NetworkWriter(network);
		plainNetworkWriter.write(inputNetworkFileName);
		
		
		

	}

}
