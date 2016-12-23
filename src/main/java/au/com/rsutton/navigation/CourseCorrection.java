package au.com.rsutton.navigation;

public class CourseCorrection
{
	public CourseCorrection(Double correctedRelativeHeading2, double speed2)
	{
		this.correctedRelativeHeading = correctedRelativeHeading2;
		this.speed = speed2;
	}

	Double correctedRelativeHeading;
	Double speed;

	public Double getCorrectedRelativeHeading()
	{
		return correctedRelativeHeading;
	}

	public Double getSpeed()
	{
		return speed;
	}
}
