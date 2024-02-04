package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

/**
 * This class manages the display of the "annotated" map,
 * which is the background map over which the route and waypoints are superimposed.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class AnnotatedMapManager {
	private final Pane paneWithoutBackground, pane;
	private final ObjectProperty<MapViewParameters> mapViewParametersP, mapViewParametersBackground;
	private final DoubleProperty mousePositionOnRouteProperty;
	private final ObjectProperty<Point2D> mouseCoords;
	private final BaseMapManager baseMapManager, baseMapBackground;
	private final SimpleLongProperty minScrollTime;
	private final ObjectProperty<Point2D> zoomMouseCoords;
	private final BooleanProperty isZooming;

	// constants
	private final static int DEFAULT_ZOOM = 12;
	private final static int DEFAULT_X_TOP_LEFT = 543200;
	private final static int DEFAULT_Y_TOP_LEFT = 370650;
	private final static int PXIELS_CLOSE = 15;
	private final static int DELTA_X_TO_CENTER = -400;
	private final static int DELTA_Y_TO_CENTER = -160;
	private static final int MIN_ALLOWED_ZOOM = 8;
	private static final int MAX_ALLOWED_ZOOM = 19;
	private static final int MIN_SCROLL_TIME_DELTA = 200;
	private static final double DELTA_SCALE_FOR_ZOOM = 0.004;
	private static final double MAX_ZOOM_IN_DELTA_SCALE = 0.09;
	private static final double MAX_ZOOM_OUT_DELTA_SCALE = 0.063;

	/**
	 * Creates an AnnotatedMapManager. An AnnotatedMapManager is composed of a Graph, a TileManager, a RouteBean and a Consumer<String>.
	 *
	 * @param graph       the graph composed of the route and the waypoints.
	 * @param tileManager the tileManager which represents an OSM tile manager.
	 * @param bean        the bean which contains the properties of the waypoints and the route.
	 * @param error       an error consumer.
	 */
	public AnnotatedMapManager(Graph graph, TileManager tileManager, RouteBean bean, Consumer<String> error) {
		this.mousePositionOnRouteProperty = new SimpleDoubleProperty(Double.NaN);
		this.mapViewParametersP = new SimpleObjectProperty<>(new MapViewParameters(DEFAULT_ZOOM, DEFAULT_X_TOP_LEFT, DEFAULT_Y_TOP_LEFT));
		this.mapViewParametersBackground = new SimpleObjectProperty<>(new MapViewParameters(DEFAULT_ZOOM, DEFAULT_X_TOP_LEFT, DEFAULT_Y_TOP_LEFT));
		this.minScrollTime = new SimpleLongProperty();
		this.zoomMouseCoords = new SimpleObjectProperty<>();
		this.isZooming = new SimpleBooleanProperty(false);

		WaypointsManager waypointsManager = new WaypointsManager(graph, mapViewParametersP, bean.waypoints(), error);
		this.baseMapManager = new BaseMapManager(tileManager, waypointsManager, this.mapViewParametersP);
		this.baseMapBackground = new BaseMapManager(tileManager, waypointsManager, this.mapViewParametersBackground);
		RouteManager routeManager = new RouteManager(bean, this.mapViewParametersP);

		// creating a pain that contains the map, the route and the waypoints
		this.paneWithoutBackground = new StackPane(baseMapManager.pane(), routeManager.pane(), waypointsManager.pane());
		this.pane = new StackPane(baseMapBackground.pane(), paneWithoutBackground);

		this.pane.getStylesheets().add("map.css");
		this.mouseCoords = new SimpleObjectProperty<>();

		// when the mouse is moved, its coordinates are saved
		paneWithoutBackground.setOnMouseMoved(e -> {
			mouseCoords.set(new Point2D(e.getX(), e.getY()));
		});


		// when the mouse is exited, then the reset value is set to the mouse position on the route
		paneWithoutBackground.setOnMouseExited(e -> {
			this.mouseCoords.set(null);
		});

		// when the mouse is dragged, then the reset value is set to the mouse position on the route
		paneWithoutBackground.setOnMouseDragged(e -> {
			mouseCoords.set(null);
		});

		// when the mouse is released, then the reset value is set to the mouse position on the route
		paneWithoutBackground.setOnMouseReleased(e -> {
			if (!e.isStillSincePress()) this.mouseCoords.set(null);
		});

		// when the mouse is scrolled and the base map is not being zoomed in then the map is zoomed.
		paneWithoutBackground.setOnScroll(this::zoomMap);

		mousePositionOnRouteProperty.bind(Bindings.createDoubleBinding(() -> {
			// when the mouse coords are null and there is no route then the mousePositionOnRouteProperty is set to its default value
			if ((mouseCoords.get() == null) || (bean.route() == null)) return Double.NaN;
			PointWebMercator pointInWeb = this.mapViewParametersP.get().pointAt(mouseCoords.get().getX(), -mouseCoords.get().getY());
			PointCh pointInSwiss = pointInWeb.toPointCh();

			// if the point in Switzerland is null then we set it to its default value
			if (pointInSwiss == null) return Double.NaN;

			RoutePoint mouseRoutePoint = bean.route().pointClosestTo(pointInSwiss);
			PointWebMercator closestPoint = PointWebMercator.ofPointCh(mouseRoutePoint.point());
			double xDifference = this.mapViewParametersP.get().viewX(closestPoint) - this.mapViewParametersP.get().viewX(pointInWeb);
			double yDifference = this.mapViewParametersP.get().viewY(closestPoint) - this.mapViewParametersP.get().viewY(pointInWeb);
			double distanceXY = Math2.norm(xDifference, yDifference);

			if (distanceXY > PXIELS_CLOSE) return Double.NaN;
			return mouseRoutePoint.position();

		}, mouseCoords, mapViewParametersP, bean.routeProperty()));
	}

	/**
	 * Gives the JavaFX pane displaying the Annotated Map.
	 *
	 * @return the pane.
	 */
	public Pane pane() {
		return this.pane;
	}


	/**
	 * Gives the property of the mouse position on the profile.
	 *
	 * @return the property of the mouse Position on the profile.
	 */
	public ReadOnlyDoubleProperty mousePositionOnRouteProperty() {
		return this.mousePositionOnRouteProperty;
	}

	/**
	 * Redraws the base map on the next pulse.
	 */
	public void refreshMap() {
		this.baseMapManager.refreshMap();
	}

	/**
	 * Zooms the map depending on the ScrollEvent properties.
	 *
	 * @param e the ScrollEvent.
	 */
	private void zoomMap(ScrollEvent e) {
		if (e.getDeltaY() == 0d) return;
		long currentTime = System.currentTimeMillis();
		if (currentTime < minScrollTime.get()) return;
		minScrollTime.set(currentTime + MIN_SCROLL_TIME_DELTA);
		int zoomDelta = (int) Math.signum(e.getDeltaY());
		int actualZoom = this.mapViewParametersP.get().zoomLevel();
		int newZoomLevel = actualZoom + zoomDelta;
		if ((newZoomLevel >= MIN_ALLOWED_ZOOM) && (newZoomLevel <= MAX_ALLOWED_ZOOM) && (!isZooming.get())) {
			isZooming.set(true);
			zoomMouseCoords.set(new Point2D(e.getX(), e.getY()));
			;
			Point2D point = this.mapViewParametersP.get().topLeft();
			PointWebMercator p = PointWebMercator.of(actualZoom, point.getX() + e.getX(), point.getY() + e.getY());
			double newX = p.xAtZoomLevel(newZoomLevel) - e.getX();
			double newY = p.yAtZoomLevel(newZoomLevel) - e.getY();
			AnimationTimer tm = new zoomAnimation(newZoomLevel, newX, newY);
			tm.start();
			setBackGround(newZoomLevel, newX, newY);
		}
	}

	/**
	 * Sets a given PointCh at the center of the map in the default zoom.
	 *
	 * @param pointCh the PointCh.
	 */
	public void setCenter(PointCh pointCh) {
		PointWebMercator pointWeb = PointWebMercator.ofPointCh(pointCh);
		this.mapViewParametersP.set(new MapViewParameters(DEFAULT_ZOOM, pointWeb.xAtZoomLevel(DEFAULT_ZOOM) + DELTA_X_TO_CENTER, pointWeb.yAtZoomLevel(DEFAULT_ZOOM) + DELTA_Y_TO_CENTER));
	}

	/**
	 * Sets a new map as background if the new zoom has changed from the actual one.
	 * for performance issues, the background only changes if the new zoom is bigger than the old one because the background can only be seen when zooming out.
	 *
	 * @param newZoomLevel the new zoom.
	 * @param newX         the new x top left coordinates of the map
	 * @param newY         the new y top left coordinates of the map
	 */
	private void setBackGround(int newZoomLevel, double newX, double newY) {
		if ((newZoomLevel == this.mapViewParametersBackground.get().zoomLevel()) || (newZoomLevel > this.mapViewParametersP.get().zoomLevel()))
			return;
		this.mapViewParametersBackground.set(new MapViewParameters(newZoomLevel, newX, newY));
	}


	/**
	 * This class manages the zoom Animation of the map.
	 */
	private final class zoomAnimation extends AnimationTimer {
		private final double xTrans;
		private final double yTrans;
		private double deltaScale;
		private final boolean zoomIn;
		private final MapViewParameters mapAfterZoom;

		/**
		 * Creates a zoomAnimation. A zoomAnimation is composed of an integer value and two double values.
		 *
		 * @param newZoomLevel the zoomLevel after the animation.
		 * @param newX         the new x top left coord of the map after the animation.
		 * @param newY         the new y top left coord of the map after the animation.
		 */
		private zoomAnimation(int newZoomLevel, double newX, double newY) {
			this.mapAfterZoom = new MapViewParameters(newZoomLevel, newX, newY);
			this.zoomIn = newZoomLevel > mapViewParametersP.get().zoomLevel();
			this.deltaScale = 0;
			this.xTrans = zoomMouseCoords.get().getX() - paneWithoutBackground.getWidth() / 2.0;
			this.yTrans = zoomMouseCoords.get().getY() - paneWithoutBackground.getHeight() / 2.0;
		}

		/**
		 * Manages the zoom Animation : the pane is firstly translated to the mouseCoords then its scale is updated to assure the animation.
		 * When the animation ends, all pane properties are reset and the new map is set.
		 */
		@Override
		public void handle(long now) {
			deltaScale += DELTA_SCALE_FOR_ZOOM;
			if (zoomIn) {
				paneWithoutBackground.setScaleX(paneWithoutBackground.getScaleX() + deltaScale);
				paneWithoutBackground.setScaleY(paneWithoutBackground.getScaleY() + deltaScale);
				paneWithoutBackground.setTranslateX(xTrans - xTrans * paneWithoutBackground.getScaleX());
				paneWithoutBackground.setTranslateY(yTrans - yTrans * paneWithoutBackground.getScaleY());
				if (deltaScale >= MAX_ZOOM_IN_DELTA_SCALE) resetMapAfterZoom();
			} else {
				paneWithoutBackground.setScaleX(paneWithoutBackground.getScaleX() - deltaScale);
				paneWithoutBackground.setScaleY(paneWithoutBackground.getScaleY() - deltaScale);
				paneWithoutBackground.setTranslateX(xTrans - xTrans * paneWithoutBackground.getScaleX());
				paneWithoutBackground.setTranslateY(yTrans - yTrans * paneWithoutBackground.getScaleY());
				if (deltaScale >= MAX_ZOOM_OUT_DELTA_SCALE) resetMapAfterZoom();
			}
		}

		/**
		 * Resets the pane properties, sets the new map and stops the animation.
		 */
		private void resetMapAfterZoom() {
			mapViewParametersP.set(mapAfterZoom);
			stop();
			isZooming.set(false);
			paneWithoutBackground.setScaleX(1);
			paneWithoutBackground.setScaleY(1);
			paneWithoutBackground.setTranslateX(0);
			paneWithoutBackground.setTranslateY(0);
		}
	}
}
