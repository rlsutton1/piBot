package au.com.rsutton.mapping.array;

public class Sparse2dSegment<T> implements Segment<T>
{

	private Segment<T>[][] map;
	private int level;
	private int size;
	private int divisor;
	private T defaultValue;

	@SuppressWarnings("unchecked")
	Sparse2dSegment(int size, int levels, T defaultValue)
	{
		this.level = levels;
		divisor = (int) Math.pow(size, level);
		this.size = size;
		this.defaultValue = defaultValue;
		map = new Segment[size][size];

	}

	@Override
	public T get(int x, int y)
	{
		int xi = x / divisor;
		int yi = y / divisor;

		Segment<T> location = getLocation(xi, yi);

		return location.get(x - (xi * divisor), y - (yi * divisor));
	}

	@Override
	public void set(int x, int y, T value)
	{
		int xi = x / divisor;
		int yi = y / divisor;

		Segment<T> location = getLocation(xi, yi);

		location.set(x - (xi * divisor), y - (yi * divisor), value);

	}

	private Segment<T> getLocation(int xi, int yi)
	{

		try
		{
			if (map[xi][yi] == null)
			{
				if (level > 1)
				{
					map[xi][yi] = new Sparse2dSegment<>(size, level - 1, defaultValue);
				} else
				{
					map[xi][yi] = new Sparse2dSegmentBase<>(size, defaultValue);
				}
			}
		} catch (Exception e)
		{
			System.out.println(xi + " " + yi + " " + divisor);
			throw e;
		}
		Segment<T> location = map[xi][yi];
		return location;
	}

}
