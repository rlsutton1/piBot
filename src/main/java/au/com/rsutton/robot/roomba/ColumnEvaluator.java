package au.com.rsutton.robot.roomba;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.openni.VideoStream;

public class ColumnEvaluator
{
	private int MAX_RANGE = 2000;
	private int BLOCK_SIZE = 5;
	private PointData[] column = new PointData[MAX_RANGE / BLOCK_SIZE];
	private float vfov;
	private double xResolution;
	private double yResolution;
	private double zAxisRotation;

	class PointData
	{

		int hitCount = 0;
		double totalZ = 0;
		Vector3D max;

		double getAverageZ()
		{
			return totalZ / hitCount;
		}

		PointData(Vector3D data)
		{
			totalZ = data.getZ();
			hitCount = 1;
			max = data;
		}

		void addHit(Vector3D data)
		{
			totalZ += data.getZ();
			hitCount++;
			if (data.getZ() > max.getZ())
			{
				max = data;
			}
		}

	}

	ColumnEvaluator(VideoStream stream, int imageXCoord)
	{
		this(stream.getHorizontalFieldOfView(), stream.getVerticalFieldOfView(), stream.getVideoMode().getResolutionX(),
				stream.getVideoMode().getResolutionY(), imageXCoord);

	}

	ColumnEvaluator(float hfov, float vfov, double xres, double yres, int imageXCoord)
	{
		for (int i = 0; i < column.length; i++)
		{
			column[i] = null;
		}

		this.vfov = vfov;

		xResolution = xres;
		yResolution = yres;

		// expressed as -0.5< xPosition < 0.5
		double xPosition = (imageXCoord / xResolution) - 0.5;
		zAxisRotation = hfov * xPosition;
	}

	Vector3D findObjects(int minimumHeightCM)
	{
		// find lowest points in first third and last third

		// calculate function.

		// find points not adhearing to function.

		PointData prior = column[0];
		for (int i = 1; i < column.length - 1; i++)
		{
			PointData current = column[i];
			PointData next = column[i + 1];
			if (current != null && next != null && prior != null)
			{
				// expectedHeight is the average of the previous and next points
				// in this column
				double expectedHeight = (prior.getAverageZ() + current.getAverageZ()) / 2.0;
				if (next.getAverageZ() - expectedHeight > minimumHeightCM)
				{
					return next.max;

				}
			}
			prior = current;
		}
		return null;
	}

	void addPoint(int imageYCoord, int distanceMM)
	{

		// expressed as -0.5< yPosition < 0.5
		double yPosition = (imageYCoord / yResolution) - 0.5;
		double xAxisRotation = -vfov * yPosition;

		double distanceCM = distanceMM / 6.0;
		Vector3D vector = new Rotation(RotationOrder.XYZ, xAxisRotation, 0, zAxisRotation)
				.applyTo(new Vector3D(0, distanceCM, 0));

		int pos = (int) (vector.getY()) / BLOCK_SIZE;

		if (pos >= 0 && pos <= column.length)
		{
			if (column[pos] == null)
			{
				column[pos] = new PointData(vector);
			} else

			{
				column[pos].addHit(vector);
			}
		}

	}
}
