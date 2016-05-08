package au.com.rsutton.mapping.particleFilter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TopNList<T>
{

	LinkedList<Wrapper> list = new LinkedList<>();
	private int size;

	class Wrapper implements Comparable<Wrapper>
	{
		public Wrapper(double value, T object2)
		{
			rating = value;
			object = object2;
		}

		double rating;
		T object;

		@Override
		public int compareTo(TopNList<T>.Wrapper o)
		{
			return (int) ((o.rating - rating) * 1000);
		}
	}

	TopNList(int size)
	{
		this.size = size;
	}

	/**
	 * this is a broken implementation
	 * 
	 * @param value
	 * @param object
	 */
	void add(double value, T object)
	{
		Wrapper wrapped = new Wrapper(value, object);
		if (list.size()< size)
		{
			list.add(wrapped);
			Collections.sort(list);
		}else
		if (list.size()<size || wrapped.rating > list.getFirst().rating)
		{
			list.addFirst(wrapped);
		} else if (wrapped.rating < list.getLast().rating)
		{
			return;
		} else
		{
			list.add(wrapped);
			Collections.sort(list);
		}
		if (list.size() > size)
		{
			list.remove(list.size() - 1);
		}
	}

	List<T> getTop()
	{
		List<T> result = new LinkedList<>();
		for (TopNList<T>.Wrapper v : list)
		{
			result.add(v.object);
		}
		return result;
	}
}
