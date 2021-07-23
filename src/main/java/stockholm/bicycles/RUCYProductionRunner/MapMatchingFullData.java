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

import stockholm.bicycles.io.GPSIO.GPSReader;
import stockholm.bicycles.io.GPSIO.GPSWriter;
import stockholm.bicycles.mapmatching.BundledShortestPathGPSSequenceMapMatcher;
import stockholm.bicycles.mapmatching.GPSPoint;
import stockholm.bicycles.mapmatching.GPSSequence;
import stockholm.bicycles.mapmatching.GPSSequenceWithDistanceToMatchedPath;
import stockholm.bicycles.mapmatching.MySpatialTemporalGPSSequenceMapMatcher;
import stockholm.bicycles.mapmatching.mapmatchingstatistics.PathWithMatchingEvaluationStatistics;
import stockholm.bicycles.routing.TravelDisutilityBicycle;
import stockholm.bicycles.utility.CsvWriter;

public class MapMatchingFullData {

	public static void main(String[] args) throws Exception {


		// read GPS data
		String inputGPSFileName="//vti.se/root/RUCY/GPS data/FinalGPSData/cykel_forMapMatching_2.csv";
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/MatsimNetwork/network_NVDB.xml";
		String writePath="//vti.se/root/RUCY/GPS data/mapMatchingValidation/LinkWeightsMethod_MapMatching_2";
		String writeGPS="//vti.se/root/RUCY/GPS data/FinalGPSData/GPSDataWithDistanceToNearestLinks/cykel_mapMatchingWithDistanceToNearestLinks_1.csv";
		GPSReader reader = new GPSReader(inputGPSFileName);
		List<GPSSequence> GPSSequences = reader.read(50);

		//		for (GPSSequence GPSSequence: GPSSequences) {
		//			GPSSequence.printInfo();
		//		}
		// load network
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		(new MatsimNetworkReader(scenario.getNetwork())).readFile(inputNetworkFileName);
		Network network = scenario.getNetwork();


		// 
		TravelDisutility travelDisutility = new TravelDisutilityBicycle("generalizedCost");




		// output to csv file matched path
		List<String[]> outputStringList= new ArrayList<String[]>();
		String[] header= new String[]{"counter", "linkID", "tripID"};
		outputStringList.add(header);

		// remove the first 10 and last one points in the GPS trace
		List<GPSSequenceWithDistanceToMatchedPath> gPSSequencesWithDistanceToMatchedPath = new ArrayList<GPSSequenceWithDistanceToMatchedPath>();
		int NGPSSequence = GPSSequences.size();
		int csvFileNumber=1;
		for (int k=0;k<NGPSSequence;k++) {
			GPSSequence gpsSequence=GPSSequences.get(0);
			
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

				Path testPath = matcher.mapMatching();
				if (testPath !=null) {
					List<Link> linksInPath = testPath.links;

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
//					GPSSequenceWithDistanceToMatchedPath gPSSequenceWithDistanceToMatchedPath = result.getgPSSequenceWithDistanceToMatchedPath();
//					gPSSequencesWithDistanceToMatchedPath.add(gPSSequenceWithDistanceToMatchedPath);
				}

				long endTime   = System.nanoTime();
				double runTime= (double) (endTime-startTime)/1_000_000_000;
				System.out.println("Total run time for persom: "+gpsSequence.getPersonID().toString()+" is: "+runTime);
			
				
			}

			
			gpsSequence=null;
			GPSSequences.remove(0);
			
			if (outputStringList.size()>10000) {
				String writePath_part = writePath+"_"+csvFileNumber+".csv";
				CsvWriter.write(outputStringList, writePath_part);
				outputStringList= new ArrayList<String[]>();
				csvFileNumber++;
			}
		}



//		GPSWriter gpsWriter = new GPSWriter();
//		gpsWriter.writeWithDistanceToLink(gPSSequencesWithDistanceToMatchedPath, writeGPS);
//		CsvWriter.write(outputStringList, writePath);





	}

}
