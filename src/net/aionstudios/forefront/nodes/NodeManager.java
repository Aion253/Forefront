package net.aionstudios.forefront.nodes;

import java.util.ArrayList;
import java.util.List;

public class NodeManager {
	
	private static List<ForefrontNode> nodes = new ArrayList<ForefrontNode>();
	
	public static void addNode(ForefrontNode node) {
		for(ForefrontNode n : nodes) {
			if(n.getNodeAddress()==node.getNodeAddress()) {
				node = n;
				return;
			}
		}
		nodes.add(node);
	}
	
	public static void updateNodeData() {
		for(ForefrontNode n : nodes) {
			n.update();
		}
	}

}
