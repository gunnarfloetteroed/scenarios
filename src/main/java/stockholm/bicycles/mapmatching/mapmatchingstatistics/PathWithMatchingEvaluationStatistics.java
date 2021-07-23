package stockholm.bicycles.mapmatching.mapmatchingstatistics;

import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import stockholm.bicycles.mapmatching.GPSSequenceWithDistanceToMatchedPath;

public class PathWithMatchingEvaluationStatistics {
	private Path matchedPath; 
	private GPSSequenceWithDistanceToMatchedPath gPSSequenceWithDistanceToMatchedPath;
	private double averageDistanceFromPathToGPS;
	
	public Path getMatchedPath() {
		return matchedPath;
	}
	public void setMatchedPath(Path matchedPath) {
		this.matchedPath = matchedPath;
	}
	public double getAverageDistanceFromPathToGPS() {
		return averageDistanceFromPathToGPS;
	}
	public void setAverageDistanceFromPathToGPS(double averageDistanceFromPathToGPS) {
		this.averageDistanceFromPathToGPS = averageDistanceFromPathToGPS;
	}

	public GPSSequenceWithDistanceToMatchedPath getgPSSequenceWithDistanceToMatchedPath() {
		return gPSSequenceWithDistanceToMatchedPath;
	}
	public void setgPSSequenceWithDistanceToMatchedPath(
			GPSSequenceWithDistanceToMatchedPath gPSSequenceWithDistanceToMatchedPath) {
		this.gPSSequenceWithDistanceToMatchedPath = gPSSequenceWithDistanceToMatchedPath;
	}


}
