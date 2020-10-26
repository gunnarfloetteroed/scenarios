package stockholm.bicycles.resultanalysis;

import java.util.HashMap;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.population.Person;
/**
 * This EventHandler implementation counts the travel time of
 * all agents and provides the average travel time per
 * agent.
 * Actually, handling Departures and Arrivals should be sufficient for this (may 2014)
 * @author dgrether
 *
 */
public class AgentTravelTimeHandler implements PersonArrivalEventHandler,PersonDepartureEventHandler {
    private HashMap<Id<Person>, Double> individualTravelTime = new HashMap<Id<Person>, Double>();
	private double timePersonOnTravel = 0.0;
	// NOTE: drivers depart, enter vehicles, eventually enter traffic, drive to destination, leave traffic, leave vehicle, arrive.
	// In consequence, the time between departure and arrival of the person may be longer than the time
	// between the vehicle entering and leaving the traffic (network).
	
	public double getTotalTravelTime() {
		return this.timePersonOnTravel ;
	}
	
	public HashMap<Id<Person>, Double> getIndividualTravelTime(){
		return this.individualTravelTime;
	}

	@Override
	public void reset(int iteration) {
		this.timePersonOnTravel = 0.0;
		this.individualTravelTime= new HashMap<Id<Person>, Double>();
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		this.timePersonOnTravel += event.getTime();
		Id<Person> personID = event.getPersonId();
		if(this.individualTravelTime.containsKey(personID)) {
		double currentTime = this.individualTravelTime.get(personID);
		this.individualTravelTime.put(personID, currentTime+event.getTime());
		} else {
			this.individualTravelTime.put(personID, event.getTime());	
		}
		
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		this.timePersonOnTravel -= event.getTime();
		Id<Person> personID = event.getPersonId();
		if(this.individualTravelTime.containsKey(personID)) {
		double currentTime = this.individualTravelTime.get(personID);
		this.individualTravelTime.put(personID, currentTime-event.getTime());
		} else {
			this.individualTravelTime.put(personID, -event.getTime());	
		}
	}


}
