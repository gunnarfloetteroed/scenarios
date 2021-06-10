package stockholm.bicycles.io.pathIO;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

public final class PathUtils {
	private static final Logger log = Logger.getLogger(PathUtils.class);
	
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


}
