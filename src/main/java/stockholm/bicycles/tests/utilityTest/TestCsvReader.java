package stockholm.bicycles.tests.utilityTest;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Table;
import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.utility.CsvReaderToIteratable;


public class TestCsvReader {
	

	public static void main(String[] args) {
		
		String inputPath = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\";
		inputPath = inputPath.replaceAll("\\\\", "/");
		final String SAMPLE_CSV_FILE_PATH = inputPath+"csvReaderTest.csv";

		CsvReaderToIteratable csvToUserClass = new CsvReaderToIteratable(SAMPLE_CSV_FILE_PATH,';');
		
		try {
			Table<String, String, String> result = csvToUserClass.readTableWithUniqueID(0);
			// Set<String> columnKey=result.columnKeySet();
			
			Map<String, String> specificColumn = result.column("Phone"); 
			 for (Map.Entry<String, String> cell : specificColumn.entrySet()) { 
		            System.out.println("row name : " + cell.getKey() + ", column name : " + "Phone" +", value Name : " + cell.getValue()); 
		        } 
			
		} catch (IOException | CsvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	    }

}
