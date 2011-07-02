/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.misc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Address {

	public String sex;
	public String street;
	public String city;
	public String state;
	public String zip;
	public String country;

	public Address(
				String sex,
				String street,
				String city,
				String state,
				String zip,
				String country ) {
		this.sex = setSex(sex);
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.country = country;
	}

	public Address(Element adrs) {
		sex = setSex(adrs.getAttribute("sex").trim());
		street = adrs.getAttribute("street").trim();
		city = adrs.getAttribute("city").trim();
		state = adrs.getAttribute("state").trim();
		zip = adrs.getAttribute("zip").trim();
		country = adrs.getAttribute("country").trim();
		if (country.equals("")) country = "US";
	}

	private String setSex(String sex) {
		sex = sex.toUpperCase();
		if (sex.equals("M") || sex.equals("F")) return sex;
		else return "";
	}

}