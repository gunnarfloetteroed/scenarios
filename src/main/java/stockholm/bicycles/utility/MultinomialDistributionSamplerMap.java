package stockholm.bicycles.utility;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.primitives.Doubles;

public class MultinomialDistributionSamplerMap extends MultinomialDistributionSampler {
	private String[] sampleCode;
	
	public MultinomialDistributionSamplerMap(List<Double> weights) {
		super(weights);
		String[] output = new String[weights.size()]; 
		for (int i=0;i<output.length;i++) {
			output[i]=Integer.toString(i+1);
		}
		this.sampleCode=output;
	}

	public MultinomialDistributionSamplerMap(double[] weights) {
		super(weights);
		String[] output = new String[weights.length]; 
		for (int i=0;i<output.length;i++) {
			output[i]=Integer.toString(i+1);
		}
		this.sampleCode=output;
	}
	
	public MultinomialDistributionSamplerMap(Map<String, String> weights) {
		String[] sampleCode = new String[weights.size()];
		double[] weightListFromInput = new double[weights.size()];
		int counter=0;
		for (Entry<String, String> entry : weights.entrySet()) {
			sampleCode[counter]=entry.getKey();

			String element= entry.getValue();
			if(element.isEmpty()) {
				element="0";
			}
			weightListFromInput[counter]=Double.parseDouble(element);
			counter++;
		}
		
		List<Double> weightsList =Doubles.asList(weightListFromInput);
		this.weights = weightsList;
		this.cdfWeights = super.cdfWeights(weightsList);
		this.sum = this.cdfWeights[weightsList.size() - 1];
		this.sampleCode=sampleCode;
	}
	
	public String sampleMap() {
		int index = Arrays.binarySearch(this.cdfWeights, random.nextDouble() * this.sum);
		int sampleIndex = (index >= 0) ? index : (-index - 1);
		return this.sampleCode[sampleIndex];
	}

	public String[] sampleMapWithReplacement(int n) {
		Integer[] samples =super.sampleWithReplacement(n);
		String[] output = new String[samples.length]; 
		for (int i=0;i<samples.length;i++) {
			output[i]=this.sampleCode[samples[i]];
		}
			
		return output;
		
	}
	
	public String[] sampleMapWithoutReplacement(int n) {
		Integer[] samples =super.sampleWithoutReplacement(n);
		String[] output = new String[samples.length]; 
		for (int i=0;i<samples.length;i++) {
			output[i]=this.sampleCode[samples[i]];
		}
		return output;
	}
	
}
