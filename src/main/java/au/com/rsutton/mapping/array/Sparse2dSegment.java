package au.com.rsutton.mapping.array;

public class Sparse2dSegment implements Segment
{

	private Segment[][] map;
	private int level;
	private int size;
	private int divisor;
	private double defaultValue;

	Sparse2dSegment(int size, int levels, double defaultValue)
	{
		this.level = levels;
		divisor = (int) Math.pow(size, level);
		this.size = size;
		this.defaultValue = defaultValue;
		map = new Segment[size][size];

	}

	public double get(int x, int y)
	{
		int xi = x / divisor;
		int yi = y / divisor;

		Segment location = getLocation(xi, yi);

		return location.get(x - (xi * divisor), y - (yi * divisor));
	}

	public void set(int x, int y, double value)
	{
		int xi = x / divisor;
		int yi = y / divisor;

		Segment location = getLocation(xi, yi);

		location.set(x - (xi * divisor), y - (yi * divisor), value);

	}

	private Segment getLocation(int xi, int yi)
	{

		if (map[xi][yi] == null)
		{
			if (level > 1)
			{
				map[xi][yi] = new Sparse2dSegment(size, level - 1,defaultValue);
			} else
			{
				map[xi][yi] = new Sparse2dSegmentBase(size,defaultValue);
			}
		}

		Segment location = map[xi][yi];
		return location;
	}

}
