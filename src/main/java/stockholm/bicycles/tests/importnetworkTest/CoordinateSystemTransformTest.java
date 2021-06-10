package stockholm.bicycles.tests.importnetworkTest;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import stockholm.bicycles.importnetwork.StockholmTransformationFactory;

public class CoordinateSystemTransformTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final CoordinateTransformation coordinateTransform = StockholmTransformationFactory.getCoordinateTransformation(
				StockholmTransformationFactory.WGS84, StockholmTransformationFactory.WGS84_SWEREF99);
		final Coord coord = coordinateTransform.transform(new Coord( 11.953521,57.714200));
		System.out.println(coord.getX()+"; "+coord.getY());
		// 318529.249600;6400971.997000 // there is a slight difference between QGIS and Matsim Coordinate transform but guess it is fine.

	}

}
