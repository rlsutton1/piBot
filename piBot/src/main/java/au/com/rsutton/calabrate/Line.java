package au.com.rsutton.calabrate;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public final class Line implements Comparable<Line>
{
	Double r;
	List<Vector3D> points = new LinkedList<>();
	List<Vector3D> rawPoints;

	public Line( List<Vector3D> rawPoints)
	{
		this.rawPoints = rawPoints;
		
		
		
		SimpleRegression regression = new SimpleRegression();

		for (Vector3D point : rawPoints)
		{
			regression.addData(point.getX(), point.getY());
		}
		r = regression.getR();

		Vector3D e1 = rawPoints.get(0);
		Vector3D p1 = new Vector3D(e1.getX(), regression.predict(e1.getX()), 0);
		Vector3D e2 = rawPoints.get(rawPoints.size() - 1);
		Vector3D p2 = new Vector3D(e2.getX(), regression.predict(e2.getX()), 0);
		points.add(p1);
		points.add(p2);
		
		
		
	}

	public List<Vector3D> getRawPoints()
	{
		return rawPoints;
	}

	public List<Vector3D> getPoints()
	{
		return points;
	}

	public Double getR()
	{
		return r;
	}

	@Override
	public int compareTo(Line arg0)
	{
		return r.compareTo(arg0.r);
	}
}