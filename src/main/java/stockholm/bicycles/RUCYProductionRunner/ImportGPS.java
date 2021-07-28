package stockholm.bicycles.RUCYProductionRunner;

import java.io.IOException;
import java.util.List;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.io.GPSIO.GPSReader;
import stockholm.bicycles.mapmatching.GPSPoint;
import stockholm.bicycles.mapmatching.GPSSequence;

public class ImportGPS {

	public static void main(String[] args) throws IOException, CsvException {
		// read GPS data
		String inputGPSFileName="//vti.se/root/RUCY/GPS data/cykel_filtered_test.csv";
		GPSReader reader = new GPSReader(inputGPSFileName);
		List<GPSSequence> GPSSequences = reader.read(50);
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
