package au.com.rsutton.mapping.array;

interface Segment<T>
{

	T get(int x, int y);

	void set(int x, int y, T value);
}
