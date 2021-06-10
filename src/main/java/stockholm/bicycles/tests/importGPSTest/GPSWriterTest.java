package stockholm.bicycles.tests.importGPSTest;

import java.io.IOException;
import java.util.List;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.io.GPSIO.GPSReader;
import stockholm.bicycles.io.GPSIO.GPSWriter;
import stockholm.bicycles.mapmatching.GPSSequence;

public class GPSWriterTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// read GPS data
				String inputGPSFileName="//vti.se/root/RUCY/GPS data/cykel_filtered_20Trips.csv";
				String writePath="//vti.se/root/RUCY/GPS data//testGPSwrite_20trips.csv";
				GPSReader reader = new GPSReader(inputGPSFileName);
				List<GPSSequence> GPSSequences = reader.read(15);
				
				GPSWriter writer=new GPSWriter();
				writer.write(GPSSequences, writePath);
	}

}
