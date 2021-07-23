package stockholm.bicycles.mapmatching;

import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

/**
 * <h1> container class to store the GPS sequence as well as some additional data: </h1>
 * the container class holds the info for GPSSequence as well as 
 * 1. the distance from each GPS point to its nearest link in the matched path;
 * 2. the link ID (string) of the nearest link for each GPS point in the matched path ;
 * 3. the distance from each GPS point to its nearest link in the alternative matched path (when the whole matched path is removed);
 * 4. the link ID (string) of the nearest link for each GPS point in the alternative matched path ;
 * <p>
 * <b>Note:</b> None.
 *
 * @author  Chengxi Liu
 * @version 1.0
 * @since   2021-07-21
 */


public class GPSSequenceWithDistanceToMatchedPath extends GPSSequence {
	private List<Double> distanceToNearestLink=null;
	private List<String> nearestLinkID=null;


	public GPSSequenceWithDistanceToMatchedPath(Id<Person> personID, List<GPSPoint> gpsPoints,
			List<Double> distanceToNearestLink,List<String> nearestLinkID) {
		super(personID, gpsPoints);
		this.distanceToNearestLink=distanceToNearestLink;
		this.nearestLinkID=nearestLinkID;

	}

	public GPSSequenceWithDistanceToMatchedPath(Id<Person> personID, List<GPSPoint> gpsPoints, String mode,
			List<Double> distanceToNearestLink,List<String> nearestLinkID) {
		super(personID, gpsPoints,mode);
		this.distanceToNearestLink=distanceToNearestLink;
		this.nearestLinkID=nearestLinkID;
	}

	public GPSSequenceWithDistanceToMatchedPath(GPSSequence sequence,
			List<Double> distanceToNearestLink,List<String> nearestLinkID) {
		super(sequence.getPersonID(), sequence.getGPSPoints(),sequence.getMode());
		this.distanceToNearestLink=distanceToNearestLink;
		this.nearestLinkID=nearestLinkID;
	}


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


	public boolean hasAdditionalData() {
		if (this.getDistanceToNearestLink()!=null & this.getNearestLinkID()!=null) {
			return true;
		} else {
			return false;
		}

	}

}
