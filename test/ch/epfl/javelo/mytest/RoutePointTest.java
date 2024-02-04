package ch.epfl.javelo.mytest;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;
import ch.epfl.javelo.routing.RoutePoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoutePointTest {
	@Test
	void RoutePointTestWithPositionShiftedBy() {
		// List of cases
		// 1. positive difference
		// 2. negative difference

		// 1.
		var posDif1 = 200.0;
		var point = new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N + 10);
		var position = 1000.0;
		var originalRoutePoint = new RoutePoint(point, position, posDif1);
		var expected1 = new RoutePoint(point, position + 200, posDif1);
		var got1 = originalRoutePoint.withPositionShiftedBy(posDif1);
		assertEquals(expected1, got1);
		assertEquals(1200, got1.position());

		// 2.
		var posDif2 = -200.0;
		var originalRoutePoint2 = new RoutePoint(point, position, posDif2);
		var expected2 = new RoutePoint(point, position - 200.0, posDif2);
		var got2 = originalRoutePoint2.withPositionShiftedBy(posDif2);
		assertEquals(expected2, got2);
		assertEquals(800, got2.position());
	}

	@Test
	void RoutePointTestMin() {
		// List of cases
		// 1. Dist to reference of this is smaller than dist to ref. of that.
		// 2. Dist to reference of this is greater than dist to ref. of that.

		// 1.
		var point = new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N + 10);
		var position = 3000.0;
		var distRef = 300.0;
		var routePThis = new RoutePoint(point, position, distRef);
		var routePThat = new RoutePoint(point, position, distRef + 2.0);
		assertEquals(routePThis, routePThis.min(routePThat));

		// 2.
		var routePThat2 = new RoutePoint(point, position, 299.0);
		assertEquals(routePThat2, routePThis.min(routePThat2));

	}

	@Test
	void RoutePointTestMinOtherArguments() {
		// 1. dist to ref. are equal
		var point = new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N + 10);
		var position = 3000.0;
		var distRef = 300.0;
		var routePThis = new RoutePoint(point, position, distRef);
		assertEquals(routePThis, routePThis.min(point, 5000, distRef));
	}

}