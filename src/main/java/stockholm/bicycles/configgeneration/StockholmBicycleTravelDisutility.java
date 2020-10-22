package stockholm.bicycles.configgeneration;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

public class StockholmBicycleTravelDisutility implements TravelDisutility {


	@SuppressWarnings("unused")
	private final TravelTime timeCalculator;
	
	
	
	public StockholmBicycleTravelDisutility(TravelTime timeCalculator) {
		super();
		this.timeCalculator = timeCalculator;
	}

	@Override
	public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
		
		// define some logic for calculating generalized cost

		// double travelTime = timeCalculator.getLinkTravelTime(link, time, person, vehicle);
		double generalizedCostTravelTime =0;
		double bicycleSpeedForGeneralizedCostCalculation = (double) link.getAttributes().getAttribute("bicycleSpeed_M_S");
		if (bicycleSpeedForGeneralizedCostCalculation<=0) {
			generalizedCostTravelTime =link.getLength()/(14.6/3.6);
		} else {
			generalizedCostTravelTime =link.getLength()/bicycleSpeedForGeneralizedCostCalculation;
		}
		return generalizedCostTravelTime;
	}

	@Override
	public double getLinkMinimumTravelDisutility(Link link) {
		// random maximum speed as 15 m/s
		return link.getLength()/15.0;
	}

}
