package ch.epfl.javelo.mytest;

import ch.epfl.javelo.data.GraphNodes;
import org.junit.jupiter.api.Test;

import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphNodesTest {

	@Test
	void GraphNodesCount() {
		// List of cases :
		// 1. normal case with 2 nodes
		// 2. case with one node
		// 3. case with no node

		// 1.
		var b1 = IntBuffer.wrap(new int[]{
				2_600_000 << 4,
				1_200_000 << 4,
				0x2_000_1234,
				2500,
				4000,
				1342177295
		});

		var n1 = new GraphNodes(b1);
		var expected = 2;
		var actual = n1.count();
		assertEquals(2, actual);

		// 2.
		var b2 = IntBuffer.wrap(new int[]{
				2_600_000 << 4,
				1_200_000 << 4,
				0x2_000_1234,
		});
		var n2 = new GraphNodes(b2);
		expected = 1;
		actual = n2.count();
		assertEquals(expected, actual);


		// 3.
		var b3 = IntBuffer.wrap(new int[]{});
		var n3 = new GraphNodes(b3);
		expected = 0;
		actual = n3.count();
		assertEquals(expected, actual);
	}
	@Test
	void GraphNodesNodeE() {
		// List of cases :
		// 1. normal case


		// 1.
		var b1 = IntBuffer.wrap(new int[]{
				2_600_000 << 4,
				1_200_000 << 4,
				0x2_000_1234,
				2500,
				4000,
				1342177295
		});
		var n1 = new GraphNodes(b1);
		assertEquals(2_600_000, n1.nodeE(0));
		assertEquals(156.25, n1.nodeE(1));
	}

	@Test
	void GraphNodesNodeN() {
		// List of cases :
		// 1. normal case

		// 1.
		var b1 = IntBuffer.wrap(new int[]{
				2_600_000 << 4,
				1_200_000 << 4,
				0x2_000_1234,
				2500,
				4000,
				1342177295
		});
		var n1 = new GraphNodes(b1);
		assertEquals(1_200_000, n1.nodeN(0));
		assertEquals(250, n1.nodeN(1));
	}

	@Test
	void GraphNodesOutDegree() {
		// List of cases :
		// 1. normal case

		// 1.
		var b1 = IntBuffer.wrap(new int[]{
				2_600_000 << 4,
				1_200_000 << 4,
				0x2_000_1234,
				2500,
				4000,
				1342177295
		});
		var n1 = new GraphNodes(b1);
		assertEquals(2, n1.outDegree(0));
		assertEquals(5, n1.outDegree(1));
	}

	@Test
	void GraphNodesEdgeId() {
		// List of cases :
		// 1. normal case :

		// 1.
		var b1 = IntBuffer.wrap(new int[]{
				2_600_000 << 4,
				1_200_000 << 4,
				0x2_000_1234,
				2500,
				4000,
				1342177295
		});
		var n1 = new GraphNodes(b1);
		assertEquals(0x1234, n1.edgeId(0, 0));
		assertEquals(0x1235, n1.edgeId(0, 1));
		assertEquals(15, n1.edgeId(1, 0));
		assertEquals(16, n1.edgeId(1, 1));

	}

}

