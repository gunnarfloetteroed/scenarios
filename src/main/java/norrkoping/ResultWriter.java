package norrkoping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.matsim.api.core.v01.network.Network;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class ResultWriter {
	// Old class used for write some outputs.

	public static String outputPath = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\ResultFile\\Export\\delayZones.csv";
	public static String outputPath2 = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\validation\\linkVolumesOut.csv";
	public static String outputPath3 = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\validation\\workerLinks.csv";

	public static HashMap<String, Double> delay = new HashMap<>();

	ArrayList<String> volumes = new ArrayList<>();
	public static String sum;

	public ResultWriter(HashMap<String, Double> inTable, ArrayList<String> inArray) {

		this.delay = inTable;
		this.volumes = inArray;

	}

	public void writeVolumes() {

		// File file = new File(outputPath);

		// BufferedWriter bf = null;
		// ;

		try {

			// bf = new BufferedWriter(new FileWriter(file));
			FileWriter csvWriter = new FileWriter(outputPath3);
			// bf.write("ID" + "," + "VALUE");
			// bf.newLine();

			for (int i = 0; i < volumes.size(); i++) {

				// bf.write(entry.getKey() + "," + entry.getValue());

				// bf.newLine();
				csvWriter.append(volumes.get(i));
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
