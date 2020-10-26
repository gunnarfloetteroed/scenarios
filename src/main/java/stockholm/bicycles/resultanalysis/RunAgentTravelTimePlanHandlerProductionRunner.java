package stockholm.bicycles.resultanalysis;

import java.util.HashMap;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

public class RunAgentTravelTimePlanHandlerProductionRunner {
	
	public static void main(String[] args) {
		String inputPath = "C:/Users/ChengxiL/Box Sync/MatsimBicycleSimulation/Simulation/output/";
		final String inputFile = inputPath+"output_plans.xml.gz";
		String outputPath = inputPath;
		final String outputFile = outputPath+"linkVolume_1.csv";
		
		AgentTravelTimePlanHandler agentTravelTimePlanHandler = new AgentTravelTimePlanHandler(inputFile);
		HashMap<Id<Person>, Double> personTravelTime = agentTravelTimePlanHandler.calculateAgentTravelTime();

		int numberOfAgents = personTravelTime.size();
		double totalTravelTime=0;
		for (Entry<Id<Person>, Double> entry:personTravelTime.entrySet()) {
			totalTravelTime+=entry.getValue();
		}
		System.out.println("total number of agent: "+numberOfAgents);
		System.out.println("total bike travel time in sec: "+totalTravelTime);
		System.out.println("average traveltime per agent in sec: "+totalTravelTime/numberOfAgents);
		
	}

}
