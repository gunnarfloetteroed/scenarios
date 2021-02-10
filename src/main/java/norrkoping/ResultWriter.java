package norrkoping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.matsim.api.core.v01.network.Network;

public class ResultWriter {
	
	public static String outputPath = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\ResultFile\\Export\\delayZones.csv";

	public static HashMap<String, Double> delay = new HashMap<>();

	public ResultWriter(HashMap<String, Double> inTable) {

		this.delay = inTable;

	}

	public void main() {

		// File file = new File(outputPath);

		// BufferedWriter bf = null;
		// ;

		try {

			// bf = new BufferedWriter(new FileWriter(file));
			FileWriter csvWriter = new FileWriter(outputPath);

			// bf.write("ID" + "," + "VALUE");
			// bf.newLine();
			csvWriter.append("ID");
			csvWriter.append(",");
			csvWriter.append("VALUE");
			csvWriter.append("\n");

			for (HashMap.Entry<String, Double> entry : delay.entrySet()) {

				// bf.write(entry.getKey() + "," + entry.getValue());

				// bf.newLine();
				csvWriter.append(entry.getKey() + "," + entry.getValue());
				csvWriter.append("\n");

			}

			// bf.flush();
			csvWriter.flush();
			csvWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {

				// bf.close();

			} catch (Exception e) {
			}
		}

	}

}
