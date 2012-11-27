package util.logging;

import java.io.OutputStream;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import core.interfaces.Logger;

public class Log4JWrapper extends Logger
{
	protected org.apache.log4j.Logger	theLog	= null;
	
	public Log4JWrapper(String name)
	{
		theLog = org.apache.log4j.Logger.getLogger(name);
	}
	
	@Override
	public void setLevel(Level level)
	{
		theLog.setLevel(org.apache.log4j.Level.toLevel(level.toString()));
	}
	
	@Override
	public void addDestination(String format, OutputStream destination)
	{
		theLog.addAppender(new WriterAppender(new PatternLayout(format), destination));		
	}

	@Override
	public void error(String message)
	{
		theLog.error(message);
	}
	
	@Override
	public void warn(String message)
	{
		theLog.warn(message);
	}
	
	@Override
	public void info(String message)
	{
		theLog.info(message);
	}
	
	@Override
	public void trace(String message)
	{
		theLog.trace(message);
	}
	
}
