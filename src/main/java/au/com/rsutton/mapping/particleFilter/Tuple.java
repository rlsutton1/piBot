package au.com.rsutton.mapping.particleFilter;

public class Tuple<S1, S2>
{

	private S1 v1;
	private S2 v2;

	Tuple(S1 v1, S2 v2)
	{
		this.v1 = v1;
		this.v2 = v2;
	}

	public S1 getV1()
	{
		return v1;
	}

	public S2 getV2()
	{
		return v2;
	}
}
