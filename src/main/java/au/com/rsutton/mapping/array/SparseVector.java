package au.com.rsutton.mapping.array;

import java.util.Map;
import java.util.TreeMap;

public class SparseVector<T>
{

	final Map<Integer, T> data = new TreeMap<>();

	final T defaultValue;

	SparseVector(T defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	T get(int i)
	{
		T value = data.get(i);
		if (value == null)
		{
			return defaultValue;
		}
		return value;
	}

	void put(int i, T value)
	{
		data.put(i, value);
	}
}
