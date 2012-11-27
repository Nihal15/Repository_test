package core.interfaces;

import java.io.OutputStream;

import util.logging.Log;

/**
 * Use this interface to implement any [wrapper of a] logging structure that is returned by {@link Log}.
 * 
 * @author Andrei Olaru
 * 
 */
public abstract class Logger // FIXME to rename to LoggerWrapper
{
	/**
	 * Supported logger types. These will extend the {@link Logger} class.
	 * 
	 * @author Andrei Olaru
	 *
	 */
	public static enum LoggerType {
		LOG4J,
		
		JADE,
	}
	
	/**
	 * Indicates the level of the log. Mimics {@link org.apache.log4j.Level}.
	 * 
	 * @author Andrei Olaru
	 * 
	 */
	public enum Level {
		
		OFF,
		
		/**
		 * unused in this project
		 */
		FATAL,
		
		ERROR,
		
		WARN,
		
		/**
		 * unused in this project
		 */
		INFO,
		
		/**
		 * unused in this project
		 */
		DEBUG,
		
		TRACE,
		
		ALL,
	}
	
	public abstract void setLevel(Level level);
	
	/**
	 * @param format : a pattern, in a format that is potentially characteristic to the wrapper
	 * @param destination : a destination stream
	 */
	public abstract void addDestination(String format, OutputStream destination);
	
	public abstract void error(String message);
	
	public abstract void warn(String message);
	
	public abstract void info(String message);
	
	public abstract void trace(String message);
	
	public void error(String message, Object... objects)
	{
		error(compose(message, objects));
	}
	
	public void warn(String message, Object... objects)
	{
		warn(compose(message, objects));
	}
	
	public void info(String message, Object... objects)
	{
		info(compose(message, objects));
	}
	
	public void trace(String message, Object... objects)
	{
		trace(compose(message, objects));
	}
	
	protected static String compose(String message, Object[] objects)
	{
		String ret = message;
		for(Object object : objects)
			ret += "," + object.toString();
		return ret;
	}
}
