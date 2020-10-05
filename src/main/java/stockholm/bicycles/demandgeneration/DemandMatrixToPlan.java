package stockholm.bicycles.demandgeneration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.matrices.Entry;
import org.matsim.matrices.Matrix;

import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.utility.CsvReaderToIteratable;
import stockholm.bicycles.utility.MultinomialDistributionSamplerMap;

public class DemandMatrixToPlan {
	private final String demandMatrixFileName;
	private final String matsimPlanFileName;
	private final Scenario scenario;


	public DemandMatrixToPlan(String demandMatrixFileName, String networkFileName, String matsimPlanFileName) {
		super();
		this.demandMatrixFileName = demandMatrixFileName;
		this.matsimPlanFileName = matsimPlanFileName;
		
		// create a config file with 2 activities: home and work
		Config config = ConfigUtils.createConfig();

		config.controler().setLastIteration(10);
		ActivityParams home = new ActivityParams("home");
		home.setTypicalDuration(16 * 60 * 60);
		config.planCalcScore().addActivityParams(home);
		ActivityParams work = new ActivityParams("work");
		work.setTypicalDuration(8 * 60 * 60);
		config.planCalcScore().addActivityParams(work);
		
		this.scenario = ScenarioUtils.createScenario(config);
		(new MatsimNetworkReader(this.scenario.getNetwork())).readFile(networkFileName);
		
	}


	public static double matrixSum(Matrix matrix) {
		
		Map<String, Double> rowSum =matrixRowSum(matrix);
	
		double sum =0;
		for (String key:rowSum.keySet()) {
			double sumOfRow=rowSum.get(key);
			sum=sum+sumOfRow;
		}
		return sum;
	}
	
	public static Map<String, Double> matrixRowSum(Matrix matrix){
		Map<String, ArrayList<Entry>> FromId = matrix.getFromLocations();
		Map<String, Double> rowSum = new HashMap<>();
		
		for (String key:FromId.keySet()) {
			ArrayList<Entry> OD=FromId.get(key);
			double sum =0;
			for (Entry eachElement : OD) {
				sum=sum+eachElement.getValue();
				// System.out.println("Row: "+eachElement.getFromLocation()+ "; Col: "+eachElement.getToLocation()+"; Value:  "+eachElement.getValue());
			}
			rowSum.put(key, sum);
		}
		
		return rowSum;
		
	}
	
	private static Map<String, Double> rowEntryListToMap(List<Entry> entryList){
		Map<String, Double> outputMap = new HashMap<>();
		for (Entry entry : entryList) {
			outputMap.put(entry.getToLocation(), entry.getValue());
		}
		return outputMap;
	}
	
	public void generatePlan() throws IOException, CsvException {
		CsvReaderToIteratable demandMatrixReader = new CsvReaderToIteratable(this.demandMatrixFileName,';');
		Matrix demandMatrixTable = demandMatrixReader.readODMatrixWithUniqueID(0);
		// 1. calculate total number of trips per zone (row)
		double totalNumberOfTrips=matrixSum(demandMatrixTable);
		System.out.println("Total number of trips: " +totalNumberOfTrips+".");
		Map<String, Double> numberOfTripsInEachOriginZone =matrixRowSum(demandMatrixTable);
		
		// 3. sample number of out-of-home trips per origin zone
		Random randomSeed = new Random(200);
		MultinomialDistributionSamplerMap originZoneTripSampler= new MultinomialDistributionSamplerMap(numberOfTripsInEachOriginZone);
		originZoneTripSampler.setSeed(randomSeed.nextLong());
		int totalNumberOfTripsToBeSampled = (int) (totalNumberOfTrips-2);  // assuming we sample X% of total number of out-of-home trips.
		String[] sampledTripsFromOrigin=originZoneTripSampler.sampleMapWithoutReplacement(totalNumberOfTripsToBeSampled);
		
        for (int i=0; i<sampledTripsFromOrigin.length;i++) {
			System.out.println("Map sampler's sample without replacement is: "+ sampledTripsFromOrigin[i]);
		}
        
        // 4. for each sampled trip, we sample a destination and randomly generate a trip timing. Create a stupid random histogram of trip timing, 
        HashMap<String,Double> departureTimeDistribution=new HashMap<>();
        departureTimeDistribution.put("1", 1.0);  // this means you have 1% chance of departing at 1:00.
        departureTimeDistribution.put("2", 1.0);
        departureTimeDistribution.put("3", 2.0);
        departureTimeDistribution.put("4", 3.0);
        departureTimeDistribution.put("5", 8.0);
        departureTimeDistribution.put("6", 15.0);
        departureTimeDistribution.put("7", 20.0);
        departureTimeDistribution.put("8", 15.0);
        departureTimeDistribution.put("9", 10.0);
        departureTimeDistribution.put("10", 5.0);
        departureTimeDistribution.put("11", 5.0);
        departureTimeDistribution.put("12", 5.0);
        departureTimeDistribution.put("13", 5.0);
        departureTimeDistribution.put("14", 5.0);
        departureTimeDistribution.put("15", 0.0);
        departureTimeDistribution.put("16", 0.0);
        departureTimeDistribution.put("17", 0.0);
        departureTimeDistribution.put("18", 0.0);
        departureTimeDistribution.put("19", 0.0);
        departureTimeDistribution.put("20", 0.0);
        departureTimeDistribution.put("21", 0.0);
        departureTimeDistribution.put("22", 0.0);
        departureTimeDistribution.put("23", 0.0);
        departureTimeDistribution.put("24", 0.0);
        
        
        // 5. for each sampled trip, generate a person-plan for that.
        for (int i=0; i<sampledTripsFromOrigin.length;i++) {
        	String personID="person_"+(i+1);
        	String tripOrigin=sampledTripsFromOrigin[i];
        	if (tripOrigin!=null) {
        		// sample a destination
            	List<Entry> oneOriginToAllDestinationEntryList = demandMatrixTable.getFromLocEntries(tripOrigin);
            	Map<String, Double> oneOriginToAllDestination = rowEntryListToMap(oneOriginToAllDestinationEntryList);
            	
            	MultinomialDistributionSamplerMap destinationZoneTripSampler= new MultinomialDistributionSamplerMap(oneOriginToAllDestination);
            	destinationZoneTripSampler.setSeed(randomSeed.nextLong());
            	String tripDestination=destinationZoneTripSampler.sampleMap();
            	
            	// randomly generate a departure time
            	MultinomialDistributionSamplerMap departureTimeSampler= new MultinomialDistributionSamplerMap(departureTimeDistribution);
            	departureTimeSampler.setSeed(randomSeed.nextLong());
            	String departureTimeInHour=departureTimeSampler.sampleMap();
            	
            	System.out.println("trip " + i + " starts from zone: " + tripOrigin +", departs at: "+ departureTimeInHour + ", ends at zone: " + tripDestination);
            	
            	createOnePersonPlan(this.scenario,personID,tripOrigin,tripDestination,departureTimeInHour);
        	}
        	
        }
        
        
        // 6. write the person-plan into a plan file.
        PopulationWriter popwriter = new PopulationWriter(this.scenario.getPopulation(), this.scenario.getNetwork());
		popwriter.write(this.matsimPlanFileName);
		
//		Controler controler = new Controler(scenario);
//		controler.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
//		controler.run();
		
		
	}


	private static void createOnePersonPlan(Scenario scenario, String personID, String tripOrigin, String tripDestination,
			String departureTimeInHour) {
		// a method to create one person plan with home-work-home pattern 
		Population population = scenario.getPopulation();
		final Person person = population.getFactory().createPerson(Id.createPersonId(personID));
		Plan plan = population.getFactory().createPlan();
		Network network=scenario.getNetwork();
		Map<Id<Node>, ? extends Node> NodeMap = network.getNodes();
		
		
		Node originNode=NodeMap.get(Id.createNodeId(tripOrigin));
		Coord originCoord = originNode.getCoord();
		
		Node destinationNode=NodeMap.get(Id.createNodeId(tripDestination));
		Coord destinationCoord = destinationNode.getCoord();
		
		Activity home = population.getFactory().createActivityFromCoord("home", originCoord);
		home.setEndTime(Double.parseDouble(departureTimeInHour)*60*60);
		plan.addActivity(home);
		
		Leg hinweg = population.getFactory().createLeg("bike");
		plan.addLeg(hinweg);
		
		Activity work = population.getFactory().createActivityFromCoord("work", destinationCoord);
		if (Double.parseDouble(departureTimeInHour)+8.0<=21) {
			work.setEndTime((Double.parseDouble(departureTimeInHour)+8.0)*60*60);
		}else {
			work.setEndTime(21*60*60);
		}
		plan.addActivity(work);
		
		
		Leg rueckweg = population.getFactory().createLeg("bike");
		plan.addLeg(rueckweg);

		Activity home2 = population.getFactory().createActivityFromCoord("home", originCoord);
		plan.addActivity(home2);

		person.addPlan(plan);
		population.addPerson(person);

		
	}





}
