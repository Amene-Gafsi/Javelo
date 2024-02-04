package ch.epfl.javelo.routing;

/**
 * This interface represents a cost function.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public interface CostFunction {

	/**
	 * Gives the factor by which the length of the "edgeId" identity edge beginning from the "nodeId" identity node should be multiplied.
	 * This factor must imperatively be greater or equal to 1.
	 *
	 * @param nodeId the identity of the node.
	 * @param edgeId the identity of the edge.
	 * @return the factor by which the length of the edge should be multiplied.
	 */
	double costFactor(int nodeId, int edgeId);
}
