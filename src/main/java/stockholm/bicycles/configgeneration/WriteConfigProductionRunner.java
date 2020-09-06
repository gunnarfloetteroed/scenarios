package stockholm.bicycles.configgeneration;

public class WriteConfigProductionRunner {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputPathScenario = "D:/MatsimBicycleSimulation/Simulation/";
		final String NetworkFile = inputPathScenario + "network_StockholmInner.xml.gz";
		// for the baseline scenario
		// final String planFile = inputPathScenario + "population_Stockholm.xml";
		// final String configFile= inputPathScenario+ "config_Stockholm.xml";
		// final String outputDir= inputPathScenario+ "output";
		
		// for the boat scenario
		final String planFile = inputPathScenario + "population_Stockholm_boatScenario.xml";
		
		final String configFile= inputPathScenario+ "config_Stockholm_boatscenario.xml";
		final String outputDir= inputPathScenario+ "output_boatScenario";
		
		WriteConfigFromNetworkAndPopulation configWriter = new WriteConfigFromNetworkAndPopulation(NetworkFile, planFile, configFile, outputDir);
		configWriter.WriteConfig();

	}

}
