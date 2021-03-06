package stockholm.bicycles.tests.importnetworkTest;

import java.io.IOException;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.importnetwork.TransCad2MATSimNetwork;

public class ImportNetworkTest {

	public static void main(String[] args) throws IOException, CsvException {
		// final String inputPath = "./ihop2/network-input/";
		String inputPath = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\";
		inputPath = inputPath.replaceAll("\\\\", "/");
		final String nodesFile = inputPath + "StockholmNodeCykelTest.csv";
		final String linksFile = inputPath + "StockholmLinkCykelTest.csv";
		
		// final String outputPath = "./ihop2/network-output/";
		String outputPath = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\testScenario\\";
		outputPath = outputPath.replaceAll("\\\\", "/");
		final String matsimPlainFile = outputPath + "network_test.xml";
		final String matsimFullFile = outputPath + "network_raw_test.xml";
		@SuppressWarnings("unused")
		final String linkAttributesFile = outputPath + "link_attributes_test.xml";
		final String nodeAttributesFile = outputPath + "node_attributes_test.xml";
		TransCad2MATSimNetwork networktransformer = new TransCad2MATSimNetwork(nodesFile, linksFile,
				matsimPlainFile, matsimFullFile);
		
		networktransformer.runGenerateNetwork();

	}

}
