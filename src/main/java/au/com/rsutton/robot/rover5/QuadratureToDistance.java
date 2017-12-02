package au.com.rsutton.robot.rover5;

import au.com.rsutton.config.Config;
import au.com.rsutton.units.Distance;
import au.com.rsutton.units.DistanceUnit;

public class QuadratureToDistance
{

	private double scale = (715d/222.0d)/6.02d ; // quadrature/mm
	
	public QuadratureToDistance(Config config,String configKey)
	{
		scale = config.loadSetting("Quadrature."+configKey, scale);
	}

	public Distance scale(Integer quadrature)
	{
		if (quadrature == null)
			return null;
		return new Distance(quadrature * scale, DistanceUnit.MM);
	}
}
