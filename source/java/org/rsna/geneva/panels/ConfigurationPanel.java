/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.panels;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.misc.SizedTextField;

/**
 * A JPanel to provide a user interface for setting the system props.
 */
public class ConfigurationPanel extends JPanel {

	HtmlJPanel text;
	SizedTextField searchText;

    /**
     * Class constructor.
     */
    public ConfigurationPanel() {
		super();
		setLayout(new BorderLayout());
		Configuration.getInstance().setConfigurationPanel(this);
		text = new HtmlJPanel();
		this.add(text,BorderLayout.CENTER);
		FooterPanel footerPanel = new FooterPanel();
		this.add(footerPanel,BorderLayout.SOUTH);
		updateText();
    }

    public void display() {
		if (Configuration.getInstance().hasChanged())
			updateText();
	}

	public void updateText() {
		text.setText(Configuration.getInstance().getPage(searchText.getText().trim()));
	}

	class FooterPanel extends Box implements ActionListener, DocumentListener {
		public JButton reloadButton;
		public FooterPanel() {
			super(BoxLayout.X_AXIS);
			reloadButton = new JButton("Reload");
			reloadButton.addActionListener(this);
			this.add(Box.createHorizontalStrut(190));
			this.add(Box.createHorizontalGlue());
			this.add(reloadButton);
			this.add(Box.createHorizontalGlue());
			this.add(new JLabel("Filter:"));
			this.add(Box.createHorizontalStrut(7));
			searchText = new SizedTextField(20,150);
			searchText.getDocument().addDocumentListener(this);
			this.add(searchText);
			this.add(Box.createHorizontalStrut(20));
		}
		//The ActionListener interface
		public void actionPerformed(ActionEvent event) {
			Configuration.getInstance().reload();
			updateText();
		}
		//The DocumentListener interface
		public void insertUpdate(DocumentEvent event) {
			updateText();
		}
		public void removeUpdate(DocumentEvent event) {
			updateText();
		}
		public void changedUpdate(DocumentEvent event) { }
	}

}
