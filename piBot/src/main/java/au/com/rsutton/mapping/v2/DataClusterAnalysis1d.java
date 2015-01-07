package au.com.rsutton.mapping.v2;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DataClusterAnalysis1d<T>
{

	private List<DataClusterRegion<T>> rawData;
	private double boundaryDistance;
	
	

	DataClusterAnalysis1d(List<DataClusterRegion<T>> rawData, double boundaryDistance)
	{
		this.rawData = rawData;
		this.boundaryDistance = boundaryDistance;
	}

	void findClusters()
	{

	}

	void findNearestNeighbours()
	{
		Double min = Double.MAX_VALUE;
		DataClusterRegion<T> n1 = null;
		DataClusterRegion<T> n2 = null;

		DataClusterRegion<T> last = null;

		for (DataClusterRegion<T> value : rawData)
		{
			if (last != null)
			{
//				if (last - value < min)
//				{
//					min = last - value;
//					n1 = last;
//					n2 = value;
//				}
			}
			last = value;
		}
	}
}
