package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a multi route (wraps multiple Route).
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class MultiRoute implements Route {
	private final List<Route> segments;
	private double[] sortedEdgePos;

	/**
	 * Constructor.
	 * In this constructor is built directly the array containing the positions of the route segments.
	 * However, the index 0 of the array is an exception because it represents nothing and its value is always 0.
	 *
	 * @param segments the list of segments of an itinerary.
	 * @throws IllegalArgumentException if the segments are empty.
	 */
	public MultiRoute(List<Route> segments) {
		this.segments = List.copyOf(segments);
		Preconditions.checkArgument(!segments.isEmpty());

		sortedEdgePos = new double[segments.size() + 1];
		sortedEdgePos[0] = 0;

		for (int i = 1; i < segments.size(); ++i) {
			sortedEdgePos[i] = sortedEdgePos[i - 1] + segments.get(i - 1).length();
		}
	}


	/**
	 * Gives the index of the segment at the given position (in meters).
	 *
	 * @param position the given position.
	 * @return the segment at the given position.
	 */
	@Override
	public int indexOfSegmentAt(double position) {
		double activePosition = Math2.clamp(0, position, this.length());
		int index = 0;

		for (Route route : this.segments) {
			if (activePosition > route.length()) {
				index += route.indexOfSegmentAt(route.length()) + 1;
				activePosition -= route.length();
			} else {
				index += route.indexOfSegmentAt(activePosition);
				break;
			}
		}
		return index;
	}

	/**
	 * Gives the length of the route, in meters.
	 *
	 * @return the length of the route.
	 */
	@Override
	public double length() {

		double length = 0.0;
		for (Route segment : this.segments) {
			length += segment.length();
		}
		return length;
	}

	/**
	 * Gives all the edges of the route.
	 *
	 * @return all the edges of the route.
	 */
	@Override
	public List<Edge> edges() {
		List<Edge> edges = new ArrayList<>();

		for (Route segment : this.segments) {
			edges.addAll(segment.edges());
		}

		return edges;
	}

	/**
	 * Gives all the points located at the ends of the edges of the route.
	 *
	 * @return the points located at the ends of the edges of the route.
	 */
	@Override
	public List<PointCh> points() {

		List<PointCh> points = new ArrayList<>();

		for (Route segment : this.segments) {
			points.addAll(segment.points());
			points.remove(points.size() - 1);
		}
		int lastSegmentIndex = segments.size() - 1;
		int lastPointIndex = segments.get(lastSegmentIndex).points().size() - 1;
		PointCh lastPoint = segments.get(lastSegmentIndex).points().get(lastPointIndex);
		points.add(lastPoint);

		return points;
	}

	/**
	 * Gives the point at the given position along the route.
	 *
	 * @param position the given position.
	 * @return the point at the given position along the route.
	 */
	@Override
	public PointCh pointAt(double position) {

		double activePosition = Math2.clamp(0, position, this.length());

		for (Route segment : this.segments) {
			if (activePosition > segment.length()) {
				activePosition -= segment.length();
			} else {
				return segment.pointAt(activePosition);
			}

		}
		return null;     //we never reach this return

	}

	/**
	 * Gives the altitude at the given position along the route.
	 *
	 * @param position the given position.
	 * @return the altitude at the given position along the route.
	 */
	@Override
	public double elevationAt(double position) {

		double activePosition = Math2.clamp(0, position, this.length());

		for (Route segment : this.segments) {
			if (activePosition > segment.length()) {
				activePosition -= segment.length();
			} else {
				return segment.elevationAt(activePosition);
			}

		}
		return 0;        //we never reach this return
	}

	/**
	 * Gives the identity of the node belonging to the route and being closest to the given position.
	 *
	 * @param position the given position.
	 * @return the identity of the node closest to the given position.
	 */
	@Override
	public int nodeClosestTo(double position) {

		double activePosition = Math2.clamp(0, position, this.length());

		for (Route segment : this.segments) {
			if (activePosition > segment.length()) {
				activePosition -= segment.length();
			} else {
				return segment.nodeClosestTo(activePosition);
			}
		}
		return 0;        //we never reach this return
	}

	/**
	 * Gives the point on the route that is closest to the given reference point.
	 *
	 * @param point the itinerary point.
	 * @return the closest point to the given reference point.
	 */
	@Override
	public RoutePoint pointClosestTo(PointCh point) {
		RoutePoint actualRoute, nextRoute;
		actualRoute = segments.get(0).pointClosestTo(point);

		for (int i = 1; i < segments.size(); i++) {
			nextRoute = segments.get(i).pointClosestTo(point);
			nextRoute = nextRoute.withPositionShiftedBy(sortedEdgePos[i]);
			actualRoute = actualRoute.min(nextRoute);

		}
		return actualRoute;
	}

}
