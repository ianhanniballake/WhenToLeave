package edu.usc.csci588team02.model;

public class DashboardEntry
{
	private Class<?> className;
	private String label;

	public DashboardEntry(final String label, final Class<?> className)
	{
		setLabel(label);
		setClassName(className);
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
