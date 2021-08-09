package stockholm.bicycles.mapmatching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.FastDijkstra;
import org.matsim.core.router.FastDijkstraFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;

import stockholm.bicycles.mapmatching.mapmatchingstatistics.DistanceFromGPSPointsToNearestLinksData;
import stockholm.bicycles.mapmatching.mapmatchingstatistics.PathWithMatchingEvaluationStatistics;
import stockholm.bicycles.routing.TravelDisutilityBicycle;
import stockholm.bicycles.utility.mapMatchningUtil.GPSUtils;
import stockholm.bicycles.utility.mapMatchningUtil.PathUtils;

public class BundledShortestPathGPSSequenceMapMatcher implements GPSSequenceMapMatcher{
	private static Logger logger = Logger.getLogger(BundledShortestPathGPSSequenceMapMatcher.class);
	protected final Network network;
	protected final GPSSequence gpsSequence;
	protected final TravelDisutility travelCosts;
	protected Node startNode;
	protected Node endNode;
	private HashMap<Id<Link>, Double> linkWeights = new HashMap<Id<Link>, Double>();

	private Map<Id<Node>,Double> costToMiddileNode = new HashMap<Id<Node>, Double>();
	private Map<Id<Node>,Id<Node>> previousMiddileNodes = new HashMap<Id<Node>, Id<Node>>();



	public BundledShortestPathGPSSequenceMapMatcher(Network network,GPSSequence gpsSequence,TravelDisutility travelCosts) {
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

	public Network getNetwork() {
		return network;
	}


	public GPSSequence getGpsSequence() {
		return gpsSequence;
	}


	public TravelDisutility getTravelCosts() {
		return travelCosts;
	}

	// main method to do map-matching
	@Override
	public Path mapMatching() {	
		/**
		 * Main method for mapMatching.
		 * @return Path the matched path given the GPS sequence.
		 */
		List<GPSPoint> points = this.gpsSequence.getGPSPoints();
		GPSPoint startPoint = points.get(0);
		int NPoints = points.size();
		GPSPoint endPoint = points.get(NPoints-1);
		double DistanceStartNode=NetworkUtils.getEuclideanDistance(this.startNode.getCoord(), startPoint.getCoord());
		double DistanceEndNode=NetworkUtils.getEuclideanDistance(this.endNode.getCoord(), endPoint.getCoord());
		if (DistanceStartNode>50000 | DistanceEndNode>50000) {
			logger.warn("the following GPS trace: "+this.gpsSequence.getPersonID().toString()+" is not within the study area.");
			return null;}
		// calculate a weight for each link. the idea is as follow:
		// if a link is close to GPS points and follows the GPS point directions, then it has a smaller weight. weight is between 0 and 1. 
		updateNetworkLinkWeights();
		logger.info("link weight updated.");
		// 2. get all possible middle nodes
		List<Collection<Node>> searchableMiddleNodes = getSearchedNodes();
		logger.info("superNetwork constructed.");
		// 3. do some logic to generate most probable path
		Path path=DijkstraThroughMidNodes(searchableMiddleNodes);
		logger.info("routing in superNetwork done.");
		restorNetworkWeight();
		logger.info("network weight restored.");
		return path;

	}
	
	private void restorNetworkWeight() {
		Map<Id<Link>, ? extends Link> links = this.network.getLinks();
		
		for (Entry<Id<Link>, Double> linkWeight: this.linkWeights.entrySet()) {
			Id<Link> LinkID = linkWeight.getKey();
			Double cost = linkWeight.getValue();
			Link link = links.get(LinkID);
			
			TravelDisutilityBicycle travelCost = (TravelDisutilityBicycle) this.travelCosts;
			String generalizedCostAttributeName = travelCost.getGeneralizedCostAttributeName();
			link.getAttributes().putAttribute(generalizedCostAttributeName, cost);
		}
		
	}

	public PathWithMatchingEvaluationStatistics mapMatchingAlternativePath() {
		/**
		 * Main method for mapMatching. Compared to this.mapMatching(), it also returns 
		 * @return Path the matched path given the GPS sequence.
		 */
		Path path = this.mapMatching();
		List<Link> matchedLinks = path.links;
		double totalLength=0;
		for (Link matchedLink:matchedLinks) {
			totalLength=totalLength+matchedLink.getLength();
		}
		
		double currentLength=0;
		for (Link matchedLink:matchedLinks) {
			if (currentLength>500 & currentLength<(totalLength-500)) {
				network.removeLink(matchedLink.getId());
			}
			currentLength=currentLength+matchedLink.getLength();
		}
		Path alternativePath = this.mapMatching();
		
		currentLength=0;
		for (Link matchedLink:matchedLinks) {
			if (currentLength>500 & currentLength<(totalLength-500)) {
				network.addLink(matchedLink);
			}
			currentLength=currentLength+matchedLink.getLength();
		}
		
		
		PathWithMatchingEvaluationStatistics matchingResults= new PathWithMatchingEvaluationStatistics();
		matchingResults.setMatchedPath(alternativePath);
		DistanceFromGPSPointsToNearestLinksData distanceFromGPSPointsToNearestLinksData = PathUtils.distanceEachGPSPointPathToGPS(this.gpsSequence, alternativePath);
		
		GPSSequenceWithDistanceToMatchedPath gPSSequenceWithDistanceToMatchedPath = 
				new GPSSequenceWithDistanceToMatchedPath(
						this.gpsSequence,
						distanceFromGPSPointsToNearestLinksData.getDistanceToNearestLink(),
						distanceFromGPSPointsToNearestLinksData.getNearestLinkID());
		matchingResults.setgPSSequenceWithDistanceToMatchedPath(gPSSequenceWithDistanceToMatchedPath);
		matchingResults.setAverageDistanceFromPathToGPS(PathUtils.averageDistancePathToGPS(this.gpsSequence, alternativePath));
		return matchingResults;
	}


	private void updateNetworkLinkWeights() {
		this.linkWeights = new HashMap<Id<Link>, Double>();
		Map<Id<Link>, ? extends Link> links = this.network.getLinks();
		List<GPSPoint> points = this.gpsSequence.getGPSPoints();
		List<Node> nodeList = new ArrayList<Node>();
		int counter=1;
		for (GPSPoint point:points) {
			nodeList.add(network.getFactory().createNode(Id.createNodeId("N_"+counter), point.getCoord()));
			counter++;
		}
		double[] boundingBox = NetworkUtils.getBoundingBox(nodeList);
		double Xmin = boundingBox[0]-500;
		double Ymin = boundingBox[1]-500;
		double Xmax = boundingBox[2]+500;
		double Ymax = boundingBox[3]+500;

		for (Link link:links.values()) {

			Coord fronNodeCoord = link.getFromNode().getCoord();
			Coord toNodeCoord = link.getToNode().getCoord();
			double X1=fronNodeCoord.getX();
			double Y1=fronNodeCoord.getY();
			double X2=toNodeCoord.getX();
			double Y2=toNodeCoord.getY();
			if ((Xmin<X1 & X1<Xmax) & (Xmin<X2 & X2<Xmax) & (Ymin<Y1 & Y1<Ymax) & (Ymin<Y2 & Y2<Ymax)) {
				double weight = calculateLinkWeight(link);
				TravelDisutilityBicycle travelCost = (TravelDisutilityBicycle) this.travelCosts;
				String generalizedCostAttributeName = travelCost.getGeneralizedCostAttributeName();
				double currentCost =(double) link.getAttributes().getAttribute(generalizedCostAttributeName);
				this.linkWeights.put(link.getId(), currentCost);
				link.getAttributes().putAttribute(generalizedCostAttributeName, currentCost*weight);
			}
		}
	}



	private double calculateLinkWeight(Link link) {
		Coord fronNodeCoord = link.getFromNode().getCoord();
		Coord toNodeCoord = link.getToNode().getCoord();
		int nearestGPSPointIndexFromNode = getNearestGPSPointfromCoord(fronNodeCoord);
		double distanceFromNodeToNearestGPSPoint=NetworkUtils.getEuclideanDistance(fronNodeCoord, this.gpsSequence.getGPSPoints().get(nearestGPSPointIndexFromNode).getCoord());

		int nearestGPSPointIndexToNode = getNearestGPSPointfromCoord(toNodeCoord);
		double distanceToNodeToNearestGPSPoint=NetworkUtils.getEuclideanDistance(toNodeCoord, this.gpsSequence.getGPSPoints().get(nearestGPSPointIndexToNode).getCoord());

		if (distanceFromNodeToNearestGPSPoint<=100 & distanceToNodeToNearestGPSPoint<=100) {
			int nearestGPSPointIndexStart=nearestGPSPointIndexFromNode;
			int nearestGPSPointIndexEnd=nearestGPSPointIndexToNode;
			if (nearestGPSPointIndexFromNode>nearestGPSPointIndexToNode) {
				nearestGPSPointIndexStart=nearestGPSPointIndexToNode;
				nearestGPSPointIndexEnd=nearestGPSPointIndexFromNode;
			}
			double averageDistance=averageEuclideanDistanceToLink(link,nearestGPSPointIndexStart,nearestGPSPointIndexEnd);
			return (1/(1+Math.exp(-averageDistance/20)))*2-1;


		} else {
			return 1;
		}

	}

	private double averageEuclideanDistanceToLink(Link candidateLink, int currentPointIndex, int candidateGPSPointIndex) {
		List<GPSPoint> points = this.gpsSequence.getGPSPoints();
		double sumDistance=0;
		int counter=0;
		for (int i=(currentPointIndex);i<=(candidateGPSPointIndex);i++) {
			if(i>=0 & i<points.size()) {
				sumDistance=sumDistance+GPSUtils.distanceFromPointToLink(points.get(i).getCoord(),candidateLink);
				counter++;
			}
		}
		return sumDistance/counter;
	}




	private int getNearestGPSPointfromCoord(Coord coord) {
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



	protected List<Collection<Node>> getSearchedNodes(){
		List<GPSPoint> points = this.gpsSequence.getGPSPoints(); // note that GPSPoints already have a sequence
		int NPoints = points.size();
		// important to remember that you should call NetworkUtils to find nearest nodes and other stuffs.
		List<Collection<Node>> searchableMiddleNodes = new ArrayList<Collection<Node>>();
		Collection<Node> middleNodes= new ArrayList<Node>();
		for (int i=0;i<NPoints;i++) { // loop first to last node
			GPSPoint midPoint = points.get(i);
			// Collection<Node> nodes = NetworkUtils.getNearestNodes(this.network, midPoint.getCoord(), midPoint.getNodeSearchRadius());
			// Collection<Node> nodes= new ArrayList<Node>();
			if (i==0) { // 
				Collection<Node> nodes= new ArrayList<Node>();
				nodes.add(this.startNode);
				searchableMiddleNodes.add(nodes);
			} else if (i==(NPoints-1)) {
				Collection<Node> nodes= new ArrayList<Node>();
				nodes.add(this.endNode);
				searchableMiddleNodes.add(nodes);
			} else if (i % 20>0 ) {
				// add node to the existing "nodes" collection.
				List<Node> relevantNodes = getNearestNodesFromNearestLinks(midPoint);
				for (Node eachNode : relevantNodes) {
					if (!middleNodes.contains(eachNode)) {
						middleNodes.add(eachNode);
					}
				}
			} else if (i % 20==0 ) {
				// add the current nodes list to searchableMiddleNodes and then start a new node List to get new nodes
				searchableMiddleNodes.add(middleNodes);
				middleNodes= new ArrayList<Node>();
				List<Node> relevantNodes = getNearestNodesFromNearestLinks(midPoint);
				for (Node eachNode : relevantNodes) {
					if (!middleNodes.contains(eachNode)) {
						middleNodes.add(eachNode);
					}
				}
			}

		}
		return searchableMiddleNodes;

	}

	protected Path DijkstraThroughMidNodes(List<Collection<Node>> searchableMiddleNodes) {
		// MatsimDijkstra dijkstraRouter = new MatsimDijkstra(this.network, this.travelCosts, null);
		FastDijkstraFactory fastDijkstraFactory = new FastDijkstraFactory();
		FastDijkstra dijkstraRouter = (FastDijkstra) fastDijkstraFactory.createPathCalculator(this.network, this.travelCosts, new FreeSpeedTravelTime());
		// 1. initialize all objects needed and return the collection of Nodes that gonna be scanned.
		Map<Id<Node>, Node> searchedNodesMap = initializeSearchedNetwork(searchableMiddleNodes);
		// 2. loop each node in searchableMiddleNodes in sequence to calculate shortest path between nodes
		int numberOfMidPoints=searchableMiddleNodes.size();
		List<Boolean> allNullMiddleNodes = new ArrayList<Boolean>();
		allNullMiddleNodes.add(false);
		first:
			for (int counter=1;counter<numberOfMidPoints;counter++) {
				Collection<Node> nodeList =searchableMiddleNodes.get(counter);
				// now we need to loop between every 2 possible combinations of nodes from two consecutive GPS points.
				Collection<Node> nodeListPreviousPoint = searchableMiddleNodes.get(counter-1);
				Boolean isNullFlag = allNullMiddleNodes.get(counter-1);
				// if the previous one is empty means we need to go to the closest non-empty one
				if (nodeListPreviousPoint.size()==0 | isNullFlag==true) {
					int countBack=counter-2;
					nodeListPreviousPoint=searchableMiddleNodes.get(countBack);
					isNullFlag = allNullMiddleNodes.get(countBack);
					while (countBack>0 && (nodeListPreviousPoint.size()==0 | isNullFlag==true)) {
						countBack--;
						nodeListPreviousPoint=searchableMiddleNodes.get(countBack);
					}
				}


				for (Node searchedNode:nodeList) {
					for (Node nodeInPreviousPoint:nodeListPreviousPoint) {
						if (this.costToMiddileNode.get(nodeInPreviousPoint.getId())<this.costToMiddileNode.get(searchedNode.getId())) {
							Path testPath = dijkstraRouter.calcLeastCostPath(nodeInPreviousPoint, searchedNode, 0, null, null);
							if (testPath != null) {
								double pathCost = testPath.travelCost;
								if (this.costToMiddileNode.get(nodeInPreviousPoint.getId())+pathCost<this.costToMiddileNode.get(searchedNode.getId())) {
									this.costToMiddileNode.put(searchedNode.getId(), this.costToMiddileNode.get(nodeInPreviousPoint.getId())+pathCost);
									this.previousMiddileNodes.put(searchedNode.getId(), nodeInPreviousPoint.getId());
								}

							}
						}
					}
					if (this.endNode.getId() == searchedNode.getId() && this.previousMiddileNodes.get(endNode.getId())!=null) {
						break first;
					}
				}
				
				// check the current MiddleNode if all nodes cannot be reached
				boolean isNullMiddleNodesAtCurrentLayer = true;
				for (Node searchedNode:nodeList) {
					 if (this.previousMiddileNodes.get(searchedNode.getId())!=null) {
						 isNullMiddleNodesAtCurrentLayer=false;
						 break;
					 }
				}
				allNullMiddleNodes.add(isNullMiddleNodesAtCurrentLayer);


			}



		// go back according to previousMiddileNodes so we can calculate the route back
		Path finalPath = new Path(new ArrayList<Node>(), new ArrayList<Link>(), 0, 0);
		Id<Node> currentNodeID =this.endNode.getId();
		List<String> visitedNodesID = new ArrayList<String>();
		while(!currentNodeID.equals(this.startNode.getId())) {
			Id<Node> previousNodeID = this.previousMiddileNodes.get(currentNodeID);
			if (previousNodeID==null) {
				finalPath=null;
				return finalPath;

			}
			Node previousNode = searchedNodesMap.get(previousNodeID);
			Node currentNode = searchedNodesMap.get(currentNodeID);
			Path appendPath = dijkstraRouter.calcLeastCostPath(previousNode, currentNode, 0, null, null);
			finalPath=appendPath(finalPath,appendPath); // a function to append a Path object back to the finalPath
			// logger.info("trip ID: "+this.gpsSequence.getPersonID().toString()+". Previous node ID: "+currentNodeID);
			currentNodeID=previousNodeID;
			
			if (visitedNodesID.contains(previousNodeID.toString())) {
				logger.warn("trip ID: "+this.gpsSequence.getPersonID().toString()+" generates infinite loop path. Discard this trip.");
				return null;
			}
			visitedNodesID.add(previousNodeID.toString());
		}
		finalPath=generateNodes(finalPath);
		return finalPath;

	}

	//	private List<Id<Link>> getNearestLinks(GPSPoint point) {
	//		List<Id<Link>> linkList = new ArrayList<Id<Link>>();
	//    	Coord pointCoord = point.getCoord();
	//    	for (int i=-5;i<6;i++) {
	//    		for (int j=-5;j<6;j++) {
	//    			Coord newCoord = new Coord(pointCoord.getX()+i*point.getNodeSearchRadius()/5,pointCoord.getY()+j*point.getNodeSearchRadius()/5);
	//    			Link candidateLink = NetworkUtils.getNearestLinkExactly(this.network, newCoord);
	//    			Id<Link> candidateLinkId = candidateLink.getId();
	//    			linkList.add(candidateLinkId);
	//    		}
	//    	}
	//		return linkList;
	//	}
	//	
	private List<Node> getNearestNodesFromNearestLinks(GPSPoint point){
		List<Node> nodes = new ArrayList<Node>();
		Coord pointCoord = point.getCoord();
		for (int i=-5;i<6;i++) {
			for (int j=-5;j<6;j++) {
				Coord newCoord = new Coord(pointCoord.getX()+i*point.getNodeSearchRadius()/5,pointCoord.getY()+j*point.getNodeSearchRadius()/5);
				Link candidateLink = NetworkUtils.getNearestLinkExactly(this.network, newCoord);
				Node fromNode = candidateLink.getFromNode();
				Node toNode = candidateLink.getToNode();
				if (!nodes.contains(fromNode)) {
					nodes.add(fromNode);
				}
				if (!nodes.contains(toNode)) {
					nodes.add(toNode);
				}


				//    			Link candidateLink2 = NetworkUtils.getNearestLink(this.network, newCoord);
				//    			Node fromNode2 = candidateLink2.getFromNode();
				//    			Node toNode2 = candidateLink2.getToNode();
				//    			if (!nodes.contains(fromNode2)) {
				//    				nodes.add(fromNode2);
				//    			}
				//    			if (!nodes.contains(toNode2)) {
				//    				nodes.add(toNode2);
				//    			}


			}
		}

		Collection<Node> nearestNodes = NetworkUtils.getNearestNodes(this.network, pointCoord, point.getNodeSearchRadius());
		for (Node nearestNode: nearestNodes) {
			if (!nodes.contains(nearestNode)) {
				nodes.add(nearestNode);
			}
		}

		return nodes;	
	}


	private Map<Id<Node>, Node> initializeSearchedNetwork(List<Collection<Node>> searchableMiddleNodes) {

		// create a Map<Id<Node>, ? extends Node> object to store all searched nodes. This includes: startNode, searchableMiddleNodes and endNode.
		Map<Id<Node>, Node> nodeMap = new HashMap<Id<Node>, Node>();

		for (Collection<Node> nodeList:searchableMiddleNodes) {
			for (Node searchedNode:nodeList) {
				nodeMap.put(searchedNode.getId(), searchedNode);
			}


			for (Node node : nodeMap.values()){
				this.costToMiddileNode.put(node.getId(), Double.POSITIVE_INFINITY);
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
		if (linkList.size()==0) {
			return null;
		}
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
