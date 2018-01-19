package au.com.rsutton.mapping.array;

public class Dynamic2dSparseArrayV2 implements SparseArray
{

	final SparseVector<SparseVector<Double>> data;

	private int minY = Integer.MAX_VALUE;

	private int maxY = Integer.MIN_VALUE;

	private int maxX = Integer.MIN_VALUE;

	private int minX = Integer.MAX_VALUE;

	private final double defaultValue;

	public Dynamic2dSparseArrayV2(Double defaultValue)
	{
		data = new SparseVector<>(null);

		this.defaultValue = defaultValue;

	}

	@Override
	public double getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public double get(int x, int y)
	{
		SparseVector<Double> vector = data.get(x);
		if (vector != null)
		{
			return vector.get(y);
		}

		return defaultValue;
	}

	@Override
	public void set(int x, int y, double value)
	{
		minX = Math.min(minX, x);
		minY = Math.min(minY, y);
		maxX = Math.max(maxX, x);
		maxY = Math.max(maxY, y);

		SparseVector<Double> vector = data.get(x);
		if (vector == null)
		{
			vector = new SparseVector<>(defaultValue);
			data.put(x, vector);
		}

		vector.put(y, value);

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
