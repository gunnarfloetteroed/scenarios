package stockholm.bicycles.mapmatching.mapmatchingstatistics;

import java.util.ArrayList;
import java.util.List;

public class DistanceFromGPSPointsToNearestLinksData {
	private List<Double> distanceToNearestLink = new ArrayList<Double>();
	private List<String> nearestLinkID = new ArrayList<String>();
	public List<Double> getDistanceToNearestLink() {
		return distanceToNearestLink;
	}
	public void setDistanceToNearestLink(List<Double> distanceToNearestLink) {
		this.distanceToNearestLink = distanceToNearestLink;
	}
	public List<String> getNearestLinkID() {
		return nearestLinkID;
	}
	public void setNearestLinkID(List<String> nearestLinkID) {
		this.nearestLinkID = nearestLinkID;
	}
	

}
