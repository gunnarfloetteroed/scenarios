package stockholm.bicycles.demandgeneration;

import java.io.IOException;

import com.opencsv.exceptions.CsvException;

public class DemandMatrixToPlanProductionRunner {

	public static void main(String[] args) throws IOException, CsvException {
		// TODO Auto-generated method stub
		String inputPath ="C:/Users/ChengxiL/Box Sync/MatsimBicycleSimulation/";
		final String demandFile = inputPath + "Data/ODDemandWholeDayOutHome.csv";

	    final String networkFile = inputPath + "Simulation/network_StockholmInner.xml.gz";
		final String planFile = inputPath + "Simulation/population_Stockholm.xml";
		DemandMatrixToPlan planreader = new DemandMatrixToPlan(demandFile, networkFile, planFile);
		planreader.generatePlan();
	}

}
