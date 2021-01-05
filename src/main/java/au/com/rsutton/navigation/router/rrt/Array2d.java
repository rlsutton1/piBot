package au.com.rsutton.navigation.router.rrt;

public class Array2d<T>
{

	private Object[][] backingArray;
	int xd;
	int yd;
	private T defaultValue;

	Array2d(int x, int y, T defaultValue)
	{
		backingArray = new Object[x][y];
		xd = x;
		yd = y;
		this.defaultValue = defaultValue;
	}

	public Array2d(T[][] value)
	{
		xd = value.length;
		yd = value[0].length;
		backingArray = new Object[xd][yd];

		for (int x = 0; x < xd; x++)
		{
			for (int y = 0; y < yd; y++)
			{
				set(x, y, value[x][y]);
			}
		}
	}

	public Array2d(int x, int y, T[][] value, T defaultValue)
	{
		xd = x;
		yd = y;
		this.defaultValue = defaultValue;
		int xm = value.length;
		int ym = value[0].length;
		backingArray = new Object[xd][yd];

		for (int xt = 0; xt < xm; xt++)
		{
			for (int yt = 0; yt < ym; yt++)
			{
				set(xt, yt, value[xt][yt]);
			}
		}
	}

	void set(int x, int y, T value)
	{
		backingArray[x][y] = value;
	}

	@SuppressWarnings("unchecked")
	T get(int x, int y)
	{
		T tmp = (T) backingArray[x][y];
		if (tmp == null)
		{
			return defaultValue;
		}
		return tmp;
	}

	int getMaxX()
	{
		return xd;
	}

	int getMaxY()
	{
		return yd;
	}
}
