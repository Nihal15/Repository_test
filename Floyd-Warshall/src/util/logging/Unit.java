package util.logging;

import java.awt.TextArea;

import core.interfaces.Logger.Level;
import core.interfaces.Logger;
import core.interfaces.Logger.LoggerType;

import util.Config;
import util.logging.Log.ReportingEntity;

/**
 * The Unit class should be extended by any class using a log obtained from {@link Log}.
 * 
 * <p>
 * It is characterized by the unitName, which also gives the name of (and helps refer) the log.
 * 
 * <p>
 * On construction, the name can be the <code>DEFAULT_UNIT_NAME</code>, in which case the class id is used; <code>null</code>, in which case a log is not created; or another name that should be unique across the JVM.
 * 
 * @author Andrei Olaru
 * 
 */
public class Unit
{
	public static class UnitLinkData extends Config
	{
		String	parentLogName	= null;
		Logger	parentLog		= null;
		boolean	exitTogether	= true;
		boolean	includeInParent	= false;
		String	prefix			= "";
		
		public UnitLinkData()
		{
			super();
		}
		
		public UnitLinkData setparentLogName(String _parentLogName)
		{
			this.parentLogName = _parentLogName;
			return this;
		}
	}
	
	public static class UnitConfigData extends Config
	{
		protected String	unitName			= null;
		boolean				ensureNew			= false;
		
		Level				level				= null;
		
		TextArea			textArea			= null;
		ReportingEntity		reporter			= null;
		
		LoggerType			loggerWrapperClass	= null;
		
		UnitLinkData		linkData			= new UnitLinkData();
		
		public UnitConfigData()
		{
			setName(null);
		}
		
		public UnitConfigData setName(String name)
		{
			if(name == null)
				setLevel(Level.OFF);
			if(name == null || DEFAULT_UNIT_NAME.equals(name))
				unitName = super.toString();
			else
				unitName = name;
			return this;
		}
		
		public UnitConfigData setTextArea(TextArea ta)
		{
			this.textArea = ta;
			return this;
		}
		
		public UnitConfigData setReporter(ReportingEntity reportingEntity)
		{
			this.reporter = reportingEntity;
			return this;
		}
		
		public UnitConfigData setLevel(Level logLevel)
		{
			this.level = logLevel;
			return this;
		}
		
		public UnitConfigData ensureNew()
		{
			ensureNew = true;
			return this;
		}
		
		public UnitConfigData setLink(UnitLinkData unitLinkData)
		{
			this.linkData = unitLinkData;
			return this;
		}
		
		public UnitConfigData setLink(String parentLogName)
		{
			return setLink(new UnitLinkData().setparentLogName(parentLogName));
		}
		
		public UnitConfigData setType(LoggerType loggerType)
		{
			this.loggerWrapperClass = loggerType;
			return this;
		}
	}
	
	public final static String	DEFAULT_UNIT_NAME	= "theDefaulUnitName";
	
	UnitConfigData				config				= null;
	protected Logger			log					= null;
	
	public Unit()
	{
		this(new UnitConfigData());
	}
	
	public Unit(UnitConfigData configuration)
	{
		config = configuration;
		if((config == null) || (config.unitName == null))
			config = new UnitConfigData();
		
		log = Log.getLogger(config.unitName, config.linkData.parentLogName, config.textArea, config.reporter, config.ensureNew, config.loggerWrapperClass);
		if(config.level != null)
			log.setLevel(config.level);
	}
	
	public Logger getLog()
	{
		return log;
	}
	
	public String getName()
	{
		return config.unitName;
	}
	
	public void exit()
	{
		doExit();
	}
	
	protected void doExit()
	{
		if(config.unitName != null)
			Log.exitLogger(config.unitName);
	}
}
