package stockholm.bicycles.tests.matmatchingTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.scenario.ScenarioUtils;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.imprtGPS.GPSReader;
import stockholm.bicycles.mapmatching.BundledShortestPathGPSSequenceMapMatcher;
import stockholm.bicycles.mapmatching.GPSPoint;
import stockholm.bicycles.mapmatching.GPSSequence;
import stockholm.bicycles.mapmatching.MySpatialTemporalGPSSequenceMapMatcher;
import stockholm.bicycles.mapmatching.ShortestPathGPSSequenceMapMatcher;
import stockholm.bicycles.routing.TravelDisutilityBicycle;
import stockholm.bicycles.utility.CsvWriter;

public class mapMatchingFullNetworkTest {

	public static void main(String[] args) throws Exception {

		// read GPS data
		String inputGPSFileName="//vti.se/root/RUCY/GPS data/cykel_filtered_oneTrip.csv";
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/network_NVDB.xml";
		String writePath="//vti.se/root/RUCY/GPS data/writePath_test.csv";
		GPSReader reader = new GPSReader(inputGPSFileName);
		List<GPSSequence> GPSSequences = reader.read(20);
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
		
		

		// output to csv file
		List<String[]> outputStringList= new ArrayList<String[]>();
		String[] header= new String[]{"counter", "linkID", "personID"};
		outputStringList.add(header);
		
		// remove the first 10 and last one points in the GPS trace
		for (GPSSequence gpsSequence: GPSSequences) {
			List<GPSPoint> points = gpsSequence.getGPSPoints();
			for (int i=0;i<10;i++) {
				points.remove(0);
			}

			for (int i=0;i<10;i++) {
				int gpsPointsLength=points.size();
				points.remove(gpsPointsLength-1);
			}
			// gpsSequence.printInfo();

    
			
            // map matching
			BundledShortestPathGPSSequenceMapMatcher matcher = new BundledShortestPathGPSSequenceMapMatcher(network,gpsSequence,travelDisutility);
			Path testPath = matcher.mapMatching();
			long endTime   = System.nanoTime();
			System.out.println("Total run time: "+(endTime-startTime)/10e9);
			System.out.println(testPath.travelCost);
			System.out.println(testPath.travelTime);
			List<Link> linksInPath = testPath.links;



			for (Link link : linksInPath) {
				System.out.println("link: "+link.getId());
			}
			
			int counter=1;
			for (Link link : linksInPath) {
				String linkIDWithABBA=link.getId().toString();
				if (linkIDWithABBA.contains("_AB")) {
					linkIDWithABBA = linkIDWithABBA.replaceAll("_AB", "");
				} else if (linkIDWithABBA.contains("_BA")) {
					linkIDWithABBA = linkIDWithABBA.replaceAll("_BA", "");
				}
				String[] newLine= new String[]{Integer.toString(counter), linkIDWithABBA, gpsSequence.getPersonID().toString()};
				outputStringList.add(newLine);
				counter++;
				
			}

		}
		
		
		CsvWriter.write(outputStringList, writePath);


		// mapmatching


	}

}
