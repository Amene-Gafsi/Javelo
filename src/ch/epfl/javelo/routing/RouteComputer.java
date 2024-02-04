package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.*;

/**
 * This class represents an itinerary planner.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class RouteComputer {

	private final static float ALREADY_EXPLORED_NODE_VALUE = Float.NEGATIVE_INFINITY;

	private final Graph graph;
	private final CostFunction costFunction;

	/**
	 * Creates a RouteComputer. A RouteComputer is composed of a Graph and a CostFunction.
	 *
	 * @param graph the graph composing the itinerary.
	 * @param costFunction the cost function that determines the cost of an edge.
	 */
	public RouteComputer(Graph graph, CostFunction costFunction) {
		this.graph = graph;
		this.costFunction = costFunction;
	}

	/**
	 * Gives the best route to take with the minimum total cost starting from the startNodeId
	 * to the endNodeId in the graph given to the constructor.
	 *
	 * @param startNodeId the identity of the first node of the itinerary.
	 * @param endNodeId   the identity of the last node of the itinerary.
	 * @return the route with the minimum total cost or null if no route exists.
	 * @throws IllegalArgumentException if the start and end nodes are identical.
	 */
	public Route bestRouteBetween(int startNodeId, int endNodeId) {
		Preconditions.checkArgument(startNodeId != endNodeId);

		record WeightedNode(int nodeId, float distance, float heuristic) implements Comparable<WeightedNode> {
			@Override
			public int compareTo(WeightedNode that) {
				return Float.compare(this.distance + this.heuristic, that.distance + that.heuristic);
			}
		}

		int nbNodes = graph.nodeCount();
		PriorityQueue<WeightedNode> inExploration = new PriorityQueue<>();

		// used to store the distances to the actual node
		float[] distances = new float[nbNodes];

		// initialize the distances
		Arrays.fill(distances, Float.POSITIVE_INFINITY);
		distances[startNodeId] = 0;

		// used to store the path
		int[] previousNodes = new int[nbNodes];

		// The point of the last node of the itinerary used to calculate the heuristic function
		PointCh endPoint = graph.nodePoint(endNodeId);

		// Add the first node in the inExploration
		inExploration.add(new WeightedNode(startNodeId, 0, getHCost(startNodeId, endPoint)));

		// store if the path has been found
		boolean pathFound = false;

		while (!inExploration.isEmpty()) { // stop only when empty
			WeightedNode actual = inExploration.remove();

			// we skip already explored nodes
			if (distances[actual.nodeId] == ALREADY_EXPLORED_NODE_VALUE) {
				continue;
			}
			// set the node as visited
			distances[actual.nodeId] = ALREADY_EXPLORED_NODE_VALUE;

			// check if reached the final node
			if (actual.nodeId == endNodeId) {
				pathFound = true;
				break;
			}

			// for each edge of the actual node, add the next node to the priority queue
			for (int i = 0; i < graph.nodeOutDegree(actual.nodeId()); i++) {
				// get the node at the end of the edge
				int edgeId = graph.nodeOutEdgeId(actual.nodeId(), i);
				int arrivalNode = graph.edgeTargetNodeId(edgeId);

				// calculate the actual distance with the cost factor
				float edgeDist = (float) (graph.edgeLength(edgeId) * costFunction.costFactor(actual.nodeId(), edgeId));
				float dist = actual.distance + edgeDist;

				if (dist < distances[arrivalNode]) { // skips the node if the node = float.Negative_Infinity
					distances[arrivalNode] = dist;
					previousNodes[arrivalNode] = actual.nodeId;
					inExploration.add(new WeightedNode(arrivalNode, dist, getHCost(arrivalNode, endPoint)));
				}
			}
			
		}

		if (pathFound) {
			// follow the edges
			List<Edge> route = new ArrayList<>();
			int actualNode = endNodeId;
			while (actualNode != startNodeId) {
				// We need to get the edge making the link from previousNodes[actualNode] --> actualNode
				// for this, we loop through all the edges starting from previousNodes[actualNode] and find the one that
				// reaches the actualNode.
				for (int i = 0; i < graph.nodeOutDegree(previousNodes[actualNode]); i++) {
					// get the node at the end of the edge
					int edgeId = graph.nodeOutEdgeId(previousNodes[actualNode], i);
					int nPrimeId = graph.edgeTargetNodeId(edgeId);
					if (nPrimeId == actualNode) { // this is this edge
						// insert the edge at the front to build the correct route
						route.add(Edge.of(this.graph, edgeId, previousNodes[actualNode], actualNode));
						actualNode = previousNodes[actualNode];
						break;
					}
				}
			}
			Collections.reverse(route);
			return new SingleRoute(route);
		} else {
			return null;
		}
	}

	/**
	 * Gives the heuristic function used to decide which node to consider at each step. This function consists of
	 * calculating the Euclidean distance between a certain node and the last node of the itinerary.
	 *
	 * @param currentNodeId the identity of the current node.
	 * @param endPoint      the point in the graph of the last node of the itinerary.
	 * @return the Euclidean distance between the currentNodeId and the endNodeId.
	 */
	private float getHCost(int currentNodeId, PointCh endPoint) {
		PointCh currentPoint = graph.nodePoint(currentNodeId);
		return (float) currentPoint.distanceTo(endPoint);
	}
}
