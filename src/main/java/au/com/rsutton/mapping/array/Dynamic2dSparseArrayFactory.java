package au.com.rsutton.mapping.array;

public class Dynamic2dSparseArrayFactory
{

	public static SparseArray<Double> getDynamic2dSparseArray(Double defaultValue)
	{
		return new Dynamic2dSparseArray<>(defaultValue);
	}
}
