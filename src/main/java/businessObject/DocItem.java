package businessObject;

import java.util.*;

public class DocItem {

	private String docLabel = null;
	
	private Map<String, Double> termFreqs = new HashMap<>();
	
	private String oriClusterLabel = null;
	
	private String clusterLabel = null;
	
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

	public String getClusterLabel() {
		return clusterLabel;
	}

	public void setClusterLabel(String clusterLabel) {
		if (this.clusterLabel != null) {
			this.oriClusterLabel = this.clusterLabel;
		}
		this.clusterLabel = clusterLabel;
	}

	public String getDocLabel() {
		return docLabel;
	}

	public Map<String, Double> getTermFreqs() {
		return termFreqs;
	}
	
}
