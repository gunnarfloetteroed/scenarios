package stockholm.bicycles.RUCYProductionRunner;

import java.io.IOException;

import com.opencsv.exceptions.CsvException;
import stockholm.bicycles.importnetwork.TransCad2MATSimNetwork_RUCY;

public class ImportNetwork {

	public static void main(String[] args) throws IOException, CsvException {
		// TODO Auto-generated method stub
		// final String inputPath = "./ihop2/network-input/";
		String inputPath = "C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/";
		final String nodesFile = inputPath + "NodesToMatsim.csv";
		final String linksFile = inputPath + "LinksToMatsim.csv";
		
		// final String outputPath = "./ihop2/network-output/";
		String outputPath = inputPath;
		final String matsimPlainFile = outputPath + "network_NVDB.xml";
		final String matsimFullFile = outputPath + "network_NVDB_raw.xml";
		@SuppressWarnings("unused")
		TransCad2MATSimNetwork_RUCY networktransformer = new TransCad2MATSimNetwork_RUCY(nodesFile, linksFile,
				matsimPlainFile, matsimFullFile);
		
		networktransformer.runGenerateNetwork();


	}

}
