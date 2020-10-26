package stockholm.bicycles.resultanalysis;

import java.util.HashMap;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

public class RunAgentTravelTimeEventsHandlingProductionRunner {

	public static void main(String[] args) {

		String inputPath = "C:/Users/ChengxiL/Box Sync/MatsimBicycleSimulation/Simulation/output/";
		final String inputFile = inputPath+"output_events.xml.gz";
		String outputPath = inputPath;
		final String outputFile = outputPath+"linkVolume_1.csv";

		//create an event object
		EventsManager events = EventsUtils.createEventsManager();

		//create the handler and add it
		AgentTravelTimeHandler linkVolumeHandler1 = new AgentTravelTimeHandler();
		events.addHandler(linkVolumeHandler1);

		//create the reader and read the file
		MatsimEventsReader reader = new MatsimEventsReader(events);
		reader.readFile(inputFile);
		
		HashMap<Id<Person>, Double> agentTravelTime= linkVolumeHandler1.getIndividualTravelTime();
		double totalTravelTime = linkVolumeHandler1.getTotalTravelTime();
		int numberOfAgents = agentTravelTime.size();
		System.out.println("average traveltime per agent in sec: "+totalTravelTime/numberOfAgents);

	}

}
