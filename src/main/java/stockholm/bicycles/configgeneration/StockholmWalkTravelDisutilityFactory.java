package stockholm.bicycles.configgeneration;

import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

public class StockholmWalkTravelDisutilityFactory implements TravelDisutilityFactory {

	@Override
	public TravelDisutility createTravelDisutility(TravelTime timeCalculator) {
		// TODO Auto-generated method stub
		return new StockholmBicycleTravelDisutility(timeCalculator);
	}

}
