package au.com.rsutton.robot.rover;

import java.util.List;

public interface KalmanMultiDataProvider
{

	/**
	 * observations must include any error inherited from the previous
	 * observation
	 * 
	 * @return
	 */
	List<KalmanValue> getObservations(KalmanValue previousObservation);

}
