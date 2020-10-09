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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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

	//static final String swedenNetworkFile = "/Users/GunnarF/OneDrive - VTI/My Data/sweden/sweden-latest.xml.gz";
	//static final String swedenScheduleFile = "/Users/GunnarF/OneDrive - VTI/My Data/sweden/transitSchedule-sweden-20190214.xml.gz";
	//static final String swedenTransitVehicleFile = "/Users/GunnarF/OneDrive - VTI/My Data/sweden/transitVehicles-sweden-20190214.xml.gz";
	
	//static final String transitVehicleTypeDefinitionsFileName = "C:\\Users\\GunnarF\\OneDrive - VTI\\My Data\\wum\\data\\input\\transitVehicles_only-types.xml";

	static final String norrkopingZoneShapeFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\OD\\od_zone_norrk.shp";
	
	static final String configFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\norrkoping-config.xml";
	static final String configFileUpdated = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\ConfigUpdated.xml";

	static final String norrkopingNetworkFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\networkUpdated.xml";
	
	static final String norrkopingNetwork = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\networkTest.xml";
	static final String norrkopingTransitScheduleFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\transitSchedule-norrkoping-20190214.xml.gz";
	static final String norrkopingTransitVehicleFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\transitVehicles-norrkoping-20190214.xml.gz";
	
	static final String norrkopingPopulationFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\norrkoping-plansSmall.xml";
	//static final String norrkoping1PctPopulationFile = "/Users/GunnarF/OneDrive - VTI/My Data/norrkoping/norrkoping-plans.1pct.xml.gz";
	//static final String norrkoping25PctPopulationFile = "/Users/GunnarF/OneDrive - VTI/My Data/norrkoping/norrkoping-plans.25pct.xml.gz";
	
	//Coordinates of construction sites
	static final Coord Soderporten2 = new Coord(569942.8, 6493546.6);
	static final Coord HotellSvea = new Coord(568874, 6495632);
	
	static final Coord Spinnhuset = new Coord(569239, 6495635);
	static final Coord InreHamnen = new Coord(569671, 6495637);
	
	static final Coord Soderporten1 = new Coord(569888.2, 6493503.9);
	static final Coord Tingsratten = new Coord(569211, 6495717);
	
	static final String vehiclesFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\outputTest\\vehicleTypes.xml";
	
	static final String populationMerged = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\outputTest\\popMerged.xml";
	static final String populationFile = "C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\outputTest\\popMergedWithLinks.xml";

	
	/*static void cutFromSwedenCarOnly(final double xMin, final double xMax, final double yMin, final double yMax) {

		final Config config = ConfigUtils.createConfig();
		config.network().setInputFile(swedenNetworkFile);
		final Scenario scenario = ScenarioUtils.loadScenario(config);
		System.out.println("raw data: " + scenario.getNetwork().getNodes().size());

		final NetworkFilterManager filters = new NetworkFilterManager(scenario.getNetwork());
		final NetworkNodeFilter nodeFilter = new NetworkNodeFilter() {
			@Override
			public boolean judgeNode(Node n) {
				final double x = n.getCoord().getX();
				final double y = n.getCoord().getY();
				return ((xMin <= x) && (x <= xMax) && (yMin <= y) && (y <= yMax));
			}
		};
		filters.addNodeFilter(nodeFilter);
		filters.addLinkFilter(new NetworkLinkFilter() {
			@Override
			public boolean judgeLink(Link l) {
				return (nodeFilter.judgeNode(l.getFromNode()) && nodeFilter.judgeNode(l.getToNode()));
			}
		});
		final Network net = filters.applyFilters();
		System.out.println("after filtering: " + net.getNodes().size());

		new NetworkCleaner().run(net);
		System.out.println("after cleaning: " + net.getNodes().size());

		new NetworkWriter(net).write(norrkopingNetworkFile);
	}

	static void cutFromSwedenPTOnly() {

		final ZonalSystem zonalSystem = new ZonalSystem(norrkopingZoneShapeFile,
				StockholmTransformationFactory.WGS84_SWEREF99, "id");

		final CropTransitSystem cropTransit = new CropTransitSystem(zonalSystem, swedenScheduleFile,
				swedenTransitVehicleFile, StockholmTransformationFactory.getCoordinateTransformation(
						StockholmTransformationFactory.WGS84_SWEREF99, StockholmTransformationFactory.WGS84_SWEREF99));
		cropTransit.run(norrkopingTransitScheduleFile, norrkopingTransitVehicleFile);

		final DetailPTVehicles detailPT = new DetailPTVehicles(transitVehicleTypeDefinitionsFileName,
				norrkopingTransitScheduleFile, swedenTransitVehicleFile);
		detailPT.run(norrkopingTransitVehicleFile);
	}*/

	public static void createDemand(final double demandUpscale) {

		System.out.println("Comment this out -- danger to overwrite existing population.");
		System.exit(0);

		final Config config = ConfigUtils.createConfig();
		config.network().setInputFile(norrkopingNetworkFile);
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

		final int timeBinCnt = od.getNumberOfTimeBins(); //24h
		final double timeBinSize_s = Units.S_PER_D / timeBinCnt;

		final int zoneCnt = zonalSystem.getId2zoneView().size();
		int processedOriginZones = 0;
		long personCnt = 0;
		for (Zone fromZone : zonalSystem.getId2zoneView().values()) {
			System.out.println(((100 * processedOriginZones++) / zoneCnt) + "% DONE");
			for (Zone toZone : zonalSystem.getId2zoneView().values()) {
				for (int timeBin = 0; timeBin < timeBinCnt; timeBin++) {
					final double demand = demandUpscale*od.getDemandPerHour(fromZone.getId(), toZone.getId(), timeBin);
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
	}
	
	
	
	
	
	public static void createDemandTrucks(final String configFileName) {
			
		System.out.println("Truck Method.");

		final Config config = ConfigUtils.loadConfig(configFileName);
		config.network().setInputFile(norrkopingNetwork);
		
		final Scenario scenario = ScenarioUtils.loadScenario(config);
				
		final ZonalSystem zonalSystem = new ZonalSystem(norrkopingZoneShapeFile, StockholmTransformationFactory.WGS84_SWEREF99, "id");

		final Scanner scanner = new Scanner(System.in);
		System.out.println("DB user name: ");
		final String user = scanner.nextLine();
		System.out.println("password: ");
		final String passwd = scanner.nextLine();
		scanner.close();
		
		final DatabaseODMatrix od = new DatabaseODMatrix(user, passwd, "localhost", 5432); // 5455);
		System.out.println("DATABASE LOG IN OK");
		
		//Create information form database to arrays 
		od.createArrayListsFromDatabase();
		
		//Save into lists
		ArrayList<Integer> idList = od.getIdList();
		ArrayList<String> oList = od.getOriginList();
		ArrayList<String> dList = od.getDestList();
		ArrayList<Integer> hourList = od.getHourList();
		ArrayList<Double> demandList = od.getDemandList();
		
		//number of trucks to add in the transport system
		int rowsInOD = od.getNumberOfIDrows();
		int totalDemand = od.getTotalDemand();
		int person = 1;
		

		final int timeBinCnt = 24; //24h
		final double timeBinSize_s = Units.S_PER_D / timeBinCnt; //24h = 3600 seconds 
		
		for(int i = 0; i < rowsInOD; i++) {				
			
			int counter = 0;
			
			for(int j = 0; j < demandList.get(i); j++) {				
				counter = counter + 1;				
			addTripMakerTrucks(scenario, Id.createPersonId(person++), zonalSystem, 
					zonalSystem.getZone(oList.get(i)), dList.get(i),((Math.random()+hourList.get(i))*timeBinSize_s), demandList.get(i), counter);
		}}
			
		//Create new population file by merging norrkoping travellers with trucks in norrkoping
		final PopulationWriter writer = new PopulationWriter(scenario.getPopulation());
		writer.writeV6(populationMerged);
		
		
	}
	
	
	
	private static void addTripMakerTrucks(Scenario scenario, Id<Person> personId, ZonalSystem zonalSystem, Zone fromZone, String toZone, double dptTime_s, double demand, int counter) {
		//Demand to zone
		int demandToZone = (int) Math.ceil(demand);
		
		//Distribution to several sites in one zone
		double toSoderporten1 = 0.7*demandToZone;		
		double toSpinnhuset = 0.5*demandToZone;
		double toTingsratten = 0.2*demandToZone;	
		
		//Exact origin for trucks
		Coord fromCoord;				
		
		//Create person, add to sub-population, create/add plan
		final Person person = scenario.getPopulation().getFactory().createPerson(personId);
		person.getAttributes().putAttribute("subpopulation", "heavyVeh");
		
		final Plan plan = scenario.getPopulation().getFactory().createPlan();
		person.addPlan(plan);
		
		Population population = scenario.getPopulation();
		PopulationFactory pop = population.getFactory();
		Network network = scenario.getNetwork();
		
		
		
		if(fromZone.getId().equals("20") && toZone.equals("22")) {
			//Exact origin for route implementation
			//Now only test route between two zones
			List<Node> nodesList = new ArrayList<Node>();			
			nodesList = NetworkUtils.getNodes(network, "298500341");			
			fromCoord = nodesList.get(0).getCoord();		
			
			
		}else {
			//Origin for zones
			fromCoord = ShapeUtils.drawPointFromGeometry(fromZone.getGeometry());
		}
		
	
		if(fromZone.getId().equals("20") && toZone.equals("22")) {			
			//Create exact route for a truck agent		
			RouteFactories route = pop.getRouteFactories();
			
			List<Link> linkList = new ArrayList<Link>();
			//Route
			linkList = NetworkUtils.getLinks(network, "617831 206276 255576 946938 1476445 206289 59308 59309 22138 22137 736126 1242933 213741 953711 953687 1242913 953709 953708 984251 1395269 1242914 196059 704898 1389535 1389536 1389537 1273824 736106 736108 736110 594765 594763 101 1");
			
			ArrayList<Id<Link>> linkIds = new ArrayList<Id<Link>>();
			for(int i = 1; i<linkList.size()-1; i++) {					
				linkIds.add(linkList.get(i).getId());
			}
			//Still Route...
			NetworkRoute routeTo = route.createRoute(NetworkRoute.class, linkList.get(0).getId(), linkList.get(linkList.size()-1).getId());
			routeTo.setLinkIds(linkList.get(0).getId(), linkIds, linkList.get(linkList.size()-1).getId());
			
			//Activity with times
			Activity start = scenario.getPopulation().getFactory().createActivityFromCoord("start", fromCoord);
			start.setLinkId(linkList.get(0).getId());						
			start.setEndTime(dptTime_s);
			plan.addActivity(start);
			
			//Add mode and route to agent
			Leg leg = pop.createLeg(TransportMode.truck);
			leg.setDepartureTime(dptTime_s);
			leg.setTravelTime(900);
			leg.setRoute(routeTo);
			plan.addLeg(leg);			
			
		}else {
			//All other agents
			Activity start = scenario.getPopulation().getFactory().createActivityFromCoord("start", fromCoord);
			start.setEndTime(dptTime_s);
			plan.addActivity(start);
			Leg leg = pop.createLeg(TransportMode.truck);
			leg.setTravelTime(900);
			plan.addLeg(leg);
			
		}				
		
		
			if(toZone.equals("22")){			
					//Destination activity with possible latest arrival time
					Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", InreHamnen);					
					end.setLinkId(Id.get("1", Link.class));		
					end.setEndTime(dptTime_s + 2400);
					plan.addActivity(end);		
							
			} else if(toZone.equals("106")) {				
				if(counter <  toSoderporten1) {
					Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", Soderporten1);
					end.setEndTime(dptTime_s + 2400);
					
					plan.addActivity(end);
				} else {
					Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", Soderporten2);
					end.setEndTime(dptTime_s + 2400);
					plan.addActivity(end);
				}
				
					
			} else if(toZone.equals("21")) {
				if(counter <  toSpinnhuset) {
					Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", Spinnhuset);
					end.setEndTime(dptTime_s + 2400);
					plan.addActivity(end);
					
				} else if(counter <  (toSpinnhuset + toTingsratten)) {
					Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", Tingsratten);
					end.setEndTime(dptTime_s + 2400);
					plan.addActivity(end);
					
				} else {
					Activity end = scenario.getPopulation().getFactory().createActivityFromCoord("end", HotellSvea);
					end.setEndTime(dptTime_s + 2400);
					plan.addActivity(end);
				}											
				
			}							
			
			
			scenario.getPopulation().addPerson(person);
	}
	
	

	private static void addTripMaker(Scenario scenario, Id<Person> personId, ZonalSystem zonalSystem, Zone fromZone,
			Zone toZone, double dptTime_s) {
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
	}

	static void runXY2Links() {
		//final Config config = ConfigUtils.createConfig();
		final Config config = ConfigUtils.loadConfig(configFile);
		config.network().setInputFile(norrkopingNetwork);
		config.plans().setInputFile(populationMerged);
		//config.plans().setInputFile(norrkopingPopulationFile);
		final Scenario scenario = ScenarioUtils.loadScenario(config);

		// new NetworkCleaner().run(scenario.getNetwork());

		final XY2Links xy2links = new XY2Links(scenario);
		for (Person person : scenario.getPopulation().getPersons().values()) {
			
			xy2links.run(person);
		}
		final PopulationWriter writer = new PopulationWriter(scenario.getPopulation());
		//writer.writeV6(norrkopingPopulationFile);
		writer.writeV6(populationFile);
	}

	//static void reducePopulation() {
	//	PopulationSampler.main(new String[] { norrkopingPopulationFile, norrkoping25PctPopulationFile, "0.25" });
	//}

	static void runSimulation(final String configFileName) {

		final Config config = ConfigUtils.loadConfig(configFileName, new SwissRailRaptorConfigGroup(),
				new SBBTransitConfigGroup(), new GreedoConfigGroup());
		
		config.network().setInputFile(norrkopingNetwork);
		config.plans().setInputFile(populationFile);
		
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
	
	private static void updateNetworkConstructionSites(final String configFile) {
		
		final Config config = ConfigUtils.loadConfig(configFile);
		
		Scenario scenario = ScenarioUtils.loadScenario(config) ;
		
		Network network = scenario.getNetwork();
		NetworkFactory netF = network.getFactory();
		
		//network.removeLink(Id.get(12, Link.class));	
		
		//Node n1 = NetworkUtils.getNearestNode(network,new Coord((double) 0, (double) 0));
		
	
		//Soderporten2 with id2 and coordinates
		Node node2 = netF.createNode(Id.create("2", Node.class), Soderporten2);
		//HotellSvea with id3 and coordinates
		Node node3 = netF.createNode(Id.create("3", Node.class), HotellSvea);
		//Spinnhuset with id4 and coordinates
		Node node4 = netF.createNode(Id.create("4", Node.class), Spinnhuset);
		//InreHamnen with id5 and coordinates
		Node node5 = netF.createNode(Id.create("5", Node.class), InreHamnen);
		//Soderporten1 with id6 and coordinates
		Node node6 = netF.createNode(Id.create("6", Node.class), Soderporten1);
		//Tingsratten with id7 and coordinates
		Node node7 = netF.createNode(Id.create("7", Node.class), Tingsratten);
		
		network.addNode(node2);
		network.addNode(node3);
		network.addNode(node4);
		network.addNode(node5);
		network.addNode(node6);
		network.addNode(node7);
		
		//Koordinat av grinden/infart in till bygget
		Node node8 = netF.createNode(Id.create("8", Node.class), new Coord(569683.620, 6495699.188));
		network.addNode(node8);
		//Grinden till Spinnhuset och Tingsratten
		Node node9 = netF.createNode(Id.create("9", Node.class), new Coord(569228.424, 6495657.435)); 
		network.addNode(node9);
		//Grinden Hotell Svea
		Node node10 = netF.createNode(Id.create("10", Node.class), new Coord(568981.739, 6495701.063)); 
		network.addNode(node10);
		//Grinden Soderporten1
		Node node11 = netF.createNode(Id.create("11", Node.class), new Coord(569864.479,6493532.280)); 
		network.addNode(node11);
		//Grinden Soderporten2
		Node node12 = netF.createNode(Id.create("12", Node.class), new Coord(569923.786,6493568.580)); 
		network.addNode(node12);
				
		
		
		
		network.removeLink(Id.get("295322", Link.class));	
		network.removeLink(Id.get("295323", Link.class));	
		
		network.removeLink(Id.get("595202", Link.class));	
		network.removeLink(Id.get("595203", Link.class));	
		
		network.removeLink(Id.get("230054", Link.class));	
		network.removeLink(Id.get("230055", Link.class));	
			
		network.removeLink(Id.get("589686", Link.class));	
		network.removeLink(Id.get("589687", Link.class));	


		
		Link link1 = netF.createLink(Id.create("1", Link.class), node8, node5);
		network.addLink(link1);		
		Link link2 = netF.createLink(Id.create("2", Link.class), node9, node4);
		network.addLink(link2);
		Link link3 = netF.createLink(Id.create("3", Link.class), node9, node7);
		network.addLink(link3);
		Link link4 = netF.createLink(Id.create("4", Link.class), node10, node3);
		network.addLink(link4);
		Link link5 = netF.createLink(Id.create("5", Link.class), node11, node6);
		network.addLink(link5);
		Link link6 = netF.createLink(Id.create("6", Link.class), node12, node2);
		network.addLink(link6);
		
		
		List<Node> nodesList = new ArrayList<Node>();		
		nodesList = NetworkUtils.getNodes(network, "303762687 303762688 1100585367 1072465763 1072465818 1186585455 266517353");
		
		
		//Node n1 = NetworkUtils.getNearestNode(network,new Coord((double) 569645.4896960644, (double) 6495688.909616182));
		//Node n2 = NetworkUtils.getNearestNode(network,new Coord((double) 569744.6208874913, (double) 6495715.4377413215));
		
		Link link101 = netF.createLink(Id.create("101", Link.class), nodesList.get(0), node8);
		Link link102 = netF.createLink(Id.create("102", Link.class), node8, nodesList.get(0));
		Link link103 = netF.createLink(Id.create("103", Link.class), nodesList.get(1), node8);
		Link link104 = netF.createLink(Id.create("104", Link.class), node8, nodesList.get(1));
		link101.setFreespeed(40.0 / 3.6);
		link102.setFreespeed(40.0 / 3.6);
		link103.setFreespeed(40.0 / 3.6);
		link104.setFreespeed(40.0 / 3.6);
		link101.setCapacity(600); // veh / hour
		link102.setCapacity(600); // veh / hour
		link103.setCapacity(600); // veh / hour
		link104.setCapacity(600); // veh / hour
		link101.setNumberOfLanes(1);
		link102.setNumberOfLanes(1);
		link103.setNumberOfLanes(1);
		link104.setNumberOfLanes(1);
		link101.setAllowedModes(CollectionUtils.stringToSet("car"));	
		link102.setAllowedModes(CollectionUtils.stringToSet("car"));
		link103.setAllowedModes(CollectionUtils.stringToSet("car"));
		link104.setAllowedModes(CollectionUtils.stringToSet("car"));
		
		Link link105 = netF.createLink(Id.create("105", Link.class), nodesList.get(2), node9);
		
		Link link106 = netF.createLink(Id.create("106", Link.class), nodesList.get(3), node10);
		Link link107 = netF.createLink(Id.create("107", Link.class), node10, nodesList.get(3));
		Link link108 = netF.createLink(Id.create("108", Link.class), nodesList.get(4), node10);
		Link link109 = netF.createLink(Id.create("109", Link.class), node10, nodesList.get(4));
		
		Link link110 = netF.createLink(Id.create("110", Link.class), nodesList.get(5), node11);
		Link link111 = netF.createLink(Id.create("111", Link.class), node11, nodesList.get(5));
		Link link112 = netF.createLink(Id.create("112", Link.class), node11, node12);
		Link link113 = netF.createLink(Id.create("113", Link.class), node12, node11);
		Link link114 = netF.createLink(Id.create("114", Link.class), nodesList.get(6), node12);
		Link link115 = netF.createLink(Id.create("115", Link.class), node12, nodesList.get(6));
		
		network.addLink(link101);
		network.addLink(link102);
		network.addLink(link103);
		network.addLink(link104);
		network.addLink(link105);
		network.addLink(link106);
		network.addLink(link107);
		network.addLink(link108);
		network.addLink(link109);
		network.addLink(link110);
		network.addLink(link111);
		network.addLink(link112);
		network.addLink(link113);
		network.addLink(link114);
		network.addLink(link115);

		
		List<Link> linkList = new ArrayList<Link>();		
		linkList = NetworkUtils.getLinks(network, "1 2 3 4 5 6 105 106 107 108 109 110 111 112 113 114 115");		
		
		for(Link link: linkList) {			
			link.setFreespeed(30.0 / 3.6); // 50 km/h in m/s			
			link.setCapacity(600); // veh / hour
			link.setNumberOfLanes(1);
			link.setAllowedModes(CollectionUtils.stringToSet("car"));					
		}
		
		
		//link.setFreespeed(50.0 / 3.6); // 50 km/h in m/s
		//link.setLength(300); // meter
		//link.setCapacity(800); // veh / hour
		//link.setNumberOfLanes(1);
		//link.setAllowedModes(CollectionUtils.stringToSet("heavy"));
		//network.addLink(link);
		
		new NetworkWriter(network).write("C:\\Users\\TOPO-O\\Documents\\Master_RZ\\matsim\\original_data_matsim\\outputTest\\networkTest.xml");
		

	}
	
	private static void createVehicleTypes(String configFileName) {
		
		Config config = ConfigUtils.loadConfig(configFileName);		
		Scenario scenario = ScenarioUtils.loadScenario(config) ;
		
		VehicleType car = scenario.getVehicles().getFactory().createVehicleType(Id.create(TransportMode.car, VehicleType.class));
        car.setMaximumVelocity(120 / 3.6);
        scenario.getVehicles().addVehicleType(car);
		
		
        VehicleType truck = scenario.getVehicles().getFactory().createVehicleType(Id.create(TransportMode.truck, VehicleType.class));
        truck.setMaximumVelocity(10 / 3.6);
        truck.setLength(40);
        truck.setPcuEquivalents(6);
        truck.setWidth(4);
        truck.setNetworkMode(TransportMode.truck);
        scenario.getVehicles().addVehicleType(truck);

        new MatsimVehicleWriter(scenario.getVehicles()).writeFile(vehiclesFile);
		

	}
	
	private static void updateConfiguration(String configFileName) {
		
		Config config = ConfigUtils.loadConfig(configFileName);		
				
	
		config.plans().setInputFile(populationFile);			
		
		config.qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);       
        config.vehicles().setVehiclesFile(vehiclesFile);	
 
		
        List<String> mainModes = Arrays.asList(new String[]{TransportMode.car, TransportMode.truck});
        config.qsim().setMainModes(mainModes);
        config.plansCalcRoute().setNetworkModes(mainModes);
        config.travelTimeCalculator().setAnalyzedModesAsString("car,truck");
        config.travelTimeCalculator().setSeparateModes(false);
        
        PlanCalcScoreConfigGroup.ModeParams truck1 = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.truck);
        truck1.setMonetaryDistanceRate(-0.00185);
        truck1.setMarginalUtilityOfTraveling(-2.34);
        truck1.setConstant(0.0);
        truck1.setMarginalUtilityOfDistance(0.0);
        config.planCalcScore().addModeParams(truck1); 
    
        
        StrategyConfigGroup.StrategySettings keepRoute = new StrategyConfigGroup.StrategySettings();   
        keepRoute.setSubpopulation("heavyVeh");        
        keepRoute.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.KeepLastSelected);
        keepRoute.setWeight(1);
        
       config.strategy().addStrategySettings(keepRoute);
        
        new ConfigWriter(config).write(configFileUpdated);
        

	}
	
	

	public static void main(String[] args) {

		System.out.println("START OF MAIN");

//		final double xMin = 531377;
//		final double xMax = 622208;
//		final double yMin = 6474497;
//		final double yMax = 6524013;
		
		
//DEMAND UPSCALE???
		// final double demandUpscale = 100.0 / 18.0;

		//final double demandUpscale = 1;
		// cutFromSwedenCarOnly(xMin, xMax, yMin, yMax);
		// cutFromSwedenPTOnly(xMin, xMax, yMin, yMax);
		
		//updateNetworkConstructionSites(configFile);
		
		createDemandTrucks(configFile);
		 //createDemand(demandUpscale);
		runXY2Links();
		
		createVehicleTypes(configFile);
		// reducePopulation();			
		updateConfiguration(configFile);		
		

		runSimulation(configFileUpdated);

		System.out.println("END OF CODE");
	}

}
