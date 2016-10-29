package au.com.rsutton.deeplearning.feature;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.datavec.api.conf.Configuration;
import org.datavec.api.records.listener.RecordListener;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.split.InputSplit;
import org.datavec.api.writable.Writable;

public abstract class DynamicRecordReader implements RecordReader
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int pos = 0;
	private int size;

	DynamicRecordReader(int size)
	{
		this.size = size;
	}

	@Override
	public void setConf(Configuration conf)
	{

	}

	@Override
	public Configuration getConf()
	{

		return null;
	}

	@Override
	public void close() throws IOException
	{

	}

	@Override
	public void setListeners(Collection<RecordListener> listeners)
	{

	}

	@Override
	public void setListeners(RecordListener... listeners)
	{

	}

	@Override
	public void reset()
	{
		pos = 0;

	}

	@Override
	public List<Writable> record(URI uri, DataInputStream dataInputStream) throws IOException
	{

		return null;
	}

	@Override
	public List<Writable> next()
	{
		pos++;

		return getNext(pos);
	}

	abstract List<Writable> getNext(int pos);

	@Override
	public void initialize(Configuration conf, InputSplit split) throws IOException, InterruptedException
	{

	}

	@Override
	public void initialize(InputSplit split) throws IOException, InterruptedException
	{

	}

	@Override
	public boolean hasNext()
	{
		return pos < size;
	}

	@Override
	public List<RecordListener> getListeners()
	{

		return null;
	}

	@Override
	public List<String> getLabels()
	{

		return null;
	}

}
