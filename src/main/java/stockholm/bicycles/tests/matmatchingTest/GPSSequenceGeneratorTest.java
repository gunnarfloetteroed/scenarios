package stockholm.bicycles.tests.matmatchingTest;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

import stockholm.bicycles.mapmatching.GPSSequenceGenerator;

public class GPSSequenceGeneratorTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/network_NVDB.xml";
		String writePath="//vti.se/root/RUCY/GPS data//writeGPSSequence_validation.csv";
		
		// load network
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		(new MatsimNetworkReader(scenario.getNetwork())).readFile(inputNetworkFileName);
		Network network = scenario.getNetwork();
		GPSSequenceGenerator generator = new GPSSequenceGenerator(network,50);
		Coord coord = new Coord(0,0);
		for (int i=0;i<50;i++) {
			Coord newCoord = generator.generateRandomCoordAlongside(coord,50);
			System.out.println(Math.sqrt(newCoord.getX()*newCoord.getX()+newCoord.getY()*newCoord.getY()));
		}
		int a=(int) (-3.5/3.5);
		System.out.println(a);
		

		double degrees = 30.0;
	    double radians = Math.toRadians(degrees);
	    System.out.println(Math.asin(0.5)/Math.PI*180);
	    System.out.println((Math.asin(-0.5)+Math.PI*2)/Math.PI*180);
	}

}
