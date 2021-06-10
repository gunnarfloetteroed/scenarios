package stockholm.bicycles.routing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.vehicles.Vehicle;

public class TravelDisutilityBicycle implements TravelDisutility {
	public final String generalizedCostAttributeName;
	
	public TravelDisutilityBicycle(String generalizedCostAttributeName) {
		super();
		this.generalizedCostAttributeName = generalizedCostAttributeName;
	}

	@Override
	public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
		
		if (generalizedCostAttributeName.equals("Length")) {
			return link.getLength();
		}
		return (double) link.getAttributes().getAttribute(generalizedCostAttributeName);
	}

	@Override
	public double getLinkMinimumTravelDisutility(Link link) {
		
		if (generalizedCostAttributeName.equals("Length")) {
			return link.getLength();
		}
		return (double) link.getAttributes().getAttribute(generalizedCostAttributeName);
	}

	public String getGeneralizedCostAttributeName() {
		return generalizedCostAttributeName;
	}

}
