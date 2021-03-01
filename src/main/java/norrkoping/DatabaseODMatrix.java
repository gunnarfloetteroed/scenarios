package norrkoping;

/**
 * @author Rasmus Ringdahl @ Link�ping University (rasmus.ringdahl@liu.se)
 */
import java.io.IOException;
import java.sql.PreparedStatement;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

public class DatabaseODMatrix implements ODMatrix {

	// Database connection variables.
	private String username;
	private String password;
	private String host;
	private int port;

	// Output variables.
	HashSet<String> zoneIds;
	int numberOfTimeBins = 24; // TODO: Load from the database?
	SimpleFeatureCollection odMatrix;
	SimpleFeatureCollection odMatrixSize;

	SimpleFeatureCollection siteInfo;

	ArrayList<Integer> idList = new ArrayList<Integer>();
	ArrayList<String> origList = new ArrayList<String>();
	ArrayList<String> destList = new ArrayList<String>();
	ArrayList<Integer> hourList = new ArrayList<Integer>();
	ArrayList<Double> demandList = new ArrayList<Double>();
	ArrayList<Integer> siteId = new ArrayList<Integer>();

	ArrayList<String> workerZone = new ArrayList<String>();
	ArrayList<String> workerTransport = new ArrayList<String>();
	ArrayList<Double> workerToWork = new ArrayList<Double>();
	ArrayList<Double> workerToHome = new ArrayList<Double>();

	ArrayList<Integer> originWorker = new ArrayList<Integer>();
	ArrayList<Integer> destWorker = new ArrayList<Integer>();
	ArrayList<Integer> flowWorker = new ArrayList<Integer>();

	HashMap<String, Double> siteX = new HashMap<String, Double>();
	HashMap<String, Double> siteY = new HashMap<String, Double>();

	HashMap<String, Double> storeX = new HashMap<String, Double>();
	HashMap<String, Double> storeY = new HashMap<String, Double>();

	int numberOfID;
	int demand;
	int size;

	/**
	 * This is the constructor for the DatabaseODMatrix class.
	 * 
	 * @param username - Username in the database.
	 * @param password - Password to the database.
	 * @param host     - Host to the database.
	 * @param port     - Port to the database.
	 */
	public DatabaseODMatrix(String username, String password, String host, int port) {
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;

	}

	/**
	 * This method returns the number of timebins.
	 * 
	 * @return Index of the highest time bin.
	 */
	public int getNumberOfTimeBins() {
		return numberOfTimeBins;
	}

	/**
	 * This method returns the OD zone ids. The zones are cached.
	 * 
	 * @return Collection of Strings with the zoneIds.
	 */
	public Collection<String> getAllZoneIds() {
		// Checking if the zones has been loaded.
		if (zoneIds == null) {
			// Creating a HashSet.
			zoneIds = new HashSet<String>();

			// Setting parameters for the database connection.
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("dbtype", "postgis");
			params.put("user", username);
			params.put("passwd", password);
			params.put("host", host);
			params.put("port", port);
			params.put("database", "norrkoping");
			params.put("schema", "sfs");
			params.put("Expose primary keys", "true");

			try {
				// Creates a data source to the database.
				DataStore dataStore = DataStoreFinder.getDataStore(params);

				// Creating data filter.
				FilterFactory2 factory = new FilterFactoryImpl();
				ArrayList<Filter> filters = new ArrayList<Filter>();
				Query query = new Query("zone", factory.and(filters));

				query.setSortBy(new SortBy[] { factory.sort("zone", SortOrder.ASCENDING) });

				// Getting a feature source to the zone table.
				SimpleFeatureSource source = dataStore.getFeatureSource("zone");

				// Extracting the features into a collection.
				SimpleFeatureCollection collection = source.getFeatures(query);

				// Loops through the collection and caches the zone ids.
				SimpleFeatureIterator it = collection.features();
				try {
					while (it.hasNext()) {
						SimpleFeature feature = it.next();
						zoneIds.add(feature.getAttribute("id").toString());
					}
				}

				// Releasing the database connection.
				finally {
					it.close();

					if (source.getDataStore() != null) {
						source.getDataStore().dispose();
					}
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return zoneIds;
	}

	/**
	 * This method extracts the demand between an origin and destination for a
	 * specific hour (time bin).
	 * 
	 * @return demand.
	 */
	public double getDemandPerHour(String originZoneId, String destinationZoneId, int timeBin) {
		double demand = 0;

		try {
			// Checking if the OD matrix has been loaded.
			if (odMatrix == null) {
				// Setting parameters for the database connection.
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("dbtype", "postgis");
				params.put("user", username);
				params.put("passwd", password);
				params.put("host", host);
				params.put("port", port);
				params.put("database", "norrkoping");
				params.put("schema", "sfs");
				params.put("Expose primary keys", "true");

				// Creates a datasource to the database.
				DataStore dataStore = DataStoreFinder.getDataStore(params);

				// Creating data filter.
				FilterFactory2 factory = new FilterFactoryImpl();
				ArrayList<Filter> filters = new ArrayList<Filter>();
				filters.add(CQL.toFilter("matrix = 'stop'"));
				filters.add(CQL.toFilter("dow = 2"));
				Query query = new Query("od-trucksTest", factory.and(filters));

				query.setSortBy(new SortBy[] { factory.sort("origin", SortOrder.ASCENDING),
						factory.sort("destination", SortOrder.ASCENDING), factory.sort("hour", SortOrder.ASCENDING) });

				// Getting a feature source to the OD table
				SimpleFeatureSource source = dataStore.getFeatureSource("od-trucksTest");

				// Extracting the features into a collection.
				odMatrix = source.getFeatures(query);
			}

			// Creating data filter with the requested OD and time bin..
			FilterFactory2 factory = new FilterFactoryImpl();
			ArrayList<Filter> filters = new ArrayList<Filter>();
			filters.add(CQL.toFilter(String.format("origin = %s", originZoneId)));
			filters.add(CQL.toFilter(String.format("destination = %s", destinationZoneId)));
			filters.add(CQL.toFilter(String.format("hour = %d", timeBin)));

			SimpleFeatureCollection filteredOD = odMatrix.subCollection(factory.and(filters));

			// Extracts the flow from the first feature (assuming 1 row).
			SimpleFeatureIterator it = filteredOD.features();
			try {
				if (it.hasNext()) {
					SimpleFeature feature = it.next();
					demand = (Float) feature.getAttribute("flow");
				}
			} finally {
				it.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CQLException e) {
			throw new RuntimeException(e);
		}

		return demand;
	}

	public ArrayList getDemandFromDatabase(int idRow) {
		double demand = 0;
		ArrayList<String> fromDatabase = new ArrayList<String>();
		fromDatabase.clear();

		try {
			// Checking if the OD matrix has been loaded.
			if (odMatrix == null) {
				// Setting parameters for the database connection.
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("dbtype", "postgis");
				params.put("user", username);
				params.put("passwd", password);
				params.put("host", host);
				params.put("port", port);
				params.put("database", "norrkoping");
				params.put("schema", "odnrkpg");
				params.put("Expose primary keys", "true");

				// Creates a datasource to the database.
				DataStore dataStore = DataStoreFinder.getDataStore(params);

				// Creating data filter.
				FilterFactory2 factory = new FilterFactoryImpl();

				ArrayList<Filter> filters = new ArrayList<Filter>();
				// filters.add(CQL.toFilter(String.format("id = %s",idRow)));

				Query query = new Query("od_hourly_visum", factory.and(filters));

				// Getting a feature source to the OD table
				SimpleFeatureSource source = dataStore.getFeatureSource("od_hourly_visum");

				// Extracting the features into a collection.
				odMatrix = source.getFeatures(query);
				System.out.println("ONLY ONCE");
			}

			// Creating data filter with the requested OD and time bin..
			FilterFactory2 factory = new FilterFactoryImpl();
			ArrayList<Filter> filters = new ArrayList<Filter>();
			filters.add(CQL.toFilter(String.format("id = %s", idRow)));

			SimpleFeatureCollection filteredOD = odMatrix.subCollection(factory.and(filters));

			// Extracts the flow from the first feature (assuming 1 row).
			SimpleFeatureIterator it = filteredOD.features();
			try {
				if (it.hasNext()) {
					SimpleFeature feature = it.next();

					fromDatabase.add(feature.getAttribute("origin").toString());
					fromDatabase.add(feature.getAttribute("destination").toString());
					fromDatabase.add(feature.getAttribute("h").toString());
					fromDatabase.add(feature.getAttribute("mean").toString());
				}
			} finally {
				it.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CQLException e) {
			throw new RuntimeException(e);
		}

		return fromDatabase;
	}

	public int getOdSize() {
		int size = 0;

		try {
			// Checking if the OD matrix has been loaded.
			if (odMatrixSize == null) {
				// Setting parameters for the database connection.
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("dbtype", "postgis");
				params.put("user", username);
				params.put("passwd", password);
				params.put("host", host);
				params.put("port", port);
				params.put("database", "norrkoping");
				params.put("schema", "odnrkpg");
				params.put("Expose primary keys", "true");

				// Creates a datasource to the database.
				DataStore dataStore = DataStoreFinder.getDataStore(params);

				// Creating data filter.
				FilterFactory2 factory = new FilterFactoryImpl();

				ArrayList<Filter> filters = new ArrayList<Filter>();

				Query query = new Query("od_hourly_visum", factory.and(filters));
				query.setSortBy(new SortBy[] { factory.sort("id", SortOrder.ASCENDING) });

				// Getting a feature source to the OD table
				SimpleFeatureSource source = dataStore.getFeatureSource("od_hourly_visum");

				// Extracting the features into a collection.
				odMatrixSize = source.getFeatures(query);

				SimpleFeatureCollection filteredOD = odMatrixSize;
				size = filteredOD.size();
				System.out.println("SIZE CRAZY" + size);

			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return size;
	}

	public void createArrayListsFromDatabase() {
		// Checking if the zones has been loaded.
		numberOfID = 0;

		// Setting parameters for the database connection.
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dbtype", "postgis");
		params.put("user", username);
		params.put("passwd", password);
		params.put("host", host);
		params.put("port", port);
		params.put("database", "norrkoping");
		params.put("schema", "sfs");
		params.put("Expose primary keys", "true");

		try {
			// Creates a data source to the database.
			DataStore dataStore = DataStoreFinder.getDataStore(params);

			// Creating data filter.
			FilterFactory2 factory = new FilterFactoryImpl();
			ArrayList<Filter> filters = new ArrayList<Filter>();
			Query query = new Query("od-trucksTest", factory.and(filters));

			// query.setSortBy(new SortBy[] {factory.sort("od-trucksTest",
			// SortOrder.ASCENDING)});

			// Getting a feature source to the zone table.
			SimpleFeatureSource source = dataStore.getFeatureSource("od-trucksTest");

			// Extracting the features into a collection.
			SimpleFeatureCollection collection = source.getFeatures(query);

			// Loops through the collection and caches the zone ids.
			SimpleFeatureIterator it = collection.features();
			try {
				while (it.hasNext()) {

					SimpleFeature feature = it.next();

					idList.add(Integer.valueOf(feature.getAttribute("id").toString()));
					origList.add(feature.getAttribute("origin").toString());
					destList.add(feature.getAttribute("destination").toString());
					siteId.add(Integer.valueOf(feature.getAttribute("siteId").toString()));
					hourList.add(Integer.valueOf(feature.getAttribute("hour").toString()));
					demandList.add(Double.parseDouble(feature.getAttribute("flow").toString()));
					numberOfID += 1;

				}
			}

			// Releasing the database connection.
			finally {
				it.close();

				if (source.getDataStore() != null) {
					source.getDataStore().dispose();
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void createArrayListsWorkers() {
		// Checking if the zones has been loaded.
		numberOfID = 0;

		// Setting parameters for the database connection.
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dbtype", "postgis");
		params.put("user", username);
		params.put("passwd", password);
		params.put("host", host);
		params.put("port", port);
		params.put("database", "norrkoping");
		params.put("schema", "sfs");
		params.put("Expose primary keys", "true");

		try {
			// Creates a data source to the database.
			DataStore dataStore = DataStoreFinder.getDataStore(params);

			// Creating data filter.
			FilterFactory2 factory = new FilterFactoryImpl();
			ArrayList<Filter> filters = new ArrayList<Filter>();
			Query query = new Query("odWorkers", factory.and(filters));

			// Getting a feature source to the zone table.
			SimpleFeatureSource source = dataStore.getFeatureSource("odWorkers");

			// Extracting the features into a collection.
			SimpleFeatureCollection collection = source.getFeatures(query);

			// Loops through the collection and caches the zone ids.
			SimpleFeatureIterator it = collection.features();
			try {
				while (it.hasNext()) {
					SimpleFeature feature = it.next();

					originWorker.add(Integer.valueOf(feature.getAttribute("origin").toString()));
					destWorker.add(Integer.valueOf(feature.getAttribute("destination").toString()));
					flowWorker.add(Integer.valueOf(feature.getAttribute("flow").toString()));

				}
			}

			// Releasing the database connection.
			finally {
				it.close();

				if (source.getDataStore() != null) {
					source.getDataStore().dispose();
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void workerInformation() {

		// Setting parameters for the database connection.
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dbtype", "postgis");
		params.put("user", username);
		params.put("passwd", password);
		params.put("host", host);
		params.put("port", port);
		params.put("database", "norrkoping");
		params.put("schema", "sfs");
		params.put("Expose primary keys", "true");

		try {
			// Creates a data source to the database.
			DataStore dataStore = DataStoreFinder.getDataStore(params);

			// Getting a feature source to the zone table.
			SimpleFeatureSource source = dataStore.getFeatureSource("workers_info");

			// Extracting the features into a collection.
			SimpleFeatureCollection collection = source.getFeatures();

			// Loops through the collection and caches the zone ids.
			SimpleFeatureIterator it = collection.features();
			try {
				while (it.hasNext()) {
					SimpleFeature feature = it.next();

					workerZone.add(feature.getAttribute("zone_type").toString());
					workerTransport.add(feature.getAttribute("transport_mode").toString());
					workerToWork.add(Double.parseDouble(feature.getAttribute("departure_home").toString()));
					workerToHome.add(Double.parseDouble(feature.getAttribute("departure_work").toString()));

				}
			}

			// Releasing the database connection.
			finally {
				it.close();

				if (source.getDataStore() != null) {
					source.getDataStore().dispose();
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void getSiteCoordinatesFromDatabase() {

		// Setting parameters for the database connection.
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dbtype", "postgis");
		params.put("user", username);
		params.put("passwd", password);
		params.put("host", host);
		params.put("port", port);
		params.put("database", "norrkoping");
		params.put("schema", "sfs");
		params.put("Expose primary keys", "true");

		try {
			// Creates a data source to the database.
			DataStore dataStore = DataStoreFinder.getDataStore(params);

			// Creating data filter.

			// Getting a feature source to the zone table.
			SimpleFeatureSource source = dataStore.getFeatureSource("site");

			SimpleFeatureCollection collection1 = source.getFeatures();
			// Extracting the features into a collection.

			// Loops through the collection and caches the zone ids.
			SimpleFeatureIterator it = collection1.features();
			try {
				while (it.hasNext()) {
					SimpleFeature feature = it.next();
					String s = feature.getAttribute("point_geom").toString();
					Pattern regex = Pattern.compile("(\\d+(?:\\.\\d+)?)");
					Matcher matcher = regex.matcher(s);
					int counter = 0;
					while (matcher.find()) {
						if (counter == 0) {
							siteX.put(feature.getAttribute("id").toString(), Double.parseDouble(matcher.group(1)));
							counter = counter + 1;
						}
						if (counter == 1) {
							siteY.put(feature.getAttribute("id").toString(), Double.parseDouble(matcher.group(1)));
						}

					}

					// siteCoordinates.add(feature.getAttribute("point_geom").toString());

				}
			}

			// Releasing the database connection.
			finally {
				it.close();

				if (source.getDataStore() != null) {
					source.getDataStore().dispose();
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void getStoreCoordinates() {

		// Setting parameters for the database connection.
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dbtype", "postgis");
		params.put("user", username);
		params.put("passwd", password);
		params.put("host", host);
		params.put("port", port);
		params.put("database", "norrkoping");
		params.put("schema", "sfs");
		params.put("Expose primary keys", "true");

		try {
			// Creates a data source to the database.
			DataStore dataStore = DataStoreFinder.getDataStore(params);

			// Getting a feature source to the zone table.
			SimpleFeatureSource source = dataStore.getFeatureSource("suppliers");

			SimpleFeatureCollection collection = source.getFeatures();
			// Extracting the features into a collection.

			// Loops through the collection and caches the zone ids.
			SimpleFeatureIterator it = collection.features();
			try {
				while (it.hasNext()) {
					// System.out.println("worksss");
					SimpleFeature feature = it.next();
					storeX.put(feature.getAttribute("id").toString(),
							Double.parseDouble(feature.getAttribute("x-coord").toString()));
					storeY.put(feature.getAttribute("id").toString(),
							Double.parseDouble(feature.getAttribute("y-coord").toString()));

				}
			}

			// Releasing the database connection.
			finally {
				it.close();

				if (source.getDataStore() != null) {
					source.getDataStore().dispose();
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public ArrayList<Integer> getIdList() {

		return idList;
	}

	public ArrayList<String> getOriginList() {

		return origList;
	}

	public ArrayList<String> getDestList() {

		return destList;
	}

	public ArrayList<Integer> getHourList() {

		return hourList;
	}

	public ArrayList<Double> getDemandList() {

		return demandList;
	}

	public ArrayList<Integer> getSiteIDList() {
		return siteId;
	}

	public ArrayList<Integer> getoriginWorker() {
		return originWorker;
	}

	public ArrayList<Integer> getDestWorker() {
		return destWorker;
	}

	public ArrayList<Integer> getFlowWorker() {
		return flowWorker;
	}

	public HashMap<String, Double> getSiteCoordinatesX() {

		return siteX;
	}

	public HashMap<String, Double> getSiteCoordinatesY() {

		return siteY;
	}

	public HashMap<String, Double> getStoreCoordinatesX() {

		return storeX;
	}

	public HashMap<String, Double> getStoreCoordinatesY() {

		return storeY;
	}

	public int getTotalDemand() {
		for (int i = 0; i < demandList.size(); i++) {

			demand += Math.ceil(demandList.get(i));
		}

		return demand;
	}

	public int getNumberOfIDrows() {
		return numberOfID;
	}

	public ArrayList<String> getWorkerZoneType() {
		return workerZone;
	}

	public ArrayList<String> getWorkerTransportMode() {
		return workerTransport;
	}

	public ArrayList<Double> getWorkerHomeDeparture() {
		return workerToWork;
	}

	public ArrayList<Double> getWorkerWorkDeparture() {
		return workerToHome;
	}

	public static void main(String[] args) {
		// Creating a DatabaseODMatrix object with credentials.
		// String user="";
		// String passwd="";
		// DatabaseODMatrix od = new DatabaseODMatrix(user, passwd, "localhost", 5432);
		// // 5455);
		// od.workerInformation();

		LocalTime tick = LocalTime.now();
		/*
		 * // Getting all zone ids. Collection<String> allZones = od.getAllZoneIds();
		 * 
		 * LocalTime tock = LocalTime.now();
		 * System.out.println("Loading zones (first time) took " +
		 * java.time.temporal.ChronoUnit.MILLIS.between(tick, tock) + " ms");
		 * System.out.println("Total number of zones: " + allZones.size() + "\n");
		 * 
		 * tick = LocalTime.now();
		 * 
		 * // Getting all zone ids. allZones = od.getAllZoneIds();
		 * 
		 * tock = LocalTime.now();
		 * System.out.println("Loading zones (second time) took " +
		 * java.time.temporal.ChronoUnit.MILLIS.between(tick, tock) + " ms");
		 * System.out.println("Total number of zones: " + allZones.size() + "\n");
		 * 
		 * tick = LocalTime.now();
		 * 
		 * // Getting demand. double demand = od.getDemandPerHour("1", "2", 0);
		 * 
		 * 
		 * tock = LocalTime.now();
		 * System.out.println("Loading demand (first time) took " +
		 * java.time.temporal.ChronoUnit.MILLIS.between(tick, tock) + " ms");
		 * System.out.println(String.
		 * format("Demand between 1 and 2 at time bin 0 is %.2f.\n", demand)); tick =
		 * LocalTime.now();
		 * 
		 * // Getting demand, connective queries is faster than the first one. demand =
		 * od.getDemandPerHour("1", "2", 8);
		 * 
		 * tock = LocalTime.now();
		 * System.out.println("Loading demand (second time) took " +
		 * java.time.temporal.ChronoUnit.MILLIS.between(tick, tock) + " ms");
		 * System.out.println(String.
		 * format("Demand between 1 and 2 at time bin 8 is %.2f.\n", demand));
		 */

	}
}