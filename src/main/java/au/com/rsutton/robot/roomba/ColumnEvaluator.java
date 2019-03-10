package au.com.rsutton.robot.roomba;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.openni.VideoMode;
import org.openni.VideoStream;

public class ColumnEvaluator
{
	private int MAX_RANGE = 2000;
	private int BLOCK_SIZE = 5;
	private int[] column = new int[MAX_RANGE / BLOCK_SIZE];
	private float hfov;
	private float vfov;
	private double halfX;
	private double halfY;
	private double xResolution;
	private double yResolution;
	private double yAxisRotation;
	private int TO_CM = 10;

	ColumnEvaluator(VideoStream stream, int x)
	{

		for (int i = 0; i < column.length; i++)
		{
			column[i] = Integer.MIN_VALUE;
		}

		hfov = (stream.getHorizontalFieldOfView());
		vfov = stream.getVerticalFieldOfView();

		VideoMode videoMode = stream.getVideoMode();
		xResolution = new Double(videoMode.getResolutionX());
		yResolution = new Double(videoMode.getResolutionY());

		halfX = xResolution / 2.0;
		halfY = yResolution / 2.0;

		yAxisRotation = -hfov * ((x - halfX) / xResolution);

	}

	Vector3D findObjects(int heightCM)
	{
		int prior = column[0];
		for (int i = 1; i < column.length - 1; i++)
		{
			int current = column[i];
			int next = column[i + 1];
			if (current != Integer.MIN_VALUE && next != Integer.MIN_VALUE && prior != Integer.MIN_VALUE)
			{
				double expected = (prior + next) / 2.0;
				if (Math.abs(current - expected) > heightCM)
				{
					return new Rotation(RotationOrder.XYZ, 0, 0, yAxisRotation)
							.applyInverseTo(new Vector3D(0, i * BLOCK_SIZE, 0));

				}
			}
			prior = current;
		}
		return null;
	}

	void addPoint(int y, int z)
	{

		double xAxisRotation = vfov * ((y - halfY) / yResolution);

		Vector3D vector = new Rotation(RotationOrder.XYZ, xAxisRotation, yAxisRotation, 0)
				.applyTo(new Vector3D(0, 0, 1));

		// rescale unit vector to correct Z value
		double salez = z / vector.getZ();
		vector = vector.scalarMultiply(salez / 7.0);

		int height = (int) (vector.getY());
		int pos = (int) (vector.getZ()) / BLOCK_SIZE;

		if (pos >= 0 && pos <= column.length)
		{
			column[pos] = Math.max(column[pos], height);
		}

	}
}
