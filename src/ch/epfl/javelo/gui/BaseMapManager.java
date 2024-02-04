package ch.epfl.javelo.gui;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

/**
 * This class manages the display and interaction with the background map.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class BaseMapManager {
	private final TileManager tileManager;
	private final WaypointsManager waypointsManager;
	private final ObjectProperty<MapViewParameters> mapViewParametersP;
	private final ObjectProperty<Point2D> mouseCoords;
	private final ObjectProperty<Point2D> topLeftPoint;
	private boolean redrawNeeded;
	private final Canvas canvas;
	private final Pane pane;

	// constants
	private static final int TILE_SIDE = 256;


	/**
	 * Creates a BaseMapManager. A BaseMapManager is composed of a TileManager, a WaypointsManager and MapViewParameters.
	 *
	 * @param tileManager        a tileManager which is in charge of downloading tiles and retrieving images.
	 * @param waypointsManager   a waypointsManager which is in charge of displaying and interacting with the waypoints.
	 * @param mapViewParametersP the mapViewParameters which represent the parameters of the background map presented in the graphic interface.
	 */
	public BaseMapManager(TileManager tileManager, WaypointsManager waypointsManager, ObjectProperty<MapViewParameters> mapViewParametersP) {
		this.tileManager = tileManager;
		this.waypointsManager = waypointsManager;
		this.mapViewParametersP = mapViewParametersP;
		this.canvas = new Canvas();
		this.pane = new Pane();
		this.mouseCoords = new SimpleObjectProperty<>();
		this.topLeftPoint = new SimpleObjectProperty<>();

		// tells to redraw the map on the next pulse for the first time
		redrawOnNextPulse();

		// binding the canvas with the pane
		canvas.widthProperty().bind(pane.widthProperty());
		canvas.heightProperty().bind(pane.heightProperty());

		// adding listeners on the windows resize
		canvas.heightProperty().addListener((p, o, n) -> redrawOnNextPulse());
		canvas.widthProperty().addListener((p, o, n) -> redrawOnNextPulse());

		// adding listener on scene property
		canvas.sceneProperty().addListener((p, oldS, newS) -> {
			assert oldS == null;
			newS.addPreLayoutPulseListener(this::redrawIfNeeded);
		});

		// adding listener to redraw the map when its params change
		mapViewParametersP.addListener((p, o, n) -> redrawOnNextPulse());

		// adding the canvas to the pane
		pane.getChildren().add(canvas);

		// add a new wayPoint at the position of the mouse when there is a click
		pane.setOnMouseClicked(e -> {
			if (e.isStillSincePress()) {
				this.waypointsManager.addWaypoint(e.getX(), -e.getY());
			}
		});

		// if the mouse is pressed, its coordinates are saved as well as the ones of the top lef corner to be used to calculate the new coords after the drag
		pane.setOnMousePressed(e -> {
			mouseCoords.set(new Point2D(e.getX(), e.getY()));
			topLeftPoint.set(new Point2D(this.mapViewParametersP.getValue().xTopLeftCoord(), this.mapViewParametersP.getValue().yTopLeftCoord()));
		});

		// when dragging, the map is translated
		pane.setOnMouseDragged(e -> {
			Point2D mouseCoordsValue = mouseCoords.getValue();
			if (mouseCoordsValue != null) {
				double xTranslation = e.getX() - mouseCoordsValue.getX();
				double yTranslation = e.getY() - mouseCoordsValue.getY();
				Point2D topLeftPoint = this.topLeftPoint.get();
				double newTopX = topLeftPoint.getX() - xTranslation;
				double newTopY = topLeftPoint.getY() - yTranslation;
				this.mapViewParametersP.setValue(this.mapViewParametersP.getValue().withMinXY(newTopX, newTopY));
			}
		});

		// when the mouse is released, we reset the coordinates of the press for the next drag
		pane.setOnMouseReleased(e -> {
			mouseCoords.set(null);
			topLeftPoint.set(null);
		});
	}

	/**
	 * Gives the JavaFX pane displaying the background map.
	 *
	 * @return the pane.
	 */
	public Pane pane() {
		return this.pane;
	}
	
	/**
	 * Redraws the map if the attribute redrawNeeded is true.
	 */
	private void redrawIfNeeded() {	
		if (!redrawNeeded) return;
		redrawNeeded = false;
		draw();
	}

	/**
	 * Delays the redraw of the map until the next pulse.
	 */
	private void redrawOnNextPulse() {
		redrawNeeded = true;
		Platform.requestNextPulse();
	}

	/**
	 * Draws the background map on the JavaFX canvas.
	 * This method is only used in the method redrawIfNeeded.
	 */
	private void draw() {
		int zoom = mapViewParametersP.get().zoomLevel();
		GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
		MapViewParameters mapValue = mapViewParametersP.get();
		graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		int minX = (int) mapValue.xTopLeftCoord() / TILE_SIDE;
		int minY = (int) mapValue.yTopLeftCoord() / TILE_SIDE;
		int maxX = (int) (mapValue.xTopLeftCoord() + canvas.getWidth()) / TILE_SIDE;
		int maxY = (int) (mapValue.yTopLeftCoord() + canvas.getHeight()) / TILE_SIDE;

		for (int i = minX; i <= maxX; i++) {
			for (int j = minY; j <= maxY; j++) {
				if (TileManager.TileId.isValid(zoom, i, j)) {
					Image image = tileManager.imageForTileAt(new TileManager.TileId(zoom, i, j));
					graphicsContext.drawImage(image, i * TILE_SIDE - mapViewParametersP.get().xTopLeftCoord(), j * TILE_SIDE - mapViewParametersP.get().yTopLeftCoord());
				}
			}
		}
	}

	/**
	 * redraws the map on the next pulse
	 * 
	 */
	public void refreshMap() {
		redrawOnNextPulse();
	}
	
}
