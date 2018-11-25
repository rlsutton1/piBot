package au.com.rsutton.mapping.array;

public class Dynamic2dSparseArray<T> implements SparseArray<T>
{

	private int cellSize;

	@SuppressWarnings("unchecked")
	Sparse2dSegment<T>[][] maps = new Sparse2dSegment[2][2];

	private int minY = Integer.MAX_VALUE;

	private int maxY = Integer.MIN_VALUE;

	private int maxX = Integer.MIN_VALUE;

	private int minX = Integer.MAX_VALUE;

	private final T defaultValue;

	public Dynamic2dSparseArray(T defaultValue)
	{
		// +-(50^3) is the maximum array size
		this.cellSize = 50;
		int levels = 3;

		this.defaultValue = defaultValue;

		maps[0][0] = new Sparse2dSegment<>(cellSize, levels, defaultValue);
		maps[0][1] = new Sparse2dSegment<>(cellSize, levels, defaultValue);
		maps[1][0] = new Sparse2dSegment<>(cellSize, levels, defaultValue);
		maps[1][1] = new Sparse2dSegment<>(cellSize, levels, defaultValue);

	}

	public DenseOffsetArray<T> copyAsDenseOffsetArray()
	{
		DenseOffsetArray<T> ret = new DenseOffsetArray<>(minX, maxX, minY, maxY, defaultValue);

		for (int x = minX; x <= maxX; x++)
		{
			for (int y = minY; y <= maxY; y++)
			{
				ret.set(x, y, get(x, y));
			}
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.array.SparseArray#getDefaultValue()
	 */
	@Override
	public T getDefaultValue()
	{
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.array.SparseArray#get(int, int)
	 */
	@Override
	public T get(int x, int y)
	{
		Sparse2dSegment<T> map = maps[(int) Math.max(0, Math.signum(x))][(int) Math.max(0, Math.signum(y))];
		return map.get(Math.abs(x), Math.abs(y));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.array.SparseArray#set(int, int, double)
	 */
	@Override
	public void set(int x, int y, T value)
	{
		minX = Math.min(minX, x);
		minY = Math.min(minY, y);
		maxX = Math.max(maxX, x);
		maxY = Math.max(maxY, y);

		Sparse2dSegment<T> map = maps[(int) Math.max(0, Math.signum(x))][(int) Math.max(0, Math.signum(y))];
		map.set(Math.abs(x), Math.abs(y), value);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.array.SparseArray#getMinY()
	 */
	@Override
	public int getMinY()
	{
		return minY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.array.SparseArray#getMaxY()
	 */
	@Override
	public int getMaxY()
	{
		return maxY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.array.SparseArray#getMaxX()
	 */
	@Override
	public int getMaxX()
	{
		return maxX;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see au.com.rsutton.mapping.array.SparseArray#getMinX()
	 */
	@Override
	public int getMinX()
	{
		return minX;
	}
}
