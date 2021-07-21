package stockholm.bicycles.io.pathIO;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.scenario.ScenarioUtils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;

import stockholm.bicycles.utility.CsvReaderToIteratable;
import stockholm.bicycles.utility.mapMatchningUtil.PathUtils;


public class PathReader {
	private static final Logger log = Logger.getLogger( PathReader.class ) ;
	private final String pathFileName;
	protected final Network network;
	private int counterIDLocation=-1;
	private int linkIDLocation=-1;
	private int tripIDLocation=-1;
	
	public PathReader(String pathFileName, String networkFileName) throws IOException, CsvException {
		super();
		this.pathFileName = pathFileName;
		
		// 1: read network
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		(new MatsimNetworkReader(scenario.getNetwork())).readFile(networkFileName);
		this.network = scenario.getNetwork();

		Reader reader = Files.newBufferedReader(Paths.get(this.pathFileName),Charset.forName("ISO-8859-1"));
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
	    	if( header[i].equals("counter")) {
	    		this.counterIDLocation=i;
	    	}
	    	if( header[i].equals("linkID")) {
	    		this.linkIDLocation=i;
	    	}
	    	if( header[i].equals("tripID")) {
	    		this.tripIDLocation=i;
	    	}
	     }  
	     csvReader.close();
		 reader.close();
	     
	     if (this.counterIDLocation==-1 | this.linkIDLocation==-1 | this.tripIDLocation==-1) {
	    	 throw new RuntimeException( "please check the input path csv header, it must include: counter,linkID,tripID.") ;
	     }	
	}
	
	
	public PathReader(String pathFileName, Network network) throws IOException, CsvException {
		super();
		this.pathFileName = pathFileName;
		this.network = network;

		Reader reader = Files.newBufferedReader(Paths.get(this.pathFileName),Charset.forName("ISO-8859-1"));
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
	    	if( header[i].equals("counter")) {
	    		this.counterIDLocation=i;
	    	}
	    	if( header[i].equals("linkID")) {
	    		this.linkIDLocation=i;
	    	}
	    	if( header[i].equals("tripID")) {
	    		this.tripIDLocation=i;
	    	}
	     }  
	     csvReader.close();
		 reader.close();
	     
	     if (this.counterIDLocation==-1 | this.linkIDLocation==-1 | this.tripIDLocation==-1) {
	    	 throw new RuntimeException( "please check the input path csv header, it must include: counter,linkID,tripID.") ;
	     }	
	}	
	
	
	

	public HashMap<String,Path> read() throws IOException, CsvException {
		
		
		// 2: read the path csv file
		// the path csv file should have the following structure
//		counter,linkID,tripID
//		1,      499778,601150
//		2,      154743,601150
//		3,      183461,601150

		CsvReaderToIteratable pathFileReader = new CsvReaderToIteratable(this.pathFileName,',');
		List<String[]> records= pathFileReader.readTable();

		
		
		// remove the header row
        records.remove(0);
		int numberOfRows=records.size();
		String currentTripId="";
		
		HashMap<String,Path> pathMap= new HashMap<String,Path>();
		ArrayList<String> linkIDList= new ArrayList<String>();
		for (int i =0; i<(numberOfRows);i++) {
			String[] record=records.get(i);
			String TripId=record[this.tripIDLocation];
			String linkId=record[this.linkIDLocation];
			
			if (i==0) {  // first record
				currentTripId=TripId;
				linkIDList.add(linkId);
			} else if(i<(numberOfRows-1) & TripId.equals(currentTripId)) { // the next link id for the given trip
				linkIDList.add(linkId);
			} else if (i<(numberOfRows-1) & (!TripId.equals(currentTripId))) { // the next trip id
				Path path = createPath(this.network,linkIDList);
				pathMap.put(currentTripId, path);
				
				linkIDList= new ArrayList<String>();
				currentTripId=TripId;
				linkIDList.add(linkId);
			} else if (i==(numberOfRows-1)) {
				linkIDList.add(linkId);
				Path path = createPath(this.network,linkIDList);
				pathMap.put(currentTripId, path);
			}
			
		}
		return pathMap;
	}

	private Path createPath(Network network, ArrayList<String> linkIDList) {
		Path path = new Path(new ArrayList<Node>(), new ArrayList<Link>(), 0, 0);
		Map<Id<Link>, ? extends Link> links = network.getLinks();
		if (linkIDList.size()==1) { // if there is only one link
			String nextLinkID=linkIDList.get(0);
			String nextLinkID_AB=nextLinkID+"_AB";
			Link nextLink = links.get(Id.create(nextLinkID_AB, Link.class));
			if (nextLink==null) {
				String nextLinkID_BA=nextLinkID+"_BA";
				nextLink = links.get(Id.create(nextLinkID_BA, Link.class));
			}
			return PathUtils.addLink(path, nextLink);
			// NetworkUtils.getLinks(network, linkIds)
		}
		
		// for the case where there are several links
		// loop from 2nd to the last link to add the link into linkList
		ArrayList<Link> linkList= new ArrayList<Link>();
		for (int i=1;i<linkIDList.size();i++) {
			// for the first link we need to determine the direction
			if (i==1) {
				String currentLinkID = linkIDList.get(0);
				String nextLinkID = linkIDList.get(1);
				
				String currentLinkID_AB=currentLinkID+"_AB";
				String currentLinkID_BA=currentLinkID+"_BA";
				String nextLinkID_AB=nextLinkID+"_AB";
				String nextLinkID_BA=nextLinkID+"_BA";
				
				Link currentLink_AB = links.get(Id.create(currentLinkID_AB, Link.class));
				Link currentLink_BA = links.get(Id.create(currentLinkID_BA, Link.class));
				boolean foundLink=false;
				if (currentLink_AB!=null) {
					// TreeMap<Double, Link> outLinks = NetworkUtils.getOutLinksSortedClockwiseByAngle(currentLink_AB);
					ArrayList<Link> outLinks = PathUtils.getOutLinks(currentLink_AB);
					for (Link outLink: outLinks) {
						String outLinkID = outLink.getId().toString();
						// check if nextLinkID_AB or nextLinkID_BA is in the outLink
						if (outLinkID.equals(nextLinkID_AB) | outLinkID.equals(nextLinkID_BA)) {
							linkList.add(currentLink_AB);
							linkList.add(outLink);
							foundLink=true;
							break;
						} 
					}
				}
				
				if (foundLink==false & currentLink_BA!=null) {
					//  TreeMap<Double, Link> outLinks = NetworkUtils.getOutLinksSortedClockwiseByAngle(currentLink_BA);
					ArrayList<Link> outLinks = PathUtils.getOutLinks(currentLink_BA);
					for (Link outLink: outLinks) {
						String outLinkID = outLink.getId().toString();
						// check if nextLinkID_AB or nextLinkID_BA is in the outLink
						if (outLinkID.equals(nextLinkID_AB) | outLinkID.equals(nextLinkID_BA)) {
							linkList.add(currentLink_BA);
							linkList.add(outLink);
							foundLink=true;
							break;
						} 
					}
				}
				
				if (foundLink==false) {
					throw new IllegalArgumentException("following links cannot be found in the linkList, current link ID: " 
				+ currentLinkID + " and downstream link ID: " + nextLinkID+" .");
				}
				
			}
			
			// for the 3rd to last link
			if (i>1) {
				Link lastLink = linkList.get(linkList.size()-1);
				String nextLinkID = linkIDList.get(i);
				String nextLinkID_AB=nextLinkID+"_AB";
				String nextLinkID_BA=nextLinkID+"_BA";
				
				boolean foundLink=false;
				// TreeMap<Double, Link> outLinks = NetworkUtils.getOutLinksSortedClockwiseByAngle(lastLink);
				ArrayList<Link> outLinks = PathUtils.getOutLinks(lastLink);
				for (Link outLink: outLinks) {
					String outLinkID = outLink.getId().toString();
					// check if nextLinkID_AB or nextLinkID_BA is in the outLink
					if (outLinkID.equals(nextLinkID_AB) | outLinkID.equals(nextLinkID_BA)) {
						linkList.add(outLink);
						foundLink=true;
						break;
					} 
				}
				if (foundLink==false) {
					throw new IllegalArgumentException("following links cannot be found in the linkList, current link ID: " 
				+ lastLink.getId()+" and downstream link ID: "+nextLinkID +" .");
				}
				
			}	
		}  // end the loop of all linkIDs
		
		return PathUtils.addLinks(path, linkList);

		
	}


}
