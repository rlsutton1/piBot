package au.com.rsutton.deeplearning.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.datavec.api.writable.Writable;

public class DoubleWriteable implements Writable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private Double value;

	DoubleWriteable(double value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return "" + value;
	}

	@Override
	public void write(DataOutput out) throws IOException
	{

	}

	@Override
	public void readFields(DataInput in) throws IOException
	{

	}

	@Override
	public double toDouble()
	{
		return value;
	}

	@Override
	public float toFloat()
	{

		return value.floatValue();
	}

	@Override
	public int toInt()
	{

		return value.intValue();
	}

	@Override
	public long toLong()
	{

		return value.longValue();
	}

}
