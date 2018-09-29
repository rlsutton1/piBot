package au.com.rsutton.mapping.array;

public interface SparseArray<T>
{

	T getDefaultValue();

	T get(int x, int y);

	void set(int x, int y, T value);

	int getMinY();

	int getMaxY();

	int getMaxX();

	int getMinX();

}