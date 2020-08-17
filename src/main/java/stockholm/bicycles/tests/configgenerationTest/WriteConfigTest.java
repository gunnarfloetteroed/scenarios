package stockholm.bicycles.tests.configgenerationTest;

import stockholm.bicycles.configgeneration.WriteConfigFromNetworkAndPopulation;

public class WriteConfigTest {


	public static void main(String[] args) {
		// requires to run importNetwork and demandGeneration to generate network and population xml files.
	    String inputPath = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\";
		inputPath = inputPath.replaceAll("\\\\", "/");
		final String NetworkFile = inputPath + "network_test.xml";
		final String planFile = inputPath + "population.xml";
		final String configFile= inputPath+ "config_test.xml";
		final String outputDir= inputPath+ "output/test";
		
		WriteConfigFromNetworkAndPopulation configWriter = new WriteConfigFromNetworkAndPopulation(NetworkFile, planFile, configFile, outputDir);
		configWriter.WriteConfig();
		
		// final String linkAttributeFile= inputPath+ "link_attributes_test.xml";
		// configWriter.EditConfig(linkAttributeFile);
	}

}
