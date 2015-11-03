package au.com.rsutton.config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config
{

	Properties properties = new Properties();
	private File file;

	public Config() throws FileNotFoundException, IOException
	{
		this.file = new File("RobotConfig.txt");
		if (file.exists())
		{
			properties.load(new BufferedInputStream(new FileInputStream(file)));
		}
	}

	public void save() throws FileNotFoundException, IOException
	{
		properties.store(new BufferedOutputStream(new FileOutputStream(file)),
				"");
	}

	public Double loadSetting(String key, Double value)
	{
		String storedValue = (String) properties.get(key);
		if (storedValue == null)
		{
			return value;
		}
		try
		{
			value = Double.parseDouble(storedValue);
		} catch (Exception e)
		{

		}
		return value;

	}

	public Integer loadSetting(String key, Integer value)
	{
		String storedValue = (String) properties.get(key);
		if (storedValue == null)
		{
			return value;
		}
		try
		{
			value = Integer.parseInt(storedValue);
		} catch (Exception e)
		{

		}
		return value;

	}

	public void storeSetting(String key, Object value)
	{
		properties.put(key, value);
	}

	String loadSetting(String key, String value)
	{
		String storedValue = (String) properties.get(key);
		if (storedValue == null)
		{
			return value;
		}
		return storedValue;

	}
}
