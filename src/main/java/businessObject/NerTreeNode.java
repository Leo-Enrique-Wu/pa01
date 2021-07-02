package businessObject;

import java.util.*;

public class NerTreeNode {

	private String fullToken = null;

	private Boolean isNerEnd = false;

	private Boolean isEntityType = false;

	private Map<String, NerTreeNode> subNerTree = new HashMap<>();

	public NerTreeNode() {
	}

	public NerTreeNode(String token, Boolean isEntityType) {
		this.fullToken = token;
		this.isEntityType = isEntityType;
	}

	public NerTreeNode appendChildNode(String nextToken, Boolean isEntityType) {

		NerTreeNode childNode = this.getChildNode(nextToken);
		if (childNode == null) {
			String fullToken = (this.fullToken == null) ? nextToken
					: (this.isEntityType) ? nextToken : String.format("%s %s", this.fullToken, nextToken);
			childNode = new NerTreeNode(fullToken, isEntityType);
			subNerTree.put(nextToken, childNode);
		}
		return childNode;

	}

	public NerTreeNode getChildNode(String nextToken) {
		return subNerTree.get(nextToken);
	}
	
	public Boolean getIsNerEnd() {
		return isNerEnd;
	}

	public void setIsNerEnd(Boolean isNerEnd) {
		this.isNerEnd = isNerEnd;
	}
	
	public String getFullToken() {
		return fullToken;
	}

	public void setFullToken(String fullToken) {
		this.fullToken = fullToken;
	}

	public String toString() {
		return String.format("{%s: isLeaf=%s, child=%s}", (this.fullToken == null) ? "root" : this.fullToken,
				(this.isNerEnd) ? "T" : "F",
				this.subNerTree);
	}

}
