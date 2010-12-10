package com.github.whentoleave.activity;

/**
 * Interface denoting that the implementing class has a method to refresh its
 * current data
 */
public interface Refreshable
{
	/**
	 * Runs any necessary queries for data and changes any display elements to
	 * reflect the latest information
	 */
	public void refreshData();
}
