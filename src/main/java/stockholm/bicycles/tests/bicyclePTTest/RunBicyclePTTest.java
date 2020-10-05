package stockholm.bicycles.tests.bicyclePTTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.BasicLocation;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.config.groups.PlansConfigGroup.HandlingOfPlansWithoutRoutingMode;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfig;
import org.matsim.core.mobsim.qsim.components.StandardQSimComponentConfigurator;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.router.TransitRouterConfig;

import com.google.inject.Provides;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.config.SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet;
import ch.sbb.matsim.mobsim.qsim.SBBTransitModule;
import ch.sbb.matsim.mobsim.qsim.pt.SBBTransitEngineQSimModule;
import ch.sbb.matsim.routing.pt.raptor.RaptorParameters;
import ch.sbb.matsim.routing.pt.raptor.RaptorUtils;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import stockholm.bicycles.configgeneration.StockholmBicycleTravelDisutilityFactory;
import stockholm.bicycles.configgeneration.StockholmWalkTravelDisutilityFactory;

public class RunBicyclePTTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputPathScenario = System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\\\testScenario\\";
		inputPathScenario = inputPathScenario.replaceAll("\\\\", "/");
		final String OutputTransitBicycleConfigFile = inputPathScenario + "configBicyclePT_test.xml";
		Config config = ConfigUtils.loadConfig(  OutputTransitBicycleConfigFile) ;
		Scenario scenario = ScenarioUtils.loadScenario(config );
		Controler controler = new Controler(scenario);
		config.plans().setHandlingOfPlansWithoutRoutingMode(HandlingOfPlansWithoutRoutingMode.useMainModeIdentifier );
		//		// To use the deterministic pt simulation (Part 1 of 2):
		//				controler.addOverridingModule(new SBBTransitModule());

		// To use the fast pt router (Part 1 of 1)
		// controler.addOverridingModule(new SwissRailRaptorModule());
		//				// To use the deterministic pt simulation (Part 2 of 2):
		//				controler.configureQSimComponents(components -> {
		//					SBBTransitEngineQSimModule.configure(components);
		//
		//					// if you have other extensions that provide QSim components, call their configure-method here
		//				});


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

		controler.addOverridingModule(new AbstractModule(){
			@Override
			public void install() {
				addTravelDisutilityFactoryBinding(TransportMode.bike).toInstance(new StockholmBicycleTravelDisutilityFactory());
				// addTravelDisutilityFactoryBinding(TransportMode.walk).toInstance(new StockholmWalkTravelDisutilityFactory());
			}
		});

//		RaptorParameters check = RaptorUtils.createParameters(config);
//		System.out.println(check.toString());
		// PlansCalcRouteConfigGroup params = config.plansCalcRoute();
		// RaptorParameters check = RaptorUtils.createParameters(config);
		// System.out.println("111");
		
//		Map<Id<Person>, ? extends Person> persons = scenario.getPopulation().getPersons();
//		Person person1 = persons.get(Id.createPersonId("person_1"));
//		List<? extends Plan> p1Plans = person1.getPlans();
//		Plan p1PlanElements= p1Plans.get(0);
//		List<PlanElement> planElements = p1PlanElements.getPlanElements();
//		PlanElement act = planElements.get(0);
//		Link link = NetworkUtils.getNearestLink(scenario.getNetwork(), ((Activity) act).getCoord());
//		System.out.println(act.toString());
//		System.out.println(link.toString());
		
		
		
		
//		SwissRailRaptorConfigGroup srrConfig = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
//		if (srrConfig.isUseIntermodalAccessEgress()) {
//			for (IntermodalAccessEgressParameterSet params : srrConfig.getIntermodalAccessEgressParameterSets()) {
//				String mode = params.getMode();
//				System.out.println(mode);
//			}
//		}
	   controler.run();
	    
	}
	

}
