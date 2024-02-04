package ch.epfl.javelo.mytest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.javelo.routing.*;
import org.junit.jupiter.api.Test;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;



public class MultiRouteTest {
	@Test
	void classTest() {
		List<Edge> edges = new ArrayList<>();
		List<Edge> edges1 = new ArrayList<>();
		List<Edge> edges2 = new ArrayList<>();
		List<Edge> edges3 = new ArrayList<>();
		List<Route> singleSegments = new ArrayList<>();
		List<Route> multiSegments = new ArrayList<>();
		List<Route> mixedSegments = new ArrayList<>();
		
		edges.add(new Edge(1, 2, new PointCh(2485010, 1075010), new PointCh(2485020, 1075020), 14.14213562373095, Functions.constant(10)));
		edges.add(new Edge(2, 3, new PointCh(2485020, 1075020), new PointCh(2485030, 1075030), 14.14213562373095, Functions.constant(-8)));
		edges.add(new Edge(3, 4, new PointCh(2485030, 1075030), new PointCh(2485040, 1075040), 14.14213562373095, Functions.constant(0)));
		var segment1 = new SingleRoute(edges);
		singleSegments.add(segment1);
		edges1.add(new Edge(4, 5, new PointCh(2485040, 1075040), new PointCh(2485050, 1075050), 14.14213562373095, Functions.constant(Double.NaN)));
		edges1.add(new Edge(5, 6, new PointCh(2485050, 1075050), new PointCh(2485060, 1075060), 14.14213562373095, Functions.constant(3)));
		edges1.add(new Edge(6, 7, new PointCh(2485060, 1075060), new PointCh(2485070, 1075070), 14.14213562373095, Functions.constant(7)));
		var segment2 = new SingleRoute(edges1);
		multiSegments.add(segment2);
//		singleSegments.add(segment2);
		
		edges2.add(new Edge(7, 8, new PointCh(2485070, 1075070), new PointCh(2485080, 1075080), 14.14213562373095, Functions.constant(1)));
		edges2.add(new Edge(8, 9, new PointCh(2485080, 1075080), new PointCh(2485090, 1075090), 14.14213562373095, Functions.constant(-1)));
		edges2.add(new Edge(9, 10, new PointCh(2485090, 1075090), new PointCh(2485100, 1075100), 14.14213562373095, Functions.constant(9)));
		var segment3 = new SingleRoute(edges2);
		multiSegments.add(segment3);
//		singleSegments.add(segment3);

		var toTest1 = new MultiRoute(singleSegments);
		var toTest2 = new MultiRoute(multiSegments);
		mixedSegments.add(toTest1);
		mixedSegments.add(toTest2);
		var singleToTest = new MultiRoute(singleSegments);
		var toTest = new MultiRoute(mixedSegments);
		
		edges3.addAll(edges);
		edges3.addAll(edges1);
		edges3.addAll(edges2);
		
		List<PointCh> expectedPoints = new ArrayList<>();
		expectedPoints.add(new PointCh(2485010, 1075010));
		expectedPoints.add(new PointCh(2485020, 1075020));
		expectedPoints.add(new PointCh(2485030, 1075030));
		expectedPoints.add(new PointCh(2485040, 1075040));
		expectedPoints.add(new PointCh(2485050, 1075050));
		expectedPoints.add(new PointCh(2485060, 1075060));
		expectedPoints.add(new PointCh(2485070, 1075070));
		expectedPoints.add(new PointCh(2485080, 1075080));
		expectedPoints.add(new PointCh(2485090, 1075090));
		expectedPoints.add(new PointCh(2485100, 1075100));
		
		
		var positionInEdge = (14.14213562373095) * 9.0;
		var pointInEdge = new PointCh(2485100, 1075100);
		var expected = new RoutePoint(pointInEdge, positionInEdge, 0);
		
		
		
//		assertEquals(toTest.length(), (14.14213562373095) * 9.0, 10E-14);       					       	       //length works
//		assertEquals(2, toTest.indexOfSegmentAt(14.14213562373095*9));					  						  //index works
//		assertEquals(toTest.edges(), edges3);                                              						 //edges works
//		assertEquals( expectedPoints, toTest.points());                                 			 			//points works	
//		assertEquals(new PointCh(2485075, 1075075), singleToTest.pointAt(14.14213562373095 * 6.5));    		   //pointAt works	
//    	assertEquals(9, toTest.elevationAt(14.14213562373095 * 8));                            	       		  //elevationAt works  	
//		assertEquals(4, toTest.nodeClosestTo(14.14213562373095 * 3));										 //nodeClosestTo works
//	    assertEquals(expected, toTest.pointClosestTo(pointInEdge));    								  	    //pointClosestTo works
		
	}
	
	
	
	


}
