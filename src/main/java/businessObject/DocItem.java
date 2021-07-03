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
		this.docLabel = docLabel ;
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
	
	public static Set<Set<DocItem>> seperateByClusterLabel(List<DocItem> docItems){
		
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
			DocItem other = (DocItem)obj;
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
