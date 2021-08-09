package stockholm.bicycles.RUCYProductionRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.io.GPSIO.GPSReader;
import stockholm.bicycles.io.pathIO.PathReader;
import stockholm.bicycles.mapmatching.GPSSequence;
import stockholm.bicycles.mapmatching.mapmatchingstatistics.PathValidationStatistics;
import stockholm.bicycles.utility.mapMatchningUtil.PathUtils;

public class MapMatchingValidationGPSToNearestLinkDistance {

	public static void main(String[] args) throws IOException, CsvException{
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		String alternativePathFile="//vti.se/root/RUCY/MapMatchingResults/mapMatchingValidation/LinkWeightsMethod_AlternativePathforMapMatchingValidation_final.csv";
		String matMatchingPathFile="//vti.se/root/RUCY/MapMatchingResults/mapMatchingValidation/LinkWeightsMethod_forMapMatchingValidation_final.csv";
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/MatsimNetwork/network_NVDB.xml";
		String inputGPSFileName="//vti.se/root/RUCY/GPS data/FinalGPSData/cykel_forMapMatchingValidation_final.csv";
		PathReader alternativePathReader = new PathReader( alternativePathFile,inputNetworkFileName);
		HashMap<String, Path> alternativePaths = alternativePathReader.read();
		
		PathReader matchedPathReader = new PathReader( matMatchingPathFile,inputNetworkFileName);
		HashMap<String, Path> matchedPaths = matchedPathReader.read();
		
		GPSReader GpsReader = new GPSReader(inputGPSFileName);
		List<GPSSequence> GPSSequences = GpsReader.read(50);
		
		for (GPSSequence gPSSequence: GPSSequences) {
			Path matchedPath=matchedPaths.get(gPSSequence.getPersonID().toString());
			Path alternativePath=alternativePaths.get(gPSSequence.getPersonID().toString());
			double distanceMatchedPath = PathUtils.averageDistancePathToGPS(gPSSequence, matchedPath);
			double distanceAlternativePath = PathUtils.averageDistancePathToGPS(gPSSequence, alternativePath);
			System.out.println("for trip: "+gPSSequence.getPersonID()+", average distance to links matched path: "+distanceMatchedPath
					+", average distance to links for alternative path: "+distanceAlternativePath);
		}
		

	
	}

}
