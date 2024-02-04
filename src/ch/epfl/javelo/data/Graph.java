package ch.epfl.javelo.data;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * This class represents a graph.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public final class Graph {

	private final GraphNodes nodes;
	private final GraphSectors sectors;
	private final GraphEdges edges;
	private final List<AttributeSet> attributeSets;

	/**
	 * Creates a Graph. A graph is composed of nodes, sectors and edges. It also has a set of attributes.
	 *
	 * @param nodes the nodes composing the graph.
	 * @param sectors the sectors framing the graph.
	 * @param edges the edges composing the graph.
	 * @param attributeSets the set of attributes linked to this graph. (Immutable)
	 */
	public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges, List<AttributeSet> attributeSets) {
		this.nodes = nodes;
		this.sectors = sectors;
		this.edges = edges;
		this.attributeSets = List.copyOf(attributeSets);
	}

	/**
	 * Gives the "JaVelo" graph obtained from the files in the directory whose path is basePath.
	 *
	 * @param basePath the path of the directory that will allow us to access different files.
	 * @return a Graph constructed from all the components in the directory basePath.
	 * @throws IOException in case of an input/output error. For example, if one of the expected files does not exist.
	 */
	public static Graph loadFrom(Path basePath) throws IOException {
		IntBuffer nodesBuffer = mapBuffer(basePath.resolve("nodes.bin")).asIntBuffer();
		ByteBuffer sectorsBuffer = mapBuffer(basePath.resolve("sectors.bin"));
		ByteBuffer edgesBuffer = mapBuffer(basePath.resolve("edges.bin"));
		IntBuffer profileIds = mapBuffer(basePath.resolve("profile_ids.bin")).asIntBuffer();
		ShortBuffer elevations = mapBuffer(basePath.resolve("elevations.bin")).asShortBuffer();
		LongBuffer attributeSets = mapBuffer(basePath.resolve("attributes.bin")).asLongBuffer();
		List<AttributeSet> attributeList = new ArrayList<>();

		for (int i = 0; i < attributeSets.capacity(); i++) {
			attributeList.add(new AttributeSet(attributeSets.get(i)));
		}
		GraphNodes nodes = new GraphNodes(nodesBuffer);
		GraphSectors sectors = new GraphSectors(sectorsBuffer);
		GraphEdges edges = new GraphEdges(edgesBuffer, profileIds, elevations);
		return new Graph(nodes, sectors, edges, attributeList);
	}

	/**
	 * Maps the content of the file in memory, in read only mode, in order to obtain a ByteBuffer whose content is that of the file.
	 * This method's only purpose is to map the content of the different files needed for the method above "loadFrom".
	 *
	 * @param p the path of the file we want to map.
	 * @return a ByteBuffer containing components needed for the method "loadFrom".
	 * @throws IOException in case of an input/output error. For example, if one of the expected files does not exist.
	 */
	private static ByteBuffer mapBuffer(Path p) throws IOException {
		try (FileChannel channel = FileChannel.open(p)) {
			return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
		}
	}

	/**
	 * Gives the total number of nodes in the Graph.
	 *
	 * @return the total number of nodes.
	 */
	public int nodeCount() {
		return this.nodes.count();
	}

	/**
	 * Gives the point of the given identity node.
	 *
	 * @param nodeId the identity of the node.
	 * @return the position of the given identity node.
	 */
	public PointCh nodePoint(int nodeId) {
		return new PointCh(this.nodes.nodeE(nodeId), this.nodes.nodeN(nodeId));
	}

	/**
	 * Gives the number of edges coming out of the given identity node.
	 *
	 * @param nodeId the identity of the node.
	 * @return the number of exiting edges.
	 */
	public int nodeOutDegree(int nodeId) {
		return this.nodes.outDegree(nodeId);
	}

	/**
	 * Gives the identity of the edgeIndex-th edge coming out of the identity nodeId.
	 *
	 * @param nodeId    the identity of the node.
	 * @param edgeIndex the index of the edge.
	 * @return the identity of the edgeIndex-th edge coming out.
	 */
	public int nodeOutEdgeId(int nodeId, int edgeIndex) {
		return this.nodes.edgeId(nodeId, edgeIndex);
	}

	/**
	 * Gives the identity of the node closest to the given point, at the given maximum distance (in meters).
	 *
	 * @param point          the point of reference.
	 * @param searchDistance the maximum distance starting from the point where the nearest node can be found and its identity given.
	 *                       This implies that we are limited by a square of  side equal to --> 2 * searchDistance where the point is
	 *                       its center.
	 * @return the identity of the node closest to the given point, at the given maximum distance. If no nodes match these criteria
	 * then this method returns -1.
	 */
	public int nodeClosestTo(PointCh point, double searchDistance) {
		List<GraphSectors.Sector> sectorsArea = sectors.sectorsInArea(point, searchDistance);
		double minDistance = searchDistance * searchDistance;
		int nodeId = -1;
		double dist;
		for (GraphSectors.Sector sec : sectorsArea) {
			for (int i = sec.startNodeId(); i < sec.endNodeId(); i++) {
				dist = nodePoint(i).squaredDistanceTo(point);
				if (dist < minDistance) {
					nodeId = i;
					minDistance = dist;
				}
			}
		}
		return nodeId;
	}

	/**
	 * Gives the identity of the destination node of the given identity edge.
	 *
	 * @param edgeId the edge identity.
	 * @return the identity of the destination node of the given identity edge
	 */
	public int edgeTargetNodeId(int edgeId) {
		return this.edges.targetNodeId(edgeId);
	}

	/**
	 * Tells if the edge, knowing its identity, goes in the opposite direction of the OSM path from which it comes.
	 *
	 * @param edgeId the edge identity.
	 * @return true if and only if the edge goes in the opposite direction of the OSM path from which it comes.
	 */
	public boolean edgeIsInverted(int edgeId) {
		return this.edges.isInverted(edgeId);
	}

	/**
	 * Gives the set of OSM attributes attached to the given identity edge.
	 *
	 * @param edgeId the edge identity.
	 * @return the set of OSM attributes attached to the given identity edge.
	 */
	public AttributeSet edgeAttributes(int edgeId) {
		return this.attributeSets.get(this.edges.attributesIndex(edgeId));
	}

	/**
	 * Gives the length, in meters, of the given identity edge.
	 *
	 * @param edgeId the edge identity.
	 * @return the length, in meters, of the given identity edge.
	 */
	public double edgeLength(int edgeId) {
		return this.edges.length(edgeId);
	}

	/**
	 * Gives the total positive elevation of the given identity edge.
	 *
	 * @param edgeId the edge identity.
	 * @return the total positive elevation of the given identity edge.
	 */
	public double edgeElevationGain(int edgeId) {
		return this.edges.elevationGain(edgeId);
	}

	/**
	 * Gives the long profile of the given identity edge, in the form of a function.
	 *
	 * @param edgeId the edge identity.
	 * @return the long profile of the edge, in the form of a function. if the edge has no profile,
	 * then this function must return Double.NaN for any argument.
	 */
	public DoubleUnaryOperator edgeProfile(int edgeId) {
		if (!edges.hasProfile(edgeId)) return Functions.constant(Double.NaN);
		double upperBound = edgeLength(edgeId);
		return Functions.sampled(edges.profileSamples(edgeId), upperBound);
	}
}
