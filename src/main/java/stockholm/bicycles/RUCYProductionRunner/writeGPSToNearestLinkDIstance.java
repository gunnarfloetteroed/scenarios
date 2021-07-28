package stockholm.bicycles.RUCYProductionRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import stockholm.bicycles.io.GPSIO.GPSReader;
import stockholm.bicycles.io.GPSIO.GPSWriter;
import stockholm.bicycles.io.pathIO.PathReader;
import stockholm.bicycles.mapmatching.GPSSequence;
import stockholm.bicycles.mapmatching.GPSSequenceWithDistanceToMatchedPath;
import stockholm.bicycles.mapmatching.mapmatchingstatistics.DistanceFromGPSPointsToNearestLinksData;
import stockholm.bicycles.utility.mapMatchningUtil.PathUtils;

public class writeGPSToNearestLinkDIstance {

	public static void main(String[] args) throws Exception {

		String inputGPSFileName="D:\\RUCY\\GPSData\\FinalGPSData/cykel_forMapMatching";
		String inputPathFileName="D:\\RUCY\\GPSData\\MapMatchingResults/LinkWeightsMethod_MapMatching";
		String inputNetworkFileName="D:\\RUCY\\network\\network_NVDB.xml";
		String writePathFileName="D:\\RUCY\\GPSData\\MapMatchingResults\\LinkWeightsMethod_GPSWithDistanceToNearestLink";
		
		int start=1;
		int end=10;
		for (int i=start;i<=end;i++) {
			String inputGPSFileName_i=inputGPSFileName+"_"+i+".csv";
			String inputPathFileName_i=inputPathFileName+"_"+i+".csv";
			String writePathFileName_i=writePathFileName+"_"+i+".csv";
			GPSToNearestLink config= new GPSToNearestLink(inputGPSFileName_i,inputPathFileName_i,inputNetworkFileName,writePathFileName_i);
			config.run();
		}
		
	}
	
}

class GPSToNearestLink {
	
	private String inputGPSFileName;
	private String inputPathFileName;
	private String inputNetworkFileName;
	private String writePathFileName;
	
	GPSToNearestLink(String inputGPSFileName, String inputPathFileName, String inputNetworkFileName, String writePathFileName) {
		super();
		this.inputGPSFileName = inputGPSFileName;
		this.inputPathFileName = inputPathFileName;
		this.writePathFileName = writePathFileName;
		this.inputNetworkFileName=inputNetworkFileName;
	}


	
	void run() throws Exception{
		GPSReader GpsReader = new GPSReader(inputGPSFileName);
		List<GPSSequence> GPSSequences = GpsReader.read(50);
		
		PathReader matchedPathReader = new PathReader( inputPathFileName,inputNetworkFileName);
		HashMap<String, Path> matchedPath = matchedPathReader.read();
		
		List<GPSSequenceWithDistanceToMatchedPath> savedData = new ArrayList<GPSSequenceWithDistanceToMatchedPath>();
		int NGPSSequence=GPSSequences.size();
		for (int i=0; i<NGPSSequence;i++) {
			GPSSequence gPSSequence = GPSSequences.get(0);
			String tripID = gPSSequence.getPersonID().toString();
			if (matchedPath.containsKey(tripID)) {
				Path path = matchedPath.get(tripID);
				DistanceFromGPSPointsToNearestLinksData distanceFromGPSPointsToNearestLinksData = PathUtils.distanceEachGPSPointPathToGPS(gPSSequence, path);
				
				GPSSequenceWithDistanceToMatchedPath gPSSequenceWithDistanceToMatchedPath = 
						new GPSSequenceWithDistanceToMatchedPath(
								gPSSequence,
								distanceFromGPSPointsToNearestLinksData.getDistanceToNearestLink(),
								distanceFromGPSPointsToNearestLinksData.getNearestLinkID());
				savedData.add(gPSSequenceWithDistanceToMatchedPath);
			}
			
			GPSSequences.remove(0);
		}
		GPSWriter gpsWriter = new GPSWriter();
		gpsWriter.writeWithDistanceToLink(savedData, writePathFileName);
		
	} // end run()
	
	
	
}
