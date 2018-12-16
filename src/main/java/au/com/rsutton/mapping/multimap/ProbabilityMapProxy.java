package au.com.rsutton.mapping.multimap;

import java.awt.Point;
import java.io.File;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.mapping.probability.Occupancy;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.mapping.probability.ProbabilityMapIIFc;

public class ProbabilityMapProxy implements ProbabilityMapIIFc
{

	ProbabilityMap map;

	public ProbabilityMapProxy(ProbabilityMap map)
	{
		this.map = map;
	}

	void changeMap(ProbabilityMap map)
	{
		this.map = map;
	}

	@Override
	public double[][] createGausian(int radius, double sigma, double centerValue)
	{
		return map.createGausian(radius, sigma, centerValue);
	}

	@Override
	public void resetPoint(int x, int y)
	{
		map.resetPoint(x, y);
	}

	@Override
	public void updatePoint(int x, int y, Occupancy occupied, double certainty, int gausianRadius)
	{
		map.updatePoint(x, y, occupied, certainty, gausianRadius);
	}

	@Override
	public List<Vector3D> getFeatures()
	{
		return map.getFeatures();
	}

	@Override
	public void drawLine(double x1, double y1, double x2, double y2, Occupancy occupancy, double certainty, int radius)
	{
		map.drawLine(x1, y1, x2, y2, occupancy, certainty, radius);
	}

	@Override
	public void dumpWorld()
	{
		map.dumpWorld();
	}

	@Override
	public void dumpTextWorld()
	{
		map.dumpTextWorld();
	}

	@Override
	public boolean equals(Object obj)
	{
		return map.equals(obj);
	}

	@Override
	public int getMaxX()
	{
		return map.getMaxX();
	}

	@Override
	public int getMinX()
	{
		return map.getMinX();
	}

	@Override
	public int getMaxY()
	{
		return map.getMaxY();
	}

	@Override
	public int getMinY()
	{
		return map.getMinY();
	}

	@Override
	public double get(double x, double y)
	{
		return map.get(x, y);
	}

	@Override
	public int getBlockSize()
	{
		return map.getBlockSize();
	}

	@Override
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

	@Override
	public void erase()
	{
		map.erase();

	}

	@Override
	public void convertToDenseOffsetArray()
	{
		map.convertToDenseOffsetArray();
	}

	@Override
	public void save(File file)
	{
		map.save(file);

	}
}
