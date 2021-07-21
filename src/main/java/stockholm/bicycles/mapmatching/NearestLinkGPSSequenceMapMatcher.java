package stockholm.bicycles.mapmatching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import stockholm.bicycles.utility.LinearRegrerssion;


public class NearestLinkGPSSequenceMapMatcher implements GPSSequenceMapMatcher{
	private static Logger logger = Logger.getLogger(NearestLinkGPSSequenceMapMatcher.class);
	protected final Network network;
	protected final GPSSequence gpsSequence;
	protected final TravelDisutility travelCosts;
	protected Node startNode;




	public NearestLinkGPSSequenceMapMatcher(Network network,GPSSequence gpsSequence,TravelDisutility travelCosts) {
		super();
		this.network=network;
		this.gpsSequence=gpsSequence;
		this.travelCosts = travelCosts;

		// get start and end node
		List<GPSPoint> points = this.gpsSequence.getGPSPoints(); // note that GPSPoints already have a sequence
		GPSPoint startPoint = points.get(0);
		Link candidateStartLink = NetworkUtils.getNearestLinkExactly(this.network, startPoint.getCoord());
		double distancep1 = NetworkUtils.getEuclideanDistance(candidateStartLink.getFromNode().getCoord(), startPoint.getCoord());
		double distancep2 = NetworkUtils.getEuclideanDistance(candidateStartLink.getToNode().getCoord(), startPoint.getCoord());
		if (distancep1<=distancep2) {
			this.startNode = candidateStartLink.getFromNode();
		} else {
			this.startNode = candidateStartLink.getToNode();
		}


	}



	// main method to do map-matching
	@Override
	public Path mapMatching() {	
		// 1. get all possible middle nodes

		// an index that marks number of GPSPoint that has been matched to a certain link. 
		// it always starts from 0 and 0 means that first point has been matched.
		int currentGPSPointIndex=0; 
		List<GPSPoint> points = this.gpsSequence.getGPSPoints();
		// initiliaze the links and node list for the construction of final path
		ArrayList<Link> pathLinks = new ArrayList<Link>();
		ArrayList<Node> pathNodes = new ArrayList<Node>();
		pathNodes.add(this.startNode);


		// add the first link into the pathLinks list.

		currentGPSPointIndex=findNextNearestLink(pathNodes,pathLinks,currentGPSPointIndex);

		boolean exitFlag=false;
		while (exitFlag==false) {
			int nextGPSPointIndex = findNextNearestLink(pathNodes,pathLinks,currentGPSPointIndex);
			if (nextGPSPointIndex==(points.size()-1)) {
				exitFlag=true;
			}
			if (nextGPSPointIndex==-1) {
				exitFlag=true;
				logger.warn("there is no outer links from the node: "+pathNodes.get(pathNodes.size()-1).getId().toString()+".");
				return null;
			}
			currentGPSPointIndex=nextGPSPointIndex;
		}


		// construct the path from links and nodes
		return (new Path(pathNodes, pathLinks, 0, 0));

	}

	private int findNextNearestLink(ArrayList<Node> pathNodes, ArrayList<Link> pathLinks, int currentPointIndex) {

		Map<Id<Link>, ? extends Link> outlinks = pathNodes.get(pathNodes.size()-1).getOutLinks();

		String oppositeLastLinkId=null;
		if (pathLinks.size()>0) {
			oppositeLastLinkId = getOppositeLinkID(pathLinks.get(pathLinks.size()-1).getId().toString());
		}

		// create a hashMap candidateLinks to store the link and its corresponding GPSPoint index (the location of GPSPoint)
		List<Link> candidateLinks = new ArrayList<Link>();
		List<Integer> candidateLinkGPSIndexList = new ArrayList<Integer>();

		// we loop all links from the node and check if any of the link is forwarding towards the GPSPoints direction, save them in candidateLinks 
		for (Link outlink:outlinks.values()) {

			String linkID= outlink.getId().toString();
			if (!linkID.equals(oppositeLastLinkId)) {
				Coord outLinkOutNodeCoord = outlink.getToNode().getCoord();
				int candidateGPSPointIndex = getNearestGPSPointIndex(outLinkOutNodeCoord);
				if (candidateGPSPointIndex>=currentPointIndex) {
					candidateLinks.add(outlink);
					candidateLinkGPSIndexList.add(candidateGPSPointIndex);

				}
			}
		}

		
		// loop each link in candidateLinks and calculate which one has the closest distance to all GPS points along the way.
		double highestScoreToLink=Double.NEGATIVE_INFINITY;
		int currentPointIndexOutput=currentPointIndex;
		Link currentMinimumDistanceLink=null;
		for (int i=0; i<=(candidateLinks.size()-1);i++) {
			// calculate the average distance from GPS points (from currentPointIndex to candidateGPSPointIndex) to the link
			Link candidateLink = candidateLinks.get(i);
			int candidateGPSPointIndex = candidateLinkGPSIndexList.get(i);
			// we need to check if there are any outlinks from the given candidate link
			TreeMap<Double, Link> nextOutLinks = NetworkUtils.getOutLinksSortedClockwiseByAngle(candidateLink);
			if (nextOutLinks.size()>0) {
				double aveargeDistanceToLink=averageEuclideanDistanceToLink(candidateLink,currentPointIndex,candidateGPSPointIndex);
				double angelDiff=angelDifferenceToLink(candidateLink,currentPointIndex,candidateGPSPointIndex);
				NormalDistribution normalDistributionDistance = new NormalDistribution(0,20.0);  // use 50 meter standard deviation
				NormalDistribution normalDistributionAngel = new NormalDistribution(0,45.0);  // use 20 å standard deviation
				double currentScore=normalDistributionDistance.logDensity(aveargeDistanceToLink)+normalDistributionAngel.logDensity(angelDiff);
				if (currentScore>=highestScoreToLink) {
					highestScoreToLink=currentScore;
					currentMinimumDistanceLink=candidateLink;
					currentPointIndexOutput=candidateGPSPointIndex;
				}
			}

		}

		if (currentMinimumDistanceLink!=null) {
			System.out.println("next link is: " +currentMinimumDistanceLink.getId().toString());
			System.out.println("next node is: " +currentMinimumDistanceLink.getToNode().getId().toString());
			pathLinks.add(currentMinimumDistanceLink);
			pathNodes.add(currentMinimumDistanceLink.getToNode());
		} else {
			return -1;
		}

		return currentPointIndexOutput;
	}





	private static String getOppositeLinkID(String linkId) {
		String linkId_opposite=null;
		if (linkId.contains("_AB")) {
			linkId_opposite = linkId.replaceAll("_AB", "_BA");
		} else if (linkId.contains("_BA")) {
			linkId_opposite = linkId.replaceAll("_BA", "_AB");
		}
		return linkId_opposite;
	}

	private double angelDifferenceToLink(Link candidateLink, int currentPointIndex, int candidateGPSPointIndex) {
		
		// calculate the theta value for the GPS points
		
		List<GPSPoint> points = this.gpsSequence.getGPSPoints();
		int startGPSPointIndex=currentPointIndex-2;
		if (currentPointIndex<2) {
			startGPSPointIndex=0;
		}
		int endGPSPointIndex=candidateGPSPointIndex+2;
		if (endGPSPointIndex>=(points.size()-1)) {
			endGPSPointIndex=points.size()-1;
		}
		int totalNumberOfGPSPoints=endGPSPointIndex-startGPSPointIndex+1;
		double[] x= new double[totalNumberOfGPSPoints];
		double[] y= new double[totalNumberOfGPSPoints];
		int counter=0;
		for (int i=startGPSPointIndex;i<=endGPSPointIndex;i++) {
			x[counter]=points.get(i).getCoord().getX();
			y[counter]=points.get(i).getCoord().getY();
			counter++;
		}
		
		LinearRegrerssion lm= new LinearRegrerssion(x,y);
		double slope = lm.getSlope();
		
		boolean xdiffPositive=directionCheck(x);
		boolean ydiffPositive=directionCheck(y);
		
		double GPSPointThetainAngel;
		if (slope>=1) {
			if (ydiffPositive==true) {
				GPSPointThetainAngel=Math.atan(slope)/Math.PI*180;
			} else {
				GPSPointThetainAngel=(Math.atan(slope)+Math.PI)/Math.PI*180;
			}
		} else if (slope<=1 & slope>=0) {
			if (xdiffPositive==true) {
				GPSPointThetainAngel=Math.atan(slope)/Math.PI*180;
			} else {
				GPSPointThetainAngel=(Math.atan(slope)+Math.PI)/Math.PI*180;
			}	
		} else if (slope<=0 & slope>=-1) {
			if (xdiffPositive==true) {
				GPSPointThetainAngel=(Math.atan(slope)+2*Math.PI)/Math.PI*180;
			} else {
				GPSPointThetainAngel=(Math.atan(slope)+Math.PI)/Math.PI*180;
			}	
			
		} else  { // if (slope<=-1)
			if (ydiffPositive==true) {
				GPSPointThetainAngel=(Math.atan(slope)+Math.PI)/Math.PI*180;
			} else {
				GPSPointThetainAngel=(Math.atan(slope)+2*Math.PI)/Math.PI*180;
			}
		}
		
		double linkThetaAngel;
		double linkSlope = (candidateLink.getToNode().getCoord().getY()-candidateLink.getFromNode().getCoord().getY())/(candidateLink.getToNode().getCoord().getX()-candidateLink.getFromNode().getCoord().getX());
		boolean xdiffLinkPositive=candidateLink.getToNode().getCoord().getX()-candidateLink.getFromNode().getCoord().getX()>=0;
		boolean ydiffLinkPositive=candidateLink.getToNode().getCoord().getY()-candidateLink.getFromNode().getCoord().getY()>=0;
		
		if (linkSlope>=1) {
			if (ydiffLinkPositive==true) {
				linkThetaAngel=Math.atan(linkSlope)/Math.PI*180;
			} else {
				linkThetaAngel=(Math.atan(linkSlope)+Math.PI)/Math.PI*180;
			}
		} else if (linkSlope<=1 & linkSlope>=0) {
			if (xdiffLinkPositive==true) {
				linkThetaAngel=Math.atan(linkSlope)/Math.PI*180;
			} else {
				linkThetaAngel=(Math.atan(linkSlope)+Math.PI)/Math.PI*180;
			}	
		} else if (linkSlope<=0 & linkSlope>=-1) {
			if (xdiffLinkPositive==true) {
				linkThetaAngel=(Math.atan(linkSlope)+2*Math.PI)/Math.PI*180;
			} else {
				linkThetaAngel=(Math.atan(linkSlope)+Math.PI)/Math.PI*180;
			}	
			
		} else  { // if (linkSlope<=-1)
			if (ydiffLinkPositive==true) {
				linkThetaAngel=(Math.atan(linkSlope)+Math.PI)/Math.PI*180;
			} else {
				linkThetaAngel=(Math.atan(linkSlope)+2*Math.PI)/Math.PI*180;
			}
		}
		
		// System.out.println("angel for link: " + candidateLink.getId()+" is: link angel: "+ linkThetaAngel + ". and GPSPoint angel: " + GPSPointThetainAngel);
		double angelDiff=Math.abs(linkThetaAngel-GPSPointThetainAngel);
		if (angelDiff>=270) {
			angelDiff=360-angelDiff;
		}
		return angelDiff;
	}

	private boolean directionCheck(double[] x) {
		// TODO Auto-generated method stub
		int numberPositive=0;
		int numberNegative=0;
		for (int i=1;i<x.length;i++) {
			if (x[i]-x[i-1]>0) {
				numberPositive++;
			} else if (x[i]-x[i-1]<0) {
				numberNegative++;
			}
		}
		
		if (numberPositive>=numberNegative) {
			return true;
		} else {
			return false;
		}
		
	}



	private double averageEuclideanDistanceToLink(Link candidateLink, int currentPointIndex, int candidateGPSPointIndex) {
		List<GPSPoint> points = this.gpsSequence.getGPSPoints();
		double sumDistance=0;
		int counter=0;
		for (int i=(currentPointIndex-2);i<=(candidateGPSPointIndex+2);i++) {
			if(i>=0 & i<points.size()) {
				sumDistance=sumDistance+distanceFromPointsToLine(points.get(i).getCoord(),candidateLink);
				counter++;
			}

		}
		return sumDistance/counter;

	}



	private double distanceFromPointsToLine(Coord coord, Link candidateLink) {
		double x=coord.getX();
		double y=coord.getY();
		double x1=candidateLink.getFromNode().getCoord().getX();
		double y1=candidateLink.getFromNode().getCoord().getY();
		double x2=candidateLink.getToNode().getCoord().getX();
		double y2=candidateLink.getToNode().getCoord().getY();

		double A = x - x1;
		double B = y - y1;
		double C = x2 - x1;
		double D = y2 - y1;

		double dot = A * C + B * D;
		double len_sq = C * C + D * D;
		double param = -1;

		if(len_sq!=0) {
			param = dot / len_sq;
		}

		double xx=0;
		double yy=0;

		if (param < 0) {
			xx = x1;
			yy = y1;
		}
		else if (param > 1) {
			xx = x2;
			yy = y2;
		}
		else {
			xx = x1 + param * C;
			yy = y1 + param * D;
		}

		double dx = x - xx;
		double dy = y - yy;

		return Math.sqrt(dx * dx + dy * dy);
	}



	private int getNearestGPSPointIndex(Coord coord) {
		List<GPSPoint> points = this.gpsSequence.getGPSPoints();
		double currentMinimumDistance=Double.POSITIVE_INFINITY;
		int currentGPSPointIndex=0;
		for (GPSPoint point:points) {
			if (NetworkUtils.getEuclideanDistance(coord, point.getCoord())<currentMinimumDistance) {
				currentMinimumDistance=NetworkUtils.getEuclideanDistance(coord, point.getCoord());
				currentGPSPointIndex=points.indexOf(point);
			}
		}

		return currentGPSPointIndex;

	}



	public Network getNetwork() {
		return network;
	}


	public GPSSequence getGpsSequence() {
		return gpsSequence;
	}


	public TravelDisutility getTravelCosts() {
		return travelCosts;
	}


}

