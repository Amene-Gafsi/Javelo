package ch.epfl.javelo.data;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This record class represents the array of all the sectors of the graph.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public record GraphSectors(ByteBuffer buffer) {

	private static final int NUMBER_OF_SECTOR_PER_SIDE = 128;
	private static final int SECTOR_SIZE_BYTE = 6;
	private final static double SECTOR_WIDTH = SwissBounds.WIDTH / NUMBER_OF_SECTOR_PER_SIDE;
	private final static double SECTOR_HEIGHT = SwissBounds.HEIGHT / NUMBER_OF_SECTOR_PER_SIDE;

	/**
	 * This method gives the sectors that intersect with the area of (2*distance)x(2*distance) with center as its center.
	 *
	 * See bellow :
	 * <- 2*distance ->
	 * |--------------|
	 * |              |
	 * |       x   <--|---center
	 * |              |
	 * |--------------|
	 *
	 * @param center   center of the area.
	 * @param distance distance of each side of the center.
	 * @return list of sectors contained inside the area (or touched by the border of the area).
	 * @throws IllegalArgumentException if the center is not inside Switzerland, see {@link SwissBounds#containsEN(double, double)}.
	 */
	public List<Sector> sectorsInArea(PointCh center, double distance) {
		Preconditions.checkArgument(SwissBounds.containsEN(center.e(), center.n()));

		// clamp to put the points inside the Swiss territory
		// Subtracting by SwissBounds.MIN_E or SwissBounds.MIN_N allows working with axes beginning at (0,0)
		double xMaxBorder = Math2.clamp(SwissBounds.MIN_E, center.e() + distance, SwissBounds.MAX_E) - SwissBounds.MIN_E;
		double xMinBorder = Math2.clamp(SwissBounds.MIN_E, center.e() - distance, SwissBounds.MAX_E) - SwissBounds.MIN_E;
		double yMaxBorder = Math2.clamp(SwissBounds.MIN_N, center.n() + distance, SwissBounds.MAX_N) - SwissBounds.MIN_N;
		double yMinBorder = Math2.clamp(SwissBounds.MIN_N, center.n() - distance, SwissBounds.MAX_N) - SwissBounds.MIN_N;

		int xMin = (int) (xMinBorder / SECTOR_WIDTH);
		int xMax = (int) (xMaxBorder / SECTOR_WIDTH);
		int yMin = (int) (yMinBorder / SECTOR_HEIGHT);
		int yMax = (int) (yMaxBorder / SECTOR_HEIGHT);

		ArrayList<Sector> sectorsArea = new ArrayList<>();

		int firstSelectedSector = yMin * NUMBER_OF_SECTOR_PER_SIDE + xMin;
		int lineUpperBound = yMax * NUMBER_OF_SECTOR_PER_SIDE + xMin;
		int columnSize = xMax - xMin;
		for (int i = firstSelectedSector; i <= lineUpperBound; i += NUMBER_OF_SECTOR_PER_SIDE) {
			for (int j = i; j <= i + columnSize; j++) {
				int crtSectIndexInBArray = j * SECTOR_SIZE_BYTE;
				Sector actualSector = new Sector(
						buffer.getInt(crtSectIndexInBArray),
						buffer.getInt(crtSectIndexInBArray) + Short.toUnsignedInt(buffer.getShort(crtSectIndexInBArray + 4))
				);
				sectorsArea.add(actualSector);
			}
		}
		return sectorsArea;
	}

	/**
	 * Sector is represented by its starting and end node id.
	 */
	public record Sector(int startNodeId, int endNodeId) {
	}
}




