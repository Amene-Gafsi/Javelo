package ch.epfl.javelo.gui;

import javafx.scene.image.Image;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class represents a disk cache.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public class DiskCache {
	private final String basePath;

	public DiskCache(Path p) {
		this.basePath = p.toAbsolutePath().toString();
	}

	/**
	 * Gives the image corresponding to the key.
	 * If we can't properly download all the tiles then the program crashes.
	 *
	 * @param key the tile identity.
	 * @return the image corresponding to the given key.
	 */
	public Image retrieve(TileManager.TileId key) {
		Path path = Paths.get(basePath + "/" + key.zoomLevel() + "/" + key.xTileCoord() + "/" + key.yTileCoord() + ".png");
		boolean tileExists = Files.exists(path);
		if (tileExists) {
			try (FileInputStream stream = new FileInputStream(path.toFile())) {
				return new Image(stream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		return null;
	}

	/**
	 * Places the tile in the disk cache.
	 * If we can't properly download all the tiles then the program crashes.
	 *
	 * @param key         the identity of the tile.
	 * @param inputStream the inputStream containing the image of the tile.
	 *                    Warning : the inputStream is fully read after the method.
	 */
	public void put(TileManager.TileId key, InputStream inputStream) {
		String folderPath = basePath + "/" + key.zoomLevel() + "/" + key.xTileCoord();
		String filePath = folderPath + "/" + key.yTileCoord() + ".png";

		boolean tileFolderExists = Files.exists(Paths.get(folderPath));
		if (!tileFolderExists) {
			try {
				Files.createDirectories(Paths.get(folderPath));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		// replace the (maybe existing?) file
		try (OutputStream img = new FileOutputStream(filePath, false)) {
			inputStream.transferTo(img);
			img.flush();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
