package au.com.rsutton.mapping.multimap;

import java.util.Random;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import au.com.rsutton.mapping.KitchenMapBuilder;
import au.com.rsutton.mapping.particleFilter.InitialWorldBuilder;
import au.com.rsutton.mapping.particleFilter.ParticleFilterImpl;
import au.com.rsutton.mapping.particleFilter.Pose;
import au.com.rsutton.mapping.particleFilter.StartPosition;
import au.com.rsutton.mapping.probability.ProbabilityMap;
import au.com.rsutton.navigation.Navigator;
import au.com.rsutton.navigation.NavigatorControl;
import au.com.rsutton.navigation.router.RouteOption;
import au.com.rsutton.robot.RobotInterface;
import au.com.rsutton.robot.RobotSimulator;
import au.com.rsutton.ui.MapDrawingWindow;
import au.com.rsutton.ui.WrapperForObservedMapInMapUI;

public class WorldBuilder
{

	private MapDrawingWindow panel;

	@Test
	public void build() throws InterruptedException
	{
		try
		{
			// work on multi-map
			// RobotInterface robot = new RobotImple();
			RobotInterface robot = new RobotSimulator(KitchenMapBuilder.buildKitchenMap());
			((RobotSimulator) robot).setLocation(-150, 300, new Random().nextInt(360));
			ProbabilityMapProxy map = new ProbabilityMapProxy(new ProbabilityMap(5));

			panel = new MapDrawingWindow();

			panel.addDataSource(new WrapperForObservedMapInMapUI(map));

			new InitialWorldBuilder(map, robot);

			ParticleFilterProxy pf = new ParticleFilterProxy(
					new ParticleFilterImpl(map, 1000, 1.5, 1.5, StartPosition.ZERO, robot, null));

			NavigatorControl navigator = new Navigator(map, pf, robot);

			Vector3D lastPosition = new Vector3D(0, 0, 0);
			for (int y = -25; y > -400; y -= 50)
			{

				navigator.calculateRouteTo(0, y, null, RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);
				navigator.go();
				while (!navigator.hasReachedDestination())
				{
					Thread.sleep(1000);
				}
				Vector3D currentPosition = new Vector3D(0, y, 0);
				ProbabilityMap nextMap = new ProbabilityMap(5);
				new SegmentMapBuilder(nextMap, navigator, robot, currentPosition, pf);

				Vector3D dumpAveragePosition = pf.dumpAveragePosition();
				Pose pose = new Pose(dumpAveragePosition.getX(), dumpAveragePosition.getY(), pf.getAverageHeading());

				ParticleFilterImpl newPf = new ParticleFilterImpl(nextMap, 1000, 1.5, 1.5, StartPosition.USE_POSE,
						robot, pose);
				PositionConsolidator consolidator = new PositionConsolidator(pf, newPf, navigator, currentPosition,
						lastPosition);

				newPf.shutdown();

				MapMerger.mergeMaps(map, nextMap, consolidator.getOffset());
				lastPosition = currentPosition;

			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
