package businessObject;

import java.util.*;

public class PerformanceRecord {

	private List<String> permutation = new ArrayList<>();

	private Double performance = 0d;

	public PerformanceRecord(List<String> permutation, Double performance) {
		this.permutation = permutation;
		this.performance = performance;
	}

	@Override
	public String toString() {
		return String.format("[Permutaion(%s): Performance = %f]", this.permutation.toString(), this.performance);
	}

	public List<String> getPermutation() {
		return permutation;
	}

	public Double getPerformance() {
		return performance;
	}

}
