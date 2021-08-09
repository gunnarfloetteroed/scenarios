package stockholm.bicycles.utility.mapMatchningUtil;


import java.util.ArrayList;
import java.util.List;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import stockholm.bicycles.mapmatching.GPSPoint;
import stockholm.bicycles.mapmatching.GPSSequence;
import stockholm.bicycles.mapmatching.GPSSequenceWithDistanceToMatchedPath;
import stockholm.bicycles.mapmatching.mapmatchingstatistics.DistanceFromGPSPointsToNearestLinksData;
import stockholm.bicycles.mapmatching.mapmatchingstatistics.PathValidationStatistics;

public final class PathUtils {

	public static Path addLink(Path path, Link link) {
		List<Link> linkList = path.links;
		List<Node> nodeList = path.nodes;
		linkList.add(link);
		if (nodeList.size()==0) {
			nodeList.add(link.getFromNode());
			nodeList.add(link.getToNode());
		}
		if (nodeList.size()>0) {
			nodeList.add(link.getToNode());
		}
		return new Path(nodeList,linkList,0,0);
	}

	public static Path addLinks(Path path, List<Link> links) {

		/**
		 * This method is used to add a list of links into existing path.
		 * @param path The existing path.
		 * @param links The list of links.
		 * @return Path The path with nodes and links added from links.
		 */


		List<Link> linkList = path.links;
		List<Node> nodeList = path.nodes;
		int n_linksToAppend = links.size();
		for(int i=0;i<n_linksToAppend;i++) {
			Link linktoAppend = links.get(i);
			linkList.add(linktoAppend);
			// if there is no node in the original path
			if (i==0 & nodeList.size()==0) {
				nodeList.add(linktoAppend.getFromNode());
				nodeList.add(linktoAppend.getToNode());
			} else {
				nodeList.add(linktoAppend.getToNode());
			}	
		}
		return new Path(nodeList,linkList,0,0);

	}

	public static ArrayList<Link> getOutLinks(Link link) {
		/**
		 * This method is used to get all downstream links from a given link's toNode.
		 * @param link The link of which its toNode will be used to generate the downstream links.
		 * @return ArrayList<Link> an arrayList with all downstream links.
		 */

		ArrayList<Link> outlinks= new ArrayList<Link>();
		//		 String fromNodeId = link.getFromNode().getId().toString();
		for (Link outLink : link.getToNode().getOutLinks().values()) {
			//			 String outLinkToNodeId = outLink.getToNode().getId().toString();
			//			 if (!outLinkToNodeId.equals(fromNodeId)) {
			//				 
			//			 }
			outlinks.add(outLink);
		}
		return outlinks;
	}

	public static PathValidationStatistics comparePath(Path groundTruthPath, Path mapmatchedPath) {
		/**
		 * This method is used to compare the map-matched path and the ground-truth path and generate the result statistics, e.g. how many links are overlapped.
		 * @param groundTruthPath The ground-truth path.
		 * @param mapmatchedPath The map-matchedPath.
		 * @return PathValidationStatistics An object holding the comparison statistics.
		 */
		
		if (groundTruthPath==null | mapmatchedPath==null) {
			return new PathValidationStatistics();
		}
		List<Link> groundTruthLinks = groundTruthPath.links;
		List<Link> mapMatchedLinks = mapmatchedPath.links;
		ArrayList<String> mapMatchedLinksID= new ArrayList<String>();
		for (Link mapMatchedLink: mapMatchedLinks) {
			mapMatchedLinksID.add(mapMatchedLink.getId().toString());
		}

		ArrayList<String> groundTruthLinksID= new ArrayList<String>();
		for (Link groundTruthLink: groundTruthLinks) {
			groundTruthLinksID.add(groundTruthLink.getId().toString());
		}

		// calculate the IARR, see map matching matsim paper, eq(1)
		double groundTruthLinksLength=0;
		double matchedLinksLength=0;
		for (Link groundTruthLink: groundTruthLinks) {
			if( mapMatchedLinksID.contains(groundTruthLink.getId().toString())) {
				matchedLinksLength=matchedLinksLength+groundTruthLink.getLength();
			}
			groundTruthLinksLength=groundTruthLinksLength+groundTruthLink.getLength();
		}


		// calculate the IARR, see map matching matsim paper, eq(2)
		double matchedPathLength=0;
		double incorrectMatchedLinksLength=0;
		for (Link mapMatchedLink: mapMatchedLinks) {
			if(!groundTruthLinksID.contains(mapMatchedLink.getId().toString())) {
				incorrectMatchedLinksLength=incorrectMatchedLinksLength+mapMatchedLink.getLength();
			}
			matchedPathLength=matchedPathLength+mapMatchedLink.getLength();
		}


		PathValidationStatistics statistics= new PathValidationStatistics();
		statistics.setPercentageLengthMatched(matchedLinksLength/groundTruthLinksLength);
		statistics.setPercentageLengthIncorrectMatched(incorrectMatchedLinksLength/matchedPathLength);
		return statistics;
	}


	public static double averageDistancePathToGPS(GPSSequence gpsSequence,Path path) {
		/**
		 * This method calculates the distance from a given path to a given GPS sequence, measured as the average of distance of each GPS point to its closest link in the path.
		 * @param gpsSequence The gpsSequence.
		 * @param path The path.
		 * @return double the average distance from GPS points to path.
		 */
		if (gpsSequence==null | path==null) {
			return -1;
		}
		// create a small network
		Network smallNetwork = NetworkUtils.createNetwork();
		List<Node> allNodes = path.nodes;
		for (Node node:allNodes) {
			if (!smallNetwork.getNodes().containsKey(node.getId())) {
				smallNetwork.addNode(node);
			}	
		}
		List<Link> allLinks = path.links;
		for (Link link:allLinks) {
			if (!smallNetwork.getLinks().containsKey(link.getId())) {
				smallNetwork.addLink(link);
			}
		}

		List<GPSPoint> points = gpsSequence.getGPSPoints();
		double sumDistance=0;
		for (GPSPoint point:points) {
			Link nearestLink = NetworkUtils.getNearestLinkExactly(smallNetwork, point.getCoord());
			sumDistance = sumDistance+ GPSUtils.distanceFromPointToLink(point.getCoord(), nearestLink);
		}
		smallNetwork=null;
		return sumDistance/points.size();
	}

	public static DistanceFromGPSPointsToNearestLinksData distanceEachGPSPointPathToGPS(GPSSequence gpsSequence,Path path) {
		/**
		 * This method calculates the distance from a given path to a given GPS sequence, measured as the average of distance of each GPS point to its closest link in the path.
		 * @param gpsSequence The gpsSequence.
		 * @param path The path.
		 * @return DistanceFromGPSPointsToNearestLinksData the object holding distance from each GPS point to its nearest link and the nearest link ID.
		 */
		
		if (gpsSequence==null | path==null) {
			return new DistanceFromGPSPointsToNearestLinksData();
		}
		List<Double> distanceToNearestLink = new ArrayList<Double>();
		List<String> nearestLinkID = new ArrayList<String>();


		// create a small network
		Network smallNetwork = NetworkUtils.createNetwork();
		List<Node> allNodes = path.nodes;
		for (Node node:allNodes) {
			if (!smallNetwork.getNodes().containsKey(node.getId())) {
				smallNetwork.addNode(node);
			}	
		}
		List<Link> allLinks = path.links;
		for (Link link:allLinks) {
			if (!smallNetwork.getLinks().containsKey(link.getId())) {
				smallNetwork.addLink(link);
			}
		}

		List<GPSPoint> points = gpsSequence.getGPSPoints();
		for (GPSPoint point:points) {
			Link nearestLink = NetworkUtils.getNearestLinkExactly(smallNetwork, point.getCoord());
			double distance = GPSUtils.distanceFromPointToLink(point.getCoord(), nearestLink);
			distanceToNearestLink.add(distance);
			nearestLinkID.add(nearestLink.getId().toString());
		}
		
		DistanceFromGPSPointsToNearestLinksData output = new DistanceFromGPSPointsToNearestLinksData();
		output.setDistanceToNearestLink(distanceToNearestLink);
		output.setNearestLinkID(nearestLinkID);
		smallNetwork=null;
		return output;

	}





}


