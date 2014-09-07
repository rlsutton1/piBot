package au.com.rsutton.mapping;

public class Translator2d
{

	public static XY rotate(XY xy, double heading)
	{
		// TO		// compensate (rotate) for camera angle
		
		double sin = Math.sin(Math.toRadians(heading));
		double cos = Math.cos(Math.toRadians(heading));
		double x = (sin * xy.getY()) - (cos * xy.getX());
		double y = (cos * xy.getY()) + (sin * xy.getX());
		
		return new XY((int)x,(int)y);

	}

}
