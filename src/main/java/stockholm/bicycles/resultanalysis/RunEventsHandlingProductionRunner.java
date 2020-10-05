package stockholm.bicycles.resultanalysis;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

import com.opencsv.CSVWriter;

public class RunEventsHandlingProductionRunner {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		
		//path to events file. For this you first need to run a simulation. Base scenario
//		String inputPath = "D:/MatsimBicycleSimulation/Simulation/";
//		final String inputFile = inputPath+"output/output_events.xml.gz";
//		String outputPath = inputPath;
//		final String outputFile = outputPath+"output/linkVolume.csv";
		
		//path to events file. For this you first need to run a simulation. Boatline scenario
		String inputPath = "C:/Users/ChengxiL/Box Sync/MatsimBicycleSimulation/Simulation/output_boatScenario/";
		final String inputFile = inputPath+"output_events.xml.gz";
		String outputPath = inputPath;
		final String outputFile = outputPath+"linkVolume.csv";
		
		//create an event object
		EventsManager events = EventsUtils.createEventsManager();

		//create the handler and add it
		LinkVolumeHandler linkVolumeHandler1 = new LinkVolumeHandler();
		events.addHandler(linkVolumeHandler1);

		//create the reader and read the file
		MatsimEventsReader reader = new MatsimEventsReader(events);
		reader.readFile(inputFile);

		HashMap<Id<Link>, double[]> linkVolume = linkVolumeHandler1.getLinkVolume();
		int counter =1;
		for (Entry<Id<Link>, double[]> entry: linkVolume.entrySet()) {
			String savePath=inputPath+"departuresPerHour_linkID_"+counter+".png";
			linkVolumeHandler1.writeChart(entry.getKey(),savePath);
			counter++;
			if (counter>5) {
				break;
			}
		}
		
		String[] title = {"linkID", "Volume"};
		List<String[]> entries = new ArrayList<>();
		entries.add(title);
		
		String[] linkIDWIthout_AB = new String[linkVolume.size()];
		double[] linkVolumeWIthout_AB = new double[linkVolume.size()];
        int counter1 =0;
		for (Entry<Id<Link>, double[]> entry: linkVolume.entrySet()) {
			String linkIDWithAB = entry.getKey().toString();
			String linkIDWithoutAB = linkIDWithAB.replaceAll("_AB", "");
			String linkIDWithoutABAndBA=linkIDWithoutAB.replaceAll("_BA", "");
			linkIDWIthout_AB[counter1]=linkIDWithoutABAndBA;
			linkVolumeWIthout_AB[counter1] = Arrays.stream(entry.getValue()).sum();
			counter1++;
		}

		String[] linkIDUnique = Arrays.stream(linkIDWIthout_AB).distinct().toArray(String[]::new);
		
		for  (String eachLinkID : linkIDUnique) {
			double linkVolumeOutput =0;
			// it should only have 2 index due to AB and BA
			int IDindexFirst = Arrays.asList(linkIDWIthout_AB).indexOf(eachLinkID);
			int IDindexLast = Arrays.asList(linkIDWIthout_AB).lastIndexOf(eachLinkID);
			
			if (IDindexFirst-IDindexFirst==0) {
				linkVolumeOutput=linkVolumeWIthout_AB[IDindexFirst];
			} else {
				linkVolumeOutput=linkVolumeWIthout_AB[IDindexFirst]+linkVolumeWIthout_AB[IDindexLast];
			}
			String[] element = {eachLinkID, Double.toString(linkVolumeOutput)};
			entries.add(element);
		}
		
		
        

        try (var fos = new FileOutputStream(outputFile); 
             var osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             var writer = new CSVWriter(osw)) {

            writer.writeAll(entries);
        }


		System.out.println("Events file read!");

	}

}
