package edu.usc.csci588team02.model;

public class DashboardEntry
{
	public enum DashboardEntryType {
		EVENT_DETAIL, EVENT_LEFT, EVENT_RIGHT, MAP_LAUNCHER, NAV_LAUNCHER
	}

	private Class<?> className;
	private DashboardEntryType entryType;
	private String label;

	public DashboardEntry(final String label, final Class<?> className,
			final DashboardEntryType type)
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
	 * @return the type of dashboard entry
	 */
	public DashboardEntryType getType()
	{
		return entryType;
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

	/**
	 * @param type
	 *            the entryType to set
	 */
	public void setType(final DashboardEntryType type)
	{
		entryType = type;
	}
}
