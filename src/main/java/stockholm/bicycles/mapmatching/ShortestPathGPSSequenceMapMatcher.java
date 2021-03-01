package stockholm.bicycles.mapmatching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import stockholm.bicycles.routing.MatsimDijkstra;

public class ShortestPathGPSSequenceMapMatcher implements GPSSequenceMapMatcher{
	protected final Network network;
	protected final GPSSequence gpsSequence;
	protected final TravelDisutility travelCosts;
	protected Node startNode;
	protected Node endNode;
	
	private Map<Id<Node>,Double> costToMiddileNode = new HashMap<Id<Node>, Double>();
	private Map<Id<Node>,Id<Node>> previousMiddileNodes = new HashMap<Id<Node>, Id<Node>>();

	

	public ShortestPathGPSSequenceMapMatcher(Network network,GPSSequence gpsSequence,TravelDisutility travelCosts) {
		super();
		this.network=network;
		this.gpsSequence=gpsSequence;
		this.travelCosts = travelCosts;
		
		// get start and end node
		List<GPSPoint> points = this.gpsSequence.getGPSPoints(); // note that GPSPoints already have a sequence
		int NPoints = points.size();
		GPSPoint startPoint = points.get(0);
		this.startNode = NetworkUtils.getNearestNode(this.network, startPoint.getCoord());
		GPSPoint endPoint = points.get(NPoints-1);
		this.endNode= NetworkUtils.getNearestNode(this.network, endPoint.getCoord());
		
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
		for (int i=0;i<NPoints;i++) { // loop first to last node
			GPSPoint midPoint = points.get(i);
			Collection<Node> nodes = NetworkUtils.getNearestNodes(this.network, midPoint.getCoord(), midPoint.getNodeSearchRadius());
			if (i==0) {
				nodes= new ArrayList<Node>();
				nodes.add(this.startNode);

			} else if (i==(NPoints-1)) {
				nodes= new ArrayList<Node>();
				nodes.add(this.endNode);
			}
			searchableMiddleNodes.add(nodes);
		}
		return searchableMiddleNodes;

	}

	protected Path DijkstraThroughMidNodes(List<Collection<Node>> searchableMiddleNodes) {
		MatsimDijkstra dijkstraRouter = new MatsimDijkstra(this.network, this.travelCosts, null);

		// 1. initialize all objects needed and return the collection of Nodes that gonna be scanned.
		Map<Id<Node>, Node> searchedNodesMap = initializeSearchedNetwork(searchableMiddleNodes);
		// 2. loop each node in searchableMiddleNodes in sequence to calculate shortest path between nodes
		int numberOfMidPoints=searchableMiddleNodes.size();
		first:
		for (int counter=1;counter<numberOfMidPoints;counter++) {
			Collection<Node> nodeList =searchableMiddleNodes.get(counter);
			 // now we need to loop between every 2 possible combinations of nodes from two consecutive GPS points.
			Collection<Node> nodeListPreviousPoint = searchableMiddleNodes.get(counter-1);
			// if the previous one is empty means we need to go to the closest non-empty one
			if (nodeListPreviousPoint.size()==0) {
				int countBack=counter-2;
				nodeListPreviousPoint=searchableMiddleNodes.get(countBack);
				while (countBack>0 && nodeListPreviousPoint.size()==0) {
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
			System.out.println("previous node ID: "+currentNodeID);
			currentNodeID=previousNodeID;
		}
		finalPath=generateNodes(finalPath);
		return finalPath;

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
