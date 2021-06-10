package stockholm.bicycles.RUCYProductionRunner;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.scenario.ScenarioUtils;

import stockholm.bicycles.io.GPSIO.GPSWriter;
import stockholm.bicycles.mapmatching.GPSSequence;
import stockholm.bicycles.mapmatching.GPSSequenceGenerator;
import stockholm.bicycles.mapmatching.MapMatchingValidationGPSSequenceAndRoute;

public class GenerateValidationGPSPointsSample {

	public static void main(String[] args) throws Exception {

		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/network_NVDB.xml";
		String writePath="//vti.se/root/RUCY/GPS data//testGPSsequence.csv";
		// load network
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		(new MatsimNetworkReader(scenario.getNetwork())).readFile(inputNetworkFileName);
		Network network = scenario.getNetwork();
		GPSSequenceGenerator generator = new GPSSequenceGenerator(network);
		// get some node ids from
		MapMatchingValidationGPSSequenceAndRoute output = generator.generate("147648", "52890");
		Path path = output.getPath();
		List<Link> linksInPath = path.links;



		for (Link link : linksInPath) {
			System.out.println("link: "+link.getId());
		}
		
		List<GPSSequence> GPSSequences=new ArrayList<GPSSequence>();
		GPSSequence sequence = output.getSequence();
		GPSSequences.add(sequence);
		GPSWriter writer=new GPSWriter();
		writer.write(GPSSequences, writePath);
		
		

	}

}
