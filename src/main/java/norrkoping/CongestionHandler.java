package norrkoping;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.Vehicles;
import org.matsim.vehicles.VehiclesFactory;

public class CongestionHandler implements LinkEnterEventHandler, LinkLeaveEventHandler, PersonDepartureEventHandler,
		PersonEntersVehicleEventHandler {

	private Map<Id<Vehicle>, Double> earliestLinkExitTime = new HashMap<>();
	private Map<Id<Vehicle>, Double> enterTime = new HashMap<>();

	private HashMap<String, Double> delay = new HashMap<>();
	private HashMap<String, Double> delayWorkers = new HashMap<>();
	private HashMap<String, Double> delayTrucks = new HashMap<>();
	private HashMap<String, Double> numberOfCars = new HashMap<>();
	private HashMap<String, Double> numberOfTrucks = new HashMap<>();
	private HashMap<String, Double> numberOfWorkers = new HashMap<>();
	private HashMap<Integer, Double> hourDelay = new HashMap<>();

	private HashMap<Integer, Double> hourCars = new HashMap<>();

	private HashMap<Integer, Double> hourDelayTrucks = new HashMap<>();
	private HashMap<Integer, Double> hourDelayWorkers = new HashMap<>();

	private HashMap<String, Double> linkSpeed = new HashMap<>();

	private Network network;
	private Scenario scenario;

	private Id<Vehicle> vehId;
	double time = 0;
	double linkTravelTime;

	public CongestionHandler(Network network, HashMap<String, Double> inTable, HashMap<String, Double> inTable2,
			HashMap<String, Double> inTable3, HashMap<String, Double> inTable4, HashMap<String, Double> inTable5,
			HashMap<String, Double> inTable7, HashMap<String, Double> inTable8) {
		this.network = network;
		this.delay = inTable;
		this.numberOfCars = inTable2;
		this.numberOfTrucks = inTable3;
		this.numberOfWorkers = inTable4;
		this.linkSpeed = inTable5;

		this.delayTrucks = inTable7;
		this.delayWorkers = inTable8;

		for (int i = 0; i < 24; i++) {
			hourDelay.put(i, 0.0);
			hourDelayTrucks.put(i, 0.0);
			hourDelayWorkers.put(i, 0.0);
			hourCars.put(i, 0.0);
		}
	}

	@Override
	public void reset(int iteration) {

		this.earliestLinkExitTime.clear();
		this.enterTime.clear();

	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		if ((!(event.getLinkId().toString().contains("tr")))) {

			Link link = network.getLinks().get(event.getLinkId());

			if (event.getVehicleId().toString().contains("truck")) {
				linkTravelTime = link.getLength() / (25 / 3.6);
			} else {
				linkTravelTime = link.getLength() / link.getFreespeed(event.getTime());

			}

			this.earliestLinkExitTime.put(event.getVehicleId(), event.getTime() + linkTravelTime);
			this.enterTime.put(event.getVehicleId(), event.getTime());

		}
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		if ((!(event.getLinkId().toString().contains("tr")))) {

			double excessTravelTime;
			double travelTime;
			double speed;

			if ((this.earliestLinkExitTime.get(event.getVehicleId()) == 0.0)) {
				excessTravelTime = 0.0;

			} else if ((event.getTime() - this.earliestLinkExitTime.get(event.getVehicleId()) < 1)) {
				excessTravelTime = 0.0;

			} else {
				excessTravelTime = event.getTime() - this.earliestLinkExitTime.get(event.getVehicleId());
			}

			int h = (int) Math.floor(event.getTime() / 3600);

			if (event.getVehicleId().toString().contains("truck")) {
				numberOfTrucks.put(event.getLinkId().toString(),
						(numberOfTrucks.get(event.getLinkId().toString()) + 1));
				delayTrucks.put(event.getLinkId().toString(),
						delayTrucks.get(event.getLinkId().toString()) + excessTravelTime);

				if (h < 24) {
					hourDelayTrucks.put(h, (hourDelayTrucks.get(h) + excessTravelTime / 60));
				}

			} else if (event.getVehicleId().toString().contains("worker")) {
				numberOfWorkers.put(event.getLinkId().toString(),
						(numberOfWorkers.get(event.getLinkId().toString()) + 1));
				delayWorkers.put(event.getLinkId().toString(),
						delayWorkers.get(event.getLinkId().toString()) + excessTravelTime);

				if (h < 24) {
					hourDelayWorkers.put(h, (hourDelayWorkers.get(h) + excessTravelTime / 60));
				}

			} else {

				delay.put(event.getLinkId().toString(), delay.get(event.getLinkId().toString()) + excessTravelTime);
				numberOfCars.put(event.getLinkId().toString(), (numberOfCars.get(event.getLinkId().toString()) + 1));
				if (h < 24) {
					hourDelay.put(h, (hourDelay.get(h) + excessTravelTime / 60));

				}
			}

			travelTime = event.getTime() - this.enterTime.get(event.getVehicleId());

			if (travelTime < 0) {

			} else {

				Link link = network.getLinks().get(event.getLinkId());
				speed = link.getLength() / travelTime;

				linkSpeed.put(event.getLinkId().toString(), linkSpeed.get(event.getLinkId().toString()) + speed);

			}

			// if statements used for validation

			if (event.getLinkId().toString().equals("315698") || event.getLinkId().toString().equals("650345")) {
				if (h < 24) {
					// hourCars.put(h, (hourCars.get(h)+1));
				}

			}

			if (event.getLinkId().toString().equals("936685")) {
				if (h < 24) {
					hourCars.put(h, (hourCars.get(h) + 1));
				}
			}

		}
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if ((!(event.getLinkId().toString().contains("tr")))) {

			String personID = event.getPersonId().toString();

			int lenghtString = personID.length();

			if (lenghtString > 30) {
				System.out.println("do nothing");

			} else {
				if (event.getPersonId().toString().contains("truck")) {

					vehId = Id.create(event.getPersonId() + "_truck", Vehicle.class);
					this.earliestLinkExitTime.put(vehId, 0.0);
					this.enterTime.put(vehId, 0.0);

				} else {
					vehId = Id.create(event.getPersonId(), Vehicle.class);
					this.earliestLinkExitTime.put(vehId, 0.0);
					this.enterTime.put(vehId, 0.0);
				}

			}

		}

	}

	public void handleEvent(PersonEntersVehicleEvent event) {
		if ((!(event.getVehicleId().toString().contains("tr")))) {

			String personID = event.getPersonId().toString();
			int lenghtString = personID.length();

			if (lenghtString > 30) {

			}

		}
	}

	public double getAverageTravelTime() {
		return 0;
	}

	public HashMap<String, Double> getDelayTable() {

		return delay;
	}

	public HashMap<String, Double> getCarsOnLinks() {

		return numberOfCars;
	}

	public HashMap<String, Double> getTrucksOnLinks() {

		return numberOfTrucks;
	}

	public HashMap<String, Double> getWorkersksOnLinks() {

		return numberOfWorkers;
	}

	public HashMap<String, Double> getLinkSpeed() {

		return linkSpeed;
	}

	public HashMap<Integer, Double> getHourDelay() {

		return hourDelay;
	}

	public HashMap<String, Double> getDelayTrucks() {

		return delayTrucks;
	}

	public HashMap<String, Double> getDelayWorkers() {

		return delayWorkers;
	}

	public HashMap<Integer, Double> getHourTruckDelay() {

		return hourDelayTrucks;
	}

	public HashMap<Integer, Double> getHourWorkerskDelay() {

		return hourDelayWorkers;
	}

	public HashMap<Integer, Double> getHourCars() {

		return hourCars;
	}

}
