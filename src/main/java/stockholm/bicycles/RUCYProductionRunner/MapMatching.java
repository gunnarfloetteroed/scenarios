package stockholm.bicycles.RUCYProductionRunner;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.scenario.ScenarioUtils;

import stockholm.bicycles.imprtGPS.GPSReader;
import stockholm.bicycles.mapmatching.BundledShortestPathGPSSequenceMapMatcher;
import stockholm.bicycles.mapmatching.GPSPoint;
import stockholm.bicycles.mapmatching.GPSSequence;
import stockholm.bicycles.mapmatching.NearestLinkGPSSequenceMapMatcher;
import stockholm.bicycles.routing.TravelDisutilityBicycle;
import stockholm.bicycles.utility.CsvWriter;

public class MapMatching {

	public static void main(String[] args) throws Exception {

		// read GPS data
		String inputGPSFileName="//vti.se/root/RUCY/GPS data/cykel_filtered_20Trips.csv";
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/network_NVDB.xml";
		String writePath="//vti.se/root/RUCY/GPS data//writePath_test_20Trips.csv";
		GPSReader reader = new GPSReader(inputGPSFileName);
		List<GPSSequence> GPSSequences = reader.read(15);
		
		for (GPSSequence GPSSequence: GPSSequences) {
			// GPSSequence.printInfo();
		}
		// load network
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		(new MatsimNetworkReader(scenario.getNetwork())).readFile(inputNetworkFileName);
		Network network = scenario.getNetwork();

		TravelDisutility travelDisutility = new TravelDisutilityBicycle("generalizedCost");
		



		// output to csv file
		List<String[]> outputStringList= new ArrayList<String[]>();
		String[] header= new String[]{"counter", "linkID", "tripID"};
		outputStringList.add(header);

		// remove the first 10 and last one points in the GPS trace
		for (int k=0;k<GPSSequences.size();k++) {
			GPSSequence gpsSequence=GPSSequences.get(k);
			
			System.out.println("Following GPS sequence starts processing: "+gpsSequence.getPersonID().toString());
			List<GPSPoint> points = gpsSequence.getGPSPoints();
			if (points.size()>100) {

				for (int i=0;i<10;i++) {
					points.remove(0);
				}

				for (int i=0;i<10;i++) {
					int gpsPointsLength=points.size();
					points.remove(gpsPointsLength-1);
				}
				// gpsSequence.printInfo();


				long startTime = System.nanoTime();
				// map matching
				BundledShortestPathGPSSequenceMapMatcher matcher = new BundledShortestPathGPSSequenceMapMatcher(network,gpsSequence,travelDisutility);
				// NearestLinkGPSSequenceMapMatcher matcher = new NearestLinkGPSSequenceMapMatcher(network,gpsSequence,travelDisutility);

				Path testPath = matcher.mapMatching();
				

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
				long endTime   = System.nanoTime();
				System.out.println("Total run time for persom: "+gpsSequence.getPersonID().toString()+" is: "+(endTime-startTime)/10e9);

				//					System.out.println(testPath.travelCost);
				//					System.out.println(testPath.travelTime);
				//					System.out.println("");

			}


		}
		
		


		CsvWriter.write(outputStringList, writePath);


		// mapmatching


	}

}
