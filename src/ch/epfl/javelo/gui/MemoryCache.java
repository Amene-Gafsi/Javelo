package ch.epfl.javelo.gui;

import javafx.scene.image.Image;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents a memory cache.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public class MemoryCache extends LinkedHashMap<TileManager.TileId, Image> {
	private static final int MAX_SIZE = 100;
	private final static float LOAD_FACTOR = 0.75F;

	public MemoryCache() {
		super(MAX_SIZE, LOAD_FACTOR, true);
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<TileManager.TileId, Image> eldest) {
		return this.size() > MAX_SIZE; //must override it if used in a fixed cache
	}

	/**
	 * Gives the image corresponding to the key.
	 *
	 * @param key the tile identity.
	 * @return the image corresponding to the given key.
	 */
	public Image retrieve(TileManager.TileId key) {
		return this.get(key);
	}
}
