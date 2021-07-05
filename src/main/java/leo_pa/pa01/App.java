package leo_pa.pa01;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import businessObject.*;
import component.*;

public class App {

	public static final String CONFIG_SIM_CAL_COSINE = "C";

	public static final String CONFIG_SIM_CAL_EUCLIDEAN = "E";

	public static List<String> simCalculatorTypes = new ArrayList<>();
	static {
		simCalculatorTypes.add(CONFIG_SIM_CAL_COSINE);
		simCalculatorTypes.add(CONFIG_SIM_CAL_EUCLIDEAN);
	}

	public static void main(String[] args) {

		Path currentRelativePath = Paths.get("");
		String currentPathStr = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current absolute path is: " + currentPathStr);

		String inputFilePath = currentPathStr + "/analyze/input/data.txt";
		File inputFile = new File(inputFilePath);
		Properties props = new Properties();

		String srcDocRootPath = currentPathStr + "/analyze/src";
		String outputFilePathStr = currentPathStr + "/analyze/output";
		String simCalculatorType = "C";

		FileReader fr;
		try {

			fr = new FileReader(inputFile);
			props.load(fr);

			if (props.containsKey("INPUT")) {
				srcDocRootPath = props.getProperty("INPUT");
				System.out.println("Updated source file path from input data.");
			}

			if (props.containsKey("OUTPUT")) {
				outputFilePathStr = props.getProperty("OUTPUT");
				System.out.println("Updated output file path from input data.");
			}

			if (props.containsKey("SIM_CAL_TYPE")) {
				String propertyValue = props.getProperty("SIM_CAL_TYPE");
				if (simCalculatorTypes.contains(propertyValue)) {
					simCalculatorType = propertyValue;
					System.out.println("Updated similarity calculator type from input data.");
				} else {
					System.out.println("Undefined similarity calculator type, use default instead: " + propertyValue);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Input: " + srcDocRootPath);
		System.out.println("Output: " + outputFilePathStr);
		System.out.println("Similarity Calculator: " + simCalculatorType);

		DescriptiveModelBuilder modelBuilder = new DescriptiveModelBuilder();
		Map<String, Map<String, Double>> docTermMatrix = modelBuilder.genDocTermMatrix(srcDocRootPath,
				outputFilePathStr);

		DataClusterer clusterer = new DataClusterer();
		SimilarityCalculator simCalculator = null;
		if (CONFIG_SIM_CAL_EUCLIDEAN.equals(simCalculatorType)) {
			simCalculator = new EuclideanDistSimCalculator();
		} else {
			simCalculator = new CosineSimCalculator();
		}
		int k = 3;
		Set<Set<DocItem>> clusters = clusterer.clusterByKMeans(k, simCalculator, docTermMatrix);

		PerformanceEvaluator.eval(clusters);

		ClusterPlotter plotter = new ClusterPlotter();
		Map<String, Set<DocItem>> labelClusters = new HashMap<>();
		List<DocItem> oriItems = new ArrayList<>();
		for (Set<DocItem> cluster : clusters) {
			String topic = DocItem.extractTopic(cluster);
			labelClusters.put(topic, cluster);
			oriItems.addAll(cluster);
		}
		plotter.plot("Output Clusters", labelClusters);

		labelClusters.clear();
		clusters = DocItem.seperateByActualClusterLabel(oriItems);
		for (Set<DocItem> cluster : clusters) {
			String topic = DocItem.extractTopic(cluster);
			labelClusters.put(topic, cluster);
		}
		plotter.plot("Original Documents Clusters", labelClusters);

	}
}
