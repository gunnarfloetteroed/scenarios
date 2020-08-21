package stockholm.bicycles.importnetwork;

import java.io.IOException;

import com.opencsv.exceptions.CsvException;

public class NetworkImportProductionRunner {

	public static void main(String[] args) throws IOException, CsvException {
		// final String inputPath = "./ihop2/network-input/";
		String inputPath = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\";
		inputPath = inputPath.replaceAll("\\\\", "/");
		final String nodesFile = inputPath + "Nodes.csv";
		final String linksFile = inputPath + "Links.csv";
		
		// final String outputPath = "./ihop2/network-output/";
		String outputPath = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\testScenario\\";
		outputPath = outputPath.replaceAll("\\\\", "/");
		final String matsimPlainFile = outputPath + "network_test.xml";
		final String matsimFullFile = outputPath + "network_raw_test.xml";
		TransCad2MATSimNetwork networktransformer = new TransCad2MATSimNetwork(nodesFile, linksFile,
				matsimPlainFile, matsimFullFile);
		
		networktransformer.runGenerateNetwork();
	} // end main

}
