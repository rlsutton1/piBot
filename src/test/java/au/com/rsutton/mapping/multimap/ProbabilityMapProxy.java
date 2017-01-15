package au.com.rsutton.mapping.multimap;

import java.awt.Point;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class ProbabilityMapProxy implements ProbabilityMapIIFc
{

	ProbabilityMap map;

	ProbabilityMapProxy(ProbabilityMap map)
	{
		this.map = map;
	}

	void changeMap(ProbabilityMap map)
	{
		this.map = map;
	}

	public double[][] createGausian(int radius, double sigma, double centerValue)
	{
		return map.createGausian(radius, sigma, centerValue);
	}

	public void resetPoint(int x, int y)
	{
		map.resetPoint(x, y);
	}

	public void updatePoint(int x, int y, Occupancy occupied, double certainty, int gausianRadius)
	{
		map.updatePoint(x, y, occupied, certainty, gausianRadius);
	}

	public List<Vector3D> getFeatures()
	{
		return map.getFeatures();
	}

	public void drawLine(double x1, double y1, double x2, double y2, Occupancy occupancy, double certainty, int radius)
	{
		map.drawLine(x1, y1, x2, y2, occupancy, certainty, radius);
	}

	public double[][] createShiftMatrix(double xShiftAmount, double yShiftAmount)
	{
		return map.createShiftMatrix(xShiftAmount, yShiftAmount);
	}

	public void dumpWorld()
	{
		map.dumpWorld();
	}

	public void dumpTextWorld()
	{
		map.dumpTextWorld();
	}

	@Override
	public boolean equals(Object obj)
	{
		return map.equals(obj);
	}

	public int getMaxX()
	{
		return map.getMaxX();
	}

	public int getMinX()
	{
		return map.getMinX();
	}

	public int getMaxY()
	{
		return map.getMaxY();
	}

	public int getMinY()
	{
		return map.getMinY();
	}

	public double get(double x, double y)
	{
		return map.get(x, y);
	}

	public int getBlockSize()
	{
		return map.getBlockSize();
	}

	public List<Point> getOccupiedPoints()
	{
		return map.getOccupiedPoints();
	}

	@Override
	public int hashCode()
	{
		return map.hashCode();
	}

	@Override
	public String toString()
	{
		return map.toString();
	}
}
