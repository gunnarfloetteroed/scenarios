package stockholm.bicycles.tests.utilityTest;

import java.util.ArrayList;
import java.util.List;

import stockholm.bicycles.utility.CsvWriter;

public class csvWriterTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		List<String[]> testStringList= new ArrayList<String[]>();
		testStringList.add(new String[] {"1", "Emma Watson", "emma.watson@example.com", "UK"});
		testStringList.add(new String[] {"2", "Nick Jones", "nick.jones@example.com", "DE"});
		
		String writePath="//vti.se/root/RUCY/GPS data/writeCsv_test.csv";
		CsvWriter.write(testStringList, writePath);
		
		List<String[]> testStringList2= new ArrayList<String[]>();
		testStringList2.add(new String[] {"1", "Emma Watson", "emma.watson@example.com", "UK"});
		CsvWriter.write(testStringList2, writePath);

	}

}
