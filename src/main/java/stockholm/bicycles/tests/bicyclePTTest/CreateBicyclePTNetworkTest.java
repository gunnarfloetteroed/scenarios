package stockholm.bicycles.tests.bicyclePTTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.OptionalTime;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.pt.utils.TransitScheduleValidator;
import org.matsim.vehicles.MatsimVehicleWriter;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehiclesFactory;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.config.SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet;

public class CreateBicyclePTNetworkTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputPathScenario = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\testScenario\\";
		inputPathScenario = inputPathScenario.replaceAll("\\\\", "/");
		
		final String ConfigFile = inputPathScenario + "config_test.xml";
		final String OutputTransitSceduleFile = inputPathScenario + "transitSchedule_test.xml";
		final String OutputTransitVehicleFile = inputPathScenario + "transitVehicle_test.xml";
		final String OutputTransitBicycleNetworkFile = inputPathScenario + "transitBicycleNetwork_test.xml";
		final String OutputTransitBicyclePlanFile = inputPathScenario + "transitBicyclePlan_test.xml";
		final String OutputTransitBicycleOutputFile = inputPathScenario + "outputBicyclePT";
		final String OutputTransitBicycleConfigFile = inputPathScenario + "configBicyclePT_test.xml";
		Config config = ConfigUtils.loadConfig(  ConfigFile ) ;
		
		Scenario scenario = ScenarioUtils.loadScenario(config );
		Network bicycleNetwork= scenario.getNetwork();
		
		// create transit links from existing nodes
		NetworkFactory nf = bicycleNetwork.getFactory();
		Map<Id<Node>, ? extends Node> nodes = bicycleNetwork.getNodes();
		
		Map<Id<Link>, ? extends Link> links = bicycleNetwork.getLinks();
		for (Entry<Id<Link>, ? extends Link> entry:links.entrySet()) {
			Link link = entry.getValue();
			link.setAllowedModes(new HashSet<>(Arrays.asList(TransportMode.bike,TransportMode.car)));
		}
		
		
		Node nodeStop0 = nodes.get(Id.createNodeId("105079"));
		Coord nodtStop0Coord = nodeStop0.getCoord();
		Node trNode0 = nf.createNode(Id.createNodeId("tr_0_105079"), nodtStop0Coord);
		bicycleNetwork.addNode(trNode0);
		Node nodeStop1 = nodes.get(Id.createNodeId("69597"));
		Coord nodtStop1Coord = nodeStop1.getCoord();
		Node trNode1 = nf.createNode(Id.createNodeId("tr_0_69597"), nodtStop1Coord);
		// bicycleNetwork.addNode(trNode1);
		
		createAndAddTransitLink(bicycleNetwork, nodeStop0, nodeStop1, Id.createLinkId("tr_0"));
		Node nodeStop2 = nodes.get(Id.createNodeId("35472"));
		Coord nodtStop2Coord = nodeStop2.getCoord();
		Node trNode2 = nf.createNode(Id.createNodeId("tr_0_35472"), nodtStop2Coord);
		// bicycleNetwork.addNode(trNode2);
		
		createAndAddTransitLink(bicycleNetwork, nodeStop1, nodeStop2, Id.createLinkId("tr_1"));
		Node nodeStop3 = nodes.get(Id.createNodeId("53139"));
		Coord nodtStop3Coord = nodeStop3.getCoord();
		Node trNode3 = nf.createNode(Id.createNodeId("tr_0_53139"), nodtStop3Coord);
		// bicycleNetwork.addNode(trNode3);
		
		createAndAddTransitLink(bicycleNetwork, nodeStop2, nodeStop3, Id.createLinkId("tr_2"));
		
		
		// create transit stop facility
		TransitSchedule schedule = scenario.getTransitSchedule();
		TransitScheduleFactory tsf = schedule.getFactory();
		
		TransitStopFacility stopFacility0 = tsf.createTransitStopFacility( Id.create( "StopFac0", TransitStopFacility.class ), nodtStop1Coord, false );
		stopFacility0.setLinkId( Id.createLinkId("tr_0") );
		stopFacility0.getAttributes().putAttribute("accessLinkId_bike", "5588953_AB");
		stopFacility0.getAttributes().putAttribute("bikeAccessible", true);
		schedule.addStopFacility( stopFacility0 );
		
		TransitStopFacility stopFacility1 = tsf.createTransitStopFacility( Id.create( "StopFac1", TransitStopFacility.class ), nodtStop2Coord, false );
		stopFacility1.setLinkId( Id.createLinkId("tr_1") );
		stopFacility1.getAttributes().putAttribute("accessLinkId_bike", "5588984_AB");
		stopFacility1.getAttributes().putAttribute("bikeAccessible", true);
		schedule.addStopFacility( stopFacility1 );
		
		TransitStopFacility stopFacility2 = tsf.createTransitStopFacility( Id.create( "StopFac2", TransitStopFacility.class ), nodtStop3Coord, false );
		stopFacility2.setLinkId( Id.createLinkId("tr_2") );
		stopFacility2.getAttributes().putAttribute("accessLinkId_bike", "5563262_AB");
		stopFacility2.getAttributes().putAttribute("bikeAccessible", true);
		schedule.addStopFacility( stopFacility2 );
		
		// create transit vehicle types
		VehiclesFactory tvf = scenario.getTransitVehicles().getFactory();
		VehicleType busType = tvf.createVehicleType( Id.create( "bus", VehicleType.class ) );
		busType.getCapacity().setSeats(100);
		busType.getCapacity().setStandingRoom(100);
		busType.setMaximumVelocity( 100. / 3.6 );
		scenario.getTransitVehicles().addVehicleType( busType );
		
		// create transit routes
		PopulationFactory pf = scenario.getPopulation().getFactory();
		List<Id<Link>> linkIds = new ArrayList<>() ;
		linkIds.add( Id.createLinkId("tr_1") ) ;
		
		
		NetworkRoute route = pf.getRouteFactories().createRoute( NetworkRoute.class, Id.createLinkId("tr_0"), Id.createLinkId("tr_2") ) ;
		route.setLinkIds( Id.createLinkId("tr_0"), linkIds, Id.createLinkId("tr_2") ) ;
		List<TransitRouteStop> stops = new ArrayList<>() ;
		{
			stops.add( tsf.createTransitRouteStop( schedule.getFacilities().get( Id.create( "StopFac0", TransitStopFacility.class ) ), 0., 0. ) );
			stops.add( tsf.createTransitRouteStop( schedule.getFacilities().get( Id.create( "StopFac1", TransitStopFacility.class ) ), 1., 1. ) );
			stops.add( tsf.createTransitRouteStop( schedule.getFacilities().get( Id.create( "StopFac2", TransitStopFacility.class ) ), 1., 1. ) );
		}

		
		TransitRoute transitRoute = tsf.createTransitRoute( Id.create( "route1", TransitRoute.class ), route, stops, "bus" );
		for ( int ii=0 ; ii<100 ; ii++ ){
			String str = "tr_" + ii ;

			scenario.getTransitVehicles().addVehicle( tvf.createVehicle( Id.createVehicleId( str ), scenario.getTransitVehicles().getVehicleTypes().get( Id.create( "bus", VehicleType.class )) ) );

			Departure departure = tsf.createDeparture( Id.create( str, Departure.class ), 7. * 3600. + ii*300 ) ;
			departure.setVehicleId( Id.createVehicleId( str ) );
			transitRoute.addDeparture( departure );
		}
		
		TransitLine line = tsf.createTransitLine( Id.create( "line1", TransitLine.class ) );
		line.addRoute( transitRoute );

		schedule.addTransitLine( line );
		TransitScheduleValidator.printResult( TransitScheduleValidator.validateAll( scenario.getTransitSchedule(), scenario.getNetwork() ) );
		
		// save the network and the schedule files
		new TransitScheduleWriter(schedule).writeFile(OutputTransitSceduleFile);
		new MatsimVehicleWriter(scenario.getTransitVehicles()).writeFile(OutputTransitVehicleFile);
		new NetworkWriter(bicycleNetwork).write(OutputTransitBicycleNetworkFile);
		
		// change plan file
		Map<Id<Person>, ? extends Person> persons = scenario.getPopulation().getPersons();
		for (Person person: persons.values()) {
			person.getAttributes().putAttribute("hasBike", true);
			List<? extends Plan> personPlans = person.getPlans();
			for (Plan plan :personPlans) {
				List<PlanElement> planElements = plan.getPlanElements();
				for (PlanElement planElement : planElements) {
					if (planElement instanceof Leg ) {
						((Leg) planElement).setMode("busPassenger");
					} else if (planElement instanceof Activity){
						OptionalTime endTime = ((Activity) planElement).getEndTime();
						String activityType = ((Activity) planElement).getType();
						if (activityType.equals("home") && endTime.isDefined()) {
							((Activity) planElement).setEndTime(3600*8);
						} else if (activityType.equals("work")) {
							((Activity) planElement).setEndTime(3600*12);
						}
					}
				}
			}
		}
		
		PopulationWriter popwriter = new PopulationWriter(scenario.getPopulation(), bicycleNetwork);
		popwriter.write(OutputTransitBicyclePlanFile);
		
		// modify config file
		config.network().setInputFile(OutputTransitBicycleNetworkFile);
		config.plans().setInputFile(OutputTransitBicyclePlanFile);
		config.controler().setOutputDirectory(OutputTransitBicycleOutputFile);
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		
		config.qsim().setEndTime( 24.*3600. );
		
		config.transit().setUseTransit(true) ;
		config.transit().setTransitScheduleFile(OutputTransitSceduleFile);
		config.transit().setVehiclesFile(OutputTransitVehicleFile);
		config.transit().setTransitModes(new HashSet<>(Arrays.asList("bus")));
		
		

		config.plansCalcRoute().setNetworkModes( Arrays.asList( "bus") );
		

		
		PlanCalcScoreConfigGroup.ModeParams bike = new PlanCalcScoreConfigGroup.ModeParams("bike");
		bike.setMarginalUtilityOfTraveling(-1);
		config.planCalcScore().addModeParams(bike);

		PlanCalcScoreConfigGroup.ModeParams bus = new PlanCalcScoreConfigGroup.ModeParams("bus");
		bus.setMarginalUtilityOfTraveling(-1);
		config.planCalcScore().addModeParams(bus);

//		PlanCalcScoreConfigGroup.ActivityParams carInteraction = new PlanCalcScoreConfigGroup.ActivityParams("car interaction");
//		carInteraction.setScoringThisActivityAtAll(false);
//		config.planCalcScore().addActivityParams(carInteraction);
//		
//		PlanCalcScoreConfigGroup.ActivityParams PTinteraction = new PlanCalcScoreConfigGroup.ActivityParams("pt interaction");
//		PTinteraction.setScoringThisActivityAtAll(false);
//		config.planCalcScore().addActivityParams(PTinteraction);
//		
//		PlanCalcScoreConfigGroup.ActivityParams bikeInteraction = new PlanCalcScoreConfigGroup.ActivityParams("bike interaction");
//		bikeInteraction.setScoringThisActivityAtAll(false);
//		config.planCalcScore().addActivityParams(bikeInteraction);
//		
//		PlanCalcScoreConfigGroup.ActivityParams drtInteraction = new PlanCalcScoreConfigGroup.ActivityParams("drt interaction");
//		drtInteraction.setScoringThisActivityAtAll(false);
//		config.planCalcScore().addActivityParams(drtInteraction);
//		
//		PlanCalcScoreConfigGroup.ActivityParams taxiInteraction = new PlanCalcScoreConfigGroup.ActivityParams("taxi interaction");
//		taxiInteraction.setScoringThisActivityAtAll(false);
//		config.planCalcScore().addActivityParams(taxiInteraction);
//		
//		PlanCalcScoreConfigGroup.ActivityParams otherInteraction = new PlanCalcScoreConfigGroup.ActivityParams("other interaction");
//		otherInteraction.setScoringThisActivityAtAll(false);
//		config.planCalcScore().addActivityParams(otherInteraction);
//		
//		PlanCalcScoreConfigGroup.ActivityParams walkInteraction = new PlanCalcScoreConfigGroup.ActivityParams("walk interaction");
//		walkInteraction.setScoringThisActivityAtAll(false);
//		config.planCalcScore().addActivityParams(walkInteraction);
		

		SwissRailRaptorConfigGroup configRaptor = createRaptorConfigGroup( 1000000);// (radius walk, radius bike)
		config.addModule(configRaptor);
		
		// ConfigUtils.writeMinimalConfig(config, OutputTransitBicycleConfigFile);

	}
	
	private static void createAndAddTransitLink( Network bicycleNetwork, Node node1, Node node2, Id<Link> TR_LINK_0_1_ID ){
		Set<String> allowedModes = new HashSet<>(Arrays.asList("pt", TransportMode.bike,TransportMode.car));
		Link trLink = bicycleNetwork.getFactory().createLink( TR_LINK_0_1_ID, node1, node2 );
		trLink.setFreespeed( 100. / 3.6 );
		trLink.setCapacity( 100000. );
		trLink.setAllowedModes(allowedModes);
		trLink.getAttributes().putAttribute("bicycleSpeed_M_S",15/3.6);
		bicycleNetwork.addLink( trLink );
	}
	
	
	private static SwissRailRaptorConfigGroup createRaptorConfigGroup(int radiusBike) {
		SwissRailRaptorConfigGroup configRaptor = new SwissRailRaptorConfigGroup();
		configRaptor.setUseIntermodalAccessEgress(true);


//		// Bike
//		IntermodalAccessEgressParameterSet paramSetBike = new IntermodalAccessEgressParameterSet();
//		paramSetBike.setMode(TransportMode.bike);
//		((IntermodalAccessEgressParameterSet) paramSetBike).setMaxRadius(radiusBike);
//		paramSetBike.setPersonFilterAttribute(null);
//		paramSetBike.setStopFilterAttribute(null);
//		configRaptor.addIntermodalAccessEgress(paramSetBike );

		return configRaptor;
	}

}
