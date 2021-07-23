package stockholm.bicycles.mapmatching;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.scenario.ScenarioUtils;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.io.GPSIO.GPSReader;
import stockholm.bicycles.mapmatching.mapmatchingstatistics.PathWithMatchingEvaluationStatistics;
import stockholm.bicycles.routing.TravelDisutilityBicycle;

public class BundledShortestPathGPSSequenceMapMatcherTest {

	@Test
	public void test() throws IOException, CsvException {
		// read GPS data
		String inputGPSFileName="//vti.se/root/RUCY/GPS data/FinalGPSData/cykel_forMapMatching_1.csv";
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/MatsimNetwork/network_NVDB.xml";
		GPSReader reader = new GPSReader(inputGPSFileName);
		List<GPSSequence> GPSSequences = reader.read(50);


		// load network
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		(new MatsimNetworkReader(scenario.getNetwork())).readFile(inputNetworkFileName);
		Network network = scenario.getNetwork();

		TravelDisutility travelDisutility = new TravelDisutilityBicycle("generalizedCost");
		long startTime = System.nanoTime();


		// remove the first 10 and last one points in the GPS trace
		for (GPSSequence gpsSequence: GPSSequences) {
			List<GPSPoint> points = gpsSequence.getGPSPoints();
			
			if (points.size()>100 & gpsSequence.getPersonID().toString().equals("698762")) {
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
				// NearestLinkGPSSequenceMapMatcher matcher = new NearestLinkGPSSequenceMapMatcher(network,gpsSequence,travelDisutility);

				PathWithMatchingEvaluationStatistics testPathWithStatistics = matcher.mapMatchingWithStatistics();
				Path testPath = testPathWithStatistics.getMatchedPath();
				long endTime   = System.nanoTime();
				if (testPath!=null) {
					System.out.println("trip id: "+gpsSequence.getPersonID());
					System.out.println("Total run time: "+(endTime-startTime)/10e9);
					System.out.println(testPath.travelCost);
					System.out.println(testPath.travelTime);
					List<Link> linksInPath = testPath.links;

					List<GPSPoint> gpsPoints = testPathWithStatistics.getgPSSequenceWithDistanceToMatchedPath().getGPSPoints();
					List<Double> distanceToNearestLink = testPathWithStatistics.getgPSSequenceWithDistanceToMatchedPath().getDistanceToNearestLink();
					for (Link link : linksInPath) {
						System.out.println("link: "+link.getId());
					}
					for (int i=0;i<gpsPoints.size();i++) {
						System.out.println("distance from gps point: "+i+" to the nearest link is: "+distanceToNearestLink.get(i)+"." );
					}
					System.out.println("average distance from GPS points to matched path is: "+testPathWithStatistics.getAverageDistanceFromPathToGPS());
				}
			}
			}
	}

}// pass
