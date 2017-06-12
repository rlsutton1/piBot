package au.com.rsutton.robot.rover;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class LogLevelHelper
{

	public static void setLevel(Logger logger, Level level)
	{
		Configurator.setLevel(logger.getName(), level);

	}

}
