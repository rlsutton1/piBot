package au.com.rsutton.mapping.array;

public class Sparse2dSegmentBase implements Segment
{

	private int size;
	private double[][] map;

	Sparse2dSegmentBase(int size, double defaultValue)
	{
		this.size = size;
		map = new double[size][size];
		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				map[x][y] = defaultValue;
			}
		}
	}

	@Override
	public double get(int x, int y)
	{
		return map[x][y];
	}

	@Override
	public void set(int x, int y, double value)
	{
		map[x][y] = value;

	}

}
