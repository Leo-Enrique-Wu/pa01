package businessObject;

import java.util.*;
import java.util.Map.*;

public class DocItem {

	private String docLabel = null;

	private Map<String, Double> termFreqs = new HashMap<>();

	private String oriClusterLabel = null;

	private String clusterLabel = null;

	private String actualClusterLabel = null;

	public DocItem(String docLabel, Map<String, Double> termFreqs) {
		this.docLabel = docLabel;
		this.termFreqs = termFreqs;
	}

	public Boolean hasChangedCluster() {

		if (this.oriClusterLabel == null) {
			return Boolean.FALSE;
		} else if (this.oriClusterLabel.equals(this.clusterLabel)) {
			return Boolean.FALSE;
		} else {
			return Boolean.TRUE;
		}

	}

	public static Set<Set<DocItem>> seperateByClusterLabel(List<DocItem> docItems) {

		Set<Set<DocItem>> clusters = new HashSet<>();

		Map<String, Set<DocItem>> labelClusterMap = new HashMap<>();
		for (DocItem docItem : docItems) {
			String clusterLabel = docItem.getClusterLabel();
			Set<DocItem> cluster = labelClusterMap.get(clusterLabel);
			if (cluster == null) {
				cluster = new HashSet<>();
				cluster.add(docItem);
				labelClusterMap.put(clusterLabel, cluster);
			} else {
				cluster.add(docItem);
				labelClusterMap.put(clusterLabel, cluster);
			}
		}

		for (Entry<String, Set<DocItem>> labelCluster : labelClusterMap.entrySet()) {
			Set<DocItem> cluster = labelCluster.getValue();
			clusters.add(cluster);
		}

		return clusters;

	}
	
	public static Set<Set<DocItem>> seperateByActualClusterLabel(List<DocItem> docItems) {

		Set<Set<DocItem>> clusters = new HashSet<>();

		Map<String, Set<DocItem>> labelClusterMap = new HashMap<>();
		for (DocItem docItem : docItems) {
			String clusterLabel = docItem.getActualClusterLabel();
			Set<DocItem> cluster = labelClusterMap.get(clusterLabel);
			if (cluster == null) {
				cluster = new HashSet<>();
				cluster.add(docItem);
				labelClusterMap.put(clusterLabel, cluster);
			} else {
				cluster.add(docItem);
				labelClusterMap.put(clusterLabel, cluster);
			}
		}

		for (Entry<String, Set<DocItem>> labelCluster : labelClusterMap.entrySet()) {
			Set<DocItem> cluster = labelCluster.getValue();
			clusters.add(cluster);
		}

		return clusters;

	}

	public static DocItem getMean(Set<DocItem> docItems) {

		if (docItems == null || docItems.isEmpty()) {
			return null;
		}

		List<DocItem> docItemList = new ArrayList<>(docItems);
		int docNum = docItemList.size();
		String clusterLabel = docItemList.get(0).getClusterLabel();

		Set<String> attributes = docItemList.get(0).getTermFreqs().keySet();
		Map<String, Double> meanTermFreqs = new HashMap<>();
		for (String attribute : attributes) {

			Double newValue = Double.valueOf(0);
			for (DocItem docItem : docItemList) {

				Map<String, Double> termFreqs = docItem.getTermFreqs();
				Double value = termFreqs.get(attribute);
				newValue += value;

			}
			newValue = newValue / docNum;
			meanTermFreqs.put(attribute, newValue);

		}
		DocItem mean = new DocItem(clusterLabel, meanTermFreqs);
		mean.setClusterLabel(clusterLabel);
		return mean;

	}

	public static String extractTopic(Set<DocItem> docItems) {

		String topic = null;
		if (docItems.isEmpty()) {
			topic = "";
			return topic;
		}

		Map<String, Double> totalTermFreqs = new HashMap<>();
		for (DocItem item : docItems) {
			Map<String, Double> termFreqs = item.getTermFreqs();
			for (Entry<String, Double> termFreq : termFreqs.entrySet()) {
				String term = termFreq.getKey();
				Double freq = termFreq.getValue();
				Double currentCumFreq = totalTermFreqs.get(term);
				if (currentCumFreq == null) {
					totalTermFreqs.put(term, freq);
				} else {
					currentCumFreq += freq;
					totalTermFreqs.put(term, currentCumFreq);
				}
			}
		}

		List<TermFreq> sortedTermFreqs = new ArrayList<>();
		for (Entry<String, Double> totalTermFreq : totalTermFreqs.entrySet()) {
			
			String term = totalTermFreq.getKey();
			Double freq = totalTermFreq.getValue();
			TermFreq termFreq = new TermFreq(term, freq);
			sortedTermFreqs.add(termFreq);
			
		}
		Collections.sort(sortedTermFreqs, new Comparator<TermFreq>() {

			@Override
			public int compare(TermFreq o1, TermFreq o2) {
				String o1Term = o1.getTerm();
				Double o1Freq = o1.getFreq();
				String o2Term = o2.getTerm();
				Double o2Freq = o2.getFreq();
				return (o1Freq.compareTo(o2Freq) != 0) ? (o1Freq.compareTo(o2Freq) * -1)
						: o1Term.compareTo(o2Term);
			}

		});
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(sortedTermFreqs.get(i).getTerm());
		}
		topic = sb.toString();

		return topic;

	}

	public String getClusterLabel() {
		return clusterLabel;
	}

	public void setClusterLabel(String clusterLabel) {
		if (this.clusterLabel != null) {
			this.oriClusterLabel = this.clusterLabel;
		}
		this.clusterLabel = clusterLabel;
	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof DocItem)) {
			return false;
		} else {
			DocItem other = (DocItem) obj;
			return this.docLabel.equals(other.docLabel);
		}

	}

	@Override
	public int hashCode() {
		return this.docLabel.hashCode();
	}

	public String getDocLabel() {
		return docLabel;
	}

	public Map<String, Double> getTermFreqs() {
		return termFreqs;
	}

	public String getActualClusterLabel() {
		return actualClusterLabel;
	}

	public void setActualClusterLabel(String actualClusterLabel) {
		this.actualClusterLabel = actualClusterLabel;
	}

	public String getOriClusterLabel() {
		return oriClusterLabel;
	}

}
