package stockholm.bicycles.imprtGPS;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.misc.OptionalTime;

import com.google.common.collect.Table;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.importnetwork.StockholmTransformationFactory;
import stockholm.bicycles.mapmatching.GPSPoint;
import stockholm.bicycles.mapmatching.GPSSequence;
import stockholm.bicycles.utility.CsvReaderToIteratable;

public class GPSReader {
	private static final Logger log = Logger.getLogger( GPSReader.class ) ;
	private final String GPSCsvFileName;
	private int personIDLocation=-1;
	private int modeLocation=-1;
	private int latPosition=-1;
	private int longPosition;
	private int deltaDistancePosition=-1;
	private int deltaTimePosition=-1;
	private int speedPosition=-1;
	
	public GPSReader(String GPSCsvFileName) throws IOException, CsvException{
		this.GPSCsvFileName=GPSCsvFileName;

		
		Reader reader = Files.newBufferedReader(Paths.get(this.GPSCsvFileName),Charset.forName("ISO-8859-1"));
	    CSVParser parser = new CSVParserBuilder()
	        .withSeparator(',')
	        .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
	        .withIgnoreLeadingWhiteSpace(true)
	        .build();
	    CSVReader csvReader = new CSVReaderBuilder(reader)
	            .withCSVParser(parser)
	            .build();
	     String[] header = csvReader.readNext();
	
	     
	     for (int i=0; i<header.length;i++) {
	    	if( header[i].equals("segment_id")) {
	    		this.personIDLocation=i;
	    	}
	    	if( header[i].equals("mode")) {
	    		this.modeLocation=i;
	    	}
	    	if( header[i].equals("lat")) {
	    		this.latPosition=i;
	    	}
	    	if( header[i].equals("lon")) {
	    		this.longPosition=i;
	    	}
	    	if( header[i].equals("delta_m")) {
	    		this.deltaDistancePosition=i;
	    	}
	    	if( header[i].equals("delta_s")) {
	    		this.deltaTimePosition=i;
	    	}
	    	if( header[i].equals("kmh")) {
	    		this.speedPosition=i;
	    	}	
	     }  
	     csvReader.close();
		 reader.close();
	     
	     if (this.personIDLocation==-1 | this.modeLocation==-1 | this.latPosition==-1 | 
	    	 this.longPosition==-1 | this.deltaDistancePosition==-1 | this.deltaTimePosition==-1 | this.speedPosition==-1) {
	    	 throw new RuntimeException( "please check the input csv header, it must include: segment_id,mode,lat,lon,delta_m,delta_s,kmh.") ;
	     }
	    
	}

	public List<GPSSequence> read(int radius) throws IOException, CsvException {
		CsvReaderToIteratable GPSDataReader = new CsvReaderToIteratable(this.GPSCsvFileName,',');
		List<String[]> records= GPSDataReader.readTable();
		String[] header=records.get(0);			
		records.remove(0);
		
		int numberOfRows=records.size();
		String currentTripId="";
		final CoordinateTransformation coordinateTransform = StockholmTransformationFactory.getCoordinateTransformation(
				StockholmTransformationFactory.WGS84, StockholmTransformationFactory.WGS84_SWEREF99);
		List<GPSSequence> gpsSequence = new ArrayList<GPSSequence>();
		List<GPSPoint> gpsPoints = new ArrayList<GPSPoint>();
		int personTripCounter=0;
		double sum_m=0;
		double sum_s=0;
		for (int i =0; i<(numberOfRows);i++) {
			String[] record=records.get(i);
			String TripId=record[this.personIDLocation];
			String currentMode=record[this.modeLocation];
			double latitude = Double. parseDouble(record[this.latPosition]);
			double longtitude = Double. parseDouble(record[this.longPosition]);
			double delta_m = Double. parseDouble(record[this.deltaDistancePosition]);
			double delta_s = Double. parseDouble(record[this.deltaTimePosition]);
			double speed = Double. parseDouble(record[this.speedPosition]);
			Coord coord = coordinateTransform.transform(new Coord(longtitude, latitude));
			// the sum of distance and time	
			if (i==0) {  // first record
				currentTripId=TripId;
			    OptionalTime time = OptionalTime.defined(0);
			    GPSPoint point = new GPSPoint(coord,time,0,radius,speed);
			    gpsPoints.add(point);
			} else if(i<(numberOfRows-1) & TripId.equals(currentTripId)) { // the next trip id
				sum_m=sum_m+delta_m;
				sum_s=sum_s+delta_s;
				OptionalTime time = OptionalTime.defined(sum_s);
			    GPSPoint point = new GPSPoint(coord,time,sum_m,radius,speed);
			    gpsPoints.add(point);
			} else if (i<(numberOfRows-1) & (!TripId.equals(currentTripId))) {
				// add the current GPS sequence
				Id<Person> personID = Id.create(currentTripId, Person.class);
				gpsSequence.add(new GPSSequence(personID,gpsPoints,currentMode));
				personTripCounter++;
				System.out.println(personTripCounter+": "+"GPS sequence for person-trip: "+ personID.toString()+" is loaded.");
				
				sum_m=0;
				sum_s=0;
				gpsPoints = new ArrayList<GPSPoint>();
				currentTripId=TripId;
				OptionalTime time = OptionalTime.defined(0);
			    GPSPoint point = new GPSPoint(coord,time,0,radius,speed);
			    gpsPoints.add(point);
			} else if (i==(numberOfRows-1)) {
				sum_m=sum_m+delta_m;
				sum_s=sum_s+delta_s;
				OptionalTime time = OptionalTime.defined(sum_s);
			    GPSPoint point = new GPSPoint(coord,time,sum_m,radius,speed);
			    gpsPoints.add(point);
			    Id<Person> personID = Id.create(currentTripId, Person.class);
				gpsSequence.add(new GPSSequence(personID,gpsPoints,currentMode));
				personTripCounter++;
				System.out.println(personTripCounter+": "+"GPS sequence for person-trip: "+ personID.toString()+" is loaded.");
			}
			
			

		}
		return gpsSequence;
	}
	
	

}
