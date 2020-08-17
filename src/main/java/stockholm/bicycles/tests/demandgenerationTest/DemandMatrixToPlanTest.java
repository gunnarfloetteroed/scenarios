package stockholm.bicycles.tests.demandgenerationTest;

import java.io.IOException;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.demandgeneration.DemandMatrixToPlan;

public class DemandMatrixToPlanTest {

	public static void main(String[] args) throws IOException, CsvException {
		// TODO Auto-generated method stub
		String inputPath = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\";
		inputPath = inputPath.replaceAll("\\\\", "/");
		final String demandFile = inputPath + "ODDemand.csv";
	    final String networkFile = inputPath + "network_test.xml";
		final String planFile = inputPath + "population.xml";
		DemandMatrixToPlan planreader = new DemandMatrixToPlan(demandFile, networkFile, planFile);
		planreader.generatePlan();
	}

}
