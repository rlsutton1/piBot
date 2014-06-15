package au.com.rsutton.mapping;

import java.util.HashMap;
import java.util.Map;

public class LaserRangeConverter
{

	private static final double HALF_X_ANGLE_RANGE = 45d;
	private static final double HALF_X_RESOLUTION = 150d;
	Map<Xrange, Map<Integer, Integer>> lookup = new HashMap<>();
	private Xrange xrange26;
	private Xrange xrange94;
	private Xrange xrange169;
	private Xrange xrange249;

	public LaserRangeConverter()
	{
		xrange26 = new Xrange(15, 35);
		lookup.put(xrange26, new HashMap<Integer, Integer>());

		xrange94 = new Xrange(85, 105);
		lookup.put(xrange94, new HashMap<Integer, Integer>());

		xrange169 = new Xrange(160, 180);
		lookup.put(xrange169, new HashMap<Integer, Integer>());

		xrange249 = new Xrange(240, 260);
		lookup.put(xrange249, new HashMap<Integer, Integer>());

		addDataSet(35, 145, 150, 150, 144);
		addDataSet(38, 146, 150, 149, 144);
		addDataSet(41, 145, 150, 149, 144);
		addDataSet(44, 144, 147, 147, 142);
		addDataSet(47, 142, 146, 146, 141);
		addDataSet(50, 142, 146, 146, 141);
		addDataSet(53, 141, 145, 145, 139);
		addDataSet(56, 141, 145, 145, 140);
		addDataSet(59, 139, 142, 142, 137);
		addDataSet(62, 139, 142, 142, 136);
		addDataSet(65, 139, 142, 142, 137);
		addDataSet(68, 137, 140, 140, 135);
		addDataSet(71, 137, 140, 140, 135);
		addDataSet(74, 136, 139, 139, 135);
		addDataSet(77, 135, 138, 138, 134);
		addDataSet(80, 135, 138, 138, 134);
		addDataSet(83, 134, 137, 136, 133);
		addDataSet(86, 134, 137, 136, 133);
		addDataSet(89, 134, 136, 136, 132);
		addDataSet(92, 133, 135, 135, 132);
		addDataSet(95, 133, 136, 135, 132);
		addDataSet(98, 132, 135, 134, 131);
		addDataSet(101, 132, 135, 134, 130);
		addDataSet(104, 132, 135, 134, 130);
		addDataSet(107, 131, 134, 133, 130);
		addDataSet(110, 131, 134, 133, 130);
		addDataSet(113, 131, 133, 132, 129);
		addDataSet(116, 130, 133, 132, 129);
		addDataSet(119, 130, 133, 132, 129);
		addDataSet(122, 129, 132, 131, 128);
		addDataSet(125, 129, 131, 131, 128);
		addDataSet(128, 129, 131, 131, 128);
		addDataSet(131, 129, 131, 130, 127);
		addDataSet(134, 129, 131, 130, 127);
		addDataSet(137, 128, 130, 130, 127);
		addDataSet(140, 128, 130, 130, 126);
		addDataSet(143, 128, 130, 129, 126);
		addDataSet(146, 127, 129, 129, 126);
		addDataSet(149, 127, 129, 129, 126);
		addDataSet(152, 127, 129, 129, 126);
		addDataSet(155, 127, 129, 128, 125);
		addDataSet(158, 127, 129, 128, 125);
		addDataSet(161, 127, 129, 128, 125);
		addDataSet(164, 126, 128, 127, 125);
		addDataSet(167, 126, 128, 127, 125);
		addDataSet(170, 126, 128, 127, 124);
		addDataSet(173, 126, 128, 127, 124);
		addDataSet(176, 125, 127, 127, 124);
		addDataSet(179, 125, 127, 127, 124);
		addDataSet(182, 125, 127, 127, 124);
		addDataSet(185, 125, 127, 127, 124);
		addDataSet(188, 125, 127, 126, 124);
		addDataSet(191, 125, 127, 126, 124);
		addDataSet(194, 124, 127, 126, 123);
		addDataSet(197, 125, 127, 126, 123);

	}

	public Integer convertRange(int xa, int ya)
	{

		for (Xrange key : lookup.keySet())
		{
			if (xa > key.min && xa < key.max)
			{
				Integer distance = lookup.get(key).get(ya);
				return distance;

			}
		}
		return null;
	}

	void addDataSet(int distance, int xa26, int xa94, int xa169, int xa249)
	{

		Map<Integer, Integer> map = lookup.get(xrange26);
		map.put(xa26, distance);

		map = lookup.get(xrange94);
		map.put(xa94, distance);

		map = lookup.get(xrange169);
		map.put(xa169, distance);

		map = lookup.get(xrange249);
		map.put(xa249, distance);

	}

	class Xrange
	{
		Xrange(int min, int max)
		{
			this.min = min;
			this.max = max;
		}

		int min = 0;
		int max = 0;
	}

	public int convertAngle(double averageX)
	{
		return (int) ((averageX-HALF_X_RESOLUTION)* (HALF_X_ANGLE_RANGE/HALF_X_RESOLUTION));
	}

}
