package au.com.rsutton.robot.rover;

public interface KalmanDataProvider
{

	KalmanValue getCalculatedNewValue(KalmanValue previousValue);

	KalmanValue getObservation();

}
