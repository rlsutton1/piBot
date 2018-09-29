package au.com.rsutton.mapping.array;

public class Sparse2dSegmentBase<T> implements Segment<T>
{

	private Object[][] map;

	Sparse2dSegmentBase(int size, T defaultValue)
	{
		map = new Object[size][size];
		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				map[x][y] = defaultValue;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(int x, int y)
	{
		return (T) map[x][y];
	}

	@Override
	public void set(int x, int y, T value)
	{
		map[x][y] = value;

	}

}
