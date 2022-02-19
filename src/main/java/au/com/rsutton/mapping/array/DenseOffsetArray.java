package au.com.rsutton.mapping.array;

class DenseOffsetArray<T> implements SparseArray<T>
{

	private int minX;
	private int maxX;
	private int minY;
	private int maxY;

	private Object[][] map;
	private T defaultValue;

	DenseOffsetArray(int minX, int maxX, int minY, int maxY, T defaultValue)
	{
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.defaultValue = defaultValue;

		map = new Object[(maxX - minX) + 2][(maxY - minY) + 2];

	}

	@Override
	public T getDefaultValue()
	{
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(int x, int y)
	{
		if (x > maxX || x < minX || y > maxY || y < minY)
		{
			return defaultValue;
		}
		return (T) map[x - minX][y - minY];
	}

	@Override
	public void set(int x, int y, T value)
	{
		if (x > maxX || x < minX || y > maxY || y < minY)
		{
			return;
		}
		map[x - minX][y - minY] = value;

	}

	@Override
	public int getMinY()
	{
		return minY;
	}

	@Override
	public int getMaxY()
	{
		return maxY;
	}

	@Override
	public int getMaxX()
	{
		return maxX;
	}

	@Override
	public int getMinX()
	{
		return minX;
	}

}
