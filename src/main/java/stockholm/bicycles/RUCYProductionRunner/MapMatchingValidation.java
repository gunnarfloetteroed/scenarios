package stockholm.bicycles.RUCYProductionRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.io.pathIO.PathReader;
import stockholm.bicycles.mapmatching.mapmatchingstatistics.PathValidationStatistics;
import stockholm.bicycles.utility.mapMatchningUtil.PathUtils;

public class MapMatchingValidation {

	public static void main(String[] args) throws IOException, CsvException {
		// TODO Auto-generated method stub
		String inputGroundTruthPathFile="//vti.se/root/RUCY/GPS data/mapMatchingValidation/validationMapMatchingGroundTruth.csv";
		String matMatchingPathFile="//vti.se/root/RUCY/GPS data/mapMatchingValidation/LinkWeightsMethod_forMapMatchingValidation_final.csv";
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/MatsimNetwork/network_NVDB.xml";
		PathReader groundTruthPathReader = new PathReader( inputGroundTruthPathFile,inputNetworkFileName);
		HashMap<String, Path> groundTruthPath = groundTruthPathReader.read();
		
		PathReader matchedPathReader = new PathReader( matMatchingPathFile,inputNetworkFileName);
		HashMap<String, Path> matchedPath = matchedPathReader.read();
		
		for (Entry<String, Path> entry:matchedPath.entrySet()) {
			String tripID = entry.getKey();
			Path gtPath = groundTruthPath.get(tripID);
			PathValidationStatistics statistics = PathUtils.comparePath(gtPath, entry.getValue());
			System.out.println("for trip: "+tripID+", the percentage matched link length is: "+statistics.getPercentageLengthMatched());
		}
		
		for (Entry<String, Path> entry:matchedPath.entrySet()) {
			String tripID = entry.getKey();
			Path gtPath = groundTruthPath.get(tripID);
			PathValidationStatistics statistics = PathUtils.comparePath(gtPath, entry.getValue());
			System.out.println("for trip: "+tripID+", the percentage incorrectly matched link length is: "+statistics.getPercentageLengthIncorrectMatched());
		}

	}

}
