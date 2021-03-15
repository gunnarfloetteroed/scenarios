package stockholm.bicycles.tests.utilityTest;

import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;


public class AngelCalculationTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// read GPS data
		String inputGPSFileName="//vti.se/root/RUCY/GPS data/cykel_filtered_oneTrip.csv";
		String inputNetworkFileName="C:/Users/ChengxiL/VTI/RUCY TrV ansökan - General/Data/Network/network_NVDB.xml";
		String writePath="//vti.se/root/RUCY/GPS data/writePath_test_NearestLinks.csv";
		// load network
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		(new MatsimNetworkReader(scenario.getNetwork())).readFile(inputNetworkFileName);
		Network network = scenario.getNetwork();
		Link specificLink=network.getLinks().get(Id.createLinkId("120187_AB"));
		System.out.println("fromNode X:"+specificLink.getFromNode().getCoord().getX());
		System.out.println("toNode X:"+specificLink.getToNode().getCoord().getX());
		System.out.println("fromNode Y:"+specificLink.getFromNode().getCoord().getY());
		System.out.println("toNode Y:"+specificLink.getToNode().getCoord().getY());
		System.out.println(linkAngel(specificLink));
	}
	
	private static double linkAngel(Link candidateLink) {
		
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
		return linkThetaAngel;
		
	};

}
