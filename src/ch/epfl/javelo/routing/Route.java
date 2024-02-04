package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import java.util.List;

/**
 * This interface represents an itinerary.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public interface Route {
	/**
	 * Gives the index of the segment at the given position (in meters).
	 *
	 * @param position the given position.
	 * @return the segment at the given position.
	 */
	int indexOfSegmentAt(double position);

	/**
	 * Gives the length of the route, in meters.
	 *
	 * @return the length of the route.
	 */
	double length();

	/**
	 * Gives all the edges of the route.
	 *
	 * @return all the edges of the route.
	 */
	List<Edge> edges();

	/**
	 * Gives all the points located at the ends of the edges of the route.
	 *
	 * @return the points located at the ends of the edges of the route.
	 */
	List<PointCh> points();

	/**
	 * Gives the point at the given position along the route.
	 *
	 * @param position the given position.
	 * @return the point at the given position along the route.
	 */
	PointCh pointAt(double position);

	/**
	 * Gives the altitude at the given position along the route.
	 *
	 * @param position the given position.
	 * @return the altitude at the given position along the route.
	 */
	double elevationAt(double position);

	/**
	 * Gives the identity of the node belonging to the route and being closest to the given position.
	 *
	 * @param position the given position.
	 * @return the identity of the node closest to the given position.
	 */
	int nodeClosestTo(double position);

	/**
	 * Gives the point on the route that is closest to the given reference point.
	 *
	 * @param point the itinerary point.
	 * @return the closest point to the given reference point.
	 */
	RoutePoint pointClosestTo(PointCh point);

}
