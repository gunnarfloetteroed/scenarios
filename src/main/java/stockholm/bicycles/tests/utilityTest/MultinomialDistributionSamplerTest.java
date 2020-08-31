package stockholm.bicycles.tests.utilityTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import stockholm.bicycles.utility.MultinomialDistributionSampler;
import stockholm.bicycles.utility.MultinomialDistributionSamplerMap;

public class MultinomialDistributionSamplerTest {

	public static void main(String[] args) {
		
//		double[] test = {0.32, 0.68};
//		double[] test2 = Arrays.copyOfRange(test, 0, test.length);
//		test2[1]=2;
//		System.out.println(test.length);
//		System.out.println(test[0]+" , "+test[1]);
//		System.out.println(test2[0]+" , "+test2[1]);
//
//		Random random=new Random();
//		double[] test3 = {0.32, 0.32, 0.32};
//		double nextRandom=random.nextDouble();
//		int index = Arrays.binarySearch(test3, nextRandom);
//		System.out.println("next random is: "+ nextRandom);
//		System.out.println("index is: "+index);
		
		// test MultinomialDistributionSampler
		List<Double> weights = Arrays.asList(2.4,2.0,3.5,1.9,0.2);
		MultinomialDistributionSampler sampler = new MultinomialDistributionSampler(weights);
		Integer[] result=sampler.sampleWithoutReplacement(10);
		
		for (int i=0; i<result.length;i++) {
			System.out.println("sample without replacement is: "+ result[i]);
		}
		
//		double[] originalWeight = sampler.getCdfWeights();
//		for (int i=0; i<originalWeight.length;i++) {
//			System.out.println("weight is: " +originalWeight[i]);
//		}
		
		
		Integer[] resultWithReplacement=sampler.sampleWithReplacement(10);
		for (int i=0; i<resultWithReplacement.length;i++) {
			System.out.println("sample with replacement is: "+ resultWithReplacement[i]);
		}
		
		// test class MultinomialDistributionSamplerMap
		
		HashMap<String, String> testMap= new HashMap<>();
		testMap.put("a", "2.0");
		testMap.put("b", "1.0");
		testMap.put("c", "3.0");
		testMap.put("d", "3.0");
		testMap.put("e", "1.0");
		MultinomialDistributionSamplerMap testSampler= new MultinomialDistributionSamplerMap(testMap);
		String[] sampleResults=testSampler.sampleMapWithoutReplacement(5);
		for (int i=0; i<sampleResults.length;i++) {
			System.out.println("Map sampler's sample without replacement is: "+ sampleResults[i]);
		}
		
	}

}
