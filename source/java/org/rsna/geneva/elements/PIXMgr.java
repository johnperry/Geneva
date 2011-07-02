/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.elements;

import java.util.*;
import org.w3c.dom.Element;
import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.misc.Registration;
import org.rsna.util.StringUtil;
import org.rsna.geneva.misc.RegSysEvent;
import org.rsna.geneva.hl7.*;

public class PIXMgr extends DataSystem {

	public String type = "PIX Manager";

	public int connectionInterval;

	public PIXMgr(Element el) {
		super(el);
		String connectionIntervalString = el.getAttribute("connectionInterval").trim();
		try { this.connectionInterval = Integer.parseInt(connectionIntervalString); }
		catch (Exception ex) { this.connectionInterval = 0; }
	}

	public void appendDataRows(StringBuffer sb) {
		super.appendDataRows(sb);
		sb.append("<tr><td>HL7 Connection Interval (ms):</td><td>"+connectionInterval+"</td></tr>");
	}

	void processRegistration(Registration reg) {
		super.processRegistration(reg);

		Configuration config = Configuration.getInstance();

		if (acceptsITI8withLocalID) {

			//Send the local IDs
			Hashtable<String,String> laaTable = new Hashtable<String,String>();
			sendLocalIDs(reg, config.getEHRSystems(), laaTable);
			sendLocalIDs(reg, config.getDCMSystems(), laaTable);

			//Log a summary event listing the
			//localAssigningAuthorities
			//for which localIDs were sent.
			Enumeration<String> keys = laaTable.keys();
			if (keys.hasMoreElements()) {
				StringBuffer sb = new StringBuffer();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					String localID = laaTable.get(key);
					sb.append("<br>" + StringUtil.displayable(localID + ": "+key));
				}
				RegSysEvent event =
					new RegSysEvent(
							this,
							RegSysEvent.STATUS_OK,
							RegSysEvent.TYPE_HL7,
							"Summary: Local IDs sent to "+type + " ["+id+"]" +
							"<br>Global Assigning Authority: " +
							globalAssigningAuthority +
							sb.toString());
				config.getEventLog().append(event);
			}
		}
	}

	//Send the LocalIDs of all EHRSystems that transmit them.
	//This method sends at most one localID for each local assigning authority.
	private void sendLocalIDs(
						Registration reg,
						EHRSystem[] ehrSystems,
						Hashtable<String,String> laaTable) {

		for (int i=0; i<ehrSystems.length; i++) {
			EHRSystem ehrsystem = ehrSystems[i];
			String localID = reg.localIDTable.get(ehrsystem.id);
			String sourceLocalAssigningAuthority = ehrsystem.localAssigningAuthority;

			//Only send when all these conditions are met simultaneously:
			// -- the EHR System is enabled
			// -- the EHR System sends localIDs
			// -- the EHR System belongs to the same Global Assigning Authority
			// -- we haven't sent the localID for this localAssigningAuthority before

			if (ehrsystem.enabled && ehrsystem.sendsITI8withLocalID &&
				ehrsystem.globalAssigningAuthority.equals(globalAssigningAuthority) &&
				!laaTable.containsKey(sourceLocalAssigningAuthority)) {

				if (connectionInterval > 0) {
					try { Thread.sleep(connectionInterval); }
					catch (Exception ignore) { }
				}

                sendPatientIdFeedWithLocalID( reg, localID,
                                  sourceLocalAssigningAuthority);

                //Put it in the table so we don't send this local ID again.
                laaTable.put(sourceLocalAssigningAuthority, localID);
                /*
				if (sendITI8WithLocalID(new HL7ITI8(), fields, reg,
							localID, sourceLocalAssigningAuthority )) {

					//Put it in the table so we don't send this local ID again.
					laaTable.put(sourceLocalAssigningAuthority, localID);
				}
                */
			}
		}
	}

    private void sendPatientIdFeedWithLocalID(Registration reg,
                                              String localID,
                                              String assigningAuthority)
    {
		Configuration config = Configuration.getInstance();
        PatientIdFeed pif
            = IHETransactionFactory.getPatientIdFeedInstance(
                      el, reg, config);
        pif.setAssigningAuthority( assigningAuthority);
        pif.setPatientId( localID);

        IHETransactionResponse response = pif.send();

        RegSysEvent event =
            new RegSysEvent(
                this,
                response.getStatusInt(),
                RegSysEvent.TYPE_HL7,
                pif.getName() +" sent to "+type + " ["+id+"]" +
                "<br>Local ID: "+localID +
                "<br>Local AssigningAuthority: " + assigningAuthority +
                "<br>Message:<br>" +
                StringUtil.displayable(pif.toString()) +
                "Response: "+StringUtil.displayable(response.getStatusString()));
        config.getEventLog().append(event);
    }

	public String getType() {
		return type;
	}

	public Element getXML() {
		try {
			Element e = super.getXML();
			e.setAttribute("connectionInterval", Integer.toString(connectionInterval));
			return e;
		}
		catch (Exception ex) { return null; }
	}

	public static Element createNewElement(String name, String id) {
		try {
			Element e = Product.createNewElement(name, id);
			e.setAttribute("type", "PIXMgr");
			return e;
		}
		catch (Exception ex) { return null; }
	}
}
