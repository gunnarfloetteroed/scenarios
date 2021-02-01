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

	public static String networkOriginal = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\originalFiles\\networkOrg.xml";

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
			if (l.getId().toString().equals("157916") || l.getId().toString().equals("286674")
					|| l.getId().toString().equals("286675")) {
				l.setCapacity(600);
				l.setNumberOfLanes(1);
			}

			if (l.getId().toString().equals("1242932") || l.getId().toString().equals("953687")
					|| l.getId().toString().equals("953710") || l.getId().toString().equals("953711")
					|| l.getId().toString().equals("770001") || l.getId().toString().equals("770002")) {
				l.setFreespeed(16.66);
			}

		}

		final DatabaseODMatrix od = new DatabaseODMatrix(user, passwd, "localhost", 5432); // 5455);
		od.getSiteCoordinatesFromDatabase();
		HashMap<String, Double> xCoord = od.getSiteCoordinatesX();
		HashMap<String, Double> yCoord = od.getSiteCoordinatesY();

		od.getStoreCoordinates();
		HashMap<String, Double> xCoordStore = od.getStoreCoordinatesX();
		HashMap<String, Double> yCoordStore = od.getStoreCoordinatesY();
		
		//CreareLong Haul coordinate

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
			linkIn.setAllowedModes(CollectionUtils.stringToSet("car, truck, truck23, carW"));
			linkOut.setAllowedModes(CollectionUtils.stringToSet("car, truck, truck23, carW"));
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
			linkIn.setAllowedModes(CollectionUtils.stringToSet("car, truck, truck23, carW"));
			linkOut.setAllowedModes(CollectionUtils.stringToSet("car, truck, truck23, carW"));
			network.addLink(linkIn);
			network.addLink(linkOut);

		}

		new NetworkWriter(network)
				.write("C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\networkUpdated.xml");

	}

}
