package stockholm.bicycles.importnetwork;

import java.io.IOException;

import com.opencsv.exceptions.CsvException;

public class NetworkImportEntry {

	public static void main(String[] args) throws IOException, CsvException {
		// final String inputPath = "./ihop2/network-input/";
		final String inputPath = "C:/Users/ChengxiL/git/MatsimPlaygroundCLI/chengxi-playground/src/test/resources/";
		final String nodesFile = inputPath + "Nodes.csv";
		final String linksFile = inputPath + "Links.csv";
		
		// final String outputPath = "./ihop2/network-output/";
		final String outputPath = "C:/Users/ChengxiL/git/MatsimPlaygroundCLI/chengxi-playground/src/test/resources/";
		final String matsimPlainFile = outputPath + "network_test.xml";
		final String matsimFullFile = outputPath + "network_raw_test.xml";
		final String linkAttributesFile = outputPath + "link_attributes_test.xml";
		final String nodeAttributesFile = outputPath + "node_attributes_test.xml";
		TransCad2MATSimNetwork networktransformer = new TransCad2MATSimNetwork(nodesFile, linksFile,
				matsimPlainFile, matsimFullFile, nodeAttributesFile,linkAttributesFile);
		
		networktransformer.run();
	} // end main

}
