/*
 * Copyright 2018 Gunnar Flötteröd
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * contact: gunnar.flotterod@gmail.com
 *
 */
package norrkoping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import javax.print.attribute.standard.Destination;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.contrib.greedo.Greedo;
import org.matsim.contrib.greedo.GreedoConfigGroup;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfig;
import org.matsim.core.mobsim.qsim.components.StandardQSimComponentConfigurator;
import org.matsim.core.mobsim.qsim.qnetsimengine.ConfigurableQNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.DefaultTurnAcceptanceLogic;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetworkFactory;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.filter.NetworkFilterManager;
import org.matsim.core.network.filter.NetworkLinkFilter;
import org.matsim.core.network.filter.NetworkNodeFilter;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.population.algorithms.XY2Links;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteFactories;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.utils.CreatePseudoNetwork;
import org.matsim.vehicles.MatsimVehicleWriter;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleType;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ScoringParameterSet;

import com.google.inject.Provides;

import ch.sbb.matsim.config.SBBTransitConfigGroup;
import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.mobsim.qsim.SBBTransitModule;
import ch.sbb.matsim.mobsim.qsim.pt.SBBTransitEngineQSimModule;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import floetteroed.utilities.Units;
import stockholm.ihop2.regent.demandreading.ZonalSystem;
import stockholm.ihop2.regent.demandreading.Zone;
import stockholm.saleem.StockholmTransformationFactory;
import stockholm.utils.ShapeUtils;
import stockholm.wum.analysis.PopulationSampler;
import stockholm.wum.creation.CropTransitSystem;
import stockholm.wum.creation.DetailPTVehicles;

/**
 *
 * @author Gunnar Flötteröd
 *
 */
public class NorrkopingProductionRunner {

	static final String norrkopingZoneShapeFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\od_zone_norrk.shp";

	static final String configFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\norrkoping-config.xml";
	static final String configFileUpdated = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\ConfigUpdated.xml";

	static final String norrkopingNetwork = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\networkUpdated.xml";
	static final String nrkpPlans = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\nrkpPlans.xml";
	static final String nrkpPlans50 = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\nrkpPlans50.xml";
	static final String vehiclesFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\vehicleTypes.xml";
	static final String populationMerged = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\popMerged.xml";

	static final String norrkopingTransitScheduleFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\transitSchedule-norrkoping-20190214.xml.gz";
	static final String norrkopingTransitVehicleFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\transitVehicles-norrkoping-20190214.xml.gz";

	static final Coord centrum = new Coord(569092.878, 6495008.141);

	static final Coord fromLinkoping = new Coord(542656.7185038754, 6478707.856454755);
	static final Coord toLinkoping = new Coord(542649.8589400095, 6478720.787579953);

	public static HashMap<String, String> zoneType = new HashMap<>();

	public static ArrayList<String> workerZone = new ArrayList<>();
	public static ArrayList<Double> workerToWork = new ArrayList<>();
	public static ArrayList<Double> workerToHome = new ArrayList<>();
	public static ArrayList<String> workerTransport = new ArrayList<>();

	public static String user;
	public static String passwd;

	/*
	 * static void cutFromSwedenCarOnly(final double xMin, final double xMax, final
	 * double yMin, final double yMax) {
	 * 
	 * final Config config = ConfigUtils.createConfig();
	 * config.network().setInputFile(swedenNetworkFile); final Scenario scenario =
	 * ScenarioUtils.loadScenario(config); System.out.println("raw data: " +
	 * scenario.getNetwork().getNodes().size());
	 * 
	 * final NetworkFilterManager filters = new
	 * NetworkFilterManager(scenario.getNetwork()); final NetworkNodeFilter
	 * nodeFilter = new NetworkNodeFilter() {
	 * 
	 * @Override public boolean judgeNode(Node n) { final double x =
	 * n.getCoord().getX(); final double y = n.getCoord().getY(); return ((xMin <=
	 * x) && (x <= xMax) && (yMin <= y) && (y <= yMax)); } };
	 * filters.addNodeFilter(nodeFilter); filters.addLinkFilter(new
	 * NetworkLinkFilter() {
	 * 
	 * @Override public boolean judgeLink(Link l) { return
	 * (nodeFilter.judgeNode(l.getFromNode()) &&
	 * nodeFilter.judgeNode(l.getToNode())); } }); final Network net =
	 * filters.applyFilters(); System.out.println("after filtering: " +
	 * net.getNodes().size());
	 * 
	 * new NetworkCleaner().run(net); System.out.println("after cleaning: " +
	 * net.getNodes().size());
	 * 
	 * new NetworkWriter(net).write(norrkopingNetworkFile); }
	 * 
	 * static void cutFromSwedenPTOnly() {
	 * 
	 * final ZonalSystem zonalSystem = new ZonalSystem(norrkopingZoneShapeFile,
	 * StockholmTransformationFactory.WGS84_SWEREF99, "id");
	 * 
	 * final CropTransitSystem cropTransit = new CropTransitSystem(zonalSystem,
	 * swedenScheduleFile, swedenTransitVehicleFile,
	 * StockholmTransformationFactory.getCoordinateTransformation(
	 * StockholmTransformationFactory.WGS84_SWEREF99,
	 * StockholmTransformationFactory.WGS84_SWEREF99));
	 * cropTransit.run(norrkopingTransitScheduleFile, norrkopingTransitVehicleFile);
	 * 
	 * final DetailPTVehicles detailPT = new
	 * DetailPTVehicles(transitVehicleTypeDefinitionsFileName,
	 * norrkopingTransitScheduleFile, swedenTransitVehicleFile);
	 * detailPT.run(norrkopingTransitVehicleFile); }
	 */

	public static void createZoneTypes() {

		System.out.println("Zones");

		final ZonalSystem zonalSystem = new ZonalSystem(norrkopingZoneShapeFile,
				StockholmTransformationFactory.WGS84_SWEREF99, "id");

		double x1 = centrum.getX();
		double x2 = 0.0;
		double y1 = centrum.getY();
		double y2 = 0.0;

		for (Zone fromZone : zonalSystem.getId2zoneView().values()) {

			final Point point = fromZone.getGeometry().getCentroid();

			x2 = point.getX();
			y2 = point.getY();

			double distance = Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));

			if (distance < 2000) {
				zoneType.put(fromZone.getId().toString(), "staden");
			} else if (distance < 10000) {
				zoneType.put(fromZone.getId().toString(), "utkanten");
			} else if (fromZone.getId().toString().equals("167")) {
				System.out.println("WORKS ");
				zoneType.put(fromZone.getId().toString(), "outside");
			} else {
				zoneType.put(fromZone.getId().toString(), "landet");
			}

		}

		// String outPath =
		// "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\ProjectData\\zoneTypeRight.csv";
		// ResultWriter writer = new ResultWriter(outPath, zoneType);
		// writer.main();

	}

	public static void createDemandWorkers(String configFile, double upScale, int nrSites) throws Exception {

		System.out.println("WORKERS");
		createZoneTypes();

		final Config config = ConfigUtils.loadConfig(configFile);

		config.network().setInputFile(norrkopingNetwork);
		final Scenario scenario = ScenarioUtils.loadScenario(config);
		final ZonalSystem zonalSystem = new ZonalSystem(norrkopingZoneShapeFile,
				StockholmTransformationFactory.WGS84_SWEREF99, "id");

		// final DatabaseODMatrix od = new DatabaseODMatrix(user, passwd, "localhost",
		// 5432); // 5455);
		// od.createArrayListsWorkers();

		ReadInputFiles inputFiles = new ReadInputFiles("run");
		inputFiles.readFileODworkers();

		// Save into lists
		ArrayList<Integer> oWorker = inputFiles.getOriginWorkers();
		ArrayList<Integer> dWorker = inputFiles.getDestinationWorkers();
		ArrayList<Integer> flowWorker = inputFiles.getFlowWorkers();

		for (int i = 0; i < nrSites; i++) {
			inputFiles.readFileWorkersInfo();
			workerZone = inputFiles.getWorkerZone();
			workerTransport = inputFiles.getWorkerTransport();
			workerToWork = inputFiles.getWorkerToWork();
			workerToHome = inputFiles.getWorkerToHome();

		}

		ArrayList<String> copyWorkerZone = new ArrayList<>(workerZone);
		ArrayList<Double> copyWokerToWork = new ArrayList<>(workerToWork);
		ArrayList<Double> copyWorkerToHome = new ArrayList<>(workerToHome);
		ArrayList<String> copyWorkerTransport = new ArrayList<>(workerTransport);

		System.out.println("ERROR ARRAY " + copyWokerToWork.size());
		int counter = 0;

		int person = 1;
		int sites = 0;
		int scale = (int) upScale;

		for (int i = 0; i < oWorker.size(); i++) {

			// Change sites here
			if (dWorker.get(i) <= 10) {

				int flow = flowWorker.get(i) * scale;
				System.out.println("ORIGIN " + oWorker.get(i));
				String type = zoneType.get(oWorker.get(i).toString());
				System.out.println("TYPE " + type);

				for (int j = 0; j < flow; j++) {
					counter = counter + 1;
					int index = copyWorkerZone.indexOf(type);
					System.out.println("INDEX " + index);
					Zone fromZone1 = zonalSystem.getZone(oWorker.get(i).toString());

					System.out.println("ZonalSystem " + fromZone1.getId());
					System.out.println("Counter " + counter);

					addTripWorkers(scenario, Id.createPersonId("worker" + (person++)), zonalSystem, fromZone1,
							dWorker.get(i),
							(copyWokerToWork.get(index) * 3600
									+ (ThreadLocalRandom.current().nextDouble(-0.08, 0.08)) * 3600),
							(copyWorkerToHome.get(index) * 3600
									+ (ThreadLocalRandom.current().nextDouble(-0.16, 0.16) * 3600)),
							copyWorkerTransport.get(index), sites);

					copyWorkerZone.remove(index);
					copyWokerToWork.remove(index);
					copyWorkerToHome.remove(index);
					copyWorkerTransport.remove(index);

				}

			}
		}

		createDemandTrucksFromOD(scenario);

	}

	private static void addTripWorkers(Scenario scenario, Id<Person> personId, ZonalSystem zonalSystem, Zone fromZone,
			Integer toZone, double departureHome, double departureWork, String transport, int sites) {

		final Coord fromCoord = ShapeUtils.drawPointFromGeometry(fromZone.getGeometry());
		final Person person = scenario.getPopulation().getFactory().createPerson(personId);

		if (transport.equals("pt")) {

		} else {
			person.getAttributes().putAttribute("subpopulation", "workers");
		}

		if (transport.equals("car")) {
			transport = "carW";
		}

		final Plan plan = scenario.getPopulation().getFactory().createPlan();
		person.addPlan(plan);

		if (fromZone.getId().equals("167")) {

			final Activity start = scenario.getPopulation().getFactory().createActivityFromCoord("start",
					fromLinkoping);
			start.setEndTime(departureHome);
			plan.addActivity(start);
			plan.addLeg(scenario.getPopulation().getFactory().createLeg(transport));

		} else {

			final Activity start = scenario.getPopulation().getFactory().createActivityFromCoord("start", fromCoord);

			if (transport.equals("pt")) {
				start.setEndTime(departureHome - (3600));
				plan.addActivity(start);
				plan.addLeg(scenario.getPopulation().getFactory().createLeg(transport));
			} else {
				start.setEndTime(departureHome);
				plan.addActivity(start);
				plan.addLeg(scenario.getPopulation().getFactory().createLeg(transport));
			}

		}

		ReadInputFiles inputFiles = new ReadInputFiles("run");
		HashMap<String, Double> xCoord = inputFiles.getXcoordinateSite();
		HashMap<String, Double> yCoord = inputFiles.getYcoordinateSite();
		/////////////////////////////////

		final Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end",
				new Coord(xCoord.get(toZone.toString()), yCoord.get(toZone.toString())));
		end.setStartTime(departureHome + 1800);
		end.setEndTime(departureWork);
		plan.addActivity(end);

		if (fromZone.getId().equals("167")) {

			plan.addLeg(scenario.getPopulation().getFactory().createLeg(transport));
			final Activity start1 = scenario.getPopulation().getFactory().createActivityFromCoord("start", toLinkoping);
			plan.addActivity(start1);

		} else {

			plan.addLeg(scenario.getPopulation().getFactory().createLeg(transport));
			Activity start1 = scenario.getPopulation().getFactory().createActivityFromCoord("start", fromCoord);
			plan.addActivity(start1);

		}

		scenario.getPopulation().addPerson(person);
	}

	public static void createDemand(final double demandUpscale) {
		/*
		 * System.out.
		 * println("Comment this out -- danger to overwrite existing population.");
		 * System.exit(0);
		 * 
		 * final Config config = ConfigUtils.createConfig();
		 * config.network().setInputFile(norrkopingNetwork); final Scenario scenario =
		 * ScenarioUtils.loadScenario(config);
		 * 
		 * final ZonalSystem zonalSystem = new ZonalSystem(norrkopingZoneShapeFile,
		 * StockholmTransformationFactory.WGS84_SWEREF99, "id");
		 * 
		 * final Scanner scanner = new Scanner(System.in);
		 * System.out.println("DB user name: "); final String user = scanner.nextLine();
		 * System.out.println("password: "); final String passwd = scanner.nextLine();
		 * scanner.close(); final DatabaseODMatrix od = new DatabaseODMatrix(user,
		 * passwd, "localhost", 5432); // 5455);
		 * 
		 * final int timeBinCnt = od.getNumberOfTimeBins(); // 24h final double
		 * timeBinSize_s = Units.S_PER_D / timeBinCnt;
		 * 
		 * final int zoneCnt = zonalSystem.getId2zoneView().size(); int
		 * processedOriginZones = 0; long personCnt = 0; for (Zone fromZone :
		 * zonalSystem.getId2zoneView().values()) { System.out.println(((100 *
		 * processedOriginZones++) / zoneCnt) + "% DONE"); for (Zone toZone :
		 * zonalSystem.getId2zoneView().values()) { for (int timeBin = 0; timeBin <
		 * timeBinCnt; timeBin++) { final double demand = demandUpscale
		 * od.getDemandPerHour(fromZone.getId(), toZone.getId(), timeBin); if (demand >
		 * 0) { for (int i = 0; i < demand; i++) { addTripMaker(scenario,
		 * Id.createPersonId(personCnt++), zonalSystem, fromZone, toZone, (Math.random()
		 * + timeBin) * timeBinSize_s); } if (Math.random() < (demand - (int) demand)) {
		 * addTripMaker(scenario, Id.createPersonId(personCnt++), zonalSystem, fromZone,
		 * toZone, (Math.random() + timeBin) * timeBinSize_s); } } } } }
		 * 
		 * final PopulationWriter writer = new
		 * PopulationWriter(scenario.getPopulation());
		 * writer.writeV6(norrkopingPopulationFile);
		 */
	}

	public static void createDemandNorrkoping(final double demandUpscale) {

		final Config config = ConfigUtils.createConfig();
		config.network().setInputFile(norrkopingNetwork);
		final Scenario scenario = ScenarioUtils.loadScenario(config);

		Network network = scenario.getNetwork();
		NetworkFactory netF = network.getFactory();

		final ZonalSystem zonalSystem = new ZonalSystem(norrkopingZoneShapeFile,
				StockholmTransformationFactory.WGS84_SWEREF99, "id");

		final DatabaseODMatrix od = new DatabaseODMatrix(user, passwd, "localhost", 5432); // 5455);

		int persons = od.getOdSize() - 1;
		// int persons = 400;

		Coord toCoord = new Coord();
		Coord fromCoord = new Coord();
		int counter = 1;
		double percent = 5.0;
		int testV = 1;
		double value;

		List<Node> nodesList = new ArrayList<Node>();
		nodesList = NetworkUtils.getNodes(network,
				"373701 373700 313501 653882137 319036 319037 3244867309 3244867314 120524 4525217764");
		Coord fromLinkoping = nodesList.get(0).getCoord();
		Coord toLinkoping = nodesList.get(1).getCoord();

		Coord toKatrineholm = nodesList.get(2).getCoord();
		Coord fromKatrineholm = nodesList.get(3).getCoord();

		Coord fromNykoping = nodesList.get(4).getCoord();
		Coord toNykoping = nodesList.get(5).getCoord();

		Coord toFinspang = nodesList.get(6).getCoord();
		Coord fromFinspang = nodesList.get(7).getCoord();

		Coord fromSoderkoping = nodesList.get(8).getCoord();
		Coord toSoderkoping = nodesList.get(9).getCoord();

		for (int i = 0; i < persons; i++) {
			value = (double) i / 2100;
			if (value > testV) {
				testV = testV + 1;
				System.out.println("DONE WITH " + percent + " %");
				percent = percent + 5;
			}

			ArrayList<String> input = new ArrayList<String>();
			input = od.getDemandFromDatabase(i + 1);

			double flow = demandUpscale * Double.parseDouble(input.get(3));
			int demand = (int) Math.round(flow);

			if (Integer.valueOf(input.get(0)) != Integer.valueOf(input.get(1))) {

				for (int j = 0; j < demand; j++) {
					if (Integer.valueOf(input.get(0)) < 168) {
						Zone fromZone = zonalSystem.getZone(input.get(0));
						fromCoord = ShapeUtils.drawPointFromGeometry(fromZone.getGeometry());
					} else {
						int fromZone = Integer.valueOf(input.get(0));

						if (fromZone == 172 || fromZone == 171) {
							fromCoord = fromKatrineholm;
						} else if (fromZone == 174 || fromZone == 175 || fromZone == 176) {
							fromCoord = fromFinspang;
						} else if (fromZone == 177 || fromZone == 178 || fromZone == 179 || fromZone == 180
								|| fromZone == 181 || fromZone == 182 || fromZone == 183) {
							fromCoord = fromLinkoping;
						} else if (fromZone == 189) {
							// Special zone on island
							fromCoord = new Coord(597442, 6476524);
						} else if (fromZone == 184 || fromZone == 185 || fromZone == 186 || fromZone == 187
								|| fromZone == 188) {
							fromCoord = fromSoderkoping;
						} else if (fromZone == 169 || fromZone == 170 || fromZone == 168 || fromZone == 171) {
							fromCoord = fromNykoping;
						}

					}

					if (Integer.valueOf(input.get(1)) < 168) {
						Zone toZone = zonalSystem.getZone(input.get(1));
						toCoord = ShapeUtils.drawPointFromGeometry(toZone.getGeometry());
					} else {
						int toZone = Integer.valueOf(input.get(1));

						if (toZone == 172 || toZone == 171) {
							toCoord = toKatrineholm;
						} else if (toZone == 174 || toZone == 175 || toZone == 176) {
							toCoord = toFinspang;
						} else if (toZone == 177 || toZone == 178 || toZone == 179 || toZone == 180 || toZone == 181
								|| toZone == 182 || toZone == 183) {
							toCoord = toLinkoping;
						} else if (toZone == 189) {
							// Special zone on island
							toCoord = new Coord(597442, 6476524);
						} else if (toZone == 184 || toZone == 185 || toZone == 186 || toZone == 187 || toZone == 188) {
							toCoord = toSoderkoping;
						} else if (toZone == 169 || toZone == 170 || toZone == 168 || toZone == 171) {
							toCoord = toNykoping;
						}
					}

					double departure = Double.parseDouble(input.get(2));

					addTripMakerPersons(scenario, Id.createPersonId(counter), fromCoord, toCoord,
							(departure + Math.random()) * 3600);
					counter = counter + 1;
				}
			}

			input.clear();

		}

		final PopulationWriter writer = new PopulationWriter(scenario.getPopulation());
		// writer.writeV6(norrkopingPlansNew);
	}

	private static void addTripMakerPersons(Scenario scenario, Id<Person> personId, Coord fromCoord, Coord toCoord,
			double dptTime_s) {

		final Person person = scenario.getPopulation().getFactory().createPerson(personId);
		final Plan plan = scenario.getPopulation().getFactory().createPlan();
		person.addPlan(plan);

		{
			final Activity start = scenario.getPopulation().getFactory().createActivityFromCoord("start", fromCoord);
			start.setEndTime(dptTime_s);
			plan.addActivity(start);
		}
		plan.addLeg(scenario.getPopulation().getFactory().createLeg("car"));
		{

			final Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", toCoord);
			plan.addActivity(end);
		}
		scenario.getPopulation().addPerson(person);
	}

	public static void createDemandTrucksFromOD(Scenario scenario) {

		System.out.println("Truck Method OD OFFILNE.");

		// final Config config = ConfigUtils.loadConfig(configFile);

		// config.network().setInputFile(norrkopingNetwork);

		// final Scenario scenario = ScenarioUtils.loadScenario(config);

		/*
		 * final DatabaseODMatrix od = new DatabaseODMatrix(user, passwd, "localhost",
		 * 5432); // 5455);
		 * 
		 * ///HERE NEEDS TO BE CHANGED od.getSiteCoordinatesFromDatabase(); //CHANGE
		 * HERE HashMap<String, Double> xCoord = od.getSiteCoordinatesX();
		 * HashMap<String, Double> yCoord = od.getSiteCoordinatesY();
		 * 
		 * od.getStoreCoordinates(); HashMap<String, Double> xCoordStore =
		 * od.getStoreCoordinatesX(); HashMap<String, Double> yCoordStore =
		 * od.getStoreCoordinatesY();
		 */

		ReadInputFiles inputFiles = new ReadInputFiles("run");
		HashMap<String, Double> xCoord = inputFiles.getXcoordinateSite();
		HashMap<String, Double> yCoord = inputFiles.getYcoordinateSite();

		HashMap<String, Double> xCoordStore = inputFiles.getXcoordLocalStore();
		HashMap<String, Double> yCoordStore = inputFiles.getYcoordLocalStore();

		odCalculationTrucks odTrucks = new odCalculationTrucks("a", "s", 243);
		odTrucks.createODTrucks();
		ArrayList<Integer> origin = odTrucks.getOriginHGV();
		ArrayList<Integer> destination = odTrucks.getDestinationHGV();
		ArrayList<Integer> departure = odTrucks.getDepartureHGV();

		final int timeBinCnt = 24; // 24h
		final double timeBinSize_s = Units.S_PER_D / timeBinCnt; // 24h = 3600 seconds

		boolean specialCase = false;

		int person = 1;
		int transports = origin.size();

		double truck23 = Math.round(transports * 0.2);
		int count = 0;
		int var = 0;
		String truck;

		Coord fromCoord;
		Coord homeCoord;

		Coord coordConstruction;

		Network network = scenario.getNetwork();

		for (int i = 0; i < origin.size(); i++) {

			if (origin.get(i) == 11) {
				// E4Linkoping
				List<Node> nodesList = new ArrayList<Node>();
				nodesList = NetworkUtils.getNodes(network, "294212 128846");
				fromCoord = nodesList.get(0).getCoord();
				homeCoord = nodesList.get(1).getCoord();

			} else if (origin.get(i) == 12) {
				// E4Stockholm
				List<Node> nodesList = new ArrayList<Node>();
				nodesList = NetworkUtils.getNodes(network, "165030 165039");
				fromCoord = nodesList.get(0).getCoord();
				homeCoord = nodesList.get(1).getCoord();

			} else if (origin.get(i) == 13) {
				// E22Soderkoping
				List<Node> nodesList = new ArrayList<Node>();
				nodesList = NetworkUtils.getNodes(network, "1041738188 1041737204");
				fromCoord = nodesList.get(0).getCoord();
				homeCoord = nodesList.get(1).getCoord();

			} else if (origin.get(i) == 3 && destination.get(i) == 3) {
				System.out.println("special case");
				specialCase = true;
				fromCoord = new Coord(xCoordStore.get(origin.get(i).toString()),
						yCoordStore.get(origin.get(i).toString()));
				homeCoord = new Coord(xCoordStore.get(origin.get(i).toString()),
						yCoordStore.get(origin.get(i).toString()));

			} else {
				// Origin for zones
				fromCoord = new Coord(xCoordStore.get(origin.get(i).toString()),
						yCoordStore.get(origin.get(i).toString()));
				homeCoord = new Coord(xCoordStore.get(origin.get(i).toString()),
						yCoordStore.get(origin.get(i).toString()));
			}

			coordConstruction = new Coord(xCoord.get(destination.get(i).toString()),
					yCoord.get(destination.get(i).toString()));

			if (var == 0) {
				truck = "truck";
				var = 1;
			} else if (var == 1 && count <= truck23) {

				truck = "truck23";
				count = count + 1;
				var = 0;

			} else {
				truck = "truck";
			}

			addTripTrucksOD(scenario, Id.createPersonId("truck" + (person++)), fromCoord, homeCoord, coordConstruction,
					(departure.get(i) + Math.random()) * timeBinSize_s, truck, specialCase);
			specialCase = false;
		}

		// Create new population file by merging norrkoping travellers with trucks and
		// workers in
		// norrkoping
		final PopulationWriter writer = new PopulationWriter(scenario.getPopulation());
		writer.writeV6(populationMerged);

	}

	private static void addTripTrucksOD(Scenario scenario, Id<Person> personId, Coord fromCoord, Coord homeCoord,
			Coord coordConstruction, double dptTime_s, String truck, boolean specialCase) {

		// Exact origin for trucks
		// final Coord fromCoord;
		// final Coord homeCoord;
		int unloadTime = 3600;
		int travelTime = 900;

		final Coord middleCoord;

		// Create person, add to sub-population, create/add plan
		final Person person = scenario.getPopulation().getFactory().createPerson(personId);
		person.getAttributes().putAttribute("subpopulation", "heavyVeh");

		final Plan plan = scenario.getPopulation().getFactory().createPlan();
		person.addPlan(plan);

		Population population = scenario.getPopulation();
		PopulationFactory pop = population.getFactory();
		Network network = scenario.getNetwork();

		// All other agents
		Activity start = scenario.getPopulation().getFactory().createActivityFromCoord("truckStart", fromCoord);
		start.setEndTime(dptTime_s);
		plan.addActivity(start);

		Leg leg;

		if (truck.equals("truck")) {
			leg = pop.createLeg(TransportMode.truck);
		} else {

			leg = pop.createLeg("truck23");
		}

		plan.addLeg(leg);

		if (specialCase == true) {
			List<Node> nodesList = new ArrayList<Node>();
			nodesList = NetworkUtils.getNodes(network, "251899658");
			middleCoord = nodesList.get(0).getCoord();

			Activity middlePoint = scenario.getPopulation().getFactory().createActivityFromCoord("middlePoint",
					middleCoord);
			middlePoint.setLinkId(Id.get("314867", Link.class));
			middlePoint.setEndTime(dptTime_s + 20);
			plan.addActivity(middlePoint);

			leg.setDepartureTime(dptTime_s + 20);
			plan.addLeg(leg);

		}

		Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("construction", coordConstruction);
		end.setStartTime(dptTime_s + travelTime);
		end.setEndTime(dptTime_s + travelTime + unloadTime);
		leg.setTravelTime(travelTime);
		plan.addActivity(end);
		plan.addLeg(leg);
		Activity start1 = scenario.getPopulation().getFactory().createActivityFromCoord("truckHome", homeCoord);
		start1.setStartTime(dptTime_s + travelTime + unloadTime + travelTime);
		leg.setTravelTime(travelTime);
		plan.addActivity(start1);

		scenario.getPopulation().addPerson(person);
	}

	private static void addTripMaker(Scenario scenario, Id<Person> personId, ZonalSystem zonalSystem, Zone fromZone,
			Zone toZone, double dptTime_s) {
		/*
		 * final Person person =
		 * scenario.getPopulation().getFactory().createPerson(personId); final Plan plan
		 * = scenario.getPopulation().getFactory().createPlan(); person.addPlan(plan); {
		 * final Coord fromCoord =
		 * ShapeUtils.drawPointFromGeometry(fromZone.getGeometry()); final Activity
		 * start =
		 * scenario.getPopulation().getFactory().createActivityFromCoord("start",
		 * fromCoord); start.setEndTime(dptTime_s); plan.addActivity(start); }
		 * plan.addLeg(scenario.getPopulation().getFactory().createLeg("car")); {
		 * 
		 * final Coord toCoord = ShapeUtils.drawPointFromGeometry(toZone.getGeometry());
		 * final Activity end =
		 * scenario.getPopulation().getFactory().createActivityFromCoord("end",
		 * toCoord); plan.addActivity(end); }
		 * scenario.getPopulation().addPerson(person);
		 */
	}

	static void runXY2Links(final String configFileName, final String networkFileName, final String popFileName) {

		final Config config = ConfigUtils.loadConfig(configFileName);
		config.network().setInputFile(networkFileName);
		config.plans().setInputFile(popFileName);
		final Scenario scenario = ScenarioUtils.loadScenario(config);

		// new NetworkCleaner().run(scenario.getNetwork());

		final XY2Links xy2links = new XY2Links(scenario);
		for (Person person : scenario.getPopulation().getPersons().values()) {
			xy2links.run(person);
		}
		final PopulationWriter writer = new PopulationWriter(scenario.getPopulation());
		writer.writeV6(popFileName);
	}

	// static void reducePopulation() {
	// PopulationSampler.main(new String[] { norrkopingPopulationFile,
	// norrkoping25PctPopulationFile, "0.25" });
	// }

	static void runSimulation(final String configFileName) {

		final Config config = ConfigUtils.loadConfig(configFileName, new SwissRailRaptorConfigGroup(),
				new SBBTransitConfigGroup(), new GreedoConfigGroup());

		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);

		final Greedo greedo = new Greedo();
		greedo.meet(config);

		final Scenario scenario = ScenarioUtils.loadScenario(config);
		createHugeCapacityPTSystem(scenario);
		new CreatePseudoNetwork(scenario.getTransitSchedule(), scenario.getNetwork(), "tr_").createNetwork();
		greedo.meet(scenario);

		final Controler controler = new Controler(scenario);

		final EventsManager events = controler.getEvents();

		controler.addOverridingQSimModule(new AbstractQSimModule() {
			@Override
			public void configureQSim() {
				final ConfigurableQNetworkFactory factory = new ConfigurableQNetworkFactory(events, scenario);
				factory.setLinkSpeedCalculator(new DefaultLinkSpeedCalculator()); // You would obviously set something
																					// else than the default

				// factory.setTurnAcceptanceLogic(new DefaultTurnAcceptanceLogic()); // You
				// would obviously set something else than the default
				bind(QNetworkFactory.class).toInstance(factory);
				// NOTE: Other than when using a provider, this uses the same factory instance
				// over all iterations, re-configuring
				// it in every iteration via the initializeFactory(...) method. kai, mar'16
			}
		});

		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				this.install(new SBBTransitModule());
				this.install(new SwissRailRaptorModule());
			}

			@Provides
			QSimComponentsConfig provideQSimComponentsConfig() {
				QSimComponentsConfig components = new QSimComponentsConfig();
				new StandardQSimComponentConfigurator(config).configure(components);
				SBBTransitEngineQSimModule.configure(components);
				return components;
			}

		});

		controler.addControlerListener(new StartupListener() {
			@Override
			public void notifyStartup(StartupEvent event) {
				Logger.getLogger(EventsManagerImpl.class).setLevel(Level.OFF);
			}
		});

		greedo.meet(controler);

		controler.run();
	}

	private static void createHugeCapacityPTSystem(final Scenario scenario) {
		Logger.getLogger(NorrkopingProductionRunner.class).warn("Creating huge-capacity PT system.");

		for (VehicleType vehicleType : scenario.getTransitVehicles().getVehicleTypes().values()) {
			final VehicleCapacity capacity = vehicleType.getCapacity();
			capacity.setSeats(100 * 1000);
			capacity.setStandingRoom(100 * 1000);
			vehicleType.setAccessTime(0);
			vehicleType.setEgressTime(0);
			// PCU equivalents -- attempting to cause a failure if used
			vehicleType.setPcuEquivalents(Double.NaN);
		}

		for (TransitLine line : scenario.getTransitSchedule().getTransitLines().values()) {
			for (TransitRoute route : line.getRoutes().values()) {
				for (TransitRouteStop stop : route.getStops()) {
					stop.setAwaitDepartureTime(true);
				}
			}
		}
	}

	private static void createVehicleTypes(String configFileName) {

		Config config = ConfigUtils.loadConfig(configFileName);
		Scenario scenario = ScenarioUtils.loadScenario(config);

		VehicleType car = scenario.getVehicles().getFactory()
				.createVehicleType(Id.create(TransportMode.car, VehicleType.class));
		car.setMaximumVelocity(120 / 3.6);
		car.setPcuEquivalents(1.0);
		scenario.getVehicles().addVehicleType(car);

		VehicleType truck = scenario.getVehicles().getFactory()
				.createVehicleType(Id.create(TransportMode.truck, VehicleType.class));
		truck.setMaximumVelocity(80 / 3.6);
		truck.setLength(12.5);
		// truck.setFlowEfficiencyFactor(1.0);
		truck.setPcuEquivalents(0.25);
		truck.setWidth(2.0);
		truck.setNetworkMode(TransportMode.truck);

		scenario.getVehicles().addVehicleType(truck);

		VehicleType truck23 = scenario.getVehicles().getFactory()
				.createVehicleType(Id.create("truck23", VehicleType.class));
		truck23.setMaximumVelocity(80 / 3.6);
		// truck23.setFlowEfficiencyFactor(1.0);
		truck23.setLength(25.5);
		truck23.setPcuEquivalents(0.5);
		truck23.setWidth(2.0);
		truck23.setNetworkMode("truck23");
		scenario.getVehicles().addVehicleType(truck23);

		VehicleType carW = scenario.getVehicles().getFactory().createVehicleType(Id.create("carW", VehicleType.class));
		carW.setMaximumVelocity(120 / 3.6);
		// carW.setFlowEfficiencyFactor(1.0);
		carW.setLength(7.5);
		carW.setPcuEquivalents(0.1);
		carW.setWidth(1.0);
		carW.setNetworkMode("carW");
		scenario.getVehicles().addVehicleType(carW);

		new MatsimVehicleWriter(scenario.getVehicles()).writeFile(vehiclesFile);

	}

	private static void updateConfiguration(String configFileName, String networkFileName, String popFileName) {

		Config config = ConfigUtils.loadConfig(configFileName);

		config.controler().setLastIteration(10);

		config.network().setInputFile(networkFileName);
		config.plans().setInputFile(popFileName);

		config.qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);

		// config.qsim().setPcuThresholdForFlowCapacityEasing(0.5);
		config.qsim().setFlowCapFactor(0.15);
		config.qsim().setStorageCapFactor(0.3);
		config.vehicles().setVehiclesFile(vehiclesFile);

		List<String> mainModes = Arrays
				.asList(new String[] { TransportMode.car, TransportMode.truck, "truck23", "carW" });
		config.qsim().setMainModes(mainModes);
		config.plansCalcRoute().setNetworkModes(mainModes);
		config.travelTimeCalculator().setAnalyzedModesAsString("car,truck, truck23, carW");
		config.travelTimeCalculator().setSeparateModes(true); // change maybe to true

		PlanCalcScoreConfigGroup.ModeParams truck1 = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.truck);
		truck1.setMonetaryDistanceRate(0); // all to zero
		truck1.setMarginalUtilityOfTraveling(-2.34);
		truck1.setConstant(0.0);
		truck1.setMarginalUtilityOfDistance(0.0);

		config.planCalcScore().addModeParams(truck1);

		PlanCalcScoreConfigGroup.ModeParams truck23 = new PlanCalcScoreConfigGroup.ModeParams("truck23");
		truck23.setMonetaryDistanceRate(0); // all to zero
		truck23.setMarginalUtilityOfTraveling(-2.34);
		truck23.setConstant(0.0);
		truck23.setMarginalUtilityOfDistance(0.0);

		config.planCalcScore().addModeParams(truck23);

		PlanCalcScoreConfigGroup.ModeParams carW = new PlanCalcScoreConfigGroup.ModeParams("carW");
		truck1.setMonetaryDistanceRate(0); // all to zero
		truck1.setMarginalUtilityOfTraveling(-2.34);
		truck1.setConstant(0.0);
		truck1.setMarginalUtilityOfDistance(0.0);

		config.planCalcScore().addModeParams(carW);

		StrategyConfigGroup.StrategySettings keepRoute = new StrategyConfigGroup.StrategySettings();
		keepRoute.setSubpopulation("heavyVeh");
		keepRoute.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.KeepLastSelected);
		keepRoute.setWeight(1);

		config.strategy().addStrategySettings(keepRoute);

		StrategyConfigGroup.StrategySettings workers = new StrategyConfigGroup.StrategySettings();
		workers.setSubpopulation("workers");
		workers.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ReRoute);
		workers.setWeight(0.2);

		config.strategy().addStrategySettings(workers);

		new ConfigWriter(config).write(configFileUpdated);

	}

	public static void main(String[] args) throws Exception {

		System.out.println("START OF MAIN");
		long start = System.currentTimeMillis();

		// Scanner scanner = new Scanner(System.in);
		// System.out.println("DB user name: ");
		// user = scanner.nextLine();
		// System.out.println("password: ");
		// passwd = scanner.nextLine();

		// scanner.close();

		user = "user";
		passwd = "password";

		// Update network with construction sites
		NetworkEditor edit = new NetworkEditor(configFile, user, passwd);
		edit.editNework();

		// Use only once to create general population in Norrkoping.
		// createDemandNorrkoping(1.0);
		// runXY2Links(configFileUpdated,norrkopingNetwork,norrkopingPlansNew);

		// Create agents based on databse demand for consruction workers and transports

		 //createDemandWorkers(configFile, 1.0, 6);
		// runXY2Links(configFile, norrkopingNetwork, populationMerged);
		// nrkpPlans50

		createVehicleTypes(configFile);
		updateConfiguration(configFile, norrkopingNetwork, populationMerged);

		runSimulation(configFileUpdated);

		System.out.println("END OF SIMULATION");

		long end = System.currentTimeMillis();
		// finding the time difference and converting it into seconds
		float sec = (end - start) / 1000F;
		System.out.println("ELAPSED TIME Minutes " + (sec / 60));
		System.out.println("PROGRAM IS FINISHED");

	}

}
