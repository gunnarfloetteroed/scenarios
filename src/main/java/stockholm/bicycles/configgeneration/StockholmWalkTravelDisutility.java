package stockholm.bicycles.configgeneration;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

public class StockholmWalkTravelDisutility implements TravelDisutility{

	@SuppressWarnings("unused")
	private final TravelTime timeCalculator;
	public StockholmWalkTravelDisutility(TravelTime timeCalculator) {
		super();
		this.timeCalculator = timeCalculator;
	}
	
	@Override
	public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
		// TODO Auto-generated method stub
		return link.getLength()/(5/3.6);
	}

	@Override
	public double getLinkMinimumTravelDisutility(Link link) {
		// TODO Auto-generated method stub
		return  link.getLength()/(5/3.6);
	}

}
