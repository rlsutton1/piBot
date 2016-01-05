package au.com.rsutton.robot.rover;

import java.util.concurrent.atomic.AtomicReference;

public class KalmanFilter
{

	AtomicReference<KalmanValue> value = new AtomicReference<>();

	KalmanFilter(KalmanValue initialValue)
	{
		this.value.set(initialValue);
	}

	void calculate(KalmanDataProvider dataProvider)
	{

		// KG = EstErr/(EstErr+MeasErr)
		// EST = EST(-1) + KG*(Meas-Est)
		// EstErr = (1-KG)*(EstErr)

		KalmanValue previousValue = value.get();
		KalmanValue observation = dataProvider.getObservation();
		KalmanValue calculatedValue = dataProvider.getCalculatedNewValue(previousValue);

		double previousEstimate = previousValue.getEstimate();

		double estError = calculatedValue.getError() + previousValue.getError();

		double gain = estError / (estError + observation.getError());

		double calculatedChange = calculatedValue.getEstimate() - previousEstimate;
		double observationChange = observation.getEstimate() - previousEstimate;
		double newEstimate = previousEstimate + (gain * observationChange) + ((1.0 - gain) * calculatedChange);
		System.out.println("gain " + gain);

		double newError = (1.0 - gain) * estError;
		System.out.println("error " + newError);

		value.set(new KalmanValue(newEstimate, newError));

	}

	void calculateNoPrediction(KalmanDataProvider dataProvider)
	{

		// KG = PrevErr/(PrevErr+MeasErr)
		// EST = EST(-1) + KG*(Meas-PrevEst)
		// EstErr = (1-KG)*(PrevErr)

		KalmanValue previousValue = value.get();
		KalmanValue observation = dataProvider.getObservation();

		double previousEstimate = previousValue.getEstimate();

		double estError = previousValue.getError();

		double gain = estError / (estError + observation.getError());

		double newEstimate = previousEstimate + (gain * (observation.getEstimate() - previousValue.getEstimate()));

		double newError = (1.0 - gain) * estError;

		value.set(new KalmanValue(newEstimate, newError));

	}

	KalmanValue getCurrentValue()
	{
		return value.get();
	}
}
