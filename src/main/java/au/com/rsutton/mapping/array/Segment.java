package au.com.rsutton.mapping.array;

public interface Segment<T>
{

	T get(int x, int y);

	void set(int x, int y, T value);
}
