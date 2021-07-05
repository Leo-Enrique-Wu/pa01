package component;

import java.util.*;

public class CosineSimCalculator implements SimilarityCalculator {

	@Override
	public Double calculate(Map<String, Double> o1, Map<String, Double> o2) {
		
		Double innerProduct = Double.valueOf(0);
		Double norm1 = Double.valueOf(0);
		Double norm2 = Double.valueOf(0);
		
		Set<String> attributes = o1.keySet();
		for (String attribute : attributes) {
			
			Double o1AttValue = o1.get(attribute);
			Double o2AttValue = o2.get(attribute);
			innerProduct += o1AttValue * o2AttValue;
			norm1 += Math.pow(o1AttValue, 2);
			norm2 += Math.pow(o2AttValue, 2);
			
		}
		norm1 = Math.sqrt(norm1);
		norm2 = Math.sqrt(norm2);
		
		return innerProduct / (norm1 * norm2);
	}

}
