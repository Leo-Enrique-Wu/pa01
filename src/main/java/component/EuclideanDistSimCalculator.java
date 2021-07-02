package component;

import java.util.*;

public class EuclideanDistSimCalculator implements SimilarityCalculator {

	@Override
	public Double calculate(Map<String, Double> o1, Map<String, Double> o2) {
		
		Double squreSum = Double.valueOf(0);
		Set<String> attributes = o1.keySet();
		for (String attribute : attributes) {
			
			Double o1AttValue = o1.get(attribute);
			Double o2AttValue = o2.get(attribute);
			Double diffSq = Math.pow(o1AttValue - o2AttValue, 2);
			squreSum += diffSq;
			
		}
		
		return Math.sqrt(squreSum);
		
	}

}
