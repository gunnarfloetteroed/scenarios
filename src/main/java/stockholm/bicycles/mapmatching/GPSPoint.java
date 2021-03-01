package stockholm.bicycles.mapmatching;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.misc.OptionalTime;

public class GPSPoint {

	private Coord coord;
	private OptionalTime timeStamp;
	private double nodeSearchRadius; // the search radius in the network.
	private double delta_m;
	private double speed;

	public GPSPoint(Coord coord, OptionalTime timeStamp, double nodeSearchRadius) {
		super();
		this.coord = coord;
		this.timeStamp = timeStamp;
		this.nodeSearchRadius=nodeSearchRadius;
	}
	
	public GPSPoint(Coord coord, OptionalTime timeStamp,  double delta_m, double nodeSearchRadius,double speed) {
		super();
		this.coord = coord;
		this.timeStamp = timeStamp;
		this.nodeSearchRadius=nodeSearchRadius;
		this.delta_m=delta_m;
		this.speed=speed;
		
	}
	
	public double getDelta_m() {
		return delta_m;
	}

	public void setDelta_m(double delta_m) {
		this.delta_m = delta_m;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public void setCoord(Coord coord) {
		this.coord = coord;
	}
	public void setTimeStamp(OptionalTime timeStamp) {
		this.timeStamp = timeStamp;
	}
	public Coord getCoord() {
		return coord;
	}
	public OptionalTime getTimeStamp() {
		return timeStamp;
	}
	public double getNodeSearchRadius() {
		return nodeSearchRadius;
	}
	public void setNodeSearchRadius(double nodeSearchRadius) {
		this.nodeSearchRadius = nodeSearchRadius;
	}

}
