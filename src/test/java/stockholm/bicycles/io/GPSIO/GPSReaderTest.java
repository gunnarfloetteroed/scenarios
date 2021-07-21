package stockholm.bicycles.io.GPSIO;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.mapmatching.GPSPoint;
import stockholm.bicycles.mapmatching.GPSSequence;

public class GPSReaderTest {

	@Test
	public void test() throws IOException, CsvException {

		// read GPS data
		// cykel_filtered_1  FinalGPSData/cykel_forMapMatching
		String inputGPSFileName="//vti.se/root/RUCY/GPS data/FinalGPSData/cykel_forMapMatchingValidation.csv";
		GPSReader reader = new GPSReader(inputGPSFileName);
		List<GPSSequence> GPSSequences = reader.read(30);
		for (GPSSequence GPSSequence: GPSSequences) {
			System.out.println(GPSSequence.getPersonID().toString());
			System.out.println(GPSSequence.getMode());
			List<GPSPoint> GPSPoints = GPSSequence.getGPSPoints();
			for (GPSPoint GPSPoint : GPSPoints) {
				System.out.println("timeStamp: "+GPSPoint.getTimeStamp().toString()+ ". distance:"+GPSPoint.getDelta_m()+". speed: "+GPSPoint.getSpeed()+ ". Coordinate: "+GPSPoint.getCoord().toString());
			}
		}
		
	
	}

}
