package au.com.rsutton.cv;

import java.io.Serializable;
import java.util.Collection;

import com.pi4j.gpio.extension.pixy.Coordinate;

public class CameraRangeData implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3069442294529076573L;
	private RangeFinderConfiguration rangeFinderConfig;
	private Collection<Coordinate> rangeData;

	public CameraRangeData(RangeFinderConfiguration rangeFinderConfig,
			Collection<Coordinate> rangeData)
	{
		this.rangeFinderConfig = rangeFinderConfig;
		this.rangeData = rangeData;
	}

	public RangeFinderConfiguration getRangeFinderConfig()
	{
		return rangeFinderConfig;
	}

	public Collection<Coordinate> getRangeData()
	{
		return rangeData;
	}

}
