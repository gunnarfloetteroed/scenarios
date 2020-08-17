package stockholm.bicycles.tests.importnetworkTest;

import java.io.IOException;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.importnetwork.TransCad2MATSimNetwork;

public class ImportNetworkTest {

	public static void main(String[] args) throws IOException, CsvException {
		// final String inputPath = "./ihop2/network-input/";
		String inputPath = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\";
		inputPath = inputPath.replaceAll("\\\\", "/");
		final String nodesFile = inputPath + "Nodes.csv";
		final String linksFile = inputPath + "Links.csv";
		
		// final String outputPath = "./ihop2/network-output/";
		final String outputPath = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\";
		final String matsimPlainFile = outputPath + "network_test.xml";
		final String matsimFullFile = outputPath + "network_raw_test.xml";
		final String linkAttributesFile = outputPath + "link_attributes_test.xml";
		final String nodeAttributesFile = outputPath + "node_attributes_test.xml";
		TransCad2MATSimNetwork networktransformer = new TransCad2MATSimNetwork(nodesFile, linksFile,
				matsimPlainFile, matsimFullFile, nodeAttributesFile,linkAttributesFile);
		
		networktransformer.run();

	}

}
