package au.com.rsutton.mapping.array;

public interface SparseArray
{

	double getDefaultValue();

	double get(int x, int y);

	void set(int x, int y, double value);

	int getMinY();

	int getMaxY();

	int getMaxX();

	int getMinX();

}