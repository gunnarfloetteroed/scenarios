package stockholm.bicycles.tests.configgenerationTest;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;



import stockholm.bicycles.configgeneration.StockholmBicycleTravelDisutilityFactory;

public class RunConfigTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputPathScenario = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\testScenario\\";
		inputPathScenario = inputPathScenario.replaceAll("\\\\", "/");
		
		final String ConfigFile = inputPathScenario + "config_test.xml";
		Config config = ConfigUtils.loadConfig(  ConfigFile ) ;
		
		Scenario scenario = ScenarioUtils.loadScenario(config );

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
