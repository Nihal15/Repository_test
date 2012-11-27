package util.logging;

import java.awt.TextArea;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import core.interfaces.Logger;
import core.interfaces.Logger.Level;
import core.interfaces.Logger.LoggerType;

/**
 * Implements a log, based on a log4j log. The idea is to have a standard log4j {@link Logger}, but with some additional features, that are accessible via
 * static functions of this class, using the Logger object itself as a unique reference.
 * 
 * <p>
 * There are currently three possible destinations for the logging information: the console output is standard; an optional {@link TextArea} may be specified to
 * be updated with the contents of the log; a {@link ReportingEntity} may be specified that will be updated with the logging information.
 * 
 * <p>
 * A new log is obtained by getLogger(unitName), where unitName should be unique and is the name of the log.
 * 
 * <p>
 * Other constructors are available, specifying a {@link TextArea} and / or a {@link ReportingEntity}.
 * 
 * <p>
 * Using the constructor(s) that contain the 'link' parameter, the log is linked to another log (its 'parent') and when the parent closes, the children close
 * too.
 * 
 * <p>
 * When a log is not needed any more, one should _always_ call exitLogger() for the that log. (Except if a parent has been given, and it is certain that the log
 * will be closed by its parent)
 * 
 * @author Andrei Olaru
 * 
 */
public class Log
{
	/**
	 * Interface for an entity / unit that keeps a log and that needs to report that log to other entities.
	 * 
	 * @author Andrei Olaru
	 * 
	 */
	public interface ReportingEntity
	{
		/**
		 * The function will be called at intervals of reportUpdateDelay, if new logging information exist since the last call.
		 * 
		 * @param content
		 *            - an update from the log containing the new logging information since the last call of this function.
		 * @return true if the reporting has completed correctly. Otherwise, the same information will be reported again at the next call.
		 */
		public boolean report(String content);
	}
	
	private static LoggerType				defaultLoggerWrapper	= LoggerType.LOG4J;
	
	public static Character					AWESOME_SEPARATOR		= new Character((char)30);
	
	// // here be the static components of the class, which by being static are unique for the current JVM.
	
	/**
	 * Contains all the currently active logs, identified by their [unit]Name. Active means that a timer is associated with them.
	 * 
	 * All access to the logs field should be synchronized explicitly.
	 */
	protected static Map<String, Log>		logs					= Collections.synchronizedMap(new HashMap<String, Log>());
	/**
	 * All access to parents should be synchronized. In this source, all accesses take place inside locks on the logs field.
	 */
	protected static Map<String, String>	parents					= new HashMap<String, String>();
	
	// /////////// here be the components of the log
	/**
	 * The logger that is being wrapped by the instance.
	 */
	protected Logger						logger					= null;
	/**
	 * The logger implementation. Indicates the wrapper to use.
	 */
	protected LoggerType					wrapperType				= null;
	/**
	 * The name of the log and of the Unit.
	 */
	protected String						name					= null;
	/**
	 * The level of the log (see {@link Logger} ).
	 */
	protected Level							logLevel				= Level.ALL;
	
	// /////////// here be the components of the log related to external reporting (to a text area and to a Jade agent, respectively
	/**
	 * Contains the entire output of the log. Version without time stamps and unit name, just level and message.
	 */
	protected ByteArrayOutputStream			logOutput				= null;
	/**
	 * Contains the entire output of the log. Version with time stamp, level, unit name, and message.
	 */
	protected ByteArrayOutputStream			logOutputStamped		= null;
	/**
	 * used to trace if there have been modifications to the log, before flushing it into the TextArea.
	 */
	protected long							logSize					= 0L;
	/**
	 * The {@link TextArea} that will be kept up to date with the contents of the log
	 */
	protected TextArea						logText					= null;
	/**
	 * Timer to update the external views of this log.
	 */
	protected Timer							logTimer				= null;
	/**
	 * Delay at which to update the text area.
	 */
	protected long							logUpdateDelay			= 250;
	/**
	 * Delay at which to update the visualizing agent.
	 */
	protected long							reportUpdateDelay		= 2000;
	protected long							timeToNextReport		= 0;
	/**
	 * The {@link AID} of the Jade agent to send logging information to.
	 */
	protected ReportingEntity				externalReporter		= null;
	/**
	 * Cumulative size of the logging information sent so far.
	 */
	protected int							lastUpdatedSize			= 0;
	
	/**
	 * Provides a logger with the given name. If the name is already in use (and <code>ensureNew</code> is <code>false</code>), the log corresponding to that
	 * name will be returned and all other parameters will be ignored.
	 * 
	 * @throws IllegalArgumentException
	 *             : if the name is not new and <code>ensureNew</code> was set to true.
	 * @param name
	 *            : is the name of the associated unit, and of the log. Should be not null unique among active logs. Use {@link Unit}.DEFAULT_UNIT_NAME for the default.
	 * @param link
	 *            : the name of another, 'parent', log that this log is linked to; when the other log will close, this will be closed as well.
	 * @param textArea
	 *            : a TextArea that will be updated with the contents of the log.
	 * @param loggerType
	 *            : the {@link Logger} class to instantiate. If null, one of them is chosen by default.
	 * @return A new, configured, Logger object.
	 */
	public static Logger getLogger(String name, String link, TextArea textArea, ReportingEntity reporter, boolean ensureNew, LoggerType loggerType)
	{
		boolean erred = false;
		int nlogs = -1;
		
		if(name == null)
			throw new IllegalArgumentException("log name cannot be null. Use unit.DEFAULT_UNIT_NAME for the default name.");
		
		Log thelog = new Log(name, loggerType, textArea, reporter);
		Log alreadyPresent = null;
		synchronized(logs)
		{
			if(logs.containsKey(name))
				if(ensureNew)
					erred = true;
				else
					alreadyPresent = logs.get(name);
			else
			{
				logs.put(name, thelog);
				parents.put(name, link);
				nlogs = logs.size();
			}
		}
		if(erred)
		{
			thelog.doexit();
			throw new IllegalArgumentException("log name already present [" + name + "]");
		}
		if(alreadyPresent != null)
		{
			thelog.doexit();
			thelog = alreadyPresent;
		}
		thelog.logger.trace("new log (count before [" + nlogs + "]).");
		return thelog.getLog();
	}
	
	/**
	 * Provides a logger with the given name. If the name is already in use, the log corresponding to that name will be returned and all other parameters will
	 * be ignored.
	 * 
	 * @throws IllegalArgumentException
	 *             : if the name already exists for an active log.
	 * @param name
	 *            : is the name of the associated unit, and of the log. Should be unique among active logs.
	 * @param link
	 *            : the name of another, 'parent', log that this log is linked to; when the other log will close, this will be closed as well.
	 * @return A new, configured, Logger object.
	 */
	public static Logger getLogger(String name, String link)
	{
		return getLogger(name, link, null, null, false, null);
	}
	
	/**
	 * Provides a logger with the given name.
	 * 
	 * @throws IllegalArgumentException
	 *             : if the name already exists for an active log.
	 * @param name
	 *            : is the name of the associated unit, and of the log. Should be unique among active logs.
	 * @return A new, configured, Logger object.
	 */
	public static Logger getLogger(String name)
	{
		return getLogger(name, null, null, null, false, null);
	}
	
	/**
	 * Provides a logger with the given name. If the name is already in use, the log corresponding to that name will be returned and all other parameters will
	 * be ignored.
	 * 
	 * @throws IllegalArgumentException
	 *             : if the name already exists for an active log.
	 * @param name
	 *            : is the name of the associated unit, and of the log. Should be unique among active logs.
	 * @param textArea
	 *            : a TextArea that will be updated with the contents of the log.
	 * @return A new, configured, Logger object.
	 */
	public static Logger getLogger(String name, TextArea textArea)
	{
		return getLogger(name, null, textArea, null, false, null);
	}
	
	/**
	 * Get the whole output of the log.
	 * 
	 * @param name
	 *            : the name of the log. If within a normal {@link Unit}, probably the return value of getName().
	 * @param shortOutput
	 *            : if <code>true</code>, the output does not contain the agent name and time stamps (just the level and message).
	 * @return the entire contents of the log.
	 */
	public static String getLoggerOutput(String name, boolean shortOutput)
	{
		Log found = null;
		synchronized(logs)
		{
			found = logs.get(name);
		}
		if(found == null)
			throw new IllegalArgumentException("log not present [" + name + "]");
		if(shortOutput)
			return found.logOutput.toString();
		return found.logOutputStamped.toString();
	}
	
	/**
	 * Closes the log specified by the name (stops the associated timer) and frees the name so it can be reused. The log with not be flushed (sent as report)
	 * before closing.
	 * 
	 * @param name
	 *            : the name of the log to be freed. If within a normal {@link Unit}, probably the return value of getName().
	 */
	public static void exitLogger(String name)
	{
		exitLogger(name, false);
	}
	
	/**
	 * Closes the log specified by the name (stops the associated timer) and frees the name so it can be reused.
	 * 
	 * @param name
	 *            the name of the log to be freed. If within a normal {@link Unit}, probably the return value of getName().
	 */
	public static void exitLogger(String name, boolean flushFirst)
	{
		boolean notpresent = false;
		Log found = null;
		int nlogs = -1;
		Vector<String> toClose = new Vector<String>();
		synchronized(logs)
		{
			if(!logs.containsKey(name))
				notpresent = true;
			else
			{
				for(Map.Entry<String, String> link : parents.entrySet())
					if(name.equals(link.getValue()))
						toClose.add(link.getKey());
				found = logs.get(name);
				logs.remove(name);
				nlogs = logs.size();
			}
		}
		for(String logName : toClose)
			exitLogger(logName, flushFirst);
		if(notpresent || (found == null))
			throw new IllegalArgumentException("log not present [" + name + "]");
		found.getLog().trace("log out (logs remaining [" + nlogs + "]).");
		if(flushFirst)
			found.updateReport();
		found.doexit();
	}
	
	protected Log(String logName, LoggerType type, TextArea ta, ReportingEntity reporter)
	{
		this.name = logName;
		logOutput = new ByteArrayOutputStream();
		logOutputStamped = new ByteArrayOutputStream();
		
		if(type == null)
			wrapperType = defaultLoggerWrapper;
		else
			wrapperType = type;
		switch(wrapperType)
		{
		case JADE:
			// TODO
			break;
		case LOG4J:
			logger = new Log4JWrapper(logName);
			
			break;
		}
		
		if(logger == null)
			throw (new IllegalStateException());
		
		logger.setLevel(logLevel);
		// level, message: for TextArea
		logger.addDestination("%-5p \t %m%n", logOutput);
		// date level name message (no new line): for reporting (also, obscure reference)
		logger.addDestination(AWESOME_SEPARATOR + "%d{HH:mm:ss:SSSS} %-5p [" + logName + "]:\t %m" + AWESOME_SEPARATOR, logOutputStamped);
		// priority (level), name, message, line break: for console
		logger.addDestination("%-5p [" + logName + "]:\t %m%n", System.out);
		
		logText = ta;
		
		externalReporter = reporter;
		
		if((logText != null) || (externalReporter != null))
		{
			// logger.trace("setting and starting timer...");
			logTimer = new Timer();
			logTimer.schedule(new TimerTask() {
				@Override
				public void run()
				{
					updateLogText();
				}
			}, 0, logUpdateDelay);
		}
	}
	
	protected Logger getLog()
	{
		return logger;
	}
	
	protected void updateLogText()
	{
		int cSize = logOutput.size();
		if((logText != null) && (logSize != cSize))
		{
			// logger.trace("updating text area...");
			logText.setText(logOutput.toString());
			logSize = cSize;
			logText.append("."); // should make it always scroll down.
			logText.repaint();
		}
		timeToNextReport -= logUpdateDelay;
		if(timeToNextReport <= 0)
		{
			timeToNextReport = reportUpdateDelay;
			updateReport();
		}
	}
	
	protected void updateReport()
	{
		int cSize2 = logOutputStamped.size();
		if((externalReporter != null) && (cSize2 != lastUpdatedSize))
		{
			// logger.trace("updating reporter...");
			byte[] content = logOutputStamped.toByteArray();
			byte[] update = copyOfRange(content, lastUpdatedSize, cSize2);
			if(externalReporter.report(new String(update).trim()))
				lastUpdatedSize = cSize2;
		}
	}
	
	private static byte[] copyOfRange(byte[] content, int start, int end)
	{
		int length = end - start;
		byte[] returnArray = new byte[length];
		
		for(int i = 0; i < length; ++i)
			returnArray[i] = content[start + i];
		
		return returnArray;
	}
	
	protected void doexit()
	{
		if(logTimer != null)
			logTimer.cancel();
	}
}