package stockholm.bicycles.mapmatching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.FastDijkstra;
import org.matsim.core.router.FastDijkstraFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;

import stockholm.bicycles.routing.MatsimDijkstra;

public class MySpatialTemporalGPSSequenceMapMatcher implements GPSSequenceMapMatcher {
	// the following variables exist in ShortestPathGPSSequenceMapMatcher.
	protected final Network network;
	protected final GPSSequence gpsSequence;
	protected final TravelDisutility travelCosts;
	protected Node startNode;
	protected Node endNode;
	private Map<Id<Node>,Double> costToMiddileNode = new HashMap<Id<Node>, Double>();
	private Map<Id<Node>,Id<Node>> previousMiddileNodes = new HashMap<Id<Node>, Id<Node>>();


	public MySpatialTemporalGPSSequenceMapMatcher(Network network, GPSSequence gpsSequence,
			TravelDisutility travelCosts) {
		super();
		this.network=network;
		this.gpsSequence=gpsSequence;
		this.travelCosts = travelCosts;

		// get start and end node
		List<GPSPoint> points = this.gpsSequence.getGPSPoints(); // note that GPSPoints already have a sequence
		int NPoints = points.size();
		GPSPoint startPoint = points.get(0);
		Link candidateStartLink = NetworkUtils.getNearestLinkExactly(this.network, startPoint.getCoord());
		double distancep1 = NetworkUtils.getEuclideanDistance(candidateStartLink.getFromNode().getCoord(), startPoint.getCoord());
		double distancep2 = NetworkUtils.getEuclideanDistance(candidateStartLink.getToNode().getCoord(), startPoint.getCoord());
		if (distancep1<=distancep2) {
			this.startNode = candidateStartLink.getFromNode();
		} else {
			this.startNode = candidateStartLink.getToNode();
		}


		GPSPoint endPoint = points.get(NPoints-1);
		Link candidateEndLink = NetworkUtils.getNearestLinkExactly(this.network, endPoint.getCoord());
		distancep1 = NetworkUtils.getEuclideanDistance(candidateEndLink.getFromNode().getCoord(), endPoint.getCoord());
		distancep2 = NetworkUtils.getEuclideanDistance(candidateEndLink.getToNode().getCoord(), endPoint.getCoord());
		if (distancep1<=distancep2) {
			this.endNode = candidateEndLink.getFromNode();
		} else {
			this.endNode = candidateEndLink.getToNode();
		}
	}


	// main method to do map-matching
	@Override
	public Path mapMatching() {	
		// 1. get all possible middle nodes
		List<Collection<Node>> searchableMiddleNodes = getSearchedNodes();

		// do some logic to generate most probable path
		Path path=DijkstraThroughMidNodes(searchableMiddleNodes);

		return path;

	}


	protected List<Collection<Node>> getSearchedNodes(){
		List<GPSPoint> points = this.gpsSequence.getGPSPoints(); // note that GPSPoints already have a sequence
		int NPoints = points.size();
		// important to remember that you should call NetworkUtils to find nearest nodes and other stuffs.
		List<Collection<Node>> searchableMiddleNodes = new ArrayList<Collection<Node>>();
		List<String> nodeIDs= new ArrayList<String>();
		for (int i=0;i<NPoints;i++) { // loop first to last node
			GPSPoint midPoint = points.get(i);
			Collection<Node> nodes = NetworkUtils.getNearestNodes(this.network, midPoint.getCoord(), midPoint.getNodeSearchRadius());
			if (i==0) {
				Collection<Node> validNodes = new ArrayList<Node>();
				validNodes.add(this.startNode);
				nodeIDs.add(this.startNode.getId().toString());
				nodeIDs.add(this.endNode.getId().toString());
				searchableMiddleNodes.add(validNodes);
			} else if (i==(NPoints-1)) {
				Collection<Node> validNodes= new ArrayList<Node>();
				validNodes.add(this.endNode);
				searchableMiddleNodes.add(validNodes);
			} else {
				Collection<Node> validNodes = new ArrayList<Node>();
				for (Node node:nodes) {
					if (!nodeIDs.contains(node.getId().toString())) {
						validNodes.add(node);
						nodeIDs.add(node.getId().toString());
					}
				}
				searchableMiddleNodes.add(validNodes);
			}

		}
		return searchableMiddleNodes;

	}


	protected Path DijkstraThroughMidNodes(List<Collection<Node>> searchableMiddleNodes) {
		FastDijkstraFactory fastDijkstraFactory = new FastDijkstraFactory();
		FastDijkstra dijkstraRouter = (FastDijkstra) fastDijkstraFactory.createPathCalculator(this.network, this.travelCosts, new FreeSpeedTravelTime());
		// MatsimDijkstra dijkstraRouter = new MatsimDijkstra(this.network, this.travelCosts, null);
		List<GPSPoint> points = this.gpsSequence.getGPSPoints();
		// 1. initialize all objects needed and return the collection of Nodes that gonna be scanned.
		Map<Id<Node>, Node> searchedNodesMap = initializeSearchedNetwork(searchableMiddleNodes);
		// 2. loop each node in searchableMiddleNodes in sequence to calculate shortest path between nodes
		int numberOfMidPoints=searchableMiddleNodes.size();
		for (int counter=1;counter<numberOfMidPoints;counter++) {
			Collection<Node> nodeList =searchableMiddleNodes.get(counter);
			GPSPoint currentPoint = points.get(counter); // the current GPSPoint
			// now we need to loop between every 2 possible combinations of nodes from two consecutive GPS points.
			Collection<Node> nodeListPreviousPoint = searchableMiddleNodes.get(counter-1);
			GPSPoint previousPoint = points.get(counter-1); // the previous GPSPoint
			// if the previous one is empty means we need to go to the closest non-empty one
			if (nodeListPreviousPoint.size()==0) {
				int countBack=counter-2;
				nodeListPreviousPoint=searchableMiddleNodes.get(countBack);
				while (countBack>0 && nodeListPreviousPoint.size()==0) {
					countBack--;
					nodeListPreviousPoint=searchableMiddleNodes.get(countBack);
					previousPoint = points.get(countBack);
				}
			}
			Node currentClosestNode=NetworkUtils.getNearestNode(this.network, currentPoint.getCoord());
			Node previousClosestNode=NetworkUtils.getNearestNode(this.network, previousPoint.getCoord());
			Path pathBetweenCurrentAndPreviousPoints = dijkstraRouter.calcLeastCostPath(previousClosestNode, currentClosestNode, 0, null, null);
			for (Node searchedNode:nodeList) {
				for (Node nodeInPreviousPoint:nodeListPreviousPoint) {
					// calculate the score. Note that we select the path with highest score and score should be positive by definition if there is no connection, returns -inf
					double costScore =calculateScorebetweenTwoCandidatePoints(dijkstraRouter,nodeInPreviousPoint,searchedNode,previousPoint,currentPoint,pathBetweenCurrentAndPreviousPoints);
					// System.out.println("current node: "+ searchedNode.getId()+" to previous node: "+nodeInPreviousPoint.getId()+ ", the score is: "+(this.costToMiddileNode.get(nodeInPreviousPoint.getId())+costScore));
					if (this.costToMiddileNode.get(nodeInPreviousPoint.getId())+costScore>this.costToMiddileNode.get(searchedNode.getId()) & nodeInPreviousPoint.getId().toString()!=searchedNode.getId().toString()) {
						this.costToMiddileNode.put(searchedNode.getId(), this.costToMiddileNode.get(nodeInPreviousPoint.getId())+costScore);
						this.previousMiddileNodes.put(searchedNode.getId(), nodeInPreviousPoint.getId());

					}
				}
			}
			// System.out.println("number of gpspoints processed: "+counter+". "+"TimeStamp: "+currentPoint.getTimeStamp().toString()+ ". distance:"+currentPoint.getDelta_m()+". speed: "+currentPoint.getSpeed()+ ". Coordinate: "+currentPoint.getCoord().toString());
		}



		// go back according to previousMiddileNodes so we can calculate the route back
		Path finalPath = new Path(new ArrayList<Node>(), new ArrayList<Link>(), 0, 0);
		Id<Node> currentNodeID =this.endNode.getId();
		while(!currentNodeID.equals(this.startNode.getId())) {
			Id<Node> previousNodeID = this.previousMiddileNodes.get(currentNodeID);
			if (previousNodeID==null) {
				finalPath=null;
				break;

			}
			Node previousNode = searchedNodesMap.get(previousNodeID);
			Node currentNode = searchedNodesMap.get(currentNodeID);
			Path appendPath = dijkstraRouter.calcLeastCostPath(previousNode, currentNode, 0, null, null);
			finalPath=appendPath(finalPath,appendPath); // a function to append a Path object back to the finalPath
			this.costToMiddileNode.get(currentNodeID);
			System.out.println("previous node ID: "+currentNodeID);
			currentNodeID=previousNodeID;

		}
		finalPath=generateNodes(finalPath);
		return finalPath;

	}

	private double calculateScorebetweenTwoCandidatePoints(LeastCostPathCalculator dijkstraRouter,Node nodeInPreviousPoint,Node searchedNode,GPSPoint previousPoint,GPSPoint currentPoint,Path pathBetweenCurrentAndPreviousPoints) {
		// calculate the score according to the Matsim route choice paper
		// calculate spatial score
		NormalDistribution currentPointNormalDistribution = new NormalDistribution(0,20.0);  // use 20 standard deviation
		//1. calculate probability of how close the candidate point to the GPS point.
		double currentPointLogProbability = currentPointNormalDistribution.logDensity(NetworkUtils.getEuclideanDistance(searchedNode.getCoord(), currentPoint.getCoord()));
		Path pathBetweenCurrentAndPreviousNodes=dijkstraRouter.calcLeastCostPath(nodeInPreviousPoint, searchedNode, 0, null, null);
		if (pathBetweenCurrentAndPreviousNodes!=null & pathBetweenCurrentAndPreviousPoints!=null) {
			double sumTimeInSeconds=0;
			double sumLength=0;
			List<Link> linkList = pathBetweenCurrentAndPreviousNodes.links;
			for (Link link: linkList) {
				sumTimeInSeconds=sumTimeInSeconds+link.getLength()/link.getFreespeed();
				sumLength=sumLength+link.getLength();
			}

			//2. calculate spatial probability: how close the candidate path distance to the distance between 2 GPS points
			double spatialNormalDensityMean=currentPoint.getDelta_m()-previousPoint.getDelta_m();
			NormalDistribution spatialNormalDistribution = new NormalDistribution(spatialNormalDensityMean,spatialNormalDensityMean*2);  // use 2*mean as std
			double spatialLogProbability=Double.NEGATIVE_INFINITY;
			spatialLogProbability=spatialNormalDistribution.logDensity(sumLength);


			//3. calculate temporal probability: how close the candidate path travel time to the GPS point gap time.
			double temporalNormalDensityMean=currentPoint.getTimeStamp().seconds()-previousPoint.getTimeStamp().seconds();
			NormalDistribution temporalNormalDistribution = new NormalDistribution(temporalNormalDensityMean,temporalNormalDensityMean*0.5);  // use 0.5*mean as std
			double temporalLogProbability=Double.NEGATIVE_INFINITY;
			temporalLogProbability=temporalNormalDistribution.logDensity(sumTimeInSeconds);

			// return the product of probability
			return Math.exp(currentPointLogProbability+spatialLogProbability+temporalLogProbability);
		} else {
			return Double.NEGATIVE_INFINITY;
		}

	}


	private Map<Id<Node>, Node> initializeSearchedNetwork(List<Collection<Node>> searchableMiddleNodes) {

		// create a Map<Id<Node>, ? extends Node> object to store all searched nodes. This includes: startNode, searchableMiddleNodes and endNode.
		Map<Id<Node>, Node> nodeMap = new HashMap<Id<Node>, Node>();

		for (Collection<Node> nodeList:searchableMiddleNodes) {
			for (Node searchedNode:nodeList) {
				nodeMap.put(searchedNode.getId(), searchedNode);
			}


			for (Node node : nodeMap.values()){
				this.costToMiddileNode.put(node.getId(), Double.NEGATIVE_INFINITY);
				this.previousMiddileNodes.put(node.getId(), null);
			}
			this.costToMiddileNode.put(this.startNode.getId(), 0.0);
		}
		return nodeMap;

	}

	private Path appendPath(Path finalPath, Path appendPath) {
		List<Link> linkList = finalPath.links;
		double travelCost = finalPath.travelCost;
		double travelTime = finalPath.travelTime;

		List<Link> linksToAppend = appendPath.links;
		int n_linksToAppend = linksToAppend.size();
		for(int i=(n_linksToAppend-1);i>=0;i--) {
			Link linktoAppend = linksToAppend.get(i);
			linkList.add(0,linktoAppend);
		}

		travelCost+=appendPath.travelCost;
		travelTime+=appendPath.travelTime;

		return new Path(null,linkList,travelTime,travelCost);


	}

	private Path generateNodes(Path finalPath) {
		if (finalPath == null) {
			return null;
		}
		List<Link> linkList = finalPath.links;
		double travelCost = finalPath.travelCost;
		double travelTime = finalPath.travelTime;

		List<Node> nodeList = new ArrayList<Node>();
		Link firstLink = linkList.get(0);
		Node firstNode = firstLink.getFromNode();
		nodeList.add(firstNode);
		for (Link link:linkList) {
			Node toNode = link.getToNode();
			nodeList.add(toNode);
		}
		return new Path(nodeList,linkList,travelTime,travelCost);
	}

}
