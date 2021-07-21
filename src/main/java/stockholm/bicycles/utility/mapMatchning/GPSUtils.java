package stockholm.bicycles.utility.mapMatchning;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;

public final class GPSUtils {
	
	public static double distanceFromPointToLink(Coord coord, Link candidateLink) {
		/**
		 * This method calculates the distance from a given coordinate to a given candidate link.
		 * @param coord The given coordinate.
		 * @param candidateLink The given link object used in matsim network.
		 * @return the distance from the coordinate to the link.
		 **/
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

}
