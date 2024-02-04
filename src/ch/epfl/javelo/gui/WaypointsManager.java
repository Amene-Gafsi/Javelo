package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

import java.util.function.Consumer;

/**
 * This class manages the display and interaction with the waypoints.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class WaypointsManager {
	private final Graph graph;
	private final ObjectProperty<MapViewParameters> mapViewParametersP;
	private final ObservableList<Waypoint> wayPoints;
	private final Consumer<String> error;
	private final ObjectProperty<Point2D> mouseCoords;
	private final ObjectProperty<Point2D> coordsInMap;
	private final Pane pane;

	// constants
	private final static int RADIUS = 500;
	private final static int NO_NODES_AROUND = -1;

	/**
	 * Creates a WaypointsManager. A WaypointsManager is composed of a Graph, an ObjectProperty<MapViewParameters>, an ObservableList<Waypoint> and a Consumer<String>.
	 *
	 * @param graph              the graph of the road network.
	 * @param mapViewParametersP a JavaFX property containing the parameters of the displayed map.
	 * @param wayPoints          the observable list of all transit points.
	 * @param error              an object to signal errors.
	 */
	public WaypointsManager(Graph graph, ObjectProperty<MapViewParameters> mapViewParametersP, ObservableList<Waypoint> wayPoints, Consumer<String> error) {
		this.graph = graph;
		this.mapViewParametersP = mapViewParametersP;
		this.wayPoints = wayPoints;
		this.error = error;
		this.mouseCoords = new SimpleObjectProperty<>();
		this.coordsInMap = new SimpleObjectProperty<>();
		this.pane = new Pane();
		this.pane.setPickOnBounds(false);

		// draws the waypoints for the first time
		drawWayPoints();

		// if the list of wayPoints changes then we redraw the waypoints on the canvas
		wayPoints.addListener((ListChangeListener<? super Waypoint>) (x) -> drawWayPoints());

		// if the parameters of the displayed map change then we redraw the waypoints on the canvas
		mapViewParametersP.addListener((p, o, n) -> drawWayPoints());
	}

	/**
	 * Gives the JavaFX pane displaying the waypoints.
	 * The pane is constructed in the constructor.
	 *
	 * @return the pane
	 */
	public Pane pane() {
		return this.pane;
	}

	/**
	 * Adds a new waypoint to the list of waypoints at a given index.
	 * If no closest Nodes are found for the new wayPoint, an error is accepted and no points are added.
	 *
	 * @param x the x-coordinate expressed according to the top-left corner of the displayed map section.
	 * @param y the y-coordinate expressed according to the top-left corner of the displayed map section.
	 */
	public void addWaypoint(double x, double y) {
		PointCh swissPoint = swissCoords(x, y);

		if (swissPoint == null) {
			error.accept("Aucune route à proximité !");
		} else {
			int closestNodeId = closestNodeId(swissPoint);

			// If no node is found
			if (closestNodeId == NO_NODES_AROUND) {
				error.accept("Aucune route à proximité !");
			} else {
				wayPoints.add(new Waypoint(swissPoint, closestNodeId));
			}
		}
	}

	/**
	 * Sets a new waypoint to the list of waypoints at a given index.
	 * If no closest nodes are found for the new wayPoint, an error is accepted and no points are added.
	 *
	 * @param x     the x-coordinate expressed according to the top-left corner of the displayed map section.
	 * @param y     the y-coordinate expressed according to the top-left corner of the displayed map section.
	 * @param index the index of the waypoint in the ObservableList 'waypoint'.
	 */
	private void setWaypoint(double x, double y, int index) {
		PointCh swissPoint = swissCoords(x, y);

		if (swissPoint == null) {
			error.accept("Aucune route à proximité !");
		} else {
			int closestNodeId = closestNodeId(swissPoint);

			// If no node is found
			if (closestNodeId == NO_NODES_AROUND) {
				error.accept("Aucune route à proximité !");
			} else {
				wayPoints.set(index, new Waypoint(swissPoint, closestNodeId));
			}
		}
	}

	/**
	 * Draws the waypoints on the JavaFX canvas.
	 */
	private void drawWayPoints() {
		// we erase everything to start from scratch
		pane.getChildren().clear();

		double xCord, yCord, xInWind, yInWind;
		double xOfWind = mapViewParametersP.getValue().xTopLeftCoord();
		double yOfWind = mapViewParametersP.getValue().yTopLeftCoord();

		PointWebMercator pointWeb;
		for (int i = 0; i < wayPoints.size(); ++i) {
			int wayPointIndex = i; // used in lambda, must be final
			pointWeb = PointWebMercator.ofPointCh(wayPoints.get(i).swissCoord());
			xCord = mapViewParametersP.getValue().viewX(pointWeb);
			yCord = mapViewParametersP.getValue().viewY(pointWeb);
			xInWind = xCord - xOfWind;
			yInWind = yCord - yOfWind;
			Group group = createGroup();
			addGroup(i, xInWind, yInWind, group);

			// if the mouse is pressed, its coordinates are saved
			group.setOnMousePressed(e -> mouseCoords.set(new Point2D(e.getX(), -e.getY())));

			// when dragging, we translate the coordinates of the waypoint in the pane
			group.setOnMouseDragged(e -> {
				coordsInMap.set(group.localToParent(new Point2D(e.getX(), e.getY())));

				pane.getChildren().get(wayPointIndex).setLayoutX(coordsInMap.get().getX() + mouseCoords.get().getX());
				pane.getChildren().get(wayPointIndex).setLayoutY(coordsInMap.get().getY() + mouseCoords.get().getY());
			});

			// when the mouse is released, we set a new waypoint at the released position (expressed in the right reference frame)
			group.setOnMouseReleased(e -> {
				if (!e.isStillSincePress()) {
					setWaypoint(coordsInMap.get().getX() + mouseCoords.get().getX(), -coordsInMap.get().getY() - mouseCoords.get().getY(), wayPointIndex);
					drawWayPoints();
				} else {
					wayPoints.remove(wayPointIndex);
				}
				mouseCoords.set(null);
				coordsInMap.set(null);
			});
		}
	}

	/**
	 * Adds a JavaFX group with the correct StyleClass to the pane.
	 * This method is only used in the method drawWayPoints.
	 *
	 * @param index the index of the waypoint in the ObservableList 'waypoint'.
	 * @param x     the x-coordinated the in the window.
	 * @param y     the y-coordinated the in the window.
	 * @param group the group.
	 */
	private void addGroup(int index, double x, double y, Group group) {
		String name;
		if (index == 0) {
			name = "first";
		} else if (index == wayPoints.size() - 1) {
			name = "last";
		} else {
			name = "middle";
		}

		group.getStyleClass().addAll("pin", name);
		group.setLayoutX(x);
		group.setLayoutY(y);
		pane.getChildren().add(group);
	}

	/**
	 * Creates a group containing two paths representing the markers.
	 *
	 * @return the group.
	 */
	private Group createGroup() {
		SVGPath svgPathO = new SVGPath();
		SVGPath svgPathI = new SVGPath();
		svgPathO.setContent("M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20");
		svgPathI.setContent("M0-23A1 1 0 000-29 1 1 0 000-23");
		svgPathO.getStyleClass().add("pin_outside");
		svgPathI.getStyleClass().add("pin_inside");
		return new Group(svgPathO, svgPathI);
	}

	/**
	 * Converts the coordinates of a point from actual window coordinates to Swiss coordinates.
	 *
	 * @param x the x-coordinate expressed according to the top-left corner of the displayed map section.
	 * @param y the y-coordinate expressed according to the top-left corner of the displayed map section.
	 * @return PointCh the coordinates of the point (x,y) in swiss coordinates.
	 */
	private PointCh swissCoords(double x, double y) {
		PointWebMercator pointWeb = this.mapViewParametersP.getValue().pointAt(x, y);
		return pointWeb.toPointCh();
	}


	/**
	 * Gives the closest Node to a given point.
	 *
	 * @param pointCh the point expressed in the swiss system.
	 * @return the closest node the the pointCh or (-1) if no closest node exists or pointCh is null.
	 */
	private int closestNodeId(PointCh pointCh) {
		return this.graph.nodeClosestTo(pointCh, RADIUS);
	}
}
