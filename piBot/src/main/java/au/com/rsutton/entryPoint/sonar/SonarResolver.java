package au.com.rsutton.entryPoint.sonar;

import au.com.rsutton.entryPoint.trig.Point;
import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

public class SonarResolver
{

	class PointData
	{

		private Distance x;
		private Distance y;
		private Distance distance;
		private Double angle;

		public PointData(Distance distanceX, Distance distanceY,
				Distance distance, double angle)
		{
			this.x = distanceX;
			this.y = distanceY;
			this.distance = distance;
			this.angle = angle;
		}

		public Distance getY()
		{

			return y;
		}

		public Distance getX()
		{
			return x;
		}
		
		public Point getPoint()
		{
			return new Point(x,y);
		}

	}

	PointData createPointData(double angle, Distance distance)
	{

		double radians = (angle / 360d) * (2d * Math.PI);

		// System.out.println("Radians "+radians);

		double x = Math.sin(radians) * distance.convert(DistanceUnit.MM);
		double y = Math.cos(radians) * distance.convert(DistanceUnit.MM);
		return new PointData(new Distance(x, DistanceUnit.MM), new Distance(y,
				DistanceUnit.MM), distance, angle);
	}
}
