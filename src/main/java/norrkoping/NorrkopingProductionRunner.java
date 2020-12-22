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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;


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
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfig;
import org.matsim.core.mobsim.qsim.components.StandardQSimComponentConfigurator;
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
	static final String norrkopingPlansNew = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\nrkpPlans.xml";
	static final String vehiclesFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\vehicleTypes.xml";
	static final String populationMerged = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\popMerged.xml";
	
	static final String norrkopingTransitScheduleFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\transitSchedule-norrkoping-20190214.xml.gz";
	static final String norrkopingTransitVehicleFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\transitVehicles-norrkoping-20190214.xml.gz";


	// Coordinates of construction sites
	static final Coord Soderporten2 = new Coord(569942.8, 6493546.6);
	static final Coord HotellSvea = new Coord(568874, 6495632);

	static final Coord Spinnhuset = new Coord(569239, 6495635);
	static final Coord InreHamnen = new Coord(569671, 6495637);

	static final Coord Soderporten1 = new Coord(569888.2, 6493503.9);
	static final Coord Tingsratten = new Coord(569211, 6495717);
	
	static final Coord centrum = new Coord(569092.878,6495008.141);
	
	static final Coord fromLinkoping = new Coord(542656.7185038754, 6478707.856454755);
	static final Coord toLinkoping = new Coord(542649.8589400095, 6478720.787579953);
	

	// static final Coord E4Linkoping = new Coord(569671, 6495637);
	// static final Coord E4Stockholm = new Coord(569888.2, 6493503.9);
	// static final Coord E22Soderkoping = new Coord(569211, 6495717);

	static final Coord Renall = new Coord(566701.128,6497985.715);
	static final Coord Optimera = new Coord(568192.68, 6497629.862);
	static final Coord Bejer = new Coord(570675.082,6494539.083);
	

	public static HashMap<String, Double> delay = new HashMap<>();
	public static HashMap<String, Double> delayTrucks = new HashMap<>();
	public static HashMap<String, Double> delayWorkers = new HashMap<>();
	
	public static HashMap<Integer, Double> hourlyDelay = new HashMap<>();
	public static HashMap<Integer, Double> hourlyDelayTrucks = new HashMap<>();
	public static HashMap<Integer, Double> hourlyDelayWorkers = new HashMap<>();
	
	public static HashMap<String, Double> numberOfCars = new HashMap<>();
	public static HashMap<String, Double> numberOfTrucks = new HashMap<>();
	public static HashMap<String, Double> numberOfWorkers = new HashMap<>();
	public static HashMap<String, Double> speedLinks = new HashMap<>();
	public static HashMap<String, String> zoneType = new HashMap<>();

	
	public static ArrayList<String> workerZone = new ArrayList<>();
	public static ArrayList<Double> workerToWork = new ArrayList<>();
	public static ArrayList<Double> workerToHome = new ArrayList<>();
	public static ArrayList<String> workerTransport = new ArrayList<>();
	
	public static ArrayList<Integer> xCoord = new ArrayList<>();
	public static ArrayList<Integer> yCoord = new ArrayList<>();
	public static ArrayList<Integer> idNumber = new ArrayList<>();
	
	public static int totalNumber;
	public static int sites106 = 0;
	public static int sites21 = 0;
	
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
				
		final ZonalSystem zonalSystem = new ZonalSystem(norrkopingZoneShapeFile, StockholmTransformationFactory.WGS84_SWEREF99, "id");
		
		double x1 = centrum.getX();
		double x2 = 0.0;
		double y1 = centrum.getY();
		double y2 = 0.0;
		
		
		for (Zone fromZone : zonalSystem.getId2zoneView().values()) {
			
			final Point point = fromZone.getGeometry().getCentroid();
			
			x2 = point.getX();
			y2 = point.getY();
				
				double distance = Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
				
				if(distance < 2000) {
					zoneType.put(fromZone.getId().toString(), "staden");					
				} else if(distance < 10000) {
					zoneType.put(fromZone.getId().toString(), "utkanten");					
				} else if(fromZone.getId().toString().equals("167")) {
					System.out.println("WORKS ");
					zoneType.put(fromZone.getId().toString(), "outside");
				} else {
					zoneType.put(fromZone.getId().toString(), "landet");
				}
			
		}
		
		//String outPath = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\ProjectData\\zoneTypeRight.csv";
		//ResultWriter writer = new ResultWriter(outPath, zoneType);
		//writer.main();
		
		
		
	}
	
	
	public static void createDemandWorkers(String configFile, double upScale, int nrSites) {

		System.out.println("WORKERS");
		createZoneTypes();
		
		final Config config = ConfigUtils.loadConfig(configFile);
		
		config.network().setInputFile(norrkopingNetwork);
		final Scenario scenario = ScenarioUtils.loadScenario(config);
		final ZonalSystem zonalSystem = new ZonalSystem(norrkopingZoneShapeFile, StockholmTransformationFactory.WGS84_SWEREF99, "id");
		
		final DatabaseODMatrix od = new DatabaseODMatrix(user, passwd, "localhost", 5432); // 5455);
		od.createArrayListsWorkers();
		
		// Save into lists
		ArrayList<Integer> oWorker = od.getoriginWorker();
		ArrayList<Integer> dWorker = od.getDestWorker();
		ArrayList<Integer> flowWorker = od.getFlowWorker();
		
		for(int i = 0; i < nrSites; i++) {
			od.workerInformation();
			workerZone = od.getWorkerZoneType();
			workerTransport = od.getWorkerTransportMode();
			workerToWork = od.getWorkerHomeDeparture();
			workerToHome = od.getWorkerWorkDeparture();
			
		}
		
		
		ArrayList<String> copyWorkerZone = new ArrayList<>(workerZone);
		ArrayList<Double> copyWokerToWork = new ArrayList<>(workerToWork);
		ArrayList<Double> copyWorkerToHome = new ArrayList<>(workerToHome);
		ArrayList<String> copyWorkerTransport = new ArrayList<>(workerTransport);
		
		
		int person = 1;
		int sites = 0;
		
		for(int i = 0; i< oWorker.size(); i++) {

			
			int flow = flowWorker.get(i);
			String type = zoneType.get(oWorker.get(i).toString());			
						
				for (int j = 0; j < flow; j++) {
					
					int index = copyWorkerZone.indexOf(type);
					
					Zone fromZone1 = zonalSystem.getZone(oWorker.get(i).toString());
					
					addTripWorkers(scenario, Id.createPersonId("worker"+(person++)), zonalSystem, fromZone1, dWorker.get(i), 
							(copyWokerToWork.get(index)*3600 + (ThreadLocalRandom.current().nextDouble(-0.08, 0.08))*3600), (copyWorkerToHome.get(index)*3600 + (ThreadLocalRandom.current().nextDouble(-0.16, 0.16)*3600)), 
							copyWorkerTransport.get(index),sites);
					
					
					copyWorkerZone.remove(index);
					copyWokerToWork.remove(index);
					copyWorkerToHome.remove(index);
					copyWorkerTransport.remove(index);
					
					
				}
		}
		
		createDemandTrucks(scenario, upScale);
		
	}
	
	
	private static void addTripWorkers(Scenario scenario, Id<Person> personId, ZonalSystem zonalSystem, Zone fromZone, Integer toZone, 
			double departureHome, double departureWork, String transport, int sites) {
		
		final Coord fromCoord = ShapeUtils.drawPointFromGeometry(fromZone.getGeometry());
		final Person person = scenario.getPopulation().getFactory().createPerson(personId);
		
		if(transport.equals("pt")){
			
		}else {
			person.getAttributes().putAttribute("subpopulation", "workers");
		}
		
		
		final Plan plan = scenario.getPopulation().getFactory().createPlan();
		person.addPlan(plan);
		
		if(fromZone.getId().equals("167")) {
			
			final Activity start = scenario.getPopulation().getFactory().createActivityFromCoord("start", fromLinkoping);
			start.setEndTime(departureHome);
			plan.addActivity(start);
			plan.addLeg(scenario.getPopulation().getFactory().createLeg(transport));
			
		}else {

			final Activity start = scenario.getPopulation().getFactory().createActivityFromCoord("start", fromCoord);
		
			if(transport.equals("pt")){
				start.setEndTime(departureHome-(3600));
				plan.addActivity(start);
				plan.addLeg(scenario.getPopulation().getFactory().createLeg(transport));
			}else {
				start.setEndTime(departureHome);
				plan.addActivity(start);
				plan.addLeg(scenario.getPopulation().getFactory().createLeg(transport));
			}
			
		}
		
		
		if(toZone == 106){
			
			if(sites106 == 0) {
				sites106++;
				final Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", Soderporten1);
				end.setStartTime(departureHome+1800);
				end.setEndTime(departureWork);				
				plan.addActivity(end);
			} else {
				final Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", Soderporten2);
				end.setStartTime(departureHome+1800);
				end.setEndTime(departureWork);
				plan.addActivity(end);
				sites106 = 0;
			}
			
		} else if(toZone == 21) {
			if(sites21 == 0) {
				sites21++;
				final Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", Spinnhuset);
				end.setStartTime(departureHome+1800);
				end.setEndTime(departureWork);				
				plan.addActivity(end);
			} else if(sites21 == 1) {
				sites21++;
				final Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", Tingsratten);
				end.setStartTime(departureHome+1800);
				end.setEndTime(departureWork);
				plan.addActivity(end);
				
			}else {
				final Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", HotellSvea);
				end.setStartTime(departureHome+1800);
				end.setEndTime(departureWork);
				plan.addActivity(end);
				sites21 = 0;
				
			}
			
		} else if(toZone == 22) {
			
			final Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", InreHamnen);
			end.setStartTime(departureHome+1800);
			end.setEndTime(departureWork);
			plan.addActivity(end);
			
		}
		
		
		if(fromZone.getId().equals("167")) {
			
			plan.addLeg(scenario.getPopulation().getFactory().createLeg(transport));
			final Activity start1 = scenario.getPopulation().getFactory().createActivityFromCoord("start", toLinkoping);
			plan.addActivity(start1);
			
		}else {
			
	
				plan.addLeg(scenario.getPopulation().getFactory().createLeg(transport));
				Activity start1 = scenario.getPopulation().getFactory().createActivityFromCoord("start", fromCoord);
				plan.addActivity(start1);


		}
		
		scenario.getPopulation().addPerson(person);
	}
	
	
	
	
	
	
	

	public static void createDemand(final double demandUpscale) {
		/*
		System.out.println("Comment this out -- danger to overwrite existing population.");
		System.exit(0);

		final Config config = ConfigUtils.createConfig();
		config.network().setInputFile(norrkopingNetwork);
		final Scenario scenario = ScenarioUtils.loadScenario(config);

		final ZonalSystem zonalSystem = new ZonalSystem(norrkopingZoneShapeFile,
				StockholmTransformationFactory.WGS84_SWEREF99, "id");

		final Scanner scanner = new Scanner(System.in);
		System.out.println("DB user name: ");
		final String user = scanner.nextLine();
		System.out.println("password: ");
		final String passwd = scanner.nextLine();
		scanner.close();
		final DatabaseODMatrix od = new DatabaseODMatrix(user, passwd, "localhost", 5432); // 5455);

		final int timeBinCnt = od.getNumberOfTimeBins(); // 24h
		final double timeBinSize_s = Units.S_PER_D / timeBinCnt;

		final int zoneCnt = zonalSystem.getId2zoneView().size();
		int processedOriginZones = 0;
		long personCnt = 0;
		for (Zone fromZone : zonalSystem.getId2zoneView().values()) {
			System.out.println(((100 * processedOriginZones++) / zoneCnt) + "% DONE");
			for (Zone toZone : zonalSystem.getId2zoneView().values()) {
				for (int timeBin = 0; timeBin < timeBinCnt; timeBin++) {
					final double demand = demandUpscale
							* od.getDemandPerHour(fromZone.getId(), toZone.getId(), timeBin);
					if (demand > 0) {
						for (int i = 0; i < demand; i++) {
							addTripMaker(scenario, Id.createPersonId(personCnt++), zonalSystem, fromZone, toZone,
									(Math.random() + timeBin) * timeBinSize_s);
						}
						if (Math.random() < (demand - (int) demand)) {
							addTripMaker(scenario, Id.createPersonId(personCnt++), zonalSystem, fromZone, toZone,
									(Math.random() + timeBin) * timeBinSize_s);
						}
					}
				}
			}
		}

		final PopulationWriter writer = new PopulationWriter(scenario.getPopulation());
		writer.writeV6(norrkopingPopulationFile);
		*/
	}
	
	
	
	public static void createDemandNorrkoping(final double demandUpscale) {

		final Config config = ConfigUtils.createConfig();
		config.network().setInputFile(norrkopingNetwork);
		final Scenario scenario = ScenarioUtils.loadScenario(config);
		
		Network network = scenario.getNetwork();
		NetworkFactory netF = network.getFactory();

		
		final ZonalSystem zonalSystem = new ZonalSystem(norrkopingZoneShapeFile, StockholmTransformationFactory.WGS84_SWEREF99, "id");

		final DatabaseODMatrix od = new DatabaseODMatrix(user, passwd, "localhost", 5432); // 5455);
	
		
		int persons = od.getOdSize()-1;
		//int persons = 400;
		
		Coord toCoord = new Coord();
		Coord fromCoord = new Coord();
		int counter = 1;
		double percent= 5.0;
		int testV = 1;
		double value;
		
		List<Node> nodesList = new ArrayList<Node>();
		nodesList = NetworkUtils.getNodes(network, "373701 373700 313501 653882137 319036 319037 3244867309 3244867314 120524 4525217764");
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
		
		
		for(int i = 0; i< persons; i++) {
			value = (double) i/2100;
			if(value > testV ) {
				testV = testV + 1;
				System.out.println("DONE WITH " + percent + " %");
				percent = percent + 5;
			}
			
			
			ArrayList<String> input = new ArrayList<String>();
			input = od.getDemandFromDatabase(i+1);
			
			double flow = demandUpscale*Double.parseDouble(input.get(3));
			int demand = (int) Math.round(flow);
			
			if(Integer.valueOf(input.get(0)) != Integer.valueOf(input.get(1))) {
			
			for(int j = 0; j<demand; j++) {
				if(Integer.valueOf(input.get(0)) < 168) {
					Zone fromZone = zonalSystem.getZone(input.get(0));
					fromCoord = ShapeUtils.drawPointFromGeometry(fromZone.getGeometry());
				}else {
					int fromZone = Integer.valueOf(input.get(0));
					
					
					if(fromZone==172 || fromZone==171) {
						fromCoord = fromKatrineholm;
					} else if(fromZone==174 || fromZone==175 || fromZone==176) {
						fromCoord = fromFinspang;
					} else if(fromZone==177 || fromZone==178 || fromZone==179 || fromZone == 180 || fromZone == 181 || fromZone == 182 || fromZone == 183) {
						fromCoord = fromLinkoping;
					} else if(fromZone==189) {
						//Special zone on island
						fromCoord = new Coord(597442, 6476524);
					} else if(fromZone==184 || fromZone==185 || fromZone==186 || fromZone==187 || fromZone==188) {
						fromCoord = fromSoderkoping;
					} else if(fromZone==169 || fromZone==170 || fromZone==168 || fromZone==171) {
						fromCoord = fromNykoping;
					}
						
				}
				
				if(Integer.valueOf(input.get(1)) < 168) {
					Zone toZone = zonalSystem.getZone(input.get(1));
					toCoord = ShapeUtils.drawPointFromGeometry(toZone.getGeometry());
				}else {
					int toZone = Integer.valueOf(input.get(1));
					
					if(toZone==172 || toZone==171) {
						toCoord = toKatrineholm;
					} else if(toZone==174 || toZone==175 || toZone==176) {
						toCoord = toFinspang;
					} else if(toZone==177 || toZone==178 || toZone==179 || toZone == 180 || toZone == 181 || toZone == 182 || toZone == 183) {
						toCoord = toLinkoping;
					} else if(toZone==189) {
						//Special zone on island
						toCoord = new Coord(597442, 6476524);
					} else if(toZone==184 || toZone==185 || toZone==186 || toZone==187 || toZone==188) {
						toCoord = toSoderkoping;
					} else if(toZone==169 || toZone==170 || toZone==168 || toZone==171) {
						toCoord = toNykoping;
					}
				}
				
				double departure = Double.parseDouble(input.get(2));
				
				
				
				addTripMakerPersons(scenario, Id.createPersonId(counter), fromCoord, toCoord, (departure + Math.random())*3600);
				counter = counter +1;
			}
			} 
			
			input.clear();
			
		}


		final PopulationWriter writer = new PopulationWriter(scenario.getPopulation());
		writer.writeV6(norrkopingPlansNew);
	}
	
	private static void addTripMakerPersons(Scenario scenario, Id<Person> personId, Coord fromCoord, Coord toCoord, double dptTime_s) {
		
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
	
	
	

	public static void createDemandTrucks(Scenario scenario, double upScale) {

		System.out.println("Truck Method.");

		final ZonalSystem zonalSystem = new ZonalSystem(norrkopingZoneShapeFile,
				StockholmTransformationFactory.WGS84_SWEREF99, "id");


		final DatabaseODMatrix od = new DatabaseODMatrix(user, passwd, "localhost", 5432); // 5455);

		// Create information form database to arrays
		od.createArrayListsFromDatabase();

		// Save into lists
		ArrayList<String> oList = od.getOriginList();
		ArrayList<String> dList = od.getDestList();
		ArrayList<Integer> siteId = od.getSiteIDList();
		ArrayList<Integer> hourList = od.getHourList();
		ArrayList<Double> demandList = od.getDemandList();

		// number of trucks to add in the transport system
		int rowsInOD = od.getNumberOfIDrows();
		int person = 1;

		final int timeBinCnt = 24; // 24h
		final double timeBinSize_s = Units.S_PER_D / timeBinCnt; // 24h = 3600 seconds

		for (int i = 0; i < rowsInOD; i++) {

			int counter = 0;
			
			double demand = upScale*demandList.get(i);
			
			for (int j = 0; j < demand; j++) {
				counter = counter + 1;
				
				addTripMakerTrucks(scenario, Id.createPersonId("truck"+(person++)), zonalSystem,
						zonalSystem.getZone(oList.get(i)), dList.get(i),
						(hourList.get(i)+Math.random())*timeBinSize_s, siteId.get(i));
			}
		}

		// Create new population file by merging norrkoping travellers with trucks and workers in
		// norrkoping
		final PopulationWriter writer = new PopulationWriter(scenario.getPopulation());
		writer.writeV6(populationMerged);

	}

	private static void addTripMakerTrucks(Scenario scenario, Id<Person> personId, ZonalSystem zonalSystem,
			Zone fromZone, String toZone, double dptTime_s, int siteID) {

		// Exact origin for trucks
		final Coord fromCoord;
		final Coord homeCoord;
		final Coord middleCoord;

		// Create person, add to sub-population, create/add plan
		final Person person = scenario.getPopulation().getFactory().createPerson(personId);
		person.getAttributes().putAttribute("subpopulation", "heavyVeh");

		final Plan plan = scenario.getPopulation().getFactory().createPlan();
		person.addPlan(plan);

		Population population = scenario.getPopulation();
		PopulationFactory pop = population.getFactory();
		Network network = scenario.getNetwork();

		if (fromZone.getId().equals("140")) {
			// Origin for zones
			List<Node> nodesList = new ArrayList<Node>();
			nodesList = NetworkUtils.getNodes(network, "294212 128846");
			fromCoord = nodesList.get(0).getCoord();
			homeCoord = nodesList.get(1).getCoord();
		} else if (fromZone.getId().equals("57")) {
			// Origin for zones
			List<Node> nodesList = new ArrayList<Node>();
			nodesList = NetworkUtils.getNodes(network, "165030 165039");
			fromCoord = nodesList.get(0).getCoord();
			homeCoord = nodesList.get(1).getCoord();
			
		} else if (fromZone.getId().equals("120")) {
			// Origin for zones
			List<Node> nodesList = new ArrayList<Node>();
			nodesList = NetworkUtils.getNodes(network, "1041738188 1041737204");
			fromCoord = nodesList.get(0).getCoord();
			homeCoord = nodesList.get(1).getCoord();
			
		} else if (fromZone.getId().equals("81")) {
			fromCoord = Bejer;			
			homeCoord = Bejer;

		} else if (fromZone.getId().equals("19")) {
			fromCoord = Renall;
			homeCoord = Renall;

		} else if (fromZone.getId().equals("20")) {
			fromCoord = Optimera;
			homeCoord = Optimera;

		} else {
			System.out.println("WRONG PLACE");
			fromCoord = ShapeUtils.drawPointFromGeometry(fromZone.getGeometry());
			homeCoord = ShapeUtils.drawPointFromGeometry(fromZone.getGeometry());
		}

			// All other agents
			Activity start = scenario.getPopulation().getFactory().createActivityFromCoord("truckStart", fromCoord);
			if(fromZone.getId().equals("81") && toZone.equals("106")) {
				start.setLinkId(Id.get("564468", Link.class));
			}
			start.setEndTime(dptTime_s);
			plan.addActivity(start);
			
			Leg leg = pop.createLeg(TransportMode.truck);

			plan.addLeg(leg);
			
			if (fromZone.getId().equals("81") && toZone.equals("21")) {
				List<Node> nodesList = new ArrayList<Node>();
				nodesList = NetworkUtils.getNodes(network, "251899658");
				middleCoord = nodesList.get(0).getCoord();
				
				Activity middlePoint = scenario.getPopulation().getFactory().createActivityFromCoord("middlePoint", middleCoord);
				middlePoint.setLinkId(Id.get("314867", Link.class));					
				middlePoint.setEndTime(dptTime_s + 20);
				plan.addActivity(middlePoint);
				
				Leg leg1 = pop.createLeg(TransportMode.truck);
				leg1.setDepartureTime(dptTime_s+20);
				plan.addLeg(leg1);			
				
			}
			
		
		
		if (toZone.equals("22")) {
			Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("construction", InreHamnen);
			end.setLinkId(Id.get("1", Link.class));
			end.setStartTime(dptTime_s + 900);
			end.setEndTime(dptTime_s + 900 + 3600);
			plan.addActivity(end);
			plan.addLeg(leg);
			Activity start1 = scenario.getPopulation().getFactory().createActivityFromCoord("truckHome", homeCoord);
			start1.setStartTime(dptTime_s + 900 + 3600 + 600);
			leg.setTravelTime(900);
			plan.addActivity(start1);

		} else if (toZone.equals("106")) {
			if (siteID == 2) {
				
				Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("construction", Soderporten1);
				end.setStartTime(dptTime_s + 900);
				end.setEndTime(dptTime_s + 900 + 3600);
				plan.addActivity(end);
				plan.addLeg(leg);
				Activity start1 = scenario.getPopulation().getFactory().createActivityFromCoord("truckHome", homeCoord);
				start1.setStartTime(dptTime_s + 900 + 3600 + 600);
				leg.setTravelTime(900);
				plan.addActivity(start1);
				

			} else {
				Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("construction", Soderporten2);
				end.setStartTime(dptTime_s + 900);
				end.setEndTime(dptTime_s + 900 + 3600);
				plan.addActivity(end);
				leg.setTravelTime(900);
				plan.addLeg(leg);
				Activity start1 = scenario.getPopulation().getFactory().createActivityFromCoord("truckHome", homeCoord);
				start1.setStartTime(dptTime_s + 900 + 3600 + 600);
				plan.addActivity(start1);
			}

		} else if (toZone.equals("21")) {
			if (siteID == 4) {
				Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("construction", Spinnhuset);
				end.setStartTime(dptTime_s + 900);
				end.setEndTime(dptTime_s + 900 + 3600);
				plan.addActivity(end);
				leg.setTravelTime(900);
				plan.addLeg(leg);
				Activity start1 = scenario.getPopulation().getFactory().createActivityFromCoord("truckHome", homeCoord);
				start1.setStartTime(dptTime_s + 900 + 3600 + 600);
				plan.addActivity(start1);

			} else if (siteID == 7) {
				Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("construction", Tingsratten);
				end.setStartTime(dptTime_s + 900);
				end.setEndTime(dptTime_s + 900 + 3600);
				plan.addActivity(end);
				leg.setTravelTime(900);
				plan.addLeg(leg);
				Activity start1 = scenario.getPopulation().getFactory().createActivityFromCoord("truckHome", homeCoord);
				start1.setStartTime(dptTime_s + 900 + 3600 + 600);
				plan.addActivity(start1);

			} else {
				Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("construction", HotellSvea);
				end.setStartTime(dptTime_s + 900);
				end.setEndTime(dptTime_s + 900 + 3600);
				plan.addActivity(end);
				leg.setTravelTime(900);
				plan.addLeg(leg);
				Activity start1 = scenario.getPopulation().getFactory().createActivityFromCoord("truckHome", homeCoord);
				start1.setStartTime(dptTime_s + 900 + 3600 + 600);
			
				plan.addActivity(start1);
			}

		}

		scenario.getPopulation().addPerson(person);
	}

	private static void addTripMaker(Scenario scenario, Id<Person> personId, ZonalSystem zonalSystem, Zone fromZone,
			Zone toZone, double dptTime_s) {
		/*
		final Person person = scenario.getPopulation().getFactory().createPerson(personId);
		final Plan plan = scenario.getPopulation().getFactory().createPlan();
		person.addPlan(plan);
		{
			final Coord fromCoord = ShapeUtils.drawPointFromGeometry(fromZone.getGeometry());
			final Activity start = scenario.getPopulation().getFactory().createActivityFromCoord("start", fromCoord);
			start.setEndTime(dptTime_s);
			plan.addActivity(start);
		}
		plan.addLeg(scenario.getPopulation().getFactory().createLeg("car"));
		{

			final Coord toCoord = ShapeUtils.drawPointFromGeometry(toZone.getGeometry());
			final Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", toCoord);
			plan.addActivity(end);
		}
		scenario.getPopulation().addPerson(person);
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

		VehicleType car = scenario.getVehicles().getFactory().createVehicleType(Id.create(TransportMode.car, VehicleType.class));
		car.setMaximumVelocity(90 / 3.6);
		scenario.getVehicles().addVehicleType(car);

		VehicleType truck = scenario.getVehicles().getFactory().createVehicleType(Id.create(TransportMode.truck, VehicleType.class));
		truck.setMaximumVelocity(50 / 3.6);
		truck.setLength(10);
		truck.setPcuEquivalents(3);
		truck.setWidth(2.5);
		truck.setNetworkMode(TransportMode.truck);
		scenario.getVehicles().addVehicleType(truck);

		new MatsimVehicleWriter(scenario.getVehicles()).writeFile(vehiclesFile);

	}

	private static void updateConfiguration(String configFileName, String networkFileName, String popFileName) {

		Config config = ConfigUtils.loadConfig(configFileName);
		
		config.controler().setLastIteration(2);
		
		config.network().setInputFile(networkFileName);
		config.plans().setInputFile(popFileName);

		config.qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);
		//config.qsim().setPcuThresholdForFlowCapacityEasing(0.5);
		config.qsim().setFlowCapFactor(0.2);
		config.qsim().setStorageCapFactor(0.4);
		config.vehicles().setVehiclesFile(vehiclesFile);

		List<String> mainModes = Arrays.asList(new String[] { TransportMode.car, TransportMode.truck });
		config.qsim().setMainModes(mainModes);
		config.plansCalcRoute().setNetworkModes(mainModes);
		config.travelTimeCalculator().setAnalyzedModesAsString("car,truck");
		config.travelTimeCalculator().setSeparateModes(true); // change maybe to true

		PlanCalcScoreConfigGroup.ModeParams truck1 = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.truck);
		truck1.setMonetaryDistanceRate(0); // all to zero
		truck1.setMarginalUtilityOfTraveling(-2.34);
		truck1.setConstant(0.0);
		truck1.setMarginalUtilityOfDistance(0.0);
		
		config.planCalcScore().addModeParams(truck1);
				
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

	private static void resultCreator(String configFileName, String eventFile) {

		Config config = ConfigUtils.loadConfig(configFileName);

		Scenario scenario = ScenarioUtils.loadScenario(config);

		Network network = scenario.getNetwork();

		HashMap<Integer, Double> hourCars = new HashMap<>();
		for(int i = 0; i<24; i++) {
			hourCars.put(i, 0.0);
		}
		
		
		for (Link l : network.getLinks().values()) {

			delay.put(l.getId().toString(), 0.0);
			delayTrucks.put(l.getId().toString(), 0.0);
			delayWorkers.put(l.getId().toString(), 0.0);
			numberOfCars.put(l.getId().toString(), 0.0);
			numberOfTrucks.put(l.getId().toString(), 0.0);
			numberOfWorkers.put(l.getId().toString(), 0.0);
			speedLinks.put(l.getId().toString(), 0.0);

		}

		
		EventsManager events = EventsUtils.createEventsManager();

		CongestionHandler congestion = new CongestionHandler(network, delay, numberOfCars, numberOfTrucks, numberOfWorkers, speedLinks, delayTrucks, delayWorkers);
		events.addHandler(congestion);

		new MatsimEventsReader(events).readFile(eventFile);

		delay = congestion.getDelayTable();
		numberOfCars = congestion.getCarsOnLinks();
		numberOfTrucks = congestion.getTrucksOnLinks();
		numberOfWorkers = congestion.getWorkersksOnLinks();
		speedLinks = congestion.getLinkSpeed();
		hourlyDelay = congestion.getHourDelay();
		delayTrucks = congestion.getDelayTrucks();
		delayWorkers = congestion.getDelayWorkers();
		hourlyDelayTrucks = congestion.getHourTruckDelay();
		hourlyDelayWorkers = congestion.getHourWorkerskDelay();
		
		hourCars = congestion.getHourCars();
		
		// NetworkToShape dataToSHP = new NetworkToShape(delay, numberOfCars);
		// dataToSHP.doEverything();

		for (HashMap.Entry<Integer, Double> entry : hourCars.entrySet()) {
		  System.out.println(entry.getValue()*5);
		    
		}
		
	}
	
	
	private static void doCalcualtions(String event1, String event2) {
		
		resultCreator(configFileUpdated, event1);
		 
		 HashMap<String, Double> copyDelay1 = new HashMap<String,Double>(delay);
		 HashMap<String, Double> copyDelayTrucks1 = new HashMap<String,Double>(delayTrucks);
		 HashMap<String, Double> copyDelayWorkers1 = new HashMap<String,Double>(delayWorkers);
		 HashMap<String, Double> copyCars1 = new HashMap<String,Double>(numberOfCars);
		 HashMap<String, Double> copyTrucks1 = new HashMap<String,Double>(numberOfTrucks);
		 HashMap<String, Double> copyWorkers1 = new HashMap<String,Double>(numberOfWorkers);
		 
		 HashMap<String, Double> copySpeedLinks1 = new HashMap<String,Double>(speedLinks);
		 
		 HashMap<Integer, Double> hourlyDelay1 = new HashMap<Integer,Double>(hourlyDelay);
		 HashMap<Integer, Double> hourlyDelayTrucks1 = new HashMap<Integer,Double>(hourlyDelayTrucks);
		 HashMap<Integer, Double> hourlyDelayWorkers1 = new HashMap<Integer,Double>(hourlyDelayWorkers);
		 				
		 resultCreator(configFileUpdated, event2);
		 
		 HashMap<String, Double> copyDelay2 = new HashMap<String,Double>(delay);
		 HashMap<String, Double> copyDelayTrucks2 = new HashMap<String,Double>(delayTrucks);
		 HashMap<String, Double> copyDelayWorkers2 = new HashMap<String,Double>(delayWorkers);
		 HashMap<String, Double> copyCars2 = new HashMap<String,Double>(numberOfCars);
		 HashMap<String, Double> copyTrucks2 = new HashMap<String,Double>(numberOfTrucks);
		 HashMap<String, Double> copyWorkers2 = new HashMap<String,Double>(numberOfWorkers);
		 HashMap<String, Double> copySpeedLinks2 = new HashMap<String,Double>(speedLinks);
		 
		 HashMap<Integer, Double> hourlyDelay2 = new HashMap<Integer,Double>(hourlyDelay);
		 HashMap<Integer, Double> hourlyDelayTrucks2 = new HashMap<Integer,Double>(hourlyDelayTrucks);
		 HashMap<Integer, Double> hourlyDelayWorkers2 = new HashMap<Integer,Double>(hourlyDelayWorkers);
		 
		 HashMap<String, Double> diffDelay = new HashMap<String,Double>(delay);
		 HashMap<String, Double> diffDelayTrucks = new HashMap<String,Double>(delay);
		 HashMap<String, Double> diffDelayWorkers = new HashMap<String,Double>(delay);
		 HashMap<String, Double> diffCars = new HashMap<String,Double>(numberOfCars);
		 HashMap<String, Double> diffTrucks = new HashMap<String,Double>(numberOfTrucks);
		 HashMap<String, Double> diffLinksSpeed = new HashMap<String,Double>(speedLinks);
		 HashMap<Integer, Double> diffHours = new HashMap<Integer,Double>(hourlyDelay);
		 
		 
		 double totalCarsDelay1 = 0.0;
		 double totalCarsDelay2 = 0.0;
		 
		 double totalTrucksDelay1 = 0.0;
		 double totalTrucksDelay2 = 0.0;
		 
		 double totalWorkersDelay1 = 0.0;
		 double totalWorkersDelay2 = 0.0;
		 
		 double totalCars = 0.0;
		 double totalTrucks = 0.0;
		 
		 HashMap<String, Double> avgDelay1 = new HashMap<String,Double>(delay);
		 HashMap<String, Double> avgDelay2 = new HashMap<String,Double>(delay);
		 
		 HashMap<String, Double> avgSpeed1 = new HashMap<String,Double>(speedLinks);
		 HashMap<String, Double> avgSpeed2 = new HashMap<String,Double>(speedLinks);
		 
		 HashMap<String, Double> diffAvgDelay = new HashMap<String,Double>(delay);
		 
		 HashMap<String, Double> copyWorkers = new HashMap<String,Double>(numberOfWorkers);
		 
		 HashMap<String, Double> constructionTransport = new HashMap<String,Double>(numberOfWorkers);
		 double total = 0.0;
		 System.out.println("");
		 System.out.println("HOURLY CARS VOLUME WITHOUT CONSTRUCTION");
			for (int i = 0; i < 24; i++) {
				
				System.out.println("HOURLY CARS BEFORE " + i + " DELAY " + hourlyDelay1.get(i));
				total += hourlyDelay1.get(i);
			}
			System.out.println("total " + total);
			System.out.println("HOURLY CARS VOLUME AFTER CONSTRUCTION");
			for (int i = 0; i < 24; i++) {
				
				System.out.println("HOURLY CARS AFTER " + i + " DELAY " + hourlyDelay2.get(i));
			}
			
			System.out.println("DIFFENCE CARS AFTER - BEFORE ");
			for (int i = 0; i < 24; i++) {
				diffHours.put(i, hourlyDelay2.get(i)-hourlyDelay1.get(i));
				System.out.println("DIFF HOURLY " + i + " DELAY " + diffHours.get(i));
			}
			
			System.out.println("");
			System.out.println("HOURLY WITHOUR CONSTRUCTION TRUCKS");
			for (int i = 0; i < 24; i++) {
				
				System.out.println("HOURLY  Trucks Before " + i + " DELAY " + hourlyDelayTrucks1.get(i));
			}
			System.out.println("HOURLY AFTER CONSTRUCTION TRUCKS");
			for (int i = 0; i < 24; i++) {
				
				System.out.println("DIFF HOURLY Trucks AFTER " + i + " DELAY " + hourlyDelayTrucks2.get(i));
			}
		 
			System.out.println("DIFFERENCE HOURLY TRUCKS");
			for (int i = 0; i < 24; i++) {
				System.out.println("DIFF HOURLY TRUCKS " + i + " DELAY " + (hourlyDelayTrucks2.get(i)-hourlyDelayTrucks1.get(i)));
			}
		 
	
			System.out.println("");
			System.out.println("HOURLY BEFORE WORKERS");
			for (int i = 0; i < 24; i++) {
				
				System.out.println("HOURLY  WORKERS WORKERS " + i + " DELAY " + hourlyDelayWorkers1.get(i));
			}
			System.out.println("DIFF HOURLY AFTER WORKERS ");
			for (int i = 0; i < 24; i++) {
				
				System.out.println("HOURLY  WORKERS AFTER " + i + " DELAY " + hourlyDelayWorkers2.get(i));
			}
		 
			System.out.println("DIFF HOURLY DIFFERENCE WORKERS");
			for (int i = 0; i < 24; i++) {
				System.out.println("DIFF HOURLY WORKERS" + i + " DELAY " + (hourlyDelayWorkers2.get(i)-hourlyDelayWorkers1.get(i)));
			}
		 
			int counter = 0;
		 
		 for (HashMap.Entry<String, Double> row : diffDelay.entrySet()) {
			 
			 if(copyCars1.get(row.getKey())==0.0){
				 avgSpeed1.put(row.getKey(), 0.0);
				 avgDelay1.put(row.getKey(), 0.0);
			 } else {
	
				 avgDelay1.put(row.getKey(), copyDelay1.get(row.getKey())/copyCars1.get(row.getKey()));
				 avgSpeed1.put(row.getKey(), 3.6*copySpeedLinks1.get(row.getKey())/copyCars1.get(row.getKey()));
			 }
			 
			 if(copyCars2.get(row.getKey())==0.0) {
				 avgSpeed2.put(row.getKey(), 0.0);
				 avgDelay2.put(row.getKey(), 0.0);
			 } else {
				 
				 avgDelay2.put(row.getKey(), copyDelay2.get(row.getKey())/copyCars2.get(row.getKey()));			 
				 avgSpeed2.put(row.getKey(), 3.6*copySpeedLinks2.get(row.getKey())/copyCars2.get(row.getKey()));
				 
			 }
			 

		 diffDelay.put(row.getKey(), (copyDelay2.get(row.getKey())-copyDelay1.get(row.getKey()))/60);
		 diffDelayTrucks.put(row.getKey(), (copyDelayTrucks2.get(row.getKey())-copyDelayTrucks1.get(row.getKey()))/60);
		 diffDelayWorkers.put(row.getKey(), (copyDelayWorkers2.get(row.getKey())-copyDelayWorkers1.get(row.getKey()))/60);
		 
		 diffCars.put(row.getKey(), (copyCars2.get(row.getKey())-copyCars1.get(row.getKey())));
		 diffTrucks.put(row.getKey(), (copyTrucks2.get(row.getKey())-copyTrucks1.get(row.getKey())));
		 
		 diffLinksSpeed.put(row.getKey(), (avgSpeed2.get(row.getKey())-avgSpeed1.get(row.getKey())));
		 diffAvgDelay.put(row.getKey(), (avgDelay2.get(row.getKey())-avgDelay1.get(row.getKey())));
		 
		 totalCarsDelay1 += copyDelay1.get(row.getKey());
		 totalCarsDelay2 += copyDelay2.get(row.getKey());
		 
		 totalTrucksDelay1 += copyDelayTrucks1.get(row.getKey());
		 totalTrucksDelay2 += copyDelayTrucks2.get(row.getKey());
		 
		 totalWorkersDelay1 += copyDelayWorkers1.get(row.getKey());
		 totalWorkersDelay2 += copyDelayWorkers2.get(row.getKey());
		 
		 
		 }		
		
		 totalCarsDelay1 = totalCarsDelay1/3600;
		 totalCarsDelay2 = totalCarsDelay2/3600;
		
		 
		 System.out.println("DELAY CARS BEFORE HOURS " + totalCarsDelay1);
		 System.out.println("DELAY CARS AFTER HOURS " + totalCarsDelay2);
		 
		 System.out.println("DELAY Trucks BEFORE HOURS " + totalTrucksDelay1/3600);
		 System.out.println("DELAY Trucks AFTER HOURS " + totalTrucksDelay2/3600);
		 
		 System.out.println("DELAY Workers BEFORE HOURS " + totalWorkersDelay1/3600);
		 System.out.println("DELAY Workers AFTER HOURS " + totalWorkersDelay2/3600);
		 
		 
		NetworkToShape dataToSHP = new NetworkToShape(diffDelay, diffCars, diffTrucks, copyWorkers, diffAvgDelay, diffLinksSpeed); 
		dataToSHP.doEverything();

		
	}
	
	

	public static void main(String[] args) {

		System.out.println("START OF MAIN");
		Scanner scanner = new Scanner(System.in);
		System.out.println("DB user name: ");
		user = scanner.nextLine();
		System.out.println("password: ");
		passwd = scanner.nextLine();
		scanner.close();
		
		//Update network with construction sites
		NetworkEditor edit = new NetworkEditor(configFile, user, passwd);
		edit.editNework();
		
		//Use only once to create general population in Norrkoping. 
		//createDemandNorrkoping(1.0);
		//runXY2Links(configFileUpdated,norrkopingNetwork,norrkopingPlansNew);
		
		//Create agents based on databse demand for consruction workers and transports
		createDemandWorkers(configFile, 1.0, 6);
		
		runXY2Links(configFile,norrkopingNetwork,populationMerged);
		
		createVehicleTypes(configFile);
		updateConfiguration(configFile,norrkopingNetwork, populationMerged);
		
		runSimulation(configFileUpdated);
		
		
		
		//Compare two event files
		/*
		String eventFile1 = "C:\\Users\\TOPO-O\\Desktop\\outputOD\\OD_fixed\\ITERS\\it.5\\5.events.xml.gz";
		String eventFile2 ="C:\\Users\\TOPO-O\\Desktop\\outputOD\\OD_trucksWorkers\\ITERS\\it.5\\5.events.xml.gz";
		//String eventFile2 ="C:\\Users\\TOPO-O\\Desktop\\outputOD\\OD_trucks2000\\ITERS\\it.5\\5.events.xml.gz";
		doCalcualtions(eventFile1, eventFile2);
		*/

		

		System.out.println("END OF SIMULATION");

		
		 
		

		System.out.println("PROGRAM IS FINISHED");

	}

}
