package stockholm.bicycles.tests.utilityTest;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

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
  
	}
	
	

}
