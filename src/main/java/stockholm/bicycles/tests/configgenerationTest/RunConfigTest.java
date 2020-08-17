package stockholm.bicycles.tests.configgenerationTest;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.vehicles.Vehicle;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class RunConfigTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputPathScenario = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\testScenario\\";
		inputPathScenario = inputPathScenario.replaceAll("\\\\", "/");
		
		final String ConfigFile = inputPathScenario + "config_test.xml";
		Config config = ConfigUtils.loadConfig(  ConfigFile ) ;
		
		Scenario scenario = ScenarioUtils.loadScenario(config );

		Controler controler = new Controler(scenario);
		
		
		controler.addOverridingModule( new AbstractModule(){
			@Override
			public void install(){
				this.addTravelTimeBinding( TransportMode.bike ).toInstance( new TravelTime(){
					@Inject @Named(TransportMode.bike) TravelTimeCalculator bikeCalculator ;
					@Override public double getLinkTravelTime( Link link, double time, Person person, Vehicle vehicle ){
						String linkType = (String) link.getAttributes().getAttribute("linkType");
						double speedFromLink = link.getFreespeed( time );
						
						// some stupid logic to calculate travel speed. WILL CHANGE WHEN WE RUN THE ACTUAL MODEL
						if (linkType.equals("cykelbana")) {
							speedFromLink=speedFromLink+2;
						}else if (linkType.equals("cykelfalt")) {
							speedFromLink=speedFromLink+1;
						}
						
						return link.getLength()/speedFromLink ;
					}
				} );
			}
		} ) ;		
		

		
		controler.run();
	}

}
