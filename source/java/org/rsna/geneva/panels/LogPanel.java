/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.panels;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.misc.EventLog;
import org.rsna.geneva.misc.SizedTextField;

public class LogPanel extends JPanel {

	public HtmlJPanel text;
	private EventLog eventLog;
	boolean displayed = true;
	SizedTextField searchText;

	public LogPanel(EventLog eventLog, Color background) {
		super();
		this.setLayout(new BorderLayout());
		this.eventLog = eventLog;
		text = new HtmlJPanel();
		this.add(text,BorderLayout.CENTER);
		FooterPanel footerPanel = new FooterPanel();
		this.add(footerPanel,BorderLayout.SOUTH);
		updateText();
	}

	public void setDisplayed(boolean displayed) {
		this.displayed = displayed;
	}

	public synchronized void updateText() {
		if (displayed) {
			text.setText(eventLog.getTable(searchText.getText().trim()));
			text.scrollToBottom();
		}
	}

	class FooterPanel extends Box implements ActionListener, DocumentListener {
		public JButton clearButton;
		public FooterPanel() {
			super(BoxLayout.X_AXIS);
			clearButton = new JButton("Clear Log");
			clearButton.addActionListener(this);
			this.add(Box.createHorizontalStrut(190));
			this.add(Box.createHorizontalGlue());
			this.add(clearButton);
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
			eventLog.clearLog();
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

