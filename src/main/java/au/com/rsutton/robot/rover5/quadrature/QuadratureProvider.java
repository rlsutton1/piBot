package au.com.rsutton.robot.rover5.quadrature;

import java.io.IOException;

public interface QuadratureProvider
{
	public long getValue() throws IOException;
}
