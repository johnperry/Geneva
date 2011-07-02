/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.installer;

/**
 * The Geneva program installer, consisting of just a
 * main method that instantiates a SimpleInstaller.
 */
public class Installer {

	static String windowTitle = "Geneva Installer";
	static String programName = "Geneva";
	static String introString = "<p><b>Geneva</b> is a support tool for IHE demonstrations.</p>"
								+ "<p>This program installs and configures all the "
								+ "required software components.</p>";

	public static void main(String args[]) {
		new SimpleInstaller(windowTitle, programName, introString);
	}
}