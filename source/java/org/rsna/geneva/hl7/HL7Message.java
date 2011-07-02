/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.hl7;

import java.io.*;
import java.net.*;
import java.util.*;
import org.rsna.geneva.misc.RegSysEvent;

public class HL7Message {

	public String name;
	LinkedList<HL7Segment> segmentList;

	static final char char11 = '\u000B';
	static final char char13 = '\r';
	static final char char28 = '\u001C';

	/**
	 * Create a new HL7Message.
	 */
	public HL7Message(String name) {
		this.name = name;
		segmentList = new LinkedList<HL7Segment>();
	}

	public void addSegment(HL7Segment segment) {
		segmentList.add(segment);
	}

	public HL7Segment getSegment(String name) {
		return getSegment(name, 0);
	}

	public HL7Segment getSegment(String name, int instance) {
		ListIterator<HL7Segment> lit = segmentList.listIterator();
		int count = 0;
		while (lit.hasNext()) {
			HL7Segment segment = lit.next();
			if (segment.name.equals(name)) {
				count++;
				if (count >= instance) return segment;
			}
		}
		return null;
	}

	//Note: this code always picks the first segment for a field
	public void setFields(HL7Field[] fields) {
		for (int i=0; i<fields.length; i++) {
			if (fields[i].msg.equals(name)) {
				HL7Segment segment = getSegment(fields[i].segment);
				if (segment != null) segment.setField(fields[i].seq, fields[i].value);
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		ListIterator<HL7Segment> lit = segmentList.listIterator();
		while (lit.hasNext()) sb.append(lit.next().toString());
		return sb.toString();
	}

	static int fileNumber = 0;
	private synchronized void saveMessage() {
		File messages = new File("messages");
		int n = ++fileNumber;
		messages.mkdirs();
		File file = new File(messages,"HL7-"+n+"-"+name+".txt");
		try {
			byte[] bytes = (char11 + toString() + char28 + char13).getBytes();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.flush();
			fos.close();
		}
		catch (Exception tooBad) { }
	}

	public String send(String urlString, int timeout) {
		String response = null;
		try {
			byte[] bytes = (char11 + toString() + char28 + char13).getBytes();
			URL url = new URL(urlString);
			Socket socket = new Socket();
			int to = (timeout>0) ? timeout : 5000;
			socket.setSoTimeout(to);
			InetSocketAddress isa = new InetSocketAddress(url.getHost(),url.getPort());
			socket.connect(isa,to);

			//Get both streams
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();

			//Write the message
			out.write(bytes);
			out.flush();

			//Get the response
			response = getResponse(in);

			out.close();
			socket.close();
			return response;
		}
		catch (Exception ex) {
			return "Error: " + ex.getMessage();
		}
	}

	public String getResponse(InputStream in) throws Exception {
		BufferedReader br = new BufferedReader( new InputStreamReader( in ) );
		StringBuffer sb = new StringBuffer();
		try {
			char lastChar = 'x';
			boolean done = false;
			int codepoint;
			while (!done && ((codepoint = br.read()) != -1)) {
				char[] chars = Character.toChars(codepoint);
				for (int i=0; i<chars.length; i++) {
					if (!Character.isISOControl(chars[i]))
						sb.append(chars[i]);
					else
						sb.append(' ');
					if ((chars[i] == char13) && (lastChar == char28))
						done = true;
					lastChar = chars[i];
				}
			}
			br.close();
			return sb.toString();
		}
		catch (Exception ex) {
			//To handle systems which don't send a complete response,
			//don't throw an exception if the amount of text we received
			//before the error (typically a read timeout) indicates success.
			String response = sb.toString().trim();
			if (isOK(response)) return response;

			//Whatever it was, it wasn't a success response, so throw
			//an exception so we can see the error.
			//ex.printStackTrace();
			throw new Exception(ex.getMessage() + (response.equals("") ? "" : "\r"+response));
		}
	}

	public static boolean isOK(String response) {
		if ((response == null) || response.startsWith("Error:")) return false;
		return (response.indexOf("MSA|AA") != -1);
	}

	public static int toStatus(String response) {
		return isOK(response) ? RegSysEvent.STATUS_OK : RegSysEvent.STATUS_ERROR;
	}

}

