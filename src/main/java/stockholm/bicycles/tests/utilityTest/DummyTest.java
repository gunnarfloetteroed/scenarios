package stockholm.bicycles.tests.utilityTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.NetworkChangeEvent.ChangeType;
import org.matsim.core.network.NetworkChangeEvent.ChangeValue;

public class DummyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		DummyTest test = new DummyTest();
		test.testFunction();
		
//		String path =System.getProperty("user.dir")+"\\input-data\\stockholm\\bicycles\\";
//		path = path.replaceAll("\\\\", "/");
//		System.out.println(path);
	}
	
	public void testFunction(){
        // Creating empty priority queue 
        PriorityQueue<Integer> pQueue = new PriorityQueue<Integer>(); 
  
        // Adding items to the pQueue using add() 
        pQueue.add(10); 
        pQueue.add(20); 
        pQueue.add(15); 
  
        // Printing the top element of PriorityQueue 
        System.out.println(pQueue.poll()); 
  
        // Printing the top element and removing it 
        // from the PriorityQueue container 
        System.out.println(pQueue.poll()); 
        
        String[] test = {"111","222","111","333"};
        String[] unique = Arrays.stream(test).distinct().toArray(String[]::new);
        for (String element : unique) {
        	System.out.println(element);
        }
        
        
        // test random seed functionality
        Random randomSeed = new Random(20);
        Random random = new Random();
        Random random2 = new Random();
        random.setSeed(randomSeed.nextLong());
        random2.setSeed(randomSeed.nextLong());
        
        System.out.println("first random: "+random.nextDouble());
        System.out.println("second random: "+random2.nextDouble());
        
        ArrayList<String> cars = new ArrayList<String>();
        cars.add("Volvo");
        cars.add("BMW");
        cars.add("Ford");
        cars.add("Mazda");
        for (String car : cars) {
        	System.out.println(car);
        }
        System.out.println(cars);
        System.out.println("ArrayList size: "+cars.size());
        
        double a = Double.POSITIVE_INFINITY;
        double b = Double.POSITIVE_INFINITY;
        if(a==b) {
        	 System.out.println("should not happen.");
        }
        
        
//        List<Collection<Node>> searchableMiddleNodes=null;
//        for (Collection<Node> nodes:searchableMiddleNodes) {
//        	System.out.println("should11111");
//        }
        
        
    	PriorityQueue<Dog> queue = new PriorityQueue<Dog>(11, new Comparator<Dog>() {

    		@Override
    		public int compare(Dog o1, Dog o2) {
    			return o1.getSize().compareTo(o2.getSize());
    		}

    	});
    	queue.add(new Dog(3.1,"AA"));
    	queue.add(new Dog(3.1,"BB"));
    	queue.add(new Dog(3.2,"CC"));
    	queue.add(new Dog(3.0,"DD"));
    	System.out.println(queue.poll().getName());
    	System.out.println(queue.poll().getName());
    	System.out.println(queue.poll().getName());
    	System.out.println(queue.poll().getName());
    	NetworkChangeEvent networkChangeEvent = new NetworkChangeEvent(1) ;
    	networkChangeEvent.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  0 ));
    	System.out.println(networkChangeEvent.getFreespeedChange());
    	System.out.println(networkChangeEvent.getFlowCapacityChange());
	}
	
	

}
