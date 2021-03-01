package stockholm.bicycles.mapmatching;

import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;


public class GPSSequence {

	private Id<Person> personID;
	private String mode;
	
	private List<GPSPoint> gpsPoints;
	
	public GPSSequence(Id<Person> personID, List<GPSPoint> gpsPoints) {
		super();
		this.personID = personID;
		this.gpsPoints = gpsPoints;
		this.mode="bike";
	}
	
	public GPSSequence(Id<Person> personID, List<GPSPoint> gpsPoints, String mode) {
		super();
		this.personID = personID;
		this.gpsPoints = gpsPoints;
		this.mode=mode;
	}
	
	public Id<Person> getPersonID() {
		return personID;
	}
	public void setPersonID(Id<Person> personID) {
		this.personID = personID;
	}
	public List<GPSPoint> getGPSPoints() {
		return gpsPoints;
	}
	public void setGpsPoints(List<GPSPoint> gpsPoints) {
		this.gpsPoints = gpsPoints;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public void printInfo() {
		System.out.println(this.personID.toString());
		System.out.println(this.mode);
		for (GPSPoint GPSPoint : this.gpsPoints) {
			System.out.println("timeStamp: "+GPSPoint.getTimeStamp().toString()+ ". distance:"+GPSPoint.getDelta_m()+". speed: "+GPSPoint.getSpeed()+ ". Coordinate: "+GPSPoint.getCoord().toString());
		}
	}

}
