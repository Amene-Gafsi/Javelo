package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.function.DoubleUnaryOperator;

/**
 * This record class represents an edge of an itinerary.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public record Edge(int fromNodeId, int toNodeId, PointCh fromPoint, PointCh toPoint, double length,
				   DoubleUnaryOperator profile) {

	/**
	 * Gives an instance of Edge whose attributes fromNodeId and toNodeId are those given.
	 * The others attributes are those of the "edgeId" identity edge in the Graph graph.
	 *
	 * @param graph      the graph that contains the edges.
	 * @param edgeId     the identity of the edge in the graph.
	 * @param fromNodeId the identity of the edge starting node.
	 * @param toNodeId   the identity of the edge arrival node.
	 * @return a new instance of Edge whose attributes fromNodeId and toNodeId are those given..
	 */
	public static Edge of(Graph graph, int edgeId, int fromNodeId, int toNodeId) {
		return new Edge(fromNodeId, toNodeId, graph.nodePoint(fromNodeId), graph.nodePoint(toNodeId), graph.edgeLength(edgeId), graph.edgeProfile(edgeId));
	}

	/**
	 * Gives the position along the edge, in meters, that is closest to the given point.
	 *
	 * @param point the itinerary point.
	 * @return the position along the edge, in meters, that is closest to the given point.
	 */
	public double positionClosestTo(PointCh point) {
		return Math2.projectionLength(fromPoint.e(), fromPoint.n(), toPoint.e(), toPoint.n(), point.e(), point.n());
	}

	/**
	 * Gives the point at the given position on the edge, expressed in meters.
	 *
	 * @param position the given position.
	 * @return the point at the given position on the edge, expressed in meters.
	 */
	public PointCh pointAt(double position) {
		double posAccordingToLength = position / (this.length);
		return new PointCh(Math2.interpolate(
				fromPoint.e(), toPoint.e(), posAccordingToLength), Math2.interpolate(fromPoint.n(), toPoint.n(), posAccordingToLength)
		);
	}

	/**
	 * Gives the altitude, in meters, at the given position on the edge.
	 *
	 * @param position the given position.
	 * @return the altitude, in meters, at the given position on the edge.
	 */
	public double elevationAt(double position) {
		return this.profile.applyAsDouble(position);
	}

}
