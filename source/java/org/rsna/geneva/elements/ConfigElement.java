/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.elements;

import org.rsna.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The abstract class at the root of all configuration elements (systems, studies, and docsets).
 */
public abstract class ConfigElement implements Comparable {

	public Element el;
	public String id;
	public boolean enabled;
	public String type = "ConfigElement";

	public ConfigElement(Element el) {
		this.el = el;
		this.id = el.getAttribute("id").trim();
	}

	public int compareTo(Object object) {
		return id.compareTo(((ConfigElement)object).id);
	}

	public String getType() {
		return type;
	}

	public void setEnable(boolean enb) {
		this.enabled = enb;
	}

	public Element getXML() {
		try {
			Document doc = XmlUtil.getDocument();
			Element root = doc.createElement(el.getTagName());
			doc.appendChild(root);
			root.setAttribute("id", id);
			root.setAttribute("enabled", yesNo(enabled));
			return root;
		}
		catch (Exception ex) { return null; }
	}

	public static Element createNewElement(String name, String id) {
		try {
			Document doc = XmlUtil.getDocument();
			Element root = doc.createElement(name);
			doc.appendChild(root);
			root.setAttribute("id", id);
			root.setAttribute("enabled", "no");
			return root;
		}
		catch (Exception ex) { return null; }
	}

	public static String yesNo(boolean b) {
		return (b ? "yes" : "no");
	}
}