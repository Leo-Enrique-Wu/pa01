package businessObject;

public class PerfMeasurement {

	private String clusterLabel = null;
	
	private Integer tp = Integer.valueOf(0);
	private Integer fn = Integer.valueOf(0);
	private Integer fp = Integer.valueOf(0);
	private Integer tn = Integer.valueOf(0);
	
	public PerfMeasurement(String clusterLabel) {
		this.clusterLabel = clusterLabel;
	}
	
	public void addTP(Integer tp) {
		this.tp += tp;
	}
	
	public void addFN(Integer fn) {
		this.fn += fn;
	}
	
	public void addFP(Integer fp) {
		this.fp += fp;
	}
	
	public void addTN(Integer tn) {
		this.tn += tn;
	}
	
	public Double getPercision() {
		
		Double precision = (this.tp * 1.0) / (this.tp + this.fp);
		return precision;
		
	}
	
	public Double getRecall() {
		
		Double recall = (this.tp * 1.0) / (this.tp + this.fn);
		return recall;
		
	}
	
	public Double getF1Score() {
		
		Double precision = getPercision();
		Double recall = getRecall();
		Double f1Score = 2 * ((precision * recall) / (precision + recall));
		return f1Score;
		
	}

	public String getClusterLabel() {
		return clusterLabel;
	}
	
	
}
