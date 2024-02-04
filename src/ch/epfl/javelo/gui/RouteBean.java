package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This class contains the properties of the waypoints and the route.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class RouteBean {
	private final RouteComputer roadComputer;
	private final ObservableList<Waypoint> waypoints;
	private final ObjectProperty<Route> route;
	private final DoubleProperty highlightedPosition;
	private final ObjectProperty<ElevationProfile> elevationProfile;
	private final RouteCache routeCache;
	private final List<Route> segments;
	private final Consumer<String> error;

	
	// constants
	private static final int MAX_SIZE = 100;
	private final static double MAX_STEP_LENGTH = 5;
	private final static float LOAD_FACTOR = 0.75F;


	/**
	 * Creates a RouteBean. A RouteBean is composed of RouteComputer.
	 *
	 * @param routeComputer a routeComputer which represents an itinerary planner.
	 */
	public RouteBean(RouteComputer routeComputer, Consumer<String> error) {
		this.roadComputer = routeComputer;
		this.waypoints = FXCollections.observableArrayList(); // the list is empty
		this.route = new SimpleObjectProperty<>();
		this.highlightedPosition = new SimpleDoubleProperty();
		this.elevationProfile = new SimpleObjectProperty<>();
		this.segments = new ArrayList<>();
		this.routeCache = new RouteCache();
		this.error = error;

		// calculating the best route between all the waypoints for the first time
		routeCalculator();

		// if the list of waypoints changes then the best route between all the waypoints is recalculated
		waypoints.addListener((ListChangeListener<? super Waypoint>) observable -> {
			routeCalculator();
			if (this.route.get() != null)
				this.elevationProfile.set(ElevationProfileComputer.elevationProfile(this.route.get(), MAX_STEP_LENGTH));
		});
	}

	/**
	 * Calculates the best route between all the waypoints and puts it in a list of segments.
	 * The route is first searched in the RouteCache (is a memory cache) and, if it is not found, it is then calculated and added to the memory cache.
	 */
	private void routeCalculator() {
		// If there are less than two waypoints in the list then the properties corresponding
		// to the itinerary and the elevation profile are null
		if (waypoints.size() < 2) {
			this.route.set(null);
			this.elevationProfile.set(null);
		} else {
			Route actualRoad;
			int node1, node2;
			Pair<Integer, Integer> wayPoints;
			// Calculating the best route between each waypoint in the list waypoints
			for (int i = 0; i < waypoints.size() - 1; i++) {
				node1 = waypoints.get(i).closestNodeId();
				node2 = waypoints.get(i + 1).closestNodeId();
				wayPoints = new Pair<>(node1, node2);

				// if the best route between a pair of waypoints already exists then we directly add it to the list of segments
				if (this.routeCache.containsKey(wayPoints)) {
					segments.add(this.routeCache.get(wayPoints));
				} else if (node1 != node2) {    // if the identities of the closest nodes are different, then we calculate the best route between the two nodes
					actualRoad = roadComputer.bestRouteBetween(node1, node2);

					if (actualRoad != null) { // if the calculated best route between two nodes is not equal to null, then we add it to the list of segments and to the memory cache
						segments.add(actualRoad);
						routeCache.put(wayPoints, actualRoad);
					} else { 
						error.accept("Aucune route adaptée aux velos à proximité !");
						this.route.set(null);
						this.elevationProfile.set(null);
						segments.clear();
						break;
					}
				}
			}

			if ((segments.size() != 0)) this.route.set(new MultiRoute(segments));
		}
		segments.clear();
	}

	/**
	 * Calculates the index of the segment containing the position along the route.
	 * This method ignores empty segments.
	 *
	 * @param position the position along the route.
	 * @return the index of the segment containing the position.
	 */
	public int indexOfNonEmptySegmentAt(double position) {
		int index = route().indexOfSegmentAt(position);
		for (int i = 0; i <= index; i += 1) {
			int n1 = waypoints.get(i).closestNodeId();
			int n2 = waypoints.get(i + 1).closestNodeId();
			if (n1 == n2) index += 1;
		}
		return index;
	}

	/**
	 * Gives the property highlightedPosition.
	 *
	 * @return the property of highlightedPosition.
	 */
	public DoubleProperty highlightedPositionProperty() {
		return highlightedPosition;
	}

	/**
	 * Gives the highlightedPosition in double.
	 *
	 * @return the highlightedPosition.
	 */
	public double highlightedPosition() {
		return highlightedPosition.doubleValue();
	}

	/**
	 * Gives the property route.
	 *
	 * @return the route propriety.
	 */
	public ReadOnlyObjectProperty<Route> routeProperty() {
		return route;
	}

	/**
	 * Gives the best route between the waypoints.
	 *
	 * @return the route.
	 */
	public Route route() {
		return route.get();
	}

	/**
	 * Gives the property waypoints.
	 *
	 * @return the property waypoints.
	 */
	public ObservableList<Waypoint> waypoints() {
		return waypoints;
	}

	/**
	 * Sets the parameter value in the property highlightedPosition.
	 *
	 * @param highlightedPosition the highlightedPosition.
	 */
	public void setHighlightedPosition(Double highlightedPosition) {
		this.highlightedPosition.set(highlightedPosition);
	}

	/**
	 * Sets the parameter value in the property waypoints.
	 *
	 * @param waypoints an ObservableList of waypoints.
	 */
	public void setWaypoints(ObservableList<Waypoint> waypoints) {
		this.waypoints.setAll(waypoints);
	}

	/**
	 * Gives the property elevationProfile.
	 *
	 * @return the property elevationProfile.
	 */
	public ReadOnlyObjectProperty<ElevationProfile> elevationProfileProperty() {
		return this.elevationProfile;
	}

	/**
	 * RoutCache is a reimplementation of a LinkedHashMap for its capabilities to remove the eldest entry when it reaches a defined size.
	 * RouteCache is used to stock the best route between a pair of nodes and this prevents from recalculating a route that had
	 * already been calculated.
	 */
	private static class RouteCache extends LinkedHashMap<Pair<Integer, Integer>, Route> {

		public RouteCache() {
			super(MAX_SIZE, LOAD_FACTOR, true);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<Pair<Integer, Integer>, Route> eldest) {
			return this.size() > MAX_SIZE; //must override it if used in a fixed cache
		}
	}
}
