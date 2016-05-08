package au.com.rsutton.mapping.particleFilter;

import java.util.LinkedList;
import java.util.List;

public class TopNList<T>
{

	List<T> list = new LinkedList<>();
	double best = Double.MIN_VALUE;
	private int size;

	TopNList(int size)
	{
		this.size = size;
	}

	/**
	 * this is a broken implementation
	 * @param value
	 * @param object
	 */
	void add(double value, T object)
	{
		if (value > best)
		{
			value = best;
			list.add(object);
			if (list.size() > size)
			{
				list.remove(list.size() - 1);
			}
		}
	}

	List<T> getTop()
	{
		return list;
	}
}
