package au.com.rsutton.navigation.router;

public final class ExpansionPoint
{
	public ExpansionPoint(int x2, int y2)
	{
		x = x2;
		y = y2;
	}

	@Override
	public String toString()
	{
		return x + "," + y;
	}

	final int x;
	final int y;

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}
}