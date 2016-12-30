package au.com.rsutton.navigation.router;

import java.util.LinkedList;
import java.util.List;

public class PrioritizedQueueGroup<T>
{

	private List<List<T>> queueGroup = new LinkedList<>();
	private int size;

	PrioritizedQueueGroup(int queues)
	{
		size = queues;
		for (int i = 0; i < queues; i++)
		{
			queueGroup.add(new LinkedList<>());
		}
	}

	void add(int group, T item)
	{
		queueGroup.get(size - group).add(item);
	}

	T take()
	{
		for (List<T> list : queueGroup)
		{
			if (!list.isEmpty())
			{
				return list.remove(0);
			}
		}
		return null;
	}

	public boolean isEmpty()
	{
		boolean empty = true;
		for (List<T> list : queueGroup)
		{
			empty = list.isEmpty();
			if (!empty)
			{
				break;
			}
		}
		return empty;
	}

	public void addAll(PrioritizedQueueGroup<T> expandPoints)
	{
		int i = 0;
		for (List<T> list : expandPoints.queueGroup)
		{
			queueGroup.get(i).addAll(list);
			i++;

		}

	}
}
