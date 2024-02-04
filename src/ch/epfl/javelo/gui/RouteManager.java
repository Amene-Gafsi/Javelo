package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class manages the display and interaction with the itinerary.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class RouteManager {
	private final RouteBean bean;
	private final ReadOnlyObjectProperty<MapViewParameters> mapViewParametersP;
	private final Pane pane;
	private final List<Double> routePoints ;
	private final Polyline polyline;
	private final Circle circle;
	
	// constants
	private static final int FIRST_INDEX = 0 ;
	private static final int SECOND_INDEX = 1;
	private static final int CIRCLE_RADIUS = 5;

	/**
	 * Creates a RouteManager. A RouteManager is composed of a RouteBean and a ReadOnlyObjectProperty<MapViewParameters>.
	 *
	 * @param bean the bean containing the properties of the waypoints and the route.
	 * @param mapViewParametersP a JavaFX property (read only) containing the parameters of the displayed map.
	 */
	public RouteManager(RouteBean bean, ReadOnlyObjectProperty<MapViewParameters> mapViewParametersP) {
		this.bean = bean;
		this.mapViewParametersP = mapViewParametersP;
		this.pane = new Pane();
		this.routePoints = new ArrayList<>();
		this.pane.setPickOnBounds(false);

		// setting the identity corresponding to the polyline and the circle
		this.polyline = new Polyline(); 
		polyline.setId("route");

		// setting the identity corresponding to the circle
		this.circle = new Circle(CIRCLE_RADIUS);
		circle.setId("highlight");	

		// if there exists a route, then we draw the polyline and the circle for the first time
		if (this.bean.route() != null) drawRouteComponents();

		// adding the polyline and the circle to the pane
		pane.getChildren().add(polyline);
		pane.getChildren().add(circle);

		// when the mouse is clicked, we add a waypoint in the same position as the circle
		circle.setOnMouseClicked(e -> {
			Point2D point = circle.localToParent(e.getX(), e.getY());
			addWaypoint(point.getX(), -point.getY());
		});

		// if the highlighted position changes, then we check if the route exists or if the highlighted position is NaN. If it is neither, we redraw the circle.
		this.bean.highlightedPositionProperty().addListener((p, o, n) -> {
			if ((this.bean.route() == null)) {
				circle.setVisible(false);
				polyline.setVisible(false);
			} else if (Double.isNaN(this.bean.highlightedPosition())){
				circle.setVisible(false);
			} else {
				drawCircle();
			}
			});

		// if the route changes, then we check if the new route exists or if the highlighted position is NaN. If it is neither, we redraw the circle and the polyline.
		this.bean.routeProperty().addListener((p, o, n) -> {
			if((n == null)) {
				circle.setVisible(false);
				polyline.setVisible(false);
			} else if (Double.isNaN(this.bean.highlightedPosition())){
				circle.setVisible(false);
				drawPolyline();
			} else {
				drawRouteComponents();
			}
		});

		// adding listener to the parameters of the displayed map
		this.mapViewParametersP.addListener((p, o, n) -> {
			if(this.bean.route() == null) { // if the route does not exist then we set the polyline and the circle invisible
				circle.setVisible(false);
				polyline.setVisible(false);
			} else if (Double.isNaN(this.bean.highlightedPosition())){ // if the highlighted position is equal to Nan then
				circle.setVisible(false);								// we set invisible the circle and redraw the polyline
				drawPolyline();
			} else if (n.zoomLevel() != o.zoomLevel()){   // if the new and old zoom level are different, then we redraw the polyline and the circle
				drawRouteComponents();	
			} else if (!n.topLeft().equals(o.topLeft())) { // if the new and old top lef coords are different, then we translate the polyline and circle coords
				double[] coordsInWindow = coordsInWindow(this.bean.route().points().get(FIRST_INDEX));
				polyline.setLayoutX(coordsInWindow[FIRST_INDEX] - polyline.getPoints().get(FIRST_INDEX));   
				polyline.setLayoutY(coordsInWindow[SECOND_INDEX] - polyline.getPoints().get(SECOND_INDEX));
				
				PointCh pointAtPosition = this.bean.route().pointAt(this.bean.highlightedPosition());
				coordsInWindow = coordsInWindow(pointAtPosition);
				circle.setLayoutX(coordsInWindow[FIRST_INDEX] - circle.getCenterX());
				circle.setLayoutY(coordsInWindow[SECOND_INDEX] - circle.getCenterY());
			}
		});


			
	}
	
	/**
	 * Gives the JavaFX pane containing the itinerary line and the highlighting circle.
	 *
	 * @return the pane.
	 */
	public Pane pane() {
		return this.pane;
	}
	
	/**
	 * Adds a wayPoint to the list of waypoints.
	 * 
	 * @param x the x coordinate of the waypoint expressed according to the top-left corner of the displayed map section (positive).
	 * @param y the y coordinate of the waypoint expressed according to the top-left corner of the displayed map section (positive)..
	 */
	private void addWaypoint(double x, double y) {
		PointWebMercator pointWeb = this.mapViewParametersP.getValue().pointAt(x, y);
		PointCh swissPoint = pointWeb.toPointCh();
		double highlightedPosition = bean.highlightedPosition();
		int circleClosestNodeId = bean.route().nodeClosestTo(highlightedPosition);

		// the way indexOfSegment is calculated, it allows us to add multiple waypoints at the same position but not two consecutive waypoints at the same position
		int indexOfSegment = this.bean.indexOfNonEmptySegmentAt(highlightedPosition) + 1;
		
		bean.waypoints().add(indexOfSegment, new Waypoint (swissPoint,  circleClosestNodeId));
		
	}
	
	/**
	 * Draws the polyline and the circle of the route.
	 * 
	 */
	private void drawRouteComponents() {
		drawPolyline();
		drawCircle();
	}

	/**
	 * Draws the polyline of the itinerary with the right position in the javaFX window.
	 * This method adds the coordinates of the points of the itinerary to the polyline.
	 * 
	 */
	private void drawPolyline() {
			List<PointCh> points = this.bean.route().points();
			double[] coordsInWindow;
			for (PointCh point : points) {
				coordsInWindow = coordsInWindow(point);
				routePoints.add(coordsInWindow[FIRST_INDEX]);
				routePoints.add(coordsInWindow[SECOND_INDEX]);
			}
			polyline.getPoints().setAll(routePoints);
			polyline.setLayoutX(routePoints.get(FIRST_INDEX) - polyline.getPoints().get(FIRST_INDEX));
			polyline.setLayoutY(routePoints.get(SECOND_INDEX) - polyline.getPoints().get(SECOND_INDEX));

			//After adding all points, the list of points is reseted.
			routePoints.clear();
			polyline.setVisible(true);
	}
	/**
	 * Draws the circle at the right position on the itinerary.
	 *
	 */
	private void drawCircle () {
		PointCh pointAtPosition = this.bean.route().pointAt(this.bean.highlightedPosition());
		double[] coordsInWindow = coordsInWindow(pointAtPosition);
		circle.setLayoutX(coordsInWindow[FIRST_INDEX]);
		circle.setLayoutY(coordsInWindow[SECOND_INDEX]);
		circle.setVisible(true);
	}
	
	/**
	 * Transforms the coordinates of a PointCh to the coordinates in the actual window.
	 * 
	 * @param pointCh the point whose coordinates will be converted in the window coordinates.
	 * @return a new array of length two : the first value is the x coordinate / the second value is the y coordinate.
	 */
	private double[] coordsInWindow(PointCh pointCh) {
		PointWebMercator pointWeb = PointWebMercator.ofPointCh(pointCh);
		double xCord, yCord, xInWind, yInWind;
		double xOfWind = mapViewParametersP.getValue().xTopLeftCoord();
		double yOfWind = mapViewParametersP.getValue().yTopLeftCoord();
		xCord = mapViewParametersP.getValue().viewX(pointWeb);
		yCord = mapViewParametersP.getValue().viewY(pointWeb);
		xInWind = xCord - xOfWind;
		yInWind = yCord - yOfWind;
		return new double[]{xInWind, yInWind};
	}
		
}
