package au.com.rsutton.depthcamera;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.hazelcast.PointCloudMessage;
import au.com.rsutton.mapping.particleFilter.RobotPoseSource;
import au.com.rsutton.navigation.feature.DistanceXY;
import au.com.rsutton.ui.DataSourceMap;
import au.com.rsutton.units.DistanceUnit;

public class PointCloudUI implements DataSourceMap, MessageListener<PointCloudMessage>
{

	AtomicReference<List<Vector3D>> vectors = new AtomicReference<>();
	private RobotPoseSource pf;

	public PointCloudUI(RobotPoseSource pf)
	{
		this.pf = pf;
		new PointCloudMessage().addMessageListener(this);
	}

	@Override
	public List<Point> getPoints()
	{
		DistanceXY pos = pf.getXyPosition();
		List<Point> points = new LinkedList<>();
		points.add(new Point((int) pos.getX().convert(DistanceUnit.CM), (int) pos.getY().convert(DistanceUnit.CM)));
		return points;
	}

	@Override
	public void drawPoint(BufferedImage image, double pointOriginX, double pointOriginY, double scale, double originalX,
			double originalY)
	{
		Graphics graphics = image.getGraphics();

		graphics.setColor(new Color(255, 255, 0));

		for (Vector3D sp : vectors.get())
		{
			Vector3D line = new Vector3D(sp.getX() * scale, sp.getY() * scale, 0);
			line = new Rotation(RotationOrder.XYZ, 0, 0, Math.toRadians(pf.getHeading())).applyTo(line);

			int x = (int) (pointOriginX + line.getX());
			int y = (int) (pointOriginY + line.getY());
			graphics.drawRect(x, y, 1, 1);
		}

	}

	@Override
	public void onMessage(Message<PointCloudMessage> message)
	{
		vectors.set(message.getMessageObject().getPoints());

	}
}
