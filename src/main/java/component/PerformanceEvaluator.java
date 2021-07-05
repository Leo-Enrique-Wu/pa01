package component;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.*;

import businessObject.*;

public class PerformanceEvaluator {

	public static List<List<String>> genPermutations(List<String> elements) {

		List<List<String>> result = new ArrayList<>();

		// base case
		// size = 1
		if (elements.size() == 1) {
			List<String> possibility = new ArrayList<>(elements);
			result.add(possibility);
			return result;
		}

		// generate permutations for the rest n - 1 elements(recursive method)
		String firstElement = elements.get(0);
		List<String> restElements = elements.subList(1, elements.size());
		List<List<String>> permutations_n_minus_1 = genPermutations(restElements);

		for (List<String> permutation_n_minus_1 : permutations_n_minus_1) {
			for (int i = 0; i <= restElements.size(); i++) {

				LinkedList<String> permutation = new LinkedList<>(permutation_n_minus_1);
				if (i != restElements.size()) {
					permutation.add(i, firstElement);
				} else {
					permutation.addLast(firstElement);
				}
				result.add(permutation);

			}
		}
		return result;

	}

	public static void eval(Set<Set<DocItem>> clusters) {

		// Print Confusion Matrix

		// Gather actual cluster labels
		Set<String> actualClusterLabels = new HashSet<>();
		for (Set<DocItem> cluster : clusters) {
			for (DocItem item : cluster) {
				actualClusterLabels.add(item.getActualClusterLabel());
			}
		}
		System.out.println(actualClusterLabels);

		List<String> actualClusterLabelList = new ArrayList<>(actualClusterLabels);
		List<List<String>> permutations = genPermutations(actualClusterLabelList);
		System.out.println("Permutations: " + permutations);

		List<Set<DocItem>> clusterList = new ArrayList<>(clusters);
		int docNum = 0;
		for (Set<DocItem> cluster : clusterList) {
			docNum += cluster.size();
		}
		System.out.println("DocNum = " + docNum);

		List<PerformanceRecord> perfRecords = new ArrayList<>();
		for (List<String> permutation : permutations) {

			for (int i = 0; i < permutation.size(); i++) {

				int predictCorrectCount = 0;
				String clusterLabel = permutation.get(i);
				Set<DocItem> cluster = clusterList.get(i);
				for (DocItem docItem : cluster) {
					if (clusterLabel.equals(docItem.getActualClusterLabel())) {
						predictCorrectCount++;
					}
				}

				double performance = (predictCorrectCount * 1.0) / docNum;
				PerformanceRecord perfRecord = new PerformanceRecord(permutation, performance);
				perfRecords.add(perfRecord);

			}

		}
		Collections.sort(perfRecords, new Comparator<PerformanceRecord>() {

			@Override
			public int compare(PerformanceRecord o1, PerformanceRecord o2) {
				Double o1Performance = o1.getPerformance();
				Double o2Performance = o2.getPerformance();
				return (o1Performance.compareTo(o2Performance) != 0) ? (o1Performance.compareTo(o2Performance) * -1)
						: 1;
			}

		});

		for (PerformanceRecord record : perfRecords) {
			System.out.println(record);
		}

		List<String> confusionMatrixOutputs = new ArrayList<>();
		StringBuilder confusionMatrixOutputSb = new StringBuilder();
		for (String actualClusterLabel : actualClusterLabels) {
			confusionMatrixOutputSb.append(",");
			confusionMatrixOutputSb.append(actualClusterLabel);
		}
		confusionMatrixOutputs.add(confusionMatrixOutputSb.toString());
		confusionMatrixOutputSb.delete(0, confusionMatrixOutputSb.length());

		List<String> predictedClusterLabels = perfRecords.get(0).getPermutation();
		Map<String, Set<DocItem>> predictedLaceledClusters = new HashMap<>();
		Map<String, Map<String, Integer>> confusionMatrix = new HashMap<>();
		for (int i = 0; i < predictedClusterLabels.size(); i++) {

			String predictedClusterLabel = predictedClusterLabels.get(i);
			Set<DocItem> cluster = clusterList.get(i);
			predictedLaceledClusters.put(predictedClusterLabel, cluster);

			Map<String, Integer> actualClusterCounts = new HashMap<>();
			for (String actualClusterLabel : actualClusterLabels) {
				actualClusterCounts.put(actualClusterLabel, Integer.valueOf(0));
			}

			for (DocItem item : cluster) {
				String actualClusterLabel = item.getActualClusterLabel();
				Integer count = actualClusterCounts.get(actualClusterLabel);
				count++;
				actualClusterCounts.put(actualClusterLabel, count);
			}
			confusionMatrix.put(predictedClusterLabel, actualClusterCounts);

			confusionMatrixOutputSb.append(predictedClusterLabel);
			for (String actualClusterLabel : actualClusterLabels) {
				confusionMatrixOutputSb.append(",");
				Integer count = actualClusterCounts.get(actualClusterLabel);
				confusionMatrixOutputSb.append(count);
			}
			confusionMatrixOutputs.add(confusionMatrixOutputSb.toString());
			confusionMatrixOutputSb.delete(0, confusionMatrixOutputSb.length());

		}

		for (String output : confusionMatrixOutputs) {
			System.out.println(output);
		}

		// Compute performance measurements(precision, recall, F1-score)
		Map<String, PerfMeasurement> clusterPerfMeasures = new HashMap<>();
		for (String clusterLabel : actualClusterLabels) {
			PerfMeasurement perfMeasure = new PerfMeasurement(clusterLabel);
			clusterPerfMeasures.put(clusterLabel, perfMeasure);
		}

		for (Entry<String, Map<String, Integer>> predictedResult : confusionMatrix.entrySet()) {

			String predictedClusterLabel = predictedResult.getKey();
			Map<String, Integer> predictedRecord = predictedResult.getValue();

			for (Entry<String, Integer> actualResult : predictedRecord.entrySet()) {

				String actualClusterLabel = actualResult.getKey();
				Integer count = actualResult.getValue();

				for (String clusterLabel : actualClusterLabels) {

					PerfMeasurement perfMeasure = clusterPerfMeasures.get(clusterLabel);
					if (actualClusterLabel.equals(clusterLabel)) {
						if (predictedClusterLabel.equals(clusterLabel)) {
							// TP
							perfMeasure.addTP(count);
						} else {
							// FN
							perfMeasure.addFN(count);
						}
					} else {
						// not actualClusterLabel
						if (predictedClusterLabel.equals(clusterLabel)) {
							// FP
							perfMeasure.addFP(count);
						} else {
							// TN
							perfMeasure.addTN(count);
						}
					}

				}

			}

		}

		List<String> perfOutputs = new ArrayList<>();
		perfOutputs.add(",Precision,Recall,F1-score");
		for (Entry<String, PerfMeasurement> clusterPerfMeasure : clusterPerfMeasures.entrySet()) {

			String clusterLabel = clusterPerfMeasure.getKey();
			PerfMeasurement measurement = clusterPerfMeasure.getValue();
			Double percision = measurement.getPercision();
			Double recall = measurement.getRecall();
			Double f1Score = measurement.getF1Score();
			String output = String.format("%s,%f,%f,%f", clusterLabel, percision, recall, f1Score);
			perfOutputs.add(output);

		}

		for (String output : perfOutputs) {
			System.out.println(output);
		}

		// Read documents from files
		Path currentRelativePath = Paths.get("");
		String currentPathStr = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current absolute path is: " + currentPathStr);
		String outputFilePathStr = currentPathStr + "/analyze/output";
		String perfEvalOutputFilePathStr = outputFilePathStr + "/perfEval.csv";
		File perfEvalOutputFile = new File(perfEvalOutputFilePathStr);
		try {

			if (perfEvalOutputFile.exists()) {
				perfEvalOutputFile.delete();
				perfEvalOutputFile.createNewFile();
			}

			FileWriter fw = new FileWriter(perfEvalOutputFile);
			BufferedWriter bw = new BufferedWriter(fw);
			
			try {
				
				bw.write("Confusion Matrix");
				bw.newLine();
				for (String output : confusionMatrixOutputs) {
					bw.write(output);
					bw.newLine();
				}
				bw.newLine();
				
				bw.write("Performance Evaluation");
				bw.newLine();
				for (String output : perfOutputs) {
					bw.write(output);
					bw.newLine();
				}
				bw.newLine();
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				bw.close();
				fw.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {

		// Read documents from files
		Path currentRelativePath = Paths.get("");
		String currentPathStr = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current absolute path is: " + currentPathStr);

		String docTermMatrixFilePath = currentPathStr + "/analyze/output/docTermMatrix.csv";
		File file = new File(docTermMatrixFilePath);

		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		String line = null;
		int count = 1;
		Map<String, Map<String, Double>> docTermMatrix = new HashMap<>();
		List<String> attributes = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			if (count == 1) {
				String[] cols = line.split(",");
				for (int i = 1; i < cols.length; i++) {
					attributes.add(cols[i]);
				}
			} else {
				String[] cols = line.split(",");
				String docLabel = cols[0];

				Map<String, Double> attributeValues = new HashMap<>();
				for (int i = 1; i < cols.length; i++) {
					String attribute = attributes.get(i - 1);
					Double value = Double.valueOf(cols[i]);
					attributeValues.put(attribute, value);
				}
				docTermMatrix.put(docLabel, attributeValues);

			}
			count++;
		}

		br.close();
		fr.close();

		// System.out.println(docTermMatrix);
		DataClusterer clusterer = new DataClusterer();
		SimilarityCalculator simCalculator = new EuclideanDistSimCalculator();
		int k = 3;
		Set<Set<DocItem>> clusters = clusterer.clusterByKMeans(k, simCalculator, docTermMatrix);

		PerformanceEvaluator.eval(clusters);

	}

}
