package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.routing.ElevationProfile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

import java.util.ArrayList;

/**
 * This class manages the display and interaction with the profile of a route.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class ElevationProfileManager {
	private final ReadOnlyObjectProperty<ElevationProfile> elevationProfile;
	private final ReadOnlyDoubleProperty positionProfile;
	private final BorderPane borderPane;
	private final DoubleProperty mousePositionProfile;
	private final VBox vBox;
	private final Pane pane;
	private final Path path;
	private final Group group;
	private final Polygon polygon;
	private final Line line;
	private final Insets insets;
	private ObjectProperty<Affine> screenToWorld;
	private ObjectProperty<Affine> worldToScreen;
	private final ObjectProperty<Rectangle2D> rectangle2D;
	private final ArrayList<Double> xPolygonPoints; // the coordinates of the polygon points along the profile x-axis.

	// constants
	private static final int M_TO_KM_CONVERTER = 1000;
	private static final double X_PIXEL_DISTANCE = 50;
	private static final double Y_PIXEL_DISTANCE = 25;
	private static final double NO_COORD = 0;

	/**
	 * Creates an ElevationProfileManager. An ElevationProfileManager is composed of a ReadOnlyObjectProperty<ElevationProfile> and a ReadOnlyDoubleProperty.
	 *
	 * @param elevationProfile the property, accessible in read-only mode, containing the profile to be displayed.
	 * @param positionProfile  the property, accessible in read-only mode, containing the position along the profile to be highlighted.
	 */
	public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> elevationProfile, ReadOnlyDoubleProperty positionProfile) {
		this.elevationProfile = elevationProfile;
		this.positionProfile = positionProfile;
		this.borderPane = new BorderPane();
		this.vBox = new VBox();
		this.path = new Path();
		this.pane = new Pane();
		this.group = new Group();
		this.polygon = new Polygon();
		this.line = new Line();
		this.insets = new Insets(10, 10, 20, 40);
		this.rectangle2D = new SimpleObjectProperty<>();
		this.xPolygonPoints = new ArrayList<>();
		this.mousePositionProfile = new SimpleDoubleProperty(Double.NaN);

		// setting the identity corresponding to the path, polygon and vBox
		this.path.setId("grid");
		this.polygon.setId("profile");
		this.vBox.setId("profile_data");

		// adding the style sheet to the borderPane
		this.borderPane.getStylesheets().add("elevation_profile.css");

		// adding all the children of the pane
		this.pane.getChildren().addAll(path, group, polygon, line);

		// placing correctly the pane and the vBox in the borderPane
		this.borderPane.setCenter(pane);
		this.borderPane.setBottom(vBox);

		// binding the rectangle2D dimensions to the pane
		rectangle2D.bind(Bindings.createObjectBinding(() -> new Rectangle2D(insets.getLeft(), insets.getTop(), Math.max(0, pane.getWidth() - insets.getRight() - insets.getLeft()),
				Math.max(0, pane.getHeight() - insets.getTop() - insets.getBottom())), pane.widthProperty(), pane.heightProperty()));

		// each time the rectangle2D dimensions change, the transform functions, grid and the polygon are updated
		rectangle2D.addListener((x) -> {
			if (rectangle2D.get().getWidth() > 0) {
				this.line.startYProperty().bind(Bindings.select(rectangle2D.get(), "minY"));
				this.line.endYProperty().bind(Bindings.select(rectangle2D.get(), "maxY"));
				updateTransform();
				drawPolygon();
				drawGrid();
				writeStatistics();
			}
		});

		// each time the elevationProfile changes, the transform functions, grid and the polygon are updated
		this.elevationProfile.addListener((x) -> {
			if (elevationProfile.get() != null) {
				updateTransform();
				drawPolygon();
				drawGrid();
				writeStatistics();
			}
		});

		// when the mouse is moving inside the rectangle2D, we save it's coordinates in the world system. Otherwise mousePositionProfile is NaN
		pane.setOnMouseMoved(e -> {
			if ((e.getX() < rectangle2D.get().getMinX())
					|| (e.getX() > rectangle2D.get().getMaxX())
					|| (e.getY() < rectangle2D.get().getMinY())
					|| (e.getY() > rectangle2D.get().getMaxY())) {
				mousePositionProfile.set(Double.NaN);
			} else {
				double xCoord = this.screenToWorld.get().transform(e.getX(), NO_COORD).getX();
				mousePositionProfile.set(xCoord);
			}
		});
		
		pane.setOnMouseExited(e -> {
			mousePositionProfile.set(Double.NaN);
		});

		// the line xLayout is bound to the position along the profile.
		this.line.layoutXProperty().bind(Bindings.createDoubleBinding(() -> {
			if (this.worldToScreen != null)
				return this.worldToScreen.get().transform(this.positionProfile.get(), NO_COORD).getX();
			return Double.NaN;
		}, this.positionProfile));

		this.line.visibleProperty().bind(this.positionProfile.greaterThanOrEqualTo(0));
	}


	/**
	 * Gives the JavaFX pane displaying the Elevation Profile.
	 *
	 * @return the pane.
	 */
	public BorderPane pane() {
		return this.borderPane;
	}

	/**
	 * Gives the property of the mouse position on the profile.
	 *
	 * @return the property of the mouse Position on the profile.
	 */
	public ReadOnlyDoubleProperty mousePositionOnProfileProperty() {
		return this.mousePositionProfile;
	}


	/**
	 * Draws the polygon of the elevation profile.
	 * This is done by adding all positions of the profile linked to their elevation to the polygon.
	 */
	private void drawPolygon() {
		xPolygonPoints.clear();
		polygon.getPoints().clear();
		double xCoord, yCoord, yScreenCoord;

		yScreenCoord = worldToScreen.get().transform(0, elevationProfile.get().minElevation()).getY();
		xPolygonPoints.add(rectangle2D.get().getMinX());
		xPolygonPoints.add(yScreenCoord);

		// we draw each point of the coordinate at its position along the x-axis and its elevation along y-Axis.
		for (int i = (int) rectangle2D.get().getMinX(); i <= rectangle2D.get().getMaxX(); ++i) {
			xCoord = screenToWorld.get().transform(i, 0).getX();
			yCoord = elevationProfile.get().elevationAt(xCoord);
			yScreenCoord = worldToScreen.get().transform(0, yCoord).getY();
			xPolygonPoints.add((double) i);
			xPolygonPoints.add(yScreenCoord);
		}
		yScreenCoord = worldToScreen.get().transform(0, elevationProfile.get().minElevation()).getY();
		xPolygonPoints.add(rectangle2D.get().getMaxX());
		xPolygonPoints.add(yScreenCoord);
		polygon.getPoints().addAll(xPolygonPoints);

	}

	/**
	 * Draws the grid of the polygon with their labels.
	 */
	private void drawGrid() {
		this.path.getElements().clear();
		this.group.getChildren().clear();

		int[] POS_STEPS = {1000, 2000, 5000, 10_000, 25_000, 50_000, 100_000};
		int[] ELE_STEPS = {5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000};

		// initialize the value of the steps to the last value of the array
		double posStep = worldToScreen.get().deltaTransform(POS_STEPS[POS_STEPS.length - 1], NO_COORD).getX();
		double eleStep = -worldToScreen.get().deltaTransform(NO_COORD, ELE_STEPS[ELE_STEPS.length - 1]).getY();
		int yStep = ELE_STEPS[ELE_STEPS.length - 1];


		if ((posStep != 0) && (eleStep != 0)) {
			// if a smaller value that respects the distance of pixels between the grid lines is found then our step takes the new value
			for (int pos : POS_STEPS) {
				if (worldToScreen.get().deltaTransform(pos, NO_COORD).getX() >= X_PIXEL_DISTANCE) {
					posStep = worldToScreen.get().deltaTransform(pos, NO_COORD).getX();
					break;
				}
			}

			for (int ele : ELE_STEPS) {
				if (-worldToScreen.get().deltaTransform(NO_COORD, ele).getY() >= Y_PIXEL_DISTANCE) {
					yStep = ele;
					eleStep = -worldToScreen.get().deltaTransform(NO_COORD, ele).getY();
					break;
				}
			}

			// initialize the first grid coordinates
			double xGrid = (int) this.rectangle2D.get().getMinX();
			double yGrid = (int) Math.ceil(this.elevationProfile.get().minElevation());

			// for the elevation grid we search for the smaller value that divides the step
			while (yGrid % yStep != 0) yGrid += 1;
			yGrid = worldToScreen.get().transform(NO_COORD, yGrid).getY();

			// while the coordinates do not exceed the rectangle2D, we draw a new gridLine after each step
			while (xGrid <= this.rectangle2D.get().getWidth() + this.rectangle2D.get().getMinX()) {
				MoveTo xStart = new MoveTo(xGrid, this.rectangle2D.get().getMinY());
				LineTo xEnd = new LineTo(xGrid, this.rectangle2D.get().getMaxY());
				this.path.getElements().addAll(xStart, xEnd);

				// we draw the label of the grid line already drawn
				String xGridCoord = Math2.ceilDiv((int) (Math.round(screenToWorld.get().transform(xGrid, NO_COORD).getX())), M_TO_KM_CONVERTER) + "";
				Text xGridText = new Text(xGrid, this.rectangle2D.get().getMaxY(), xGridCoord);
				xGridText.getStyleClass().addAll("grid_label", "horizontal");
				xGridText.setFont(Font.font("Avenir", 10));
				xGridText.setTextOrigin(VPos.TOP);
				xGridText.setLayoutX(-xGridText.prefWidth(0) / 2);

				this.group.getChildren().add(xGridText);

				xGrid += posStep;
			}

			// we do the same for the elevation grid
			while (yGrid >= this.rectangle2D.get().getMinY()) {
				MoveTo yStart = new MoveTo(this.rectangle2D.get().getMinX(), yGrid);
				LineTo yEnd = new LineTo(this.rectangle2D.get().getMaxX(), yGrid);
				this.path.getElements().addAll(yStart, yEnd);

				String yGridCoord = (int) Math.round(screenToWorld.get().transform(NO_COORD, yGrid).getY()) + "";
				Text yGridText = new Text(this.rectangle2D.get().getMinX(), yGrid, yGridCoord);
				yGridText.getStyleClass().addAll("grid_label", "vertical");
				yGridText.setFont(Font.font("Avenir", 10));
				yGridText.setTextOrigin(VPos.CENTER);
				yGridText.setLayoutX(-yGridText.prefWidth(0) - 2);

				this.group.getChildren().add(yGridText);

				yGrid -= eleStep;
			}
		}
	}

	/**
	 * Draws the statistics of the profile on the bottom part of the borderPane.
	 */
	private void writeStatistics() {
		this.vBox.getChildren().clear();
		double length = this.elevationProfile.get().length() / M_TO_KM_CONVERTER;
		double ascent = this.elevationProfile.get().totalAscent();
		double descent = this.elevationProfile.get().totalDescent();
		double minElevation = this.elevationProfile.get().minElevation();
		double maxElevation = this.elevationProfile.get().maxElevation();

		String statistics = String.format("Longueur : %.1f km" +
				"     Montée : %.0f m" +
				"     Descente : %.0f m" +
				"     Altitude : de %.0f m à %.0f m", length, ascent, descent, minElevation, maxElevation);

		Text vBoxText = new Text(statistics);

		this.vBox.getChildren().add(vBoxText);
	}

	/**
	 * Updates the function that converts from screen coordinates to world coordinates and vice-versa.
	 */
	private void updateTransform() {
		this.worldToScreen = new SimpleObjectProperty<>(new Affine());
		this.screenToWorld = new SimpleObjectProperty<>(new Affine());
		screenToWorld.get().prependTranslation(-rectangle2D.get().getMinX(), -rectangle2D.get().getMinY());
		double xScale = elevationProfile.get().length() / rectangle2D.get().getWidth();
		double yScale = -(elevationProfile.get().maxElevation() - elevationProfile.get().minElevation()) / rectangle2D.get().getHeight();
		screenToWorld.get().prependScale(xScale, yScale);
		screenToWorld.get().prependTranslation(NO_COORD, elevationProfile.get().maxElevation());
		try {
			worldToScreen.set(screenToWorld.get().createInverse());
		} catch (NonInvertibleTransformException e)  {
			// should never happen
			throw new Error();
		}
	}
}