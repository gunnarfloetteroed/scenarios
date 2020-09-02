package stockholm.bicycles.importnetwork;

import java.io.IOException;

import com.opencsv.exceptions.CsvException;

public class NetworkImportProductionRunner {

	public static void main(String[] args) throws IOException, CsvException {
		// final String inputPath = "./ihop2/network-input/";
//		String inputPath = "C:/Users/ChengxiL/Box Sync/MatsimBicycleSimulation/Data/";
//		final String nodesFile = inputPath + "NodeCykelStudyArea.csv";
//		final String linksFile = inputPath + "LinkCykelStudyArea.csv";
//		
//		// final String outputPath = "./ihop2/network-output/";
//		String outputPath = "C:/Users/ChengxiL/Box Sync/MatsimBicycleSimulation/Simulation/";
//		final String matsimPlainFile = outputPath + "network_StockholmInner.xml.gz";
//		final String matsimFullFile = outputPath + "network_raw_StockholmInner.xml.gz";
//		TransCad2MATSimNetwork networktransformer = new TransCad2MATSimNetwork(nodesFile, linksFile,
//				matsimPlainFile, matsimFullFile);
//		
//		networktransformer.runGenerateNetwork();
		
		
		
		
		
		String inputPath = "C:/Users/ChengxiL/Box Sync/MatsimBicycleSimulation/Data/";
		final String nodesFile = inputPath + "NodeCykelStockholmLan.csv";
		final String linksFile = inputPath + "LinkCykelStockholmLan.csv";
		
		// final String outputPath = "./ihop2/network-output/";
		String outputPath = "C:/Users/ChengxiL/Box Sync/MatsimBicycleSimulation/Simulation/";
		final String matsimPlainFile = outputPath + "network_StockholmLan.xml.gz";
		final String matsimFullFile = outputPath + "network_raw_StockholmLan.xml.gz";
		TransCad2MATSimNetwork networktransformer = new TransCad2MATSimNetwork(nodesFile, linksFile,
				matsimPlainFile, matsimFullFile);
		
		networktransformer.runGenerateNetwork();
	} // end main

}
