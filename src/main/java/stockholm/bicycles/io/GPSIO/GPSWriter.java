package stockholm.bicycles.io.GPSIO;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import stockholm.bicycles.importnetwork.StockholmTransformationFactory;
import stockholm.bicycles.mapmatching.GPSPoint;
import stockholm.bicycles.mapmatching.GPSSequence;
import stockholm.bicycles.mapmatching.GPSSequenceWithDistanceToMatchedPath;
import stockholm.bicycles.utility.CsvWriter;

public class GPSWriter {

	public void write(List<GPSSequence> gpsSequences, String filePath) throws Exception {
		// output to csv file
		List<String[]> outputStringList= new ArrayList<String[]>();
		// segment_id,mode,timestamp,lat,lon,delta_m,delta_s,kmh
		String[] header= new String[]{"segment_id", "mode", "timestamp","lat","lon","delta_m","delta_s","kmh"};
		outputStringList.add(header);

		final CoordinateTransformation coordinateTransform = StockholmTransformationFactory.getCoordinateTransformation(
				StockholmTransformationFactory.WGS84_SWEREF99, StockholmTransformationFactory.WGS84);		
		for (int k=0;k<gpsSequences.size();k++) {
			GPSSequence gpsSequence=gpsSequences.get(k);
			List<GPSPoint> points = gpsSequence.getGPSPoints();
			int counter=1;
			double currentTime=0;
			double currentDistance=0;
			for (GPSPoint point: points) {
				String segment_id = gpsSequence.getPersonID().toString();
				String mode=gpsSequence.getMode();

				if (counter==1) {
					currentTime=point.getTimeStamp().seconds();
					currentDistance=point.getDelta_m();
				}

				String timeStamp = Double.toString(point.getTimeStamp().seconds());
				Coord coord = point.getCoord();
				Coord coordWGS84 = coordinateTransform.transform(coord);
				String longtitude = Double.toString(coordWGS84.getX());
				String latitude = Double.toString(coordWGS84.getY());
				String delta_m = Double.toString(point.getDelta_m()-currentDistance);
				String delta_s = Double.toString(point.getTimeStamp().seconds()-currentTime);
				String speed=Double.toString(point.getSpeed());
				String[] newLine= new String[]{segment_id, mode, timeStamp,latitude,longtitude,delta_m,delta_s,speed};
				outputStringList.add(newLine);
				currentTime=point.getTimeStamp().seconds();
				currentDistance=point.getDelta_m();
				counter++;
			}
		}
		CsvWriter.write(outputStringList, filePath);


	}

	public void writeWithDistanceToLink (List<GPSSequenceWithDistanceToMatchedPath> gpsSequences, String filePath) throws Exception {
		// output to csv file
		List<String[]> outputStringList= new ArrayList<String[]>();
		// segment_id,mode,timestamp,lat,lon,delta_m,delta_s,kmh
		String[] header= new String[]{"segment_id", "mode", "timestamp","lat","lon","delta_m","delta_s","kmh","distanceToNearestLink","nearestLinkID"};
		outputStringList.add(header);

		final CoordinateTransformation coordinateTransform = StockholmTransformationFactory.getCoordinateTransformation(
				StockholmTransformationFactory.WGS84_SWEREF99, StockholmTransformationFactory.WGS84);		
		for (int k=0;k<gpsSequences.size();k++) {
			GPSSequenceWithDistanceToMatchedPath gpsSequence=gpsSequences.get(k);
			List<GPSPoint> points = gpsSequence.getGPSPoints();
			List<Double> distanceToNearestLinks = gpsSequence.getDistanceToNearestLink();
			List<String> nearestLinkIDs = gpsSequence.getNearestLinkID();
			int counter=1;
			double currentTime=0;
			double currentDistance=0;
			for (int i=0;i<points.size();i++) {
				GPSPoint point=points.get(i);
				String distanceToNearestLink = Double.toString(distanceToNearestLinks.get(i));
				String nearestLinkID = nearestLinkIDs.get(i);
				String segment_id = gpsSequence.getPersonID().toString();
				String mode=gpsSequence.getMode();

				if (counter==1) {
					currentTime=point.getTimeStamp().seconds();
					currentDistance=point.getDelta_m();
				}

				String timeStamp = Double.toString(point.getTimeStamp().seconds());
				Coord coord = point.getCoord();
				Coord coordWGS84 = coordinateTransform.transform(coord);
				String longtitude = Double.toString(coordWGS84.getX());
				String latitude = Double.toString(coordWGS84.getY());
				String delta_m = Double.toString(point.getDelta_m()-currentDistance);
				String delta_s = Double.toString(point.getTimeStamp().seconds()-currentTime);
				String speed=Double.toString(point.getSpeed());
				String[] newLine= new String[]{segment_id, mode, timeStamp,latitude,longtitude,delta_m,delta_s,speed,distanceToNearestLink,nearestLinkID};
				outputStringList.add(newLine);
				currentTime=point.getTimeStamp().seconds();
				currentDistance=point.getDelta_m();
				counter++;
			}
		}
		CsvWriter.write(outputStringList, filePath);


	}

}
