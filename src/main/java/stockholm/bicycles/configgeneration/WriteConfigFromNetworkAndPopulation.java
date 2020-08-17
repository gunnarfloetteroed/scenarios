package stockholm.bicycles.configgeneration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;
import org.matsim.utils.objectattributes.attributable.Attributes;


public class WriteConfigFromNetworkAndPopulation {
	private final String matsimPlanFileName;
	private final String matsimNetworkFileName;
	private final String matsimConfigFileName;
	private final String matsimOutputDirName;
	
	public WriteConfigFromNetworkAndPopulation(String networkFile, String planFile, String configFile, String outputDir) {
		this.matsimNetworkFileName = networkFile;
		this.matsimPlanFileName = planFile;
		this.matsimConfigFileName=configFile;
		this.matsimOutputDirName=outputDir;
		// TODO Auto-generated constructor stub
	}
	
	public void WriteConfig() {
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		(new MatsimNetworkReader(scenario.getNetwork())).readFile(this.matsimNetworkFileName);
		
		// just to check if the network actually being loaded
		Network checkNetwork= scenario.getNetwork();
		Map<Id<Link>, ? extends Link> links = checkNetwork.getLinks();
	    for (Entry<Id<Link>, ? extends Link> linkMap:links.entrySet()) {
	    	System.out.println("following links are read: "+linkMap.getKey());
	    	Link link=linkMap.getValue();
	    	Attributes attributes = link.getAttributes();
	    	System.out.println(attributes.toString());
	    }
	    
		
		PopulationReader populationReader = new PopulationReader(scenario);
		populationReader.readFile(this.matsimPlanFileName);
		
//		// just to check if the population actually being loaded
//		Population checkPopulation= scenario.getPopulation();
//		Map<Id<Person>, ? extends Person> persons = checkPopulation.getPersons();
//	    for (Entry<Id<Person>, ? extends Person> entry:persons.entrySet()) {
//	    	System.out.println("following persons are read: "+entry.getKey());
//	    }
		
		// setup config file variables
		config.network().setInputFile(this.matsimNetworkFileName);
	    // config.network().setInputCRS("SWEREF99 TM");
		// "PROJCS[\"SWEREF99 TM\", GEOGCS[\"SWEREF99\", DATUM[\"SWEREF99\", SPHEROID[\"GRS 1980\",6378137,298.257222101, AUTHORITY[\"EPSG\",\"7019\"]], TOWGS84[0,0,0,0,0,0,0],  AUTHORITY[\"EPSG\",\"6619\"]], PRIMEM[\"Greenwich\",0, AUTHORITY[\"EPSG\",\"8901\"]], UNIT[\"degree\",0.01745329251994328, AUTHORITY[\"EPSG\",\"9122\"]], AUTHORITY[\"EPSG\",\"4619\"]], UNIT[\"metre\",1, AUTHORITY[\"EPSG\",\"9001\"]], PROJECTION[\"Transverse_Mercator\"], PARAMETER[\"latitude_of_origin\",0], PARAMETER[\"central_meridian\",15], PARAMETER[\"scale_factor\",0.9996], PARAMETER[\"false_easting\",500000], PARAMETER[\"false_northing\",0], AUTHORITY[\"EPSG\",\"3006\"], AXIS[\"y\",EAST], AXIS[\"x\",NORTH]]"
		config.plans().setInputFile(this.matsimPlanFileName);
		config.controler().setLastIteration(0);
		ActivityParams home = new ActivityParams("home");
		home.setTypicalDuration(16 * 60 * 60);
		config.planCalcScore().addActivityParams(home);
		ActivityParams work = new ActivityParams("work");
		work.setTypicalDuration(8 * 60 * 60);
		config.planCalcScore().addActivityParams(work);
		
		config.controler().setOutputDirectory(this.matsimOutputDirName);
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		
		// all below are copied from the example "RunBicycleExample", see org.matsim.codeexamples.extensions.bicycle
		config.plansCalcRoute().setNetworkModes( Arrays.asList( TransportMode.bike ) );
		config.plansCalcRoute().removeModeRoutingParams( TransportMode.bike );
		
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName( DefaultPlanStrategiesModule.DefaultStrategy.SubtourModeChoice );
			stratSets.setWeight( 1. );
			config.strategy().addStrategySettings( stratSets );

			config.subtourModeChoice().setModes( new String[]{ TransportMode.bike } ) ;
			config.subtourModeChoice().setChainBasedModes( new String[]{ TransportMode.bike } );
		}
		config.qsim().setMainModes( Arrays.asList(TransportMode.bike ) );
		
		for( Link link : scenario.getNetwork().getLinks().values() ){
			link.setAllowedModes( new HashSet<>( Arrays.asList( TransportMode.bike ) ) ) ;
		}
		
		
		ConfigUtils.writeConfig(config, this.matsimConfigFileName);
	}

	
	
	
	public void EditConfig(String linkAttributeFile) {
		ObjectAttributes attributes= new ObjectAttributes();
	    ObjectAttributesXmlReader objectAttributeReader= new ObjectAttributesXmlReader(attributes);
	    objectAttributeReader.readFile(linkAttributeFile);
	    System.out.println(attributes.toString());
	    
	    
	}
}
