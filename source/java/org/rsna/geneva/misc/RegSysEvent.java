/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.misc;

import java.awt.AWTEvent;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * The event that passes XDSI information to XDSIListeners.
 */
public class RegSysEvent extends AWTEvent {

	public static final int REGSYS_EVENT	= AWTEvent.RESERVED_ID_MAX + 2010;

	public static final int STATUS_OK		= 0;
	public static final int STATUS_ERROR	= 1;

	public static final int TYPE_STATUS		= 0;
	public static final int TYPE_HL7		= 1;
	public static final int TYPE_HTTP		= 2;
	public static final int TYPE_DICOM		= 3;
	public static final int TYPE_SOAP		= 4;
	public static final int TYPE_XML		= 5;

	public int status;
	public int type;
	public long time;
	public String text;
	public String timeText;

	private String[] statusStrings = {
		"OK",
		"ERROR"
	};

	private String[] typeStrings = {
		"Status",
		"HL7",
		"HTTP",
		"DICOM",
		"SOAP",
		"XML"
	};

	private String[] colors = {
		"green",
		"black",
		"blue",
		"green",
		"maroon",
		"maroon"
	};

	private static Calendar calendar = new GregorianCalendar();
	private static final String zeroes = "00000";

	/**
	 * Class constructor capturing information about an XSDI event.
	 * @param source the source of the event.
	 * @param status the status conveyed by the event (OK or ERROR).
	 * @param type the type of event.
	 * @param text the message.
	 */
	public RegSysEvent(Object source, int status, int type, String text) {
		super(source, REGSYS_EVENT);
		this.status = status;
		this.type = type;
		this.text = text;
		this.time = System.currentTimeMillis();
		this.timeText = setTimeText();
	}

	/**
	 * Class constructor to clone an existing event.
	 * @param e the event to clone.
	 */
	public RegSysEvent(RegSysEvent e) {
		this(e.source, e.status, e.type, e.text);
	}

	public static RegSysEvent getOKEvent(Object source, String message) {
		return new RegSysEvent(source, STATUS_OK, TYPE_STATUS, message);
	}

	public static RegSysEvent getErrorEvent(Object source, String message) {
		return new RegSysEvent(source, STATUS_ERROR, TYPE_STATUS, message);
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getStatus() {
		return getString(statusStrings,status);
	}

	public String getType() {
		return getString(typeStrings,type);
	}

	public String getText() {
		return text;
	}

	public String getTime() {
		return timeText;
	}

	public void appendTableRow(StringBuffer sb) {
		sb.append("<tr><td valign=\"top\" width=\"90\">"+timeText+"</td><td>");
		if (status == STATUS_ERROR) sb.append("<font color=\"red\">");
		else sb.append("<font color=\""+colors[type]+"\">");
		sb.append(getType() + " (" + getStatus() + ")<br>" + text.replaceAll("\\r","<br>"));
		sb.append("</font>");
		sb.append("</td></tr>\n");
	}

	private String getString(String[] strings, int index) {
		if ((index >= 0) && (index < strings.length)) return strings[index];
		return "Unknown";
	}

	private synchronized String setTimeText() {
		calendar.setTimeInMillis(time);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		int sec = calendar.get(Calendar.SECOND);
		int msec = calendar.get(Calendar.MILLISECOND);
 		return zero(hour,2) + ":" + zero(min,2) + ":" + zero(sec,2) + "." + zero(msec,3);
	}

	private String zero(int val, int len) {
		String valText = Integer.toString(val);
		int n = len - valText.length();
		if (n <= 0) return valText;
		return zeroes.substring(0,n) + valText;
	}

}
