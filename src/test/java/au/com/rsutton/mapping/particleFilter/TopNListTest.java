package au.com.rsutton.mapping.particleFilter;

import org.junit.Test;

public class TopNListTest
{

	@Test
	public void test()
	{
		TopNList<Long> list = new TopNList<Long>(4);
		
		list.add(2, 2L);
		list.add(1, 1L);
		list.add(3, 3L);
		list.add(0, 0L);
		list.add(4, 4L);
		list.add(-1, -1L);
		
		for (Long v:list.getTop())
		{
			System.out.println(v);
		}
	}

}
