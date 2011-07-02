/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.elements;

import org.w3c.dom.Element;
import org.rsna.geneva.main.Configuration;

public class Registry extends PIXMgr {

	public String type = "Registry";

	public Registry(Element el) {
		super(el);
	}

	public String getType() {
		return type;
	}

	public static Element createNewElement(String name, String id) {
		try {
			Element e = Product.createNewElement(name, id);
			e.setAttribute("type", "Registry");
			return e;
		}
		catch (Exception ex) { return null; }
	}
}