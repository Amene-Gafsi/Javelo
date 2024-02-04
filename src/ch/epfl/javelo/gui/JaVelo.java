package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.CityBikeCF;
import ch.epfl.javelo.routing.CostFunction;
import ch.epfl.javelo.routing.GpxGenerator;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class represents the main class of the Javelo application.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class JaVelo extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Entry point of the graphical interface of the application.
	 *
	 * @param primaryStage the stage on which the different scenes will be displayed.
	 * @throws Exception If an issue occurs.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		// creating a TileManager with the standard OpenStreetMap
		TileManager tileManager = new TileManager(TileManager.MapStyle.STANDARD);

		// creating an ErrorManager
		ErrorManager errorManager = new ErrorManager();

		// loading the Javelo graph
		Graph graph = Graph.loadFrom(Path.of("javelo-data"));

		// creating a RouteComputer, an itinerary planner, that is composed of a CostFunction
		CostFunction cf = new CityBikeCF(graph);
		RouteComputer computer = new RouteComputer(graph, cf);

		// creating a RouteBean that is composed of a RouteComputer
		RouteBean bean = new RouteBean(computer, errorManager::displayError);

		// creating an AnnotatedMapManager that manages the display of the background map
		// containing the route and the waypoints
		AnnotatedMapManager annotatedMap = new AnnotatedMapManager(graph, tileManager, bean, errorManager::displayError);

		// creating an ElevationProfileManager that manages the display and interaction with the profile of a route.
		ElevationProfileManager elevationProfileManager = new ElevationProfileManager(bean.elevationProfileProperty(), bean.highlightedPositionProperty());

		// the highlighted position is bound to the mouse position on route if it's greater or equal than 0. Otherwise, it is bound the mouse position on the profile.
		bean.highlightedPositionProperty().bind(Bindings.createObjectBinding(() -> {
			if (annotatedMap.mousePositionOnRouteProperty().get() >= 0)
				return annotatedMap.mousePositionOnRouteProperty().get();
			return elevationProfileManager.mousePositionOnProfileProperty().get();
		}, annotatedMap.mousePositionOnRouteProperty(), elevationProfileManager.mousePositionOnProfileProperty()));

		// creating the Split pane containing the background map and the profile.
		// The profile is only added to the SplitPane if a route exists.
		SplitPane mapAndProfile = new SplitPane(annotatedMap.pane());

		// if a route no longer exists, then it's pane is removed.
		// if a new route is added then it's pane is added.
		bean.routeProperty().addListener((p, o, n) -> {
			if ((n != null) && (mapAndProfile.getItems().size() == 1)) {
				mapAndProfile.getItems().add(elevationProfileManager.pane());
			} else if ((n == null) && mapAndProfile.getItems().size() == 2) {
				mapAndProfile.getItems().remove(1);
			}
		});

		// ensure that the panel containing the profile is not resized vertically when the window is
		SplitPane.setResizableWithParent(elevationProfileManager.pane(), false);
		mapAndProfile.setOrientation(Orientation.VERTICAL);

		// creating a Menu containing two MenuItems that can export or import routes in GPX format
		Menu fileMenu = new Menu("Fichier");
		MenuItem exportGpx = new MenuItem("Exporter GPX");
		MenuItem importGpx = new MenuItem("Importer GPX");
		fileMenu.getItems().addAll(importGpx, exportGpx);

		// creating a Menu containing two MenuItems that can erase or inverse a route
		Menu routeMenu = new Menu("Route");
		MenuItem deleteRoute = new MenuItem("Effacer la route");
		MenuItem inverseRoute = new MenuItem("Inverser la route");
		routeMenu.getItems().addAll(deleteRoute, inverseRoute);

		// at the beginning we can't select these MenuItems because there is no route
		inverseRoute.setDisable(true);
		deleteRoute.setDisable(true);

		// creating a Menu containing two MenuItems that can change the style of the map
		Menu mapMenu = new Menu("Carte");
		// the standard map
		MenuItem standardOpenStreetMap = new MenuItem("Standard OpenStreetMap");

		// at the beginning we can't select this MenuItem because the current map is already the standard OpenStreetMap
		standardOpenStreetMap.setDisable(true);

		// the cyclosm map that is better for bicycles
		MenuItem cyclosmOpenStreetMap = new MenuItem("Cyclosm OpenStreetMap");

		mapMenu.getItems().addAll(standardOpenStreetMap, cyclosmOpenStreetMap);

		// deleting a route
		deleteRoute.setOnAction(e -> {
			bean.waypoints().clear();
		});

		// reversing a route
		inverseRoute.setOnAction(e -> {
			Collections.reverse(bean.waypoints());
		});

		// we change the current map style to the standardOpenStreetMap when we click on it
		standardOpenStreetMap.setOnAction(event -> {
			// we only change the map style if the current map style is different from the new map style
			if (tileManager.changeStyle(TileManager.MapStyle.STANDARD)) {
				// using errorManager to display a simple message, no need for another message manager with another style
				errorManager.displayError("La carte a été changée");
				annotatedMap.refreshMap();
			}
		});

		// we change the current map style to the cyclosmOpenStreetMap when we click on it
		cyclosmOpenStreetMap.setOnAction(event -> {
			// we only change the map style if the current map style is different from the new map style
			if (tileManager.changeStyle(TileManager.MapStyle.CYCLOSM)) {
				// using errorManager to display a simple message, no need for another message manager with another style
				errorManager.displayError("La carte a été changée");
				annotatedMap.refreshMap();
			}
		});

		// creating a MenuBar containing the fileMenu, the routeMenu and the mapMenu.
		MenuBar menuBar = new MenuBar(fileMenu, routeMenu, mapMenu);

		// at the beginning we can't export a route because there is no route
		exportGpx.setDisable(true);

		// we setDisable the MenuItems for changing the map style depending on the current map style
		tileManager.currentStyleProperty().addListener(c -> {
			if (tileManager.currentStyleProperty().get().equals(TileManager.MapStyle.STANDARD)) {
				standardOpenStreetMap.setDisable(true);
				cyclosmOpenStreetMap.setDisable(false);
			} else {
				standardOpenStreetMap.setDisable(false);
				cyclosmOpenStreetMap.setDisable(true);
			}
		});

		// if there is no route, then we shouldn't be able to export, delete or inverse a route
		bean.routeProperty().addListener(p -> {
			exportGpx.setDisable(bean.route() == null);
			exportGpx.setDisable(bean.route() == null);
			inverseRoute.setDisable(bean.route() == null);
			deleteRoute.setDisable(bean.route() == null);
		});

		// when we click on this menuItem, then we should be able to export the route is GPX format
		exportGpx.setOnAction(event -> {
			try {
				GpxGenerator.writeGpx(bean.route(), bean.elevationProfileProperty().get(), bean.waypoints());
			} catch (IOException e) {
				// should never happen
				throw new UncheckedIOException(e);
			}
		});

		// when we click on this menuItem, then we should be able to import a route in GPX format
		importGpx.setOnAction(event -> {
			try {
				List<Waypoint> wayPoints = GpxGenerator.readGpx();
				if (wayPoints.size() > 1) {
					bean.waypoints().clear();
					annotatedMap.setCenter(wayPoints.get(0).swissCoord());
					bean.waypoints().addAll(wayPoints);
				}
			} catch (ParserConfigurationException | SAXException e1) {
				System.out.println(e1);
			}
		});

		// creating the mainPane that contains all the panes
		BorderPane mainPane = new BorderPane();
		StackPane allPanes = new StackPane(mapAndProfile, errorManager.pane());

		// the map, profile and error pane are centered
		mainPane.setCenter(allPanes);

		// the menuBar is at the top
		mainPane.setTop(menuBar);

		// the primaryStage setups
		primaryStage.setMinWidth(800);
		primaryStage.setMinHeight(600);
		primaryStage.setScene(new Scene(mainPane));
		primaryStage.setTitle("Javelo");
		primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/bike.jpg"))));
		primaryStage.show();
	}
}

