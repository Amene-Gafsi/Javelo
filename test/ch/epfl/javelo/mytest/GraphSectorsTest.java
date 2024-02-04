package ch.epfl.javelo.mytest;

import ch.epfl.javelo.data.GraphSectors;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphSectorsTest {
	@Test
	void GraphSectorsInArea() {

		double sectorWidth = SwissBounds.WIDTH / 128; // 2'726.5625
		double sectorHeight = SwissBounds.HEIGHT / 128; // 1'726.5625

		// test one sector
		var point = new PointCh(SwissBounds.MIN_E + 1000, SwissBounds.MIN_N + 1000); // in the sector 0
		var s = ByteBuffer.allocate(1500 * 6);
		s.putInt(0, 1);
		s.putShort(4, (short) 4);
		var sectors = new GraphSectors(s);
		var got = sectors.sectorsInArea(point, 10); // must return only the first sector
		assertEquals(1, got.size()); // must only have one inside
		assertEquals(1, got.get(0).startNodeId());
		assertEquals(1 + 4, got.get(0).endNodeId());

		// test square going outside the box
		s.putInt(1 * 6, 1);
		s.putShort(1 * 6 + 4, (short) 5);
		s.putInt(128 * 6, 1);
		s.putShort(128 * 6 + 4, (short) 6);
		s.putInt(129 * 6, 1);
		s.putShort(129 * 6 + 4, (short) 7);
		got = sectors.sectorsInArea(point, 2000);
		assertEquals(4, got.size()); // must only have one inside
		for (int i = 4; i < 8; i++) {
			assertEquals(1, got.get(i - 4).startNodeId());
			assertEquals(1 + i, got.get(i - 4).endNodeId());
		}

		point = new PointCh(SwissBounds.MIN_E + 10 * sectorWidth + sectorWidth/2, SwissBounds.MIN_N + 10 * sectorHeight + sectorHeight/2);
		s.putInt(1290 * 6, 1);
		s.putShort(1290 * 6 + 4, (short) 5);
		got = sectors.sectorsInArea(point, 2000);
		assertEquals(9, got.size());
		assertEquals(1, got.get(4).startNodeId());
		assertEquals(1 + 5, got.get(4).endNodeId());
	}
}