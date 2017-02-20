package au.com.rsutton.mapping.multimap;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import au.com.rsutton.entryPoint.controllers.HeadingHelper;
import au.com.rsutton.mapping.multimap.Averager.Sample;
import au.com.rsutton.mapping.particleFilter.ParticleFilterIfc;
import au.com.rsutton.navigation.NavigatorControl;
import au.com.rsutton.navigation.router.RouteOption;

public class PositionConsolidator
{
	Averager averager = new Averager();
	private ParticleFilterIfc mainPf;
	private ParticleFilterIfc newPf;

	PositionConsolidator(ParticleFilterIfc mainPf, ParticleFilterIfc newPf, NavigatorControl navigator, Vector3D p1,
			Vector3D p2) throws InterruptedException
	{
		this.mainPf = mainPf;
		this.newPf = newPf;

		for (int i = 0; i < 4; i++)
		{
			navigateTo(i, navigator, p1);

			navigateTo(i, navigator, p2);
			if (averager.hasEnoughData())
			{
				break;
			}
		}

		newPf.shutdown();
	}

	public Sample getOffset()
	{
		return averager.getRefinedValue();
	}

	private void navigateTo(int iteration, NavigatorControl navigator, Vector3D p1) throws InterruptedException
	{
		navigator.calculateRouteTo((int) p1.getX(), (int) p1.getY(), null, RouteOption.ROUTE_THROUGH_CLEAR_SPACE_ONLY);

		navigator.go();

		while (!navigator.hasReachedDestination())
		{
			TimeUnit.MILLISECONDS.sleep(200);
			Vector3D mp1 = mainPf.dumpAveragePosition();
			Vector3D mp2 = newPf.dumpAveragePosition();
			double h1 = mainPf.getAverageHeading();
			double h2 = newPf.getAverageHeading();
			double e1 = mainPf.getStdDev();
			double e2 = newPf.getStdDev();
			if (iteration > 0)
			{
				averager.addSample(mp1.getX() - mp2.getX(), mp1.getY() - mp2.getY(),
						HeadingHelper.getChangeInHeading(h1, h2), e1 + e2);
				if (averager.hasEnoughData())
				{
					break;
				}

			}
		}
	}
}
