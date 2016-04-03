package au.com.rsutton.mapping.particleFilter;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import au.com.rsutton.entryPoint.units.DistanceUnit;
import au.com.rsutton.hazelcast.RobotLocation;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.robot.rover.Angle;
import au.com.rsutton.robot.rover.LidarObservation;

public class ParticleFilterLiveTest
{

	@Test
	public void test() throws InterruptedException
	{
		final ProbabilityMap world = KitchenMapBuilder.buildKitchenMap();

		final ParticleFilter pf = new ParticleFilter(world,1000);
		pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());

		new RobotLocation().addMessageListener(new MessageListener<RobotLocation>()
		{

			Double lastx = null;
			Double lasty = null;
			Angle lastheading;

			@Override
			public void onMessage(Message<RobotLocation> message)
			{

				final RobotLocation robotLocation = message.getMessageObject();
				pf.addObservation(world, robotLocation);
				if (lastx != null)
				{
					pf.moveParticles(new ParticleUpdate()
					{

						@Override
						public double getDeltaHeading()
						{
							return lastheading.difference(robotLocation.getDeadReaconingHeading());
						}

						@Override
						public double getMoveDistance()
						{
							double dx = (lastx - robotLocation.getX().convert(DistanceUnit.CM));
							double dy = (lasty - robotLocation.getY().convert(DistanceUnit.CM));
							return Vector3D.distance(Vector3D.ZERO, new Vector3D(dx, dy, 0));
						}
					});
				}
				lasty = robotLocation.getY().convert(DistanceUnit.CM);
				lastx = robotLocation.getX().convert(DistanceUnit.CM);
				lastheading = robotLocation.getDeadReaconingHeading();
				pf.resample(world);
				pf.dumpTextWorld(KitchenMapBuilder.buildKitchenMap());

			}
		});
		;

		Thread.sleep(60 * 5 * 1000);
	}

	
}
