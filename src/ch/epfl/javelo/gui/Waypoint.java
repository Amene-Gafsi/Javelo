package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;

/**
 * This record class represents a transit point.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public record Waypoint(PointCh swissCoord, int closestNodeId) {

}
