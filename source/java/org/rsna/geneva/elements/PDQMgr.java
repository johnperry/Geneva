/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.elements;

import org.w3c.dom.Element;
import org.rsna.geneva.main.Configuration;

public class PDQMgr extends DataSystem {

	public String type = "PDQ Manager";

	public PDQMgr(Element el) {
		super(el);
	}

	public String getType() {
		return type;
	}

	public static Element createNewElement(String name, String id) {
		try {
			Element e = Product.createNewElement(name, id);
			e.setAttribute("type", "PDQMgr");
			return e;
		}
		catch (Exception ex) { return null; }
	}
}