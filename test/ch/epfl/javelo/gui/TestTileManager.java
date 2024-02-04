package ch.epfl.javelo.gui;

import java.nio.file.Path;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class TestTileManager extends Application {
	public static void main(String[] args) { launch(args); }

	@Override
	public void start(Stage primaryStage) throws Exception {
		TileManager tm = new TileManager(TileManager.MapStyle.STANDARD);
		Image tileImage = tm.imageForTileAt(
				new TileManager.TileId(19, 271725, 185422));
		Platform.exit();
	}
}
