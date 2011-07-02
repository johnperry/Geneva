/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.elements;

import org.rsna.geneva.elements.ConfigElement;
import org.rsna.geneva.misc.Registration;
import org.w3c.dom.Element;

/**
 * The class at the root of all systems.
 */
public abstract class Product extends ConfigElement {

	public int startupDelay;

	public Product(Element el) {
		super(el);
		String startupDelayString = el.getAttribute("startupDelay").trim();
		try { this.startupDelay = Integer.parseInt(startupDelayString); }
		catch (Exception ex) { this.startupDelay = 0; }
	}

	public void process(Registration reg, int priority) {
		ProcessingThread processor = new ProcessingThread(reg, priority);
		processor.start();
	}

	class ProcessingThread extends Thread {
		Registration reg;
		public ProcessingThread(Registration reg, int priority) {
			super();
			this.reg = reg;
			this.setPriority(priority);
		}
		public void run() {
			if (enabled) {
				if (startupDelay > 0) {
					try { sleep(startupDelay); }
					catch (Exception ignore) { }
				}
				processRegistration(reg);
			}
		}
	}

	abstract void processRegistration(Registration reg);

	public Element getXML() {
		try {
			Element e = super.getXML();
			e.setAttribute("startupDelay", Integer.toString(startupDelay));
			return e;
		}
		catch (Exception ex) { return null; }
	}

}

