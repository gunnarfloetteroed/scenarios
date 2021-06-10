package stockholm.bicycles.mapmatching;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.FastDijkstra;
import org.matsim.core.router.FastDijkstraFactory;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.core.utils.misc.OptionalTime;

import stockholm.bicycles.routing.TravelDisutilityBicycle;

public class GPSSequenceGenerator {
	private Network network;
	private Random random = new Random();

	public GPSSequenceGenerator(Network network) {
		super();
		this.network = network;
	}

	public GPSSequenceGenerator(Network network, long seed) {
		super();
		this.network = network;
		this.random.setSeed(seed);
	}


	public MapMatchingValidationGPSSequenceAndRoute generate(String nodeStartId, String nodeEndId) {
		MapMatchingValidationGPSSequenceAndRoute output= new MapMatchingValidationGPSSequenceAndRoute();
		Node startNode = this.network.getNodes().get(Id.createNodeId(nodeStartId));
		Node endNode = this.network.getNodes().get(Id.createNodeId(nodeEndId));
		FastDijkstraFactory fastDijkstraFactory = new FastDijkstraFactory();
		FastDijkstra dijkstraRouter = (FastDijkstra) fastDijkstraFactory.createPathCalculator(this.network, new TravelDisutilityBicycle("generalizedCost"), new FreeSpeedTravelTime());
		Path actualPath = dijkstraRouter.calcLeastCostPath(startNode, endNode, 0, null, null);		
		output.setPath(actualPath);

		Id<Person> personID = Id.createPersonId("P_"+nodeStartId+"_"+nodeEndId);

		// from the given path, we generate GPS points alongside
		List<GPSPoint> gpsPoints = new ArrayList<GPSPoint>();
		List<Node> nodes = actualPath.nodes;
		double currentTime=0;
		double currentDistance=0;
		for (int i=0;i<(nodes.size()-1);i++) {
			if (i==0) {
				Coord fromNodeCoord = nodes.get(i).getCoord();
				Coord toNodeCoord= nodes.get(i+1).getCoord();
				Coord candicateFromCoord = this.generateRandomCoordAlongside(fromNodeCoord, 50);
				Coord candicateToCoord = this.generateRandomCoordAlongside(toNodeCoord, 50);

				int xSign=(int) ((toNodeCoord.getX()-fromNodeCoord.getX())/Math.abs(toNodeCoord.getX()-fromNodeCoord.getX()));
				int ySign=(int) ((toNodeCoord.getY()-fromNodeCoord.getY())/Math.abs(toNodeCoord.getY()-fromNodeCoord.getY()));

				int xSignCandidate=(int) ((candicateToCoord.getX()-candicateFromCoord.getX())/Math.abs(candicateToCoord.getX()-candicateFromCoord.getX()));
				int ySignCandidate=(int) ((candicateToCoord.getY()-candicateFromCoord.getY())/Math.abs(candicateToCoord.getY()-candicateFromCoord.getY()));
				while (xSign!=xSignCandidate | ySign!=ySignCandidate) {
					candicateFromCoord = this.generateRandomCoordAlongside(fromNodeCoord, 50);
					candicateToCoord = this.generateRandomCoordAlongside(toNodeCoord, 50);
					xSignCandidate=(int) ((candicateToCoord.getX()-candicateFromCoord.getX())/Math.abs(candicateToCoord.getX()-candicateFromCoord.getX()));
					ySignCandidate=(int) ((candicateToCoord.getY()-candicateFromCoord.getY())/Math.abs(candicateToCoord.getY()-candicateFromCoord.getY()));
				}
				gpsPoints.add(new GPSPoint(candicateFromCoord,OptionalTime.defined(currentTime),currentDistance,50,0));

				double theta = calculateTheta(candicateFromCoord,candicateToCoord);
				Coord previousCoord=candicateFromCoord;
				int numberOfGPSPoints= (int) Math.floor(NetworkUtils.getEuclideanDistance(candicateFromCoord,candicateToCoord)/20);
				for (int k=1;k<numberOfGPSPoints;k++) {
					Coord nextCoord = new Coord(candicateFromCoord.getX()+xSign*k*20*Math.cos(theta),candicateFromCoord.getY()+ySign*k*20*Math.sin(theta));
					Coord nextRandomCoord = this.generateRandomCoordAlongside(nextCoord, 9.99);
					currentTime=currentTime+2;
					double distanceToAdd=NetworkUtils.getEuclideanDistance(nextRandomCoord, previousCoord);
					currentDistance=currentDistance+distanceToAdd;
					gpsPoints.add(new GPSPoint(nextRandomCoord,OptionalTime.defined(currentTime),currentDistance,10,distanceToAdd/2*3.6));
					previousCoord=nextRandomCoord;
				}
				// add last point
				currentTime=currentTime+2;
				double distanceToAdd=NetworkUtils.getEuclideanDistance(candicateToCoord, previousCoord);
				currentDistance=currentDistance+distanceToAdd;
				gpsPoints.add(new GPSPoint(candicateToCoord,OptionalTime.defined(currentTime),currentDistance,50,distanceToAdd/2*3.6));

			} else {
				Coord fromNodeCoord = nodes.get(i).getCoord();
				Coord toNodeCoord= nodes.get(i+1).getCoord();
				Coord candicateFromCoord = gpsPoints.get(gpsPoints.size()-1).getCoord();
				Coord candicateToCoord = this.generateRandomCoordAlongside(toNodeCoord, 50);

				int xSign=(int) ((toNodeCoord.getX()-fromNodeCoord.getX())/Math.abs(toNodeCoord.getX()-fromNodeCoord.getX()));
				int ySign=(int) ((toNodeCoord.getY()-fromNodeCoord.getY())/Math.abs(toNodeCoord.getY()-fromNodeCoord.getY()));

				int xSignCandidate=(int) ((candicateToCoord.getX()-candicateFromCoord.getX())/Math.abs(candicateToCoord.getX()-candicateFromCoord.getX()));
				int ySignCandidate=(int) ((candicateToCoord.getY()-candicateFromCoord.getY())/Math.abs(candicateToCoord.getY()-candicateFromCoord.getY()));
				while (xSign!=xSignCandidate | ySign!=ySignCandidate) {
					candicateFromCoord = this.generateRandomCoordAlongside(fromNodeCoord, 50);
					candicateToCoord = this.generateRandomCoordAlongside(toNodeCoord, 50);
					xSignCandidate=(int) ((candicateToCoord.getX()-candicateFromCoord.getX())/Math.abs(candicateToCoord.getX()-candicateFromCoord.getX()));
					ySignCandidate=(int) ((candicateToCoord.getY()-candicateFromCoord.getY())/Math.abs(candicateToCoord.getY()-candicateFromCoord.getY()));
				}
				double theta = calculateTheta(candicateFromCoord,candicateToCoord);
				Coord previousCoord=candicateFromCoord;
				int numberOfGPSPoints= (int) Math.floor(NetworkUtils.getEuclideanDistance(candicateFromCoord,candicateToCoord)/20);
				for (int k=1;k<numberOfGPSPoints;k++) {
					Coord nextCoord = new Coord(candicateFromCoord.getX()+xSign*k*20*Math.cos(theta),candicateFromCoord.getY()+ySign*k*20*Math.sin(theta));
					Coord nextRandomCoord = this.generateRandomCoordAlongside(nextCoord, 9.99);
					currentTime=currentTime+2;
					double distanceToAdd=NetworkUtils.getEuclideanDistance(nextRandomCoord, previousCoord);
					currentDistance=currentDistance+distanceToAdd;
					gpsPoints.add(new GPSPoint(nextRandomCoord,OptionalTime.defined(currentTime),currentDistance,10,distanceToAdd/2*3.6));
					previousCoord=nextRandomCoord;
				}
				// add last point
				currentTime=currentTime+2;
				double distanceToAdd=NetworkUtils.getEuclideanDistance(candicateToCoord, previousCoord);
				currentDistance=currentDistance+distanceToAdd;
				gpsPoints.add(new GPSPoint(candicateToCoord,OptionalTime.defined(currentTime),currentDistance,50,distanceToAdd/2*3.6));
			}


		}
		
		GPSSequence sequence = new GPSSequence(personID, gpsPoints);

		output.setSequence(sequence);
		return output;
	}




	private double calculateTheta(Coord candicateFromCoord, Coord candicateToCoord) {
		double theta=0;
		double deltaX=candicateToCoord.getX()-candicateFromCoord.getX();
		double deltaY=candicateToCoord.getY()-candicateFromCoord.getY();
		if (deltaX>=0 & deltaY>=0) {
			theta=Math.asin(deltaY/Math.sqrt(deltaX*deltaX+deltaY*deltaY));
		} else if (deltaX>=0 & deltaY<0) {
			theta=Math.PI*2+Math.asin(deltaY/Math.sqrt(deltaX*deltaX+deltaY*deltaY));
		} else if (deltaX<0 & deltaY>=0) {
			theta=Math.PI-Math.asin(deltaY/Math.sqrt(deltaX*deltaX+deltaY*deltaY));
		} else if (deltaX<0 & deltaY<0) {
			theta=Math.PI+Math.asin(-deltaY/Math.sqrt(deltaX*deltaX+deltaY*deltaY));
		}

		return theta;

	}

	public Coord generateRandomCoordAlongside(Coord coord,double radius) {

		double radians = Math.toRadians(random.nextDouble()*360);
		double sinValue = Math.sin(radians);
		double cosValue = Math.cos(radians);
		double Xsample=radius*random.nextDouble()*cosValue;
		double Ysample=radius*random.nextDouble()*sinValue;
		Coord randomCoord = new Coord(coord.getX()+Xsample,coord.getY()+Ysample);
		return randomCoord;
	}
}
