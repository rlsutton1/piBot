package au.com.rsutton.mapping.v2;

import java.util.LinkedList;
import java.util.List;

public class DataClusterRegion<T>
{
	double min;
	double max;
	List<T> values = new LinkedList<T>();
}