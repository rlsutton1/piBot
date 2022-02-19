package au.com.rsutton.mapping.probability;

import java.awt.Point;
import java.io.File;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.ui.DataSourcePoint;

public interface ProbabilityMapIIFc extends DataSourcePoint
{

	double[][] createGausian(int radius, double sigma, double centerValue);

	void resetPoint(int x, int y);

	/**
	 * 
	 * @param x
	 * @param y
	 * @param occupied
	 * @param certainty
	 *            - Indicates how strongly the location should be updated to the
	 *            new state <br>
	 *            ie. location = (location * certainty) + ((location
	 *            *(1.0-certainty))
	 * @param gausianRadius
	 */
	void updatePoint(int x, int y, Occupancy occupied, double certainty, int gausianRadius);

	List<Vector3D> getFeatures();

	void drawLine(double x1, double y1, double x2, double y2, Occupancy occupancy, double certainty, int radius);

	void dumpWorld();

	void dumpTextWorld();

	int getMaxX();

	int getMinX();

	int getMaxY();

	int getMinY();

	double get(double x, double y);

	double getSubPixelValue(double x, double y);

	int getBlockSize();

	@Override
	List<Point> getOccupiedPoints();

	void erase();

	void convertToDenseOffsetArray();

	void save(File file);

	void setValue(double x, double y, double value);

}