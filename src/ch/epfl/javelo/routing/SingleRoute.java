package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents a single route.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class SingleRoute implements Route {
	private final List<Edge> edges;
	private final double[] sortedEdgePos;
	private final List<PointCh> totalPoints;

	/**
	 * In this constructor we initialize the list containing all the edges
	 * and also the array containing the positions of the edges where the indexes represent the nodes.
	 * However, the index 0 of the array is an exception because it represents nothing and its value is always 0.
	 *
	 * @param edges the list of edges.
	 */
	public SingleRoute(List<Edge> edges) {
		Preconditions.checkArgument(!edges.isEmpty());
		this.edges = List.copyOf(edges);
		this.totalPoints = new ArrayList<>();
		sortedEdgePos = new double[this.edges.size() + 1];
		sortedEdgePos[0] = 0;
		for (int i = 1; i < sortedEdgePos.length; i++) {
			sortedEdgePos[i] = sortedEdgePos[i - 1] + this.edges.get(i - 1).length();
		}
	}

	@Override
	public int indexOfSegmentAt(double position) {
		return 0;
	}

	@Override
	public double length() {
		return sortedEdgePos[sortedEdgePos.length - 1];
	}

	@Override
	public List<Edge> edges() {
		return this.edges;
	}

	@Override
	public List<PointCh> points() {
		if (totalPoints.isEmpty()) {
			totalPoints.add(this.edges.get(0).fromPoint());
			for (Edge e : this.edges) {
				// we add the last point of each edge since the
				// first point of the very first edge has been already added.
				totalPoints.add(e.toPoint());
			}
		}
		return totalPoints;
	}

	/**
	 * Gives the index in the edge list of a given position.
	 *
	 * @param position the given position.
	 * @return the index in the edge list of the given position.
	 */
	private int edgeIndex(double position) {
		int indexEdge = Arrays.binarySearch(sortedEdgePos, position);
		if (indexEdge > 0) return indexEdge - 1;
		if (indexEdge == 0) return indexEdge;
		return ((-indexEdge) - 2);
	}

	@Override
	public PointCh pointAt(double position) {
		double newPosition = Math2.clamp(0, position, this.length());
		int edgeIndex = edgeIndex(newPosition);
		double positionOnEdge = newPosition - sortedEdgePos[edgeIndex];
		return this.edges.get(edgeIndex).pointAt(positionOnEdge);
	}

	@Override
	public double elevationAt(double position) {
		double newPosition = Math2.clamp(0, position, this.length());
		double positionOnEdge = newPosition - sortedEdgePos[edgeIndex(newPosition)];
		return this.edges.get(edgeIndex(newPosition)).elevationAt(positionOnEdge);
	}

	@Override
	public int nodeClosestTo(double position) {
		double newPosition = Math2.clamp(0, position, this.length());
		double positionInEdge = newPosition - sortedEdgePos[edgeIndex(newPosition)];
		double edgeLength = edges.get(edgeIndex(newPosition)).length();

		if (positionInEdge <= (edgeLength - positionInEdge)) {
			return this.edges.get(edgeIndex(newPosition)).fromNodeId();
		}

		return this.edges.get(edgeIndex(newPosition)).toNodeId();
	}

	@Override
	public RoutePoint pointClosestTo(PointCh point) {
		double position = Math2.clamp(0, edges.get(0).positionClosestTo(point), edges.get(0).length());
		PointCh initPoint = edges.get(0).pointAt(position);
		RoutePoint closest = new RoutePoint(initPoint, position, initPoint.distanceTo(point));

		for (int i = 1; i < edges.size(); i++) {
			double positionClosest = Math2.clamp(0, edges.get(i).positionClosestTo(point), edges.get(i).length());
			PointCh pointCh = edges.get(i).pointAt(positionClosest);
			RoutePoint current = new RoutePoint(pointCh, positionClosest + sortedEdgePos[i], pointCh.distanceTo(point));
			closest = closest.min(current);
		}
		return closest;
	}
}
