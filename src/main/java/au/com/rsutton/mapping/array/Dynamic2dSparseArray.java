package au.com.rsutton.mapping.array;

public class Dynamic2dSparseArray
{

	private int cellSize;

	Sparse2dSegment[][] maps = new Sparse2dSegment[2][2];

	private int minY = Integer.MAX_VALUE;

	private int maxY = Integer.MIN_VALUE;

	private int maxX = Integer.MIN_VALUE;

	private int minX = Integer.MAX_VALUE;

	public Dynamic2dSparseArray(double defaultValue)
	{
		// +-(50^3) is the maximum array size
		this.cellSize = 50;
		int levels = 3;
		

		maps[0][0] = new Sparse2dSegment(cellSize,levels,defaultValue);
		maps[0][1] = new Sparse2dSegment(cellSize,levels,defaultValue);
		maps[1][0] = new Sparse2dSegment(cellSize,levels,defaultValue);
		maps[1][1] = new Sparse2dSegment(cellSize,levels,defaultValue);


	}
	
	public double get(int x, int y)
	{
		Sparse2dSegment map = maps[(int) Math.max(0,Math.signum(x))][(int) Math.max(0, Math.signum(y))];
		return map.get(Math.abs(x),Math.abs(y));
	}

	public void set(int x, int y, double value)
	{
		minX = Math.min(minX, x);
		minY = Math.min(minY,y);
		maxX = Math.max(maxX,x);
		maxY = Math.max(maxY, y);
		
		Sparse2dSegment map = maps[(int) Math.max(0,Math.signum(x))][(int) Math.max(0, Math.signum(y))];
		 map.set(Math.abs(x),Math.abs(y),value);
		
	}

	public int getMinY()
	{
		return minY;
	}

	public int getMaxY()
	{
		return maxY;
	}

	public int getMaxX()
	{
		return maxX;
	}

	public int getMinX()
	{
		return minX;
	}
}
