package kiberaTB;

import sim.field.network.Edge;
import sim.util.Bag;

public class EdgeFunctions {

	public static boolean doesEdgeExist(ResidentTB node1, ResidentTB node2, KiberaTB kibera) {
		
		Bag myConnections = new Bag(kibera.socialNetwork.getEdgesOut(node1));
		if (myConnections != null) {
		
			for (int i = 0; i < myConnections.size(); i++) {
				Edge e = (Edge)(myConnections.get(i));
				ResidentTB otherNode = (ResidentTB) e.getOtherNode(node1);
				if (otherNode == node2) {
					return true;
				}
			}
		}
		
		return false;
		
	}
	
	public static Edge getEdge(ResidentTB node1, ResidentTB node2, KiberaTB kibera) {
		
		Bag myConnections = new Bag(kibera.socialNetwork.getEdgesOut(node1));
		if (myConnections != null) {
		
			for (int i = 0; i < myConnections.size(); i++) {
				Edge e = (Edge)(myConnections.get(i));
				ResidentTB otherNode = (ResidentTB) e.getOtherNode(node1);
				if (otherNode == node2) {
					return e;
				}
			}
		}
		
		return null;
	}
}
