package edu.usc.csci588team02.model;

public class DashboardEntry
{
	public enum DashboardEntryType { 
		MAP_LAUNCHER, 
		NAV_LAUNCHER, 
		EVENT_DETAIL,
		EVENT_RIGHT, 
		EVENT_LEFT
	}
	private DashboardEntryType entryType;
	private Class<?> className;
	private String label;
	
	public DashboardEntry(final String label, final Class<?> className, final DashboardEntryType type)
	{
		setLabel(label);
		setClassName(className);
		setType(type);
	}
	
	public DashboardEntry(final String label, final DashboardEntryType type)
	{
		setLabel(label);
		setType(type);
	}

	/**
	 * @return the type of dashboard entry
	 */
	public DashboardEntryType getType()
	{
		return entryType;
	}
	
	/**
	 * @return the className
	 */
	public Class<?> getClassName()
	{
		return className;
	}

	/**
	 * @return the label
	 */
	public String getLabel()
	{
		return label;
	}
	
	/**
	 * @param type
	 * 			the entryType to set
	 */
	public void setType(DashboardEntryType type)
	{
		this.entryType = type;
	}

	/**
	 * @param className
	 *            the className to set
	 */
	public void setClassName(final Class<?> className)
	{
		this.className = className;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(final String label)
	{
		this.label = label;
	}
}
