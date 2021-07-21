package stockholm.bicycles.utility.mapMatchning;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.io.GPSIO.GPSReader;
import stockholm.bicycles.io.pathIO.PathReader;
import stockholm.bicycles.mapmatching.GPSSequence;
import stockholm.bicycles.utility.mapMatchningUtil.PathUtils;

public class PathUtilsTest {

	@Test
	public void test() throws IOException, CsvException {
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/MatsimNetwork/network_NVDB.xml";
		String pathFile="//vti.se/root/RUCY/GPS data/mapMatchingValidation/LinkWeightsMethod_forMapMatchingValidation_final.csv";
		String inputGPSFileName="//vti.se/root/RUCY/GPS data/FinalGPSData/cykel_forMapMatchingValidation.csv";
		
		// read GPS sequences
		GPSReader gpsReader = new GPSReader(inputGPSFileName);
		List<GPSSequence> GPSSequences = gpsReader.read(50);
		
		// read paths
		PathReader pathReader = new PathReader( pathFile,inputNetworkFileName);
		HashMap<String, Path> paths = pathReader.read();
		System.out.println("data loaded.");
		
		for (GPSSequence GPSsequence: GPSSequences) {
			Id<Person> tripID = GPSsequence.getPersonID();
			Path path = paths.get(tripID.toString());
			System.out.println("average distance for trip id: "+tripID.toString()+" is: "+PathUtils.averageDistancePathToGPS(GPSsequence, path));
		}
		
		
		

	}

}
