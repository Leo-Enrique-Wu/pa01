package component;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.*;

import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.correlation.*;
import org.knowm.xchart.*;
import org.knowm.xchart.XYSeries.*;
import org.knowm.xchart.style.Styler.*;

import businessObject.*;

public class ClusterPlotter {

	public Map<String, List<List<Double>>> reduceDimByPca(Integer k, Map<String, Set<DocItem>> clusters) {

		Map<String, List<List<Double>>> lowerDimClusters = new HashMap<>();

		List<Integer> clusterItemNums = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		List<DocItem> items = new ArrayList<>();
		for (Entry<String, Set<DocItem>> cluster : clusters.entrySet()) {
			String label = cluster.getKey();
			Set<DocItem> clusterItems = cluster.getValue();
			clusterItemNums.add(clusterItems.size());
			items.addAll(clusterItems);
			labels.add(label);
		}

		int attributeDimNum = items.get(0).getTermFreqs().keySet().size();
		int dataptDimNum = items.size();

		// create points in a double array
		double[][] pointsArray = new double[dataptDimNum][attributeDimNum];
		for (int i = 0; i < dataptDimNum; i++) {

			DocItem item = items.get(i);
			Map<String, Double> termFreqs = item.getTermFreqs();

			int j = 0;
			for (Entry<String, Double> termFreq : termFreqs.entrySet()) {

				Double freq = termFreq.getValue();
				pointsArray[i][j] = freq;
				j++;

			}
		}

		// create real matrix
		RealMatrix realMatrix = MatrixUtils.createRealMatrix(pointsArray);

		// create covariance matrix of points, then find eigen vectors
		Covariance covariance = new Covariance(realMatrix);
		RealMatrix covarianceMatrix = covariance.getCovarianceMatrix();
		EigenDecomposition ed = new EigenDecomposition(covarianceMatrix);

		double[][] valueMatrix = new double[attributeDimNum][k];
		for (int j = 0; j < k; j++) {

			RealVector eigenVector = ed.getEigenvector(j);
			double[] values = eigenVector.toArray();
			for (int i = 0; i < values.length; i++) {
				double value = values[i];
				valueMatrix[i][j] = value;
			}

		}
		RealMatrix pcaMatrix = MatrixUtils.createRealMatrix(valueMatrix);
		RealMatrix result = realMatrix.multiply(pcaMatrix);
		

//		System.out.println(result.getColumnDimension());

		double[][] data2dArr = result.getData();
		int clusterCount = 0;
		int count = 0;
		List<List<Double>> lowerDimCluster = new ArrayList<>();
		String label = labels.get(clusterCount);
		lowerDimClusters.put(label, lowerDimCluster);
		for (double[] dataArr : data2dArr) {

			count++;
			while (count > clusterItemNums.get(clusterCount)) {
				clusterCount++;
				count = 1;
				label = labels.get(clusterCount);
				lowerDimCluster = new ArrayList<>();
				lowerDimClusters.put(label, lowerDimCluster);
			}

			List<Double> pt = new ArrayList<>();
			for (double data : dataArr) {
				pt.add(data);
			}
			lowerDimCluster.add(pt);
		}

		return lowerDimClusters;

	}

	public XYChart getChart(Map<String, List<List<Double>>> labelClusters) {

		// Create Chart
		XYChart chart = new XYChartBuilder().width(800).height(600).build();

		// Customize Chart
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
		chart.getStyler().setChartTitleVisible(false);
		chart.getStyler().setLegendPosition(LegendPosition.InsideSW);
		chart.getStyler().setMarkerSize(16);

		// Series
		for (Entry<String, List<List<Double>>> labelCluster : labelClusters.entrySet()) {

			String label = labelCluster.getKey();
			List<List<Double>> pts = labelCluster.getValue();
			if (pts.isEmpty()) {
				continue;
			}

			List<Double> xData = new LinkedList<Double>();
			List<Double> yData = new LinkedList<Double>();
			for (List<Double> pt : pts) {
				xData.add(pt.get(0));
				yData.add(pt.get(1));
			}
			chart.addSeries(label, xData, yData);
		}

		return chart;
	}

	public void plot(String title, Map<String, Set<DocItem>> labelClusters) {

		Integer n = 2;
		Map<String, List<List<Double>>> lowerDimClusters = this.reduceDimByPca(n,
				labelClusters);

//		for (Entry<String, List<List<Double>>> cluster : lowerDimClusters.entrySet()) {

//			String label = cluster.getKey();
//			List<List<Double>> pts = cluster.getValue();
//			System.out.println("Cluster label: " + label);
//			for (List<Double> pt : pts) {
//				System.out.println(pt);
//			}

//		}

		XYChart chart = this.getChart(lowerDimClusters);
		new SwingWrapper<XYChart>(chart).displayChart(title);

	}

	public static void main(String[] args) throws Exception {

		// Read documents from files
		Path currentRelativePath = Paths.get("");
		String currentPathStr = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current absolute path is: " + currentPathStr);

		String docTermMatrixFilePath = currentPathStr +
				"/analyze/output/docTermMatrix.csv";
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

		DataClusterer clusterer = new DataClusterer();
		SimilarityCalculator simCalculator = new CosineSimCalculator();
		int k = 3;
		Set<Set<DocItem>> clusters = clusterer.clusterByKMeans(k, simCalculator,
				docTermMatrix);

		Map<String, Set<DocItem>> labelClusters = new HashMap<>();
		for (Set<DocItem> cluster : clusters) {
			String clusterLabel = cluster.iterator().next().getClusterLabel();
			labelClusters.put(clusterLabel,
					cluster);
		}

		ClusterPlotter plotter = new ClusterPlotter();
		Integer n = 2;
		Map<String, List<List<Double>>> lowerDimClusters = plotter.reduceDimByPca(n,
				labelClusters);

		for (Entry<String, List<List<Double>>> cluster : lowerDimClusters.entrySet()) {

			String label = cluster.getKey();
			List<List<Double>> pts = cluster.getValue();
			System.out.println("Cluster label: " + label);
			for (List<Double> pt : pts) {
				System.out.println(pt);
			}

		}

		XYChart chart = plotter.getChart(lowerDimClusters);
		chart.setTitle("ABC");
		new SwingWrapper<XYChart>(chart).displayChart("ABC");

	}

}
