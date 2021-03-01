package stockholm.bicycles.utility;


import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;


public class CsvWriter {
	public static void write(List<String[]> stringArray, String filePath) throws Exception {
		
		try {
			Writer writer = Files.newBufferedWriter(Paths.get(filePath));
			
			ICSVWriter csvWriter = new CSVWriterBuilder(writer)
		            .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
		            .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
		            .withEscapeChar(CSVWriter.DEFAULT_ESCAPE_CHARACTER)
		            .withLineEnd(CSVWriter.DEFAULT_LINE_END)
		            .build();
			
			for (int i=0;i<stringArray.size();i++) {
				csvWriter.writeNext(stringArray.get(i));
			}
			System.out.println("files written to: "+filePath);
			csvWriter.close();
		    writer.close();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		
		
	}
	
}
