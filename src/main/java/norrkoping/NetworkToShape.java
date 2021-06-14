package norrkoping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.PointFeatureFactory;
import org.matsim.core.utils.gis.PolylineFeatureFactory;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class NetworkToShape {

	public static HashMap<String, Double> delay = new HashMap<>();
	public static HashMap<String, Double> numCars = new HashMap<>();
	public static HashMap<String, Double> numTrucks = new HashMap<>();

	public static HashMap<String, Double> numWork = new HashMap<>();
	public static HashMap<String, Double> avgDelay = new HashMap<>();
	public static HashMap<String, Double> speed = new HashMap<>();
	public static HashMap<String, Double> delayPrc = new HashMap<>();

	public NetworkToShape(HashMap<String, Double> inTable1, HashMap<String, Double> inTable2,
			HashMap<String, Double> inTable3, HashMap<String, Double> inTable4, HashMap<String, Double> inTable5,
			HashMap<String, Double> inTable6, HashMap<String, Double> inTable7) {

		this.delay = inTable1;
		this.numCars = inTable2;
		this.numTrucks = inTable3;
		this.numWork = inTable4;
		this.avgDelay = inTable5;
		this.speed = inTable6;
		this.delayPrc = inTable7;

	}

	public static void doEverything() {

		Config config = ConfigUtils.createConfig();
		config.network().setInputFile("./networkUpdated.xml");
		Scenario scenario = ScenarioUtils.loadScenario(config);
		Network network = scenario.getNetwork();

		// CHANGE COORDIANTE SYSTEM
		CoordinateReferenceSystem crs = MGC.getCRS("EPSG:3006"); // EPSG Code SWEDEN SWEREF99 TM

		Collection<SimpleFeature> features = new ArrayList<>();
		PolylineFeatureFactory linkFactory = new PolylineFeatureFactory.Builder().setCrs(crs).setName("link")
				.addAttribute("ID", String.class).addAttribute("fromID", String.class)
				.addAttribute("toID", String.class).addAttribute("length", Double.class)
				.addAttribute("type", String.class).addAttribute("capacity", Double.class)
				.addAttribute("freespeed", Double.class).addAttribute("delay", Double.class)
				.addAttribute("cars", Double.class).addAttribute("trucks", Double.class)
				.addAttribute("workers", Double.class).addAttribute("avgDelay", Double.class)
				.addAttribute("avgSpeed", Double.class).addAttribute("delayPrc", Double.class).create();

		for (Link link : network.getLinks().values()) {
			Coordinate fromNodeCoordinate = new Coordinate(link.getFromNode().getCoord().getX(),
					link.getFromNode().getCoord().getY());
			Coordinate toNodeCoordinate = new Coordinate(link.getToNode().getCoord().getX(),
					link.getToNode().getCoord().getY());
			Coordinate linkCoordinate = new Coordinate(link.getCoord().getX(), link.getCoord().getY());

			String valueToInster = (delay.get(link.getId().toString())).toString();
			String nrCars = (numCars.get(link.getId().toString())).toString();
			String nrTrucks = (numTrucks.get(link.getId().toString())).toString();

			String nrWorkers = (numWork.get(link.getId().toString())).toString();
			String avgDelay1 = (avgDelay.get(link.getId().toString())).toString();
			String avgSpeed = (speed.get(link.getId().toString())).toString();

			String delayPrc1 = (delayPrc.get(link.getId().toString())).toString();

			SimpleFeature ft = linkFactory.createPolyline(
					new Coordinate[] { fromNodeCoordinate, linkCoordinate, toNodeCoordinate },
					new Object[] { link.getId().toString(), link.getFromNode().getId().toString(),
							link.getToNode().getId().toString(), link.getLength(), NetworkUtils.getType(link),
							link.getCapacity(), link.getFreespeed(), valueToInster, nrCars, nrTrucks, nrWorkers,
							avgDelay1, avgSpeed, delayPrc1 },
					null);
			features.add(ft);
		}
		ShapeFileWriter.writeGeometries(features,
				"C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\ResultFile\\network_linksTest.shp");

		features = new ArrayList<>();
		PointFeatureFactory nodeFactory = new PointFeatureFactory.Builder().setCrs(crs).setName("nodes")
				.addAttribute("ID", String.class).create();

		for (Node node : network.getNodes().values()) {
			SimpleFeature ft = nodeFactory.createPoint(node.getCoord(), new Object[] { node.getId().toString() }, null);
			features.add(ft);
		}
		ShapeFileWriter.writeGeometries(features,
				"C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\ResultFile\\network_nodesTest.shp");

	}

	public static void main(String[] args) {

	}

}
