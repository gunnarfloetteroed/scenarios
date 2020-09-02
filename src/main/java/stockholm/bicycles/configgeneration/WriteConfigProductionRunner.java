package stockholm.bicycles.configgeneration;

public class WriteConfigProductionRunner {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputPathScenario = "C:/Users/ChengxiL/Box Sync/MatsimBicycleSimulation/Simulation/";
		final String NetworkFile = inputPathScenario + "network_StockholmInner.xml.gz";
		final String planFile = inputPathScenario + "population_Stockholm.xml";
		final String configFile= inputPathScenario+ "config_Stockholm.xml";
		final String outputDir= inputPathScenario+ "output";
		
		WriteConfigFromNetworkAndPopulation configWriter = new WriteConfigFromNetworkAndPopulation(NetworkFile, planFile, configFile, outputDir);
		configWriter.WriteConfig();

	}

}
