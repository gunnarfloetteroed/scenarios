package stockholm.bicycles.demandgeneration;

import java.io.IOException;

import com.opencsv.exceptions.CsvException;

public class DemandMatrixToPlanProductionRunner {

	public static void main(String[] args) throws IOException, CsvException {
		// TODO Auto-generated method stub
		String inputPath ="C:/Users/ChengxiL/Box Sync/MatsimBicycleSimulation/";
		
		// for the demand file of the baseline
		// final String demandFile = inputPath + "Data/ODDemandWholeDayOutHome.csv";

		// for the demnad file of boat scenario
		final String demandFile = inputPath + "Data/Baseline/ODDemandWholeDayOutHome.csv";
		
		
	    final String networkFile = inputPath + "Simulation/network_StockholmInner.xml.gz";
	    
	    // for the plan file of the baseline
		// final String planFile = inputPath + "Simulation/population_Stockholm.xml";
		
		// for the plan file of the boat scenario
	    final String planFile = inputPath + "Simulation/population_Stockholm_baseline.xml";
		
		DemandMatrixToPlan planreader = new DemandMatrixToPlan(demandFile, networkFile, planFile);
		planreader.generatePlan();
	}

}
