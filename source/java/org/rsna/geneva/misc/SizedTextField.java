/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.misc;

import javax.swing.JTextField;
import java.awt.Dimension;

public class SizedTextField extends JTextField {

	int width;

	public SizedTextField(int columns, int width) {
		super(columns);
		this.width = width;
	}

	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.width = width;
		return d;
	}

	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

}
