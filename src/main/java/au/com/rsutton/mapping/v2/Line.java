package au.com.rsutton.mapping.v2;

import java.util.LinkedList;
import java.util.List;

import au.com.rsutton.mapping.XY;

public class Line
{

	private XY xy2;
	private XY xy;
	private double variance;
	private List<XY> points;

	public Line(XY xy, XY xy2, double variance, List<XY> points)
	{
		this.xy = xy;
		this.xy2 = xy2;
		this.variance = variance;
		this.points = new LinkedList<>(points);
	}

	public XY getStart()
	{
		return xy;
	}

	public XY getEnd()
	{
		return xy2;
	}

	double getVariance()
	{
		return variance;
	}
	
	public List<XY> getPoints()
	{
		return points;
	}

	@Override
	public String toString()
	{
		return xy.toString() + " " + xy2.toString() + " V:" + variance + " A:"
				+ getAngle();
	}

	double getAngle()
	{
		double x = xy.getX()-xy2.getX();
		double y=  xy.getY()-xy2.getY();
		return Math.toDegrees(Math.atan2(-x,y));
	}

	public void setStart(XY xy3)
	{
		xy =xy3;
		
	}

	public void setEnd(XY xy3)
	{
		xy2 = xy3;
		// TODO Auto-generated method stub
		
	}

	
}
