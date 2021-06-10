package stockholm.bicycles.RUCYProductionRunner;

import java.io.IOException;
import java.util.HashMap;

import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.io.pathIO.PathReader;

public class MapMatchingValidation {

	public static void main(String[] args) throws IOException, CsvException {
		// TODO Auto-generated method stub
		String inputGroundTruthPathFile="//vti.se/root/RUCY/GPS data//writePath_test_20Trips.csv";
		String matMatchingPathFile="//vti.se/root/RUCY/GPS data//writePath_test_20Trips.csv";
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/MatsimNetwork/network_NVDB.xml";
		PathReader groundTruthPathReader = new PathReader( inputGroundTruthPathFile,inputNetworkFileName);
		HashMap<String, Path> groundTruthPath = groundTruthPathReader.read();
		
		PathReader matchedPathReader = new PathReader( matMatchingPathFile,inputNetworkFileName);
		HashMap<String, Path> matchedPath = matchedPathReader.read();
		
		
		

	}

}
