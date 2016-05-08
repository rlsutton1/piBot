package au.com.rsutton.mapping.v3.linearEquasion;

import au.com.rsutton.mapping.XY;

class InterceptResult
{
	public InterceptResult(InterceptType intercept, XY xy)
	{
		type = intercept;
		location = xy;
	}

	public InterceptResult(InterceptType type)
	{
		this.type = type;
	}

	InterceptType type;
	XY location;
}
