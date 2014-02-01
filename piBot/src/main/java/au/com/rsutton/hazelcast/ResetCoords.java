package au.com.rsutton.hazelcast;

import au.com.rsutton.entryPoint.units.Distance;
import au.com.rsutton.entryPoint.units.DistanceUnit;

public class ResetCoords extends MessageBase<ResetCoords>
{
	private static final long serialVersionUID = -5793926984954181842L;

	public ResetCoords()
	{
		super(HcTopic.RESET_COORDS);

	}

	public Distance getX()
	{
		// TODO Auto-generated method stub
		return new Distance(0,DistanceUnit.MM);
	}

	public Distance getY()
	{
		// TODO Auto-generated method stub
		return new Distance(0,DistanceUnit.MM);
	}

	public int getHeading()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
