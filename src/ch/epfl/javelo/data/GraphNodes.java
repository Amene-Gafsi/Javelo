package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Q28_4;

import java.nio.IntBuffer;

/**
 * This record class represents the array of all the nodes of the graph.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public record GraphNodes(IntBuffer buffer) {

	// linked to the attributes
	private static final int OFFSET_E = 0;
	private static final int OFFSET_N = OFFSET_E + 1;
	private static final int OFFSET_OUT_EDGES = OFFSET_N + 1;
	private static final int NODE_INTS = OFFSET_OUT_EDGES + 1;
	// only for the third attribute
	private static final int OFFSET_OUT_DEGREE_IN_THIRD_ATTR = 28;
	private static final int OUT_DEGREE_LENGTH_IN_THIRD_ATTR = 4;
	private static final int OFFSET_ID_EDGE_IN_THIRD_ATTR = 0;
	private static final int LENGTH_ID_EDGE_IN_THIRD_ATTR = 28;



	/**
	 * Calculates the total number of nodes of the graph.
	 *
	 * @return the number of nodes.
	 */
	public int count() {
		return buffer.capacity() / NODE_INTS;
	}

	/**
	 * Gives the E coordinate of the given identity node.
	 *
	 * @param nodeId the identity of the node.
	 * @return the E coordinate of the node.
	 */
	public double nodeE(int nodeId) {
		return Q28_4.asDouble(buffer.get(NODE_INTS * nodeId + OFFSET_E));
	}

	/**
	 * Gives the N coordinate of the given identity node.
	 *
	 * @param nodeId the identity of the node.
	 * @return the N coordinate of the node.
	 */
	public double nodeN(int nodeId) {
		return Q28_4.asDouble(buffer.get(NODE_INTS * nodeId + OFFSET_N));
	}

	/**
	 * Calculates the number of edges leaving the given identity node.
	 *
	 * @param nodeId the identity of the node.
	 * @return the number of edges leaving the given identity node.
	 */
	public int outDegree(int nodeId) {
		int thirdAttribute = buffer.get(NODE_INTS * nodeId + OFFSET_OUT_EDGES);
		return Bits.extractUnsigned(thirdAttribute, OFFSET_OUT_DEGREE_IN_THIRD_ATTR, OUT_DEGREE_LENGTH_IN_THIRD_ATTR);
	}

	/**
	 * Gives the identity of the "edgeIndex" edge leaving the given identity node.
	 *
	 * @param nodeId    the identity of the node.
	 * @param edgeIndex the index of the edge.
	 * @return the identity of the given "edgeIndex" edge leaving the given node.
	 */
	public int edgeId(int nodeId, int edgeIndex) {
		assert 0 <= edgeIndex && edgeIndex < outDegree(nodeId);

		int thirdAttribute = buffer.get(NODE_INTS * nodeId + OFFSET_OUT_EDGES);
		int firstEdgeOut = Bits.extractUnsigned(thirdAttribute, OFFSET_ID_EDGE_IN_THIRD_ATTR, LENGTH_ID_EDGE_IN_THIRD_ATTR);
		return firstEdgeOut + edgeIndex;

	}

}
