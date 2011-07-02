/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.panels;

import java.awt.BorderLayout;
import java.awt.Point;
import java.io.File;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.text.Document;
import org.rsna.util.FileUtil;

/**
 * An extension of JPanel containing a Scrollable JEditorPane set to display
 * HTML in a JScrollPane, with the rendered width tracking the width of the
 * JPanel. This provides the type of interface normally seen in a browser.
 */
public class HtmlJPanel extends JPanel {

	/** The text editor - provided for direct access to the JEditorPane methods. */
	public ScrollableJEditorPane editor;
	/** The scroll pane - provided for direct access to the JScrollPane methods. */
	public JScrollPane scrollPane;

	/**
	 * Class constructor creating a Scrollable HTML panel with no text.
	 */
	public HtmlJPanel() {
		this("");
	}

	/**
	 * Class constructor creating a Scrollable HTML panel with text read from a file.
	 * @param file the file containing the initial text string.
	 */
	public HtmlJPanel(File file) {
		this(FileUtil.getFileText(file));
	}

	/**
	 * Class constructor creating a Scrollable HTML panel with an initial text string.
	 * @param text the initial text string.
	 */
	public HtmlJPanel(String text) {
		super();
		editor = new ScrollableJEditorPane("text/html",text);
		editor.setEditable(false);
		this.setLayout(new BorderLayout());
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(editor);
		scrollPane.getVerticalScrollBar().setUnitIncrement(25);
		scrollPane.getVerticalScrollBar().setBlockIncrement(25);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(15);
		scrollPane.getHorizontalScrollBar().setBlockIncrement(15);
		this.add(scrollPane,BorderLayout.CENTER);
	}

	/**
	 * Replace the editor's current text with new text.
	 * @param text the replacement text string.
	 */
	public void setText(String text) {
		JScrollBar sb = scrollPane.getVerticalScrollBar();
		int min = sb.getMinimum();
		int max = sb.getMaximum();
		int pos = sb.getValue();
		float cpf = (float)(pos - min) / (float)(max - min);
		editor.setText(text);
		Document doc = editor.getDocument();
		if (doc != null) {
			int length = doc.getLength();
			int cp = (int)(length * cpf);
			editor.setCaretPosition(cp);
		}
	}

	/**
	 * Return the editor's current text.
	 */
	public String getText() {
		return editor.getText();
	}

	/**
	 * Scroll to the top of the text.
	 */
	public void scrollToTop() {
		editor.setCaretPosition(0);
	}

	/**
	 * Scroll to the bottom of the text.
	 */
	public void scrollToBottom() {
		editor.setCaretPosition(editor.getDocument().getLength());
	}

	/**
	 * An extension of JEditorPane that implements the Scrollable interface
	 * and forces the width to track the width of the Viewport.
	 */
	public class ScrollableJEditorPane extends JEditorPane implements Scrollable {
		/**
		 * Class constructor creating an editor for a specified content type
		 * (e.g., "text/html") and with an initial text string.
		 * @param type the content type.
		 * @param text the initial text string.
		 */
		public ScrollableJEditorPane(String type, String text) {
			super(type,text);
		}
		/**
		 * Inplement the Scrollable interface for tracking the Viewport width.
		 * @return true if tracking Viewport width; false otherwise.
		 */
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}
	}

}

