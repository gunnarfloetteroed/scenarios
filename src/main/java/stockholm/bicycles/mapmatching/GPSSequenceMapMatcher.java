package stockholm.bicycles.mapmatching;

import org.matsim.core.router.util.LeastCostPathCalculator.Path;

public interface GPSSequenceMapMatcher {
	public Path mapMatching();
}
