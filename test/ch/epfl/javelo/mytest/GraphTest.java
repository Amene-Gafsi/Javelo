package ch.epfl.javelo.mytest;

import ch.epfl.javelo.Q28_4;
import ch.epfl.javelo.data.*;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphTest {
	@Test
	void GraphTestLoadFromNormalCase() throws IOException {
		var basePath = Path.of("lausanne");
		IntBuffer nodesBuffer;
		ByteBuffer sectorsBuffer;
		ByteBuffer edgesBuffer;
		IntBuffer profileIds;
		ShortBuffer elevations;
		LongBuffer attributeSets;

		try (FileChannel channel = FileChannel.open(basePath.resolve("nodes.bin"))) {
			nodesBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()).asIntBuffer();
		}
		try (FileChannel channel = FileChannel.open(basePath.resolve("sectors.bin"))) {
			sectorsBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
		}
		try (FileChannel channel = FileChannel.open(basePath.resolve("edges.bin"))) {
			edgesBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
		}
		try (FileChannel channel = FileChannel.open(basePath.resolve("profile_ids.bin"))) {
			profileIds = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()).asIntBuffer();
		}
		try (FileChannel channel = FileChannel.open(basePath.resolve("elevations.bin"))) {
			elevations = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()).asShortBuffer();
		}
		try (FileChannel channel = FileChannel.open(basePath.resolve("attributes.bin"))) {
			attributeSets = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()).asLongBuffer();
		}
		List<AttributeSet> attributeList = new ArrayList<>();
		for (int i = 0; i < attributeSets.capacity(); i++) {
			attributeList.add(new AttributeSet(attributeSets.get(i)));
		}
		var graph = new Graph(
				new GraphNodes(nodesBuffer), new GraphSectors(sectorsBuffer), new GraphEdges(edgesBuffer, profileIds, elevations), attributeList
		);
		assertEquals(graph, Graph.loadFrom(basePath));
	}

	@Test
	void GraphTestNodeClosestTo() {

		// Normal Case
		var point = new PointCh(SwissBounds.MIN_E + 1000, SwissBounds.MIN_N + 1000); // in the sector 0
		var s = ByteBuffer.allocate(1500 * 6);
		s.putInt(0, 0);
		s.putShort(4, (short) 2);
		var sectors = new GraphSectors(s);

		var n = IntBuffer.allocate(1500 * 6);
		n.put(0, Q28_4.ofInt((int) (SwissBounds.MIN_E + 3)));
		n.put(1, Q28_4.ofInt((int) (SwissBounds.MIN_N + 5)));
		n.put(2, 0);
		n.put(3, Q28_4.ofInt((int) (SwissBounds.MIN_E + 3))); //999 returns 1
		n.put(4, Q28_4.ofInt((int) (SwissBounds.MIN_N + 5))); //999 returns 1
		n.put(5, 0);
		var nodes = new GraphNodes(n);

		var edges = new GraphEdges(null, null, null);
		var listAttributes = new ArrayList<AttributeSet>();
		var graph = new Graph(nodes, sectors, edges, listAttributes);
		assertEquals(-1, graph.nodeClosestTo(point, 10));
	}
}