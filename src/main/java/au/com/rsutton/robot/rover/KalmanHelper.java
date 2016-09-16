package au.com.rsutton.robot.rover;

public class KalmanHelper
{

	private double lastDeadReconning=0;
	private double lastKalmanValue=0;

	public void setCurrentKalmanAndDeadReconningValue(double kalman,double dead)
	{
		lastKalmanValue = kalman;
		lastDeadReconning = dead;
	}

//	public double getDeadReconningSinceLastKalmanValue(double newDeadReconning)
//	{
//		double change = getChangeInDeadReconning(newDeadReconning);
//
//		lastDeadReconning = newDeadReconning;
//
//		return lastKalmanValue + change;
//	}

	public double getValueBasedOnChangedDeadReconningValue(double currentDeadReconning)
	{
		return lastKalmanValue+(currentDeadReconning - lastDeadReconning);
	}

//	/**
//	 * returns a value such that it can be added to kalmanValue to equal
//	 * particleFilterValue
//	 * 
//	 * @param kalmanValue
//	 * @param particleFilterValue
//	 * @return
//	 */
//	double getDelta(double kalmanValue, double particleFilterValue)
//	{
//		return particleFilterValue - kalmanValue;
//	}
}
