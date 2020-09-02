package stockholm.bicycles.tests.utilityTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.matsim.matrices.Entry;
import org.matsim.matrices.Matrix;

import com.google.common.collect.Table;
import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.utility.CsvReaderToIteratable;

public class TestODmatrixReader {

	public static void main(String[] args) throws IOException, CsvException {
		// TODO Auto-generated method stub
//		String inputPath = "C:/Users/ChengxiL/Box Sync/MatsimBicycleSimulation/Data/";
//
//		final String SAMPLE_CSV_FILE_PATH = inputPath+"ODDemandWholeDayOutHome.csv";
		
		String inputPath = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\";
		inputPath = inputPath.replaceAll("\\\\", "/");
		final String SAMPLE_CSV_FILE_PATH = inputPath+"ODDemandTest.csv";
		
		
		
		CsvReaderToIteratable demandMatrixReader = new CsvReaderToIteratable(SAMPLE_CSV_FILE_PATH,';');
		Matrix demandMatrixTable = demandMatrixReader.readODMatrixWithUniqueID(0);
		
		Map<String, ArrayList<Entry>> FromId = demandMatrixTable.getFromLocations();
		for (String key:FromId.keySet()) {
			ArrayList<Entry> OD=FromId.get(key);
			for (Entry eachElement : OD) {
				System.out.println("Row: "+eachElement.getFromLocation()+ "; Col: "+eachElement.getToLocation()+"; Value:  "+eachElement.getValue());
			}
		}
		System.out.println("DemandMatrixRead");

	}

}
