/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.misc;

import java.util.*;
import java.util.EventListener;
import javax.swing.event.EventListenerList;
import javax.swing.SwingUtilities;

/**
 * A recycling log of XDSEvents.
 */
public class EventLog {

	private int logSize = 100;
	private int next = 0;
	private int size = 0;

	private RegSysEvent[] events;
	EventListenerList listenerList;


	public EventLog(int logSize) {
		this.logSize = logSize;
		listenerList = new EventListenerList();
		events = new RegSysEvent[logSize];
	}

	/**
	 * Add an event to the log and send it to all the listeners.
	 */
	public synchronized void append(RegSysEvent event) {
		events[next] = event;
		next = (next + 1) % logSize;
		size = (size < logSize) ? size+1 : logSize;
		sendRegSysEvent(event);
	}

	/**
	 * Clear the log.
	 */
	public synchronized void clearLog() {
		size = 0;
	}

	/**
	 * Get an HTML table string with the current contents of the entire log.
	 * @return the log contents in an HTML table element.
	 */
	public synchronized String getTable() {
		int first = (next - size + logSize) % logSize;
		StringBuffer sb = new StringBuffer(10000);
		sb.append("<table>");
		for (int i=0; i<size; i++) {
			events[ (first + i) % logSize ].appendTableRow(sb);
		}
		sb.append("</table><span id=\"here\">&nbsp;</span>");
		return sb.toString();
	}

	/**
	 * Get an HTML table string with the current contents of the log,
	 * selecting only events which contain a specific string.
	 * @param searchString the string which must appear in all displayed events.
	 * @return the log contents in an HTML table element.
	 */
	public synchronized String getTable(String searchString) {
		searchString = searchString.trim().toLowerCase();
		if (searchString.equals("")) return getTable();
		int first = (next - size + logSize) % logSize;
		StringBuffer sb = new StringBuffer(10000);
		sb.append("<table>");
		for (int i=0; i<size; i++) {
			int k = (first + i) % logSize;
			if (events[k].text.toLowerCase().contains(searchString)) {
				events[k].appendTableRow(sb);
			}
		}
		sb.append("</table><span id=\"here\">&nbsp;</span>");
		return sb.toString();
	}

	/**
	 * Add an IHEListener to the listener list.
	 * @param listener the IHEListener.
	 */
	public void addRegSysListener(RegSysListener listener) {
		listenerList.add(RegSysListener.class, listener);
	}

	/**
	 * Remove a IHEListener from the listener list.
	 * @param listener the IHEListener.
	 */
	public void removeRegSysListener(RegSysListener listener) {
		listenerList.remove(RegSysListener.class, listener);
	}

	/**
	 * Send an RegSysEvent to all IHEListeners. This method sends the RegSysEvent
	 * in the Swing thread to make it thread-safe for GUI components.
	 * @param event the RegSysEvent to send.
	 */
	public synchronized void sendRegSysEvent(RegSysEvent event) {
		final RegSysEvent ev = event;
		final EventListener[] listeners = listenerList.getListeners(RegSysListener.class);
		Runnable sendEvents = new Runnable() {
			public void run() {
				for (int i=0; i<listeners.length; i++) {
					((RegSysListener)listeners[i]).regsysEventOccurred(ev);
				}
			}
		};
		SwingUtilities.invokeLater(sendEvents);
	}
}