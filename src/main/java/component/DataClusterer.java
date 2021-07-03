package component;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.*;

import businessObject.*;

public class DataClusterer {

	private final static Integer STOP_ITER_COND_MAX_SOFT_ITER_BOUND = Integer.valueOf(100);

	private final static Integer STOP_ITER_COND_MAX_HARD_ITER_BOUND = Integer.valueOf(500);

	private final static Double STOP_ITER_COND_CLUSTER_CHANGED_PERCENT_BOUND = Double.valueOf(0.05);

	public Set<Set<DocItem>> clusterByKMeans(Integer k, SimilarityCalculator simCalculator,
			Map<String, Map<String, Double>> docTermMatrix) {

		Set<Set<DocItem>> clusters = new HashSet<>();

		List<DocItem> docItems = new ArrayList<>();
		for (Entry<String, Map<String, Double>> docTerm : docTermMatrix.entrySet()) {
			String docLabel = docTerm.getKey();
			String actualClusterLabel = docLabel.split("_")[0];
			Map<String, Double> attributeValueMap = docTerm.getValue();
			DocItem docItem = new DocItem(docLabel, attributeValueMap);
			docItem.setActualClusterLabel(actualClusterLabel);
			docItems.add(docItem);
		}

		Integer docNum = docItems.size();
		if (docNum < k) {
			for (DocItem docItem : docItems) {
				Set<DocItem> cluster = new HashSet<>();
				cluster.add(docItem);
				clusters.add(cluster);
			}
			return clusters;
		}

		Set<DocItem> means = new HashSet<>();
		for (int i = 0; i < k; i++) {
			DocItem choice = docItems.get(i);
			String clusterLabel = String.format("PC_%d", i);
			DocItem mean = new DocItem(clusterLabel, choice.getTermFreqs());
			mean.setClusterLabel(clusterLabel);
			means.add(mean);
			System.out
					.println(String.format("Choose doc[%s] to be the mean of %s", choice.getDocLabel(), clusterLabel));
		}

		int iterCount = 0;
		double clusterChangedPercent = Double.valueOf(0);
		do {

			iterCount++;
			System.out.println("Iter " + iterCount + ":");
			for (DocItem docItem : docItems) {

				Map<String, Double> targetAttributeValueMap = docItem.getTermFreqs();
				List<ClusterDist> clusterDists = new ArrayList<>();
				System.out.println(docItem.getDocLabel());

				for (DocItem mean : means) {
					String clusterLabel = mean.getClusterLabel();
					Map<String, Double> meanAttributeValueMap = mean.getTermFreqs();
					Double dist = simCalculator.calculate(targetAttributeValueMap, meanAttributeValueMap);
					ClusterDist clusterDist = new ClusterDist(clusterLabel, dist);
					clusterDists.add(clusterDist);
					System.out.println(String.format("Dist(%s):%f", clusterLabel, dist));
				}

				Collections.sort(clusterDists, ClusterDist.getComparator());
				ClusterDist nearestMean = clusterDists.get(0);
				String newClusterLabel = nearestMean.getClusterLabel();
				docItem.setClusterLabel(newClusterLabel);
				System.out.println(
						String.format("cluster: %s --> %s", docItem.getOriClusterLabel(), docItem.getClusterLabel()));

			}

			int changedClusterCount = 0;
			for (DocItem docItem : docItems) {
				String docLabel = docItem.getDocLabel();
				String oriClusterLabel = docItem.getOriClusterLabel();
				String clusterLabel = docItem.getClusterLabel();
				if (docItem.hasChangedCluster()) {
					changedClusterCount++;
				}
				System.out.println(String.format("[%s] %s", docLabel,
						(docItem.hasChangedCluster()) ? String.format("%s --> %s", oriClusterLabel, clusterLabel)
								: clusterLabel));
			}
			clusterChangedPercent = (changedClusterCount * 1.0) / docNum;
			System.out.println(clusterChangedPercent);

			means.clear();
			Set<Set<DocItem>> newClusters = DocItem.seperateByClusterLabel(docItems);
			for (Set<DocItem> newCluster : newClusters) {
				DocItem newMean = DocItem.getMean(newCluster);
				means.add(newMean);
			}

		} while (!checkStopCondition(iterCount, clusterChangedPercent));

		return clusters;

	}

	private Boolean checkStopCondition(Integer iterCount, double clusterChangedPercent) {

		Boolean result = Boolean.FALSE;
		if (iterCount == 1) {
			return result;
		}

		if (iterCount < STOP_ITER_COND_MAX_SOFT_ITER_BOUND) {
			if (Double.valueOf(0).compareTo(clusterChangedPercent) == 0) {
				result = Boolean.TRUE;
			}
		} else if (iterCount >= STOP_ITER_COND_MAX_SOFT_ITER_BOUND && iterCount < STOP_ITER_COND_MAX_HARD_ITER_BOUND) {
			if (STOP_ITER_COND_CLUSTER_CHANGED_PERCENT_BOUND.compareTo(clusterChangedPercent) >= 0) {
				result = Boolean.TRUE;
			}
		} else {
			// iterCount >= STOP_ITER_COND_MAX_HARD_ITER_BOUND
			result = Boolean.TRUE;
		}

		return result;

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
		clusterer.clusterByKMeans(k, simCalculator, docTermMatrix);

	}

}
