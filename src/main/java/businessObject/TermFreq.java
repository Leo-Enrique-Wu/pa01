package businessObject;

public class TermFreq {

	private String term = null;
	
	private Double freq = null;
	
	public TermFreq(String term, Double freq) {
		this.term = term;
		this.freq = freq;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public Double getFreq() {
		return freq;
	}

	public void setFreq(Double freq) {
		this.freq = freq;
	}
	
}
