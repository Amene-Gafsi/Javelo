package ch.epfl.javelo.mytest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.javelo.routing.Edge;
import ch.epfl.javelo.routing.RoutePoint;
import ch.epfl.javelo.routing.SingleRoute;
import org.junit.jupiter.api.Test;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;


public class SingleRouteTest {
	
	@Test
	void classTest() {
		List<Edge> edges = new ArrayList<>();
		List<PointCh> expectedPoints = new ArrayList<>();
		
		edges.add(new Edge(1, 2, new PointCh(2485010, 1075010), new PointCh(2485020, 1075020), 14.14213562373095, Functions.constant(Double.NaN)));
		edges.add(new Edge(2, 4, new PointCh(2485020, 1075020), new PointCh(2485030, 1075030), 14.14213562373095, Functions.constant(-8)));
		edges.add(new Edge(4, 6, new PointCh(2485030, 1075030), new PointCh(2485040, 1075040), 14.14213562373095, Functions.constant(0)));
		
		expectedPoints.add(new PointCh(2485010, 1075010));
		expectedPoints.add(new PointCh(2485020, 1075020));
		expectedPoints.add(new PointCh(2485030, 1075030));
		expectedPoints.add(new PointCh(2485040, 1075040));
		
		var toTest = new SingleRoute(edges);
		var positionInEdge = (14.14213562373095) * 2.0; //(14.14213562373095) * 1.0
		var pointInEdge = new PointCh(2485030, 1075030);
		var expected = new RoutePoint(pointInEdge, positionInEdge, 0);
		
		assertEquals(toTest.length(), (14.14213562373095) * 3.0);                                               //length works
		assertEquals(toTest.indexOfSegmentAt(500), 0);                                    //indexOfSegment works
		assertEquals(toTest.edges(), edges);                                              //edges works
		assertEquals(toTest.points(), expectedPoints);                                    //points works
		assertEquals(new PointCh(2485015, 1075015), toTest.pointAt((14.14213562373095)/2.0));                   //pointAt works
		assertEquals(-8, toTest.elevationAt(16));                                  //elevationAt works
		assertEquals(2, toTest.nodeClosestTo(15));//nodeClosestTo works
	    assertEquals(expected, toTest.pointClosestTo(new PointCh(2485030, 1075030)));     //pointClosestTo problem
		
	}

}
