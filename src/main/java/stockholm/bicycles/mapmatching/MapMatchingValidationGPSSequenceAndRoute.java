package stockholm.bicycles.mapmatching;

import org.matsim.core.router.util.LeastCostPathCalculator.Path;
//a simple container class for the purpose of validation of mapmacthing algo. it contains a true path and a GPSsequence for that path
public class MapMatchingValidationGPSSequenceAndRoute {
	private GPSSequence sequence=null;
	private Path path=null;
	
	public GPSSequence getSequence() {
		return sequence;
	}
	public void setSequence(GPSSequence sequence) {
		this.sequence = sequence;
	}
	public Path getPath() {
		return path;
	}
	public void setPath(Path path) {
		this.path = path;
	}
	

}
