package au.com.rsutton.entryPoint.quadrature;

import java.io.IOException;

public interface QuadratureProvider
{
	public long getValue() throws IOException;
}
