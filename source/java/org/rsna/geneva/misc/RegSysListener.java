/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.misc;

import java.util.EventListener;

/**
 * The interface for listeners to RegSysEvents.
 */
public interface RegSysListener extends EventListener {

	public void regsysEventOccurred (RegSysEvent event);

}
