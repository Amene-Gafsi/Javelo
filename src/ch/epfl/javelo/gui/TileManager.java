package ch.epfl.javelo.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

/**
 * This class represents an OSM tile manager.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class TileManager {
	private final ObjectProperty<MapStyle> currentStyle;

	/**
	 * Enumeration describing the different styles of the map.
	 * Each style has its own memory and disk cache.
	 */
	public enum MapStyle {
		/**
		 * The standard map which is a basic openstreetmap.
		 */
		STANDARD(new MemoryCache(), new DiskCache(Path.of("osm-cache")), "tile.openstreetmap.org"),
		/**
		 * The cyclosm map destined for bicycles.
		 */
		CYCLOSM(new MemoryCache(), new DiskCache(Path.of("cyclosm-cache")), "a.tile-cyclosm.openstreetmap.fr/cyclosm");

		private final MemoryCache memoryCache;
		private final DiskCache diskCache;
		private final String name;

		public MemoryCache getMemoryCache() {
			return memoryCache;
		}

		public DiskCache getDiskCache() {
			return diskCache;
		}

		public String getName() {
			return name;
		}

		private MapStyle(MemoryCache memoryCache, DiskCache diskCache, String name) {
			this.memoryCache = memoryCache;
			this.diskCache = diskCache;
			this.name = name;
		}
	}

	/**
	 * Creates a TileManager. A TileManager is composed of a MapStyle.
	 *
	 * @param mapStyle the initial style of the map.
	 */
	public TileManager(MapStyle mapStyle) {
		this.currentStyle = new SimpleObjectProperty<>(mapStyle);
	}

	/**
	 * Gives the image corresponding to the tile identity.
	 * The image is first searched in the memory cache, then in the disk cache and if it is not found in both,
	 * it is then loaded from the tile server and placed in both the disk cache and the memory cache. If the image
	 * is not found in the memory cache but in the disk cache then we place the image in the memory cache before returning it.
	 * If we can't properly download all the tiles then the program crashes.
	 *
	 * @param tileId the identity of the tile.
	 * @return the image corresponding to the tile identity. The image can never be null because it
	 * is always loaded from the tile server if not found in both of the caches.
	 */
	public Image imageForTileAt(TileId tileId) {
		Image image = currentStyle.get().getMemoryCache().retrieve(tileId);
		if (image == null) {
			image = currentStyle.get().getDiskCache().retrieve(tileId);
			if (image == null) {
				try (ByteArrayOutputStream baos = downloadTile(tileId)) {
					// duplicate input stream. Low memory.
					currentStyle.get().getDiskCache().put(tileId, new ByteArrayInputStream(baos.toByteArray()));
					image = new Image(new ByteArrayInputStream(baos.toByteArray()));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
			currentStyle.get().getMemoryCache().put(tileId, image);
		}
		return image;
	}

	/**
	 * Downloads the tile from the tile server.
	 * If we can't properly download all the tiles then the program crashes.
	 *
	 * @param tileId the identity of the tile.
	 * @return a ByteArrayOutputStream from which you can read the image of the tile. May be empty if download not successful.
	 */
	private ByteArrayOutputStream downloadTile(TileId tileId) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			URL u = new URL(
					"https://" + currentStyle.get().getName() + "/" + tileId.zoomLevel + "/" + tileId.xTileCoord + "/" + tileId.yTileCoord + ".png");
			URLConnection c = u.openConnection();
			c.setRequestProperty("User-Agent", "JaVelo");
			c.getInputStream().transferTo(baos);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
			//TODO (leaved explicitly) possible amelioration :
			// we can try to download the tile 5 times and if after 5 attempts the download still fails
			// then we crash the program.
		}
		return baos;
	}

	/**
	 * Changes the style of the map.
	 * Does nothing if the current mapStyle is already the one given in parameter.
	 *
	 * @param mapStyle the new style of the map.
	 * @return true is the map style has changed and false otherwise.
	 */
	public boolean changeStyle(MapStyle mapStyle) {
		// it does nothing
		if (mapStyle == currentStyle.get()) return false;
		currentStyle.set(mapStyle);
		return true;
	}

	/**
	 * Gives the property that indicates the current style of the map.
	 *
	 * @return currentStyle ReadOnly property.
	 */
	public ReadOnlyObjectProperty<MapStyle> currentStyleProperty() {
		return this.currentStyle;
	}

	/**
	 * This nested record class represents the identity of a tile.
	 */
	public record TileId(int zoomLevel, int xTileCoord, int yTileCoord) {
		/**
		 * Checks if the identity of a tile is valid.
		 *
		 * @param zoomLevel  the zoom level.
		 * @param xTileCoord the x-index of the tile.
		 * @param yTileCoord the y-index of the tile.
		 * @return true if the identity of a tile is valid.
		 */
		public static boolean isValid(int zoomLevel, int xTileCoord, int yTileCoord) {
			int maxTileCoord = 1 << zoomLevel;
			return (xTileCoord >= 0) && (xTileCoord < maxTileCoord) && (yTileCoord >= 0) && (yTileCoord < maxTileCoord);
		}
	}

}
