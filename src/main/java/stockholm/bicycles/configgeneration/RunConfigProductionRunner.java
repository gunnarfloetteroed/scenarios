package stockholm.bicycles.configgeneration;

import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;



public class RunConfigProductionRunner {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputPathScenario = "D:/MatsimBicycleSimulation/Simulation/";

		final String ConfigFile = inputPathScenario + "config_Stockholm.xml";
		Config config = ConfigUtils.loadConfig(  ConfigFile ) ;
		
		Scenario scenario = ScenarioUtils.loadScenario(config );
		Network network= scenario.getNetwork();
		Map<Id<Link>, ? extends Link> links = network.getLinks();
	    for (Entry<Id<Link>, ? extends Link> linkMap:links.entrySet()) {
	    	Link link=linkMap.getValue();
	    	if(link.getLength()<=0) {
	    		link.setLength(0.01);
	    	}
	    	
	    }
		
		
		

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
