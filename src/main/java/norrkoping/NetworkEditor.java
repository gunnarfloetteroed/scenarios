package norrkoping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.CollectionUtils;

public class NetworkEditor {

	public static String configFile;

	public static String networkOriginal = "./networkOrg.xml";

	public static String user;
	public static String passwd;

	public NetworkEditor(String inFile, String user, String pass) {

		this.configFile = inFile;
		this.user = user;
		this.passwd = pass;

	}

	public static void editNework() {

		final Config config = ConfigUtils.loadConfig(configFile);
		config.network().setInputFile(networkOriginal);

		Scenario scenario = ScenarioUtils.loadScenario(config);

		Network netOriginal = NetworkUtils.createNetwork();
		new MatsimNetworkReader(netOriginal).readFile(networkOriginal);

		Network network = scenario.getNetwork();
		NetworkFactory netF = network.getFactory();

		for (Link l : network.getLinks().values()) {

			l.setAllowedModes(CollectionUtils.stringToSet("car, truck, truck23, carW"));
			if (l.getId().toString().equals("1412451") || l.getId().toString().equals("1412450")) {
				l.setAllowedModes(CollectionUtils.stringToSet("walk"));
				l.setCapacity(1);
				l.setFreespeed(1);
			}

			// Line below are based on validation process
			// It increases link capacities and fixes the location with too much congestion
			// Remove the lines below till last if statement with capacity
			if (l.getId().toString().equals("157916") || l.getId().toString().equals("286674")
					|| l.getId().toString().equals("286675")) {
				l.setCapacity(600.0);
				l.setNumberOfLanes(1);
			}

			if (l.getId().toString().equals("1242932") || l.getId().toString().equals("953687")
					|| l.getId().toString().equals("953710") || l.getId().toString().equals("953711")
					|| l.getId().toString().equals("770001") || l.getId().toString().equals("770002")) {
				l.setFreespeed(16.66);
			}
			if (l.getId().toString().equals("690250") || l.getId().toString().equals("690248")
					|| l.getId().toString().equals("690247") || l.getId().toString().equals("690252")) {
				l.setCapacity(2000.0);
			}
			if (l.getId().toString().equals("608016") || l.getId().toString().equals("439357")
					|| l.getId().toString().equals("439355") || l.getId().toString().equals("607881")
					|| l.getId().toString().equals("538823") || l.getId().toString().equals("538821")
					|| l.getId().toString().equals("538819") || l.getId().toString().equals("690312")) {
				l.setCapacity(2000.0);

			}

			if (l.getId().toString().equals("213740") || l.getId().toString().equals("953710")
					|| l.getId().toString().equals("953685")) {
				l.setCapacity(2000.0);

			}
			if (l.getId().toString().equals("1054977") || l.getId().toString().equals("217631")
					|| l.getId().toString().equals("1054977") || l.getId().toString().equals("1041055")
					|| l.getId().toString().equals("737396") || l.getId().toString().equals("737394")
					|| l.getId().toString().equals("825493") || l.getId().toString().equals("737402")
					|| l.getId().toString().equals("737400") || l.getId().toString().equals("737399")) {
				l.setCapacity(2000.0);

			}
			if (l.getId().toString().equals("608026") || l.getId().toString().equals("607951")
					|| l.getId().toString().equals("607957") || l.getId().toString().equals("607969")
					|| l.getId().toString().equals("607955")) {
				l.setCapacity(2000.0);

			}

			if (l.getId().toString().equals("217679") || l.getId().toString().equals("217692")
					|| l.getId().toString().equals("217687") || l.getId().toString().equals("1006623")) {
				l.setCapacity(2000.0);

			}

			if (l.getId().toString().equals("538817") || l.getId().toString().equals("538815")
					|| l.getId().toString().equals("607963") || l.getId().toString().equals("607961")
					|| l.getId().toString().equals("607977") || l.getId().toString().equals("607991")
					|| l.getId().toString().equals("607989") || l.getId().toString().equals("607987")) {
				l.setCapacity(1200.0);

			}

			if (l.getId().toString().equals("1046157") || l.getId().toString().equals("161300")
					|| l.getId().toString().equals("1462098") || l.getId().toString().equals("274612")) {
				l.setCapacity(1000.0);

			}

			if (l.getId().toString().equals("286725") || l.getId().toString().equals("286726")
					|| l.getId().toString().equals("286727") || l.getId().toString().equals("681670")
					|| l.getId().toString().equals("286724") || l.getId().toString().equals("286723")
					|| l.getId().toString().equals("683422")) {
				l.setCapacity(1000.0);

			}

			if (l.getId().toString().equals("675496") || l.getId().toString().equals("885698")
					|| l.getId().toString().equals("885700") || l.getId().toString().equals("1333123")
					|| l.getId().toString().equals("1333122") || l.getId().toString().equals("1333124")) {
				l.setCapacity(1200.0);

			}

			if (l.getId().toString().equals("274615") || l.getId().toString().equals("274614")
					|| l.getId().toString().equals("1416308") || l.getId().toString().equals("31278")
					|| l.getId().toString().equals("31281") || l.getId().toString().equals("715292")) {
				l.setCapacity(1200.0);

			}

			if (l.getId().toString().equals("1228786") || l.getId().toString().equals("736126")) {
				l.setCapacity(1500.0);

			}

			if (l.getId().toString().equals("259682")) {
				l.setCapacity(1200.0);

			}

			if (l.getId().toString().equals("450237") || l.getId().toString().equals("450205")
					|| l.getId().toString().equals("1323371")) {
				l.setCapacity(1200.0);

			}

			if (l.getId().toString().equals("1315267") || l.getId().toString().equals("1315274")
					|| l.getId().toString().equals("1315264") || l.getId().toString().equals("1315274")
					|| l.getId().toString().equals("1315265") || l.getId().toString().equals("1315183")) {
				l.setCapacity(1300.0);

			}

			if (l.getId().toString().equals("141205") || l.getId().toString().equals("141204")
					|| l.getId().toString().equals("141203") || l.getId().toString().equals("313026")
					|| l.getId().toString().equals("1315176") || l.getId().toString().equals("141201")) {
				l.setCapacity(1300.0);

			}

			if (l.getId().toString().equals("313040") || l.getId().toString().equals("1315184")
					|| l.getId().toString().equals("1315177") || l.getId().toString().equals("141202")
					|| l.getId().toString().equals("313017") || l.getId().toString().equals("313041")
					|| l.getId().toString().equals("1315266")) {

				l.setCapacity(1300.0);

			}

			if (l.getId().toString().equals("1273824") || l.getId().toString().equals("19387")) {
				l.setCapacity(1000.0);

			}

			if (l.getId().toString().equals("770002") || l.getId().toString().equals("770001")) {
				l.setCapacity(1200.0);

			}

			if (l.getId().toString().equals("190730")) {
				l.setCapacity(900.0);

			}

			if (l.getId().toString().equals("456674") || l.getId().toString().equals("456677")) {
				l.setCapacity(1200.0);

			}

			if (l.getId().toString().equals("1041702")) {
				l.setCapacity(3000.0);

			}

			if (l.getId().toString().equals("19435") || l.getId().toString().equals("1389585")
					|| l.getId().toString().equals("1251378") || l.getId().toString().equals("9921")
					|| l.getId().toString().equals("9904") || l.getId().toString().equals("286687")
					|| l.getId().toString().equals("1389569") || l.getId().toString().equals("19436")) {
				l.setCapacity(900.0);

			}

			if (l.getId().toString().equals("19446") || l.getId().toString().equals("19445")) {
				l.setCapacity(900.0);

			}

			if (l.getId().toString().equals("19380") || l.getId().toString().equals("19379")
					|| l.getId().toString().equals("19382") || l.getId().toString().equals("19381")) {
				l.setCapacity(900.0);

			}

			if (l.getId().toString().equals("1338810") || l.getId().toString().equals("1338811")
					|| l.getId().toString().equals("1338812") || l.getId().toString().equals("1315174")
					|| l.getId().toString().equals("1338809")) {
				l.setCapacity(800.0);

			}

			if (l.getId().toString().equals("19444") || l.getId().toString().equals("19443")) {
				l.setCapacity(900.0);

			}

			if (l.getId().toString().equals("443423")) {
				l.setCapacity(800.0);

			}
			if (l.getId().toString().equals("1385057")) {
				l.setCapacity(1750.0);

			}

			if (l.getId().toString().equals("736091")) {
				l.setCapacity(1200.0);

			}
			if (l.getId().toString().equals("595247")) {
				l.setCapacity(800.0);

			}

			if (l.getId().toString().equals("19442")) {
				l.setCapacity(700.0);

			}

			if (l.getId().toString().equals("1290511")) {
				l.setCapacity(1200.0);

			}
			if (l.getId().toString().equals("736112") || l.getId().toString().equals("736116")) {
				l.setCapacity(800.0);

			}

			if (l.getId().toString().equals("296049")) {
				l.setCapacity(800.0);

			}

			if (l.getId().toString().equals("1387270") || l.getId().toString().equals("953697")
					|| l.getId().toString().equals("953698")) {
				l.setCapacity(1200.0);

			}

			if (l.getId().toString().equals("1242933")) {
				l.setCapacity(1300.0);

			}

			if (l.getId().toString().equals("213741")) {
				l.setCapacity(1200.0);

			} // Delay validation and link capacities end here

		}

		// From database
		/*
		 * final DatabaseODMatrix od = new DatabaseODMatrix(user, passwd, "localhost",
		 * 5432); // 5455); od.getSiteCoordinatesFromDatabase(); HashMap<String, Double>
		 * xCoord = od.getSiteCoordinatesX(); HashMap<String, Double> yCoord =
		 * od.getSiteCoordinatesY();
		 * 
		 * od.getStoreCoordinates(); HashMap<String, Double> xCoordStore =
		 * od.getStoreCoordinatesX(); HashMap<String, Double> yCoordStore =
		 * od.getStoreCoordinatesY();
		 */

		// Offline input
		ReadInputFiles inputFiles = new ReadInputFiles("run");
		HashMap<String, Double> xCoord = inputFiles.getXcoordinateSite();
		HashMap<String, Double> yCoord = inputFiles.getYcoordinateSite();

		HashMap<String, Double> xCoordStore = inputFiles.getXcoordLocalStore();
		HashMap<String, Double> yCoordStore = inputFiles.getYcoordLocalStore();

		// CreareLong Haul coordinate

		for (HashMap.Entry<String, Double> entry : xCoord.entrySet()) {

			Node siteNode = netF.createNode(Id.create("node" + entry.getKey(), Node.class),
					new Coord(entry.getValue(), yCoord.get(entry.getKey())));
			Node entrySite = NetworkUtils.getNearestNode(netOriginal,
					new Coord(entry.getValue(), yCoord.get(entry.getKey())));
			network.addNode(siteNode);
			Link linkIn = netF.createLink(Id.create("in" + entry.getKey(), Link.class), entrySite, siteNode);
			Link linkOut = netF.createLink(Id.create("out" + entry.getKey(), Link.class), siteNode, entrySite);
			linkIn.setCapacity(50000);
			linkOut.setCapacity(50000);
			linkIn.setFreespeed(50);
			linkOut.setFreespeed(50);
			linkIn.setAllowedModes(CollectionUtils.stringToSet("truck, truck23, carW"));
			linkOut.setAllowedModes(CollectionUtils.stringToSet("truck, truck23, carW"));
			network.addLink(linkIn);
			network.addLink(linkOut);

		}

		for (HashMap.Entry<String, Double> entry : xCoordStore.entrySet()) {

			Node storeNode = netF.createNode(Id.create("nodeStore" + entry.getKey(), Node.class),
					new Coord(entry.getValue(), yCoordStore.get(entry.getKey())));
			Node entryStore = NetworkUtils.getNearestNode(network,
					new Coord(entry.getValue(), yCoordStore.get(entry.getKey())));
			network.addNode(storeNode);
			Link linkIn = netF.createLink(Id.create("inStore" + entry.getKey(), Link.class), entryStore, storeNode);
			Link linkOut = netF.createLink(Id.create("outStore" + entry.getKey(), Link.class), storeNode, entryStore);
			linkIn.setCapacity(50000);
			linkOut.setCapacity(50000);
			linkIn.setFreespeed(50);
			linkOut.setFreespeed(50);
			linkIn.setAllowedModes(CollectionUtils.stringToSet("truck, truck23, carW"));
			linkOut.setAllowedModes(CollectionUtils.stringToSet("truck, truck23, carW"));
			network.addLink(linkIn);
			network.addLink(linkOut);

		}

		new NetworkWriter(network).write("./networkUpdated.xml");

	}

}
