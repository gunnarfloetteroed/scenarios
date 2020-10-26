package stockholm.bicycles.resultanalysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.OptionalTime;

public class AgentTravelTimePlanHandler {
	private Population population;
	
	
	
	public AgentTravelTimePlanHandler(Population population) {
		super();
		this.population = population;
	}

	public AgentTravelTimePlanHandler(String planFile) {
		super();
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		PopulationReader populationReader = new PopulationReader(scenario);
		populationReader.readFile(planFile);
		this.population = scenario.getPopulation();
	}

	
	public HashMap<Id<Person>, Double> calculateAgentTravelTime() {
		HashMap<Id<Person>, Double> individualTravelTime = new HashMap<Id<Person>, Double>();
		
		Map<Id<Person>, ? extends Person> persons = this.population.getPersons();
	    for (Entry<Id<Person>, ? extends Person> entry:persons.entrySet()) {
	    	Person person = entry.getValue();
	    	Id<Person> personID = entry.getKey();
	    	individualTravelTime.put(personID, 0.0);
	    	Plan selectedPlan = person.getSelectedPlan();
	    	for ( Leg leg : TripStructureUtils.getLegs( selectedPlan ) ) {
	    		if ( TransportMode.bike.equals( leg.getMode() ) ) {
	    			OptionalTime traveltime = leg.getTravelTime();
	    			double tripTime = traveltime.seconds();
	    			double currentBikeTravelTime = individualTravelTime.get(personID);
	    			individualTravelTime.put(personID, currentBikeTravelTime+tripTime);
	    		}
	    	}
	    }
	    
	    return individualTravelTime;
	}

	public Population getPopulation() {
		return this.population;
	}
	
	

}
