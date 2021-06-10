package stockholm.bicycles.io.pathIO;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.scenario.ScenarioUtils;

import com.opencsv.exceptions.CsvException;

public class PathReaderTest {

	@Test
	public void test() throws IOException, CsvException {
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/MatsimNetwork/network_NVDB.xml";
		String pathFile="//vti.se/root/RUCY/GPS data//writePath_test_20Trips.csv";
		PathReader reader = new PathReader( pathFile,inputNetworkFileName);
		HashMap<String, Path> check = reader.read();
		for (Entry<String, Path> entry:check.entrySet()) {
			// System.out.println("Trip id: "+entry.getKey()+" .");
			List<Link> links = entry.getValue().links;
			for (Link link:links) {
				System.out.println("Trip id: "+entry.getKey()+"-->" + "linkID: "+link.getId());
			}
		}
//		// fail("Not yet implemented");
		
		
//		Config config = ConfigUtils.createConfig();
//		Scenario scenario = ScenarioUtils.createScenario(config);
//		(new MatsimNetworkReader(scenario.getNetwork())).readFile(inputNetworkFileName);
//		Network network = scenario.getNetwork();
//		Link link = network.getLinks().get(Id.create("72350_AB", Link.class));
//		// TreeMap<Double, Link> outLinks = NetworkUtils.getOutLinksSortedClockwiseByAngle(link);
//		ArrayList<Link> outLinks = PathUtils.getOutLinks(link);
//		for (Link outLink: outLinks) {
//			System.out.println(outLink.getId().toString());
//		}
//		System.out.println("72350_AB"+link.getFromNode().getId().toString());
//		System.out.println("72350_AB"+link.getToNode().getId().toString());
//		
//		Link link2 = network.getLinks().get(Id.create("72348_AB", Link.class));
//		System.out.println("72348_AB_"+link2.getFromNode().getId().toString());
//		System.out.println("72348_AB_"+link2.getToNode().getId().toString());
//		
//		Link link3 = network.getLinks().get(Id.create("72348_BA", Link.class));
//		System.out.println("72348_BA_"+link3.getFromNode().getId().toString());
//		System.out.println("72348_BA_"+link3.getToNode().getId().toString());
//		
//		Link link4 = network.getLinks().get(Id.create("72349_AB", Link.class));
//		System.out.println("72349_AB_"+link4.getFromNode().getId().toString());
//		System.out.println("72349_AB_"+link4.getToNode().getId().toString());
//		
//		Link link5 = network.getLinks().get(Id.create("72349_BA", Link.class));
//		System.out.println("72349_BA_"+link5.getFromNode().getId().toString());
//		System.out.println("72349_BA_"+link5.getToNode().getId().toString());
		
	}

}
