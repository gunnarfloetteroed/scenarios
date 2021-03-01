package stockholm.bicycles.routing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

public class MatsimDijkstra implements LeastCostPathCalculator {
	private final Network network;
	private final TravelDisutility travelCosts;
	private Map<Id<Node>,Double> costToNode = new HashMap<Id<Node>, Double>();
	private Map<Id<Node>,Id<Node>> previousNodes = new HashMap<Id<Node>, Id<Node>>();
	PriorityQueue<Id<Node>> queue = new PriorityQueue<Id<Node>>(11, new Comparator<Id<Node>>() {

		@Override
		public int compare(Id<Node> o1, Id<Node> o2) {
			return costToNode.get(o1).compareTo(costToNode.get(o2));
		}

	});
	
	
	
	public MatsimDijkstra(Network network, TravelDisutility travelCosts,
			TravelTime travelTimes) {
		this.network = network;
		this.travelCosts=travelCosts;

	}
	
	@Override
	public Path calcLeastCostPath(Node fromNode, Node toNode, double starttime, Person person, Vehicle vehicle) {
		
		initializeNetwork(fromNode.getId());
		while (!queue.isEmpty()) {
			Id<Node> currentId = queue.poll();
			if (currentId == toNode.getId()) return createPath(toNode.getId(),fromNode.getId(),starttime,person,vehicle);
			Node currentNode = network.getNodes().get(currentId);
			for (Link link:  currentNode.getOutLinks().values()){
				Node currentToNode = link.getToNode();
				
				double travelCost = this.travelCosts.getLinkTravelDisutility(link, starttime, person, vehicle);
				double totalCost = travelCost + this.costToNode.get(currentId);
				if (totalCost < this.costToNode.get(currentToNode.getId())){
					this.costToNode.put(currentToNode.getId(), totalCost);
					update(currentToNode.getId());
					this.previousNodes.put(currentToNode.getId(), currentId);
				}
			}
		}

		return null;
	}
	
	public Map<Id<Node>,Path> calcOnetoManyLeastCostPath(Node fromNode, List<Node> toNodes, double starttime, Person person, Vehicle vehicle) {
		int numberofToNodes=toNodes.size();
		Map<Id<Node>,Path> pathMap = new HashMap<Id<Node>, Path>();
		HashMap<Id<Node>, Node> toNodesMap = new HashMap<Id<Node>,Node>();
		// check if all toNodes are included in network.getNodes()
		for (Node node:toNodes) {
			toNodesMap.put(node.getId(), node);
			
		}
		
		
		initializeNetwork(fromNode.getId());
		int numberofToNodesFound=0;
		while (!queue.isEmpty()) {
			Id<Node> currentId = queue.poll();
			
			if (toNodesMap.containsKey(currentId)) {
				Path pathForOneToNode=createPath(currentId,fromNode.getId(),starttime,person,vehicle);
				pathMap.put(currentId, pathForOneToNode);
				numberofToNodesFound++;
			} 
			
			if (numberofToNodesFound==numberofToNodes) {
				return pathMap;
			}
			
			Node currentNode = network.getNodes().get(currentId);
			for (Link link:  currentNode.getOutLinks().values()){
				Node currentToNode = link.getToNode();
				
				double travelCost = this.travelCosts.getLinkTravelDisutility(link, starttime, person, vehicle);
				double totalCost = travelCost + this.costToNode.get(currentId);
				if (totalCost < this.costToNode.get(currentToNode.getId())){
					this.costToNode.put(currentToNode.getId(), totalCost);
					update(currentToNode.getId());
					this.previousNodes.put(currentToNode.getId(), currentId);
				}
			}
		}
		return null;
	}
	
	
	private Path createPath(Id<Node> toNodeId, Id<Node> fromNodeId,double starttime, Person person, Vehicle vehicle) {
		List<Node> nodes = new ArrayList<Node>();
		List<Link> links = new ArrayList<Link>();
		double travelTime=0.0;
		double travelCost=0.0;
		
		Node lastNode = network.getNodes().get(toNodeId);
		while (!lastNode.getId().equals(fromNodeId)){
			if (!lastNode.getId().equals(toNodeId)) 
				nodes.add(0, lastNode);
			Node newLastNode = network.getNodes().get(this.previousNodes.get(lastNode.getId()));
			Link l = NetworkUtils.getConnectingLink(newLastNode,lastNode);
			links.add(0, l);
			lastNode = newLastNode;
			
			double travelCostL = this.travelCosts.getLinkTravelDisutility(l, starttime, person, vehicle);
			travelCost=travelCost+travelCostL;
			travelTime=travelTime+l.getLength()/l.getFreespeed(starttime);
		}


		return new Path(nodes,links,travelTime,travelCost);
	}
	
	
	private void initializeNetwork(Id<Node> startNode) {
		this.queue = new PriorityQueue<Id<Node>>(11, new Comparator<Id<Node>>() {

			@Override
			public int compare(Id<Node> o1, Id<Node> o2) {
				return costToNode.get(o1).compareTo(costToNode.get(o2));
			}

		});
		
		for (Node node : network.getNodes().values()){
			this.costToNode.put(node.getId(), Double.POSITIVE_INFINITY);
			this.previousNodes.put(node.getId(), null);
		}
		this.costToNode.put(startNode, 0.0);
		this.queue.add(startNode);

	}
	
	private void update(Id<Node> nodeToUpdate){
		this.queue.remove(nodeToUpdate);
		this.queue.add(nodeToUpdate);
	}

}
