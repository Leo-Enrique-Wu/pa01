package businessObject;

import java.util.*;

public class ClusterDist {

	private String clusterLabel = null;

	private Double dist = null;
	
	private static DistComparator comparator = new DistComparator();

	public ClusterDist(String clusterLabel, Double dist) {
		this.clusterLabel = clusterLabel;
		this.dist = dist;
	}

	public String getClusterLabel() {
		return clusterLabel;
	}

	public Double getDist() {
		return dist;
	}

	public static class DistComparator implements Comparator<ClusterDist> {

		@Override
		public int compare(ClusterDist o1, ClusterDist o2) {
			return (o1.dist.compareTo(o2.dist) != 0) ? o1.dist.compareTo(o2.dist)
					: o1.clusterLabel.compareTo(o2.clusterLabel);
		}

	}

	public static DistComparator getComparator() {
		return comparator;
	}
	
	public static void main(String[] args) {
		
		List<ClusterDist> dists = new ArrayList<>();
		ClusterDist dist1 = new ClusterDist("C1", 1.0);
		ClusterDist dist2 = new ClusterDist("C2", 2.3);
		ClusterDist dist3 = new ClusterDist("C3", 1.0);
		dists.add(dist1);
		dists.add(dist2);
		dists.add(dist3);
		
		Collections.sort(dists, ClusterDist.getComparator());
		for (ClusterDist dist : dists) {
			System.out.println(dist.getClusterLabel());
		}
		
	}

}
