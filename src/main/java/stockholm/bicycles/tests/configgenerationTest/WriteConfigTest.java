package stockholm.bicycles.tests.configgenerationTest;

import stockholm.bicycles.configgeneration.WriteConfigFromNetworkAndPopulation;

public class WriteConfigTest {


	public static void main(String[] args) {
		// requires to run importNetwork and demandGeneration to generate network and population xml files.
	    String inputPathScenario = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\testScenario\\";
	    inputPathScenario = inputPathScenario.replaceAll("\\\\", "/");
		final String NetworkFile = inputPathScenario + "network_test.xml";
		final String planFile = inputPathScenario + "population.xml";
		final String configFile= inputPathScenario+ "config_test.xml";
		final String outputDir= inputPathScenario+ "output";
		
		WriteConfigFromNetworkAndPopulation configWriter = new WriteConfigFromNetworkAndPopulation(NetworkFile, planFile, configFile, outputDir);
		configWriter.WriteConfig();
		
		// final String linkAttributeFile= inputPath+ "link_attributes_test.xml";
		// configWriter.EditConfig(linkAttributeFile);
	}

}
