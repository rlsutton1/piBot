package au.com.rsutton.navigation.router.rrt;

import java.util.HashMap;
import java.util.Map;

class ATan2Cached
{

	private static Map<String, Double> cache = new HashMap<>();

	public static double atan2(double x, double y)
	{
		double ratio = (x / y) * 100;

		int iratio = (int) ratio;

		int xsign = (int) Math.signum(x);
		int ysign = (int) Math.signum(y);
		String cacheKey = "" + xsign + " " + ysign + " " + iratio;
		Double result = cache.get(cacheKey);
		if (result == null)
		{
			result = Math.atan2(x, y);
			cache.put(cacheKey, result);
		}

		return result;
	}
}
