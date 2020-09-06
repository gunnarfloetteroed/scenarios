package stockholm.bicycles.configgeneration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

import stockholm.bicycles.importnetwork.TransCad2MATSimNetwork;

public class RunConfigBoatlineProductionRunner {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		

		// TODO Auto-generated method stub
		String inputPathScenario = "D:/MatsimBicycleSimulation/Simulation/";

		final String ConfigFile = inputPathScenario + "config_Stockholm_boatscenario.xml";
		Config config = ConfigUtils.loadConfig(  ConfigFile ) ;
		
		Scenario scenario = ScenarioUtils.loadScenario(config );
		Network network= scenario.getNetwork();
		Map<Id<Link>, ? extends Link> links = network.getLinks();
	    for (Entry<Id<Link>, ? extends Link> linkMap:links.entrySet()) {
	    	Link link=linkMap.getValue();
	    	if(link.getLength()<=0) {
	    		link.setLength(0.01);
	    		System.out.println("Link: "+link.getId().toString() +" has 0 length, changed to 0.01m.");
	    	}
	    	
	    }
	    
	    // add one link as the boatline
	    NetworkFactory networkFactory = network.getFactory();
	    Map<Id<Node>, ? extends Node> allNodes = network.getNodes();
	    Node node1 = allNodes.get(Id.create("69605", Node.class));  // the node of the boatline in the network
	    Node node2 = allNodes.get(Id.create("150017", Node.class));  // the node of the boatline in the network
	    
	    double linkLengthFromDataMeter= 570.0;
	    Set<String> allowedModes = new HashSet<>(Arrays.asList("bike"));
	    // create AB boatline
	    Link boatLine_AB = networkFactory.createLink(Id.createLinkId("9197612_AB"), node1, node2);
	    double linkLength = TransCad2MATSimNetwork.checkLength(linkLengthFromDataMeter,boatLine_AB);
		
		boatLine_AB.setLength(linkLength); // change back to: matsimLink.setLength(LinkLengthKM * Units.M_PER_KM);
		boatLine_AB.setFreespeed(5.7/3.6);  
		boatLine_AB.setAllowedModes(allowedModes);
		boatLine_AB.getAttributes().putAttribute("SHAPE_LEN",linkLengthFromDataMeter);
		boatLine_AB.getAttributes().putAttribute("bicycleSpeed_M_S",5.7/3.6);
		boatLine_AB.getAttributes().putAttribute("generalizedCost",0.855);
		boatLine_AB.getAttributes().putAttribute("linkType","0");
		boatLine_AB.getAttributes().putAttribute("slope","0.0");
		boatLine_AB.getAttributes().putAttribute("connector","0");
		network.addLink(boatLine_AB);
		
	    // create BA boatline
		Link boatLine_BA = networkFactory.createLink(Id.createLinkId("9197612_BA"),  node2,node1);
		boatLine_BA.setLength(linkLength); // change back to: matsimLink.setLength(LinkLengthKM * Units.M_PER_KM);
		boatLine_BA.setFreespeed(5.7/3.6);  
		boatLine_BA.setAllowedModes(allowedModes);
		boatLine_BA.getAttributes().putAttribute("SHAPE_LEN",linkLengthFromDataMeter);
		boatLine_BA.getAttributes().putAttribute("bicycleSpeed_M_S",5.7/3.6);
		boatLine_BA.getAttributes().putAttribute("generalizedCost",0.855);
		boatLine_BA.getAttributes().putAttribute("linkType","0");
		boatLine_BA.getAttributes().putAttribute("slope","0.0");
		boatLine_BA.getAttributes().putAttribute("connector","0");
		network.addLink(boatLine_BA);
		
		

		Controler controler = new Controler(scenario);
		
		
//		controler.addOverridingModule( new AbstractModule(){
//			@Override
//			public void install(){
//				this.addTravelTimeBinding( TransportMode.bike ).toInstance( new TravelTime(){
//					@Inject @Named(TransportMode.bike) TravelTimeCalculator bikeCalculator ;
//					@Override public double getLinkTravelTime( Link link, double time, Person person, Vehicle vehicle ){
//						String linkType = (String) link.getAttributes().getAttribute("linkType");
//						double speedFromLink = link.getFreespeed( time );
//						double maxSpeedFromObservation = bikeCalculator.getLinkTravelTimes().getLinkTravelTime( link, time, person, vehicle ) ;
//						// some stupid logic to calculate travel speed. WILL CHANGE WHEN WE RUN THE ACTUAL MODEL
//						if (linkType.equals("1")) {
//							speedFromLink=speedFromLink+2;
//						}else if (linkType.equals("2")) {
//							speedFromLink=speedFromLink+1;
//						}
//						
//						return link.getLength()/speedFromLink ;
//					}
//				} );
//			}
//		} ) ;	
		
		controler.addOverridingModule(new AbstractModule(){
			@Override
			public void install() {
		        addTravelDisutilityFactoryBinding(TransportMode.bike).toInstance(new StockholmBicycleTravelDisutilityFactory());

			}
		});
		

		
		controler.run();

	

	}

}
