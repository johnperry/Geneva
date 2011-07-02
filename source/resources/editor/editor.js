var elementsItems = new Array(
		new Item("New PIXMgr", newElement, "newpixmgr"),
		new Item("New Registry", newElement, "newregistry"),
		new Item("New PDQMgr", newElement, "newpdqmgr"),
		new Item("New Repository", newElement, "newrepository"),
		new Item("New EHRSystem", newElement, "newehrsystem"),
		new Item("New DCMSystem", newElement, "newdcmsystem"),
		new Item("New Study", newElement, "newstudy"),
		new Item("New DocSet", newElement, "newdocset"),
		new Item("", null),
		new Item("Delete", deleteElement, "delete"),
		new Item("", null),
		new Item("Close", closeHandler) );


var helpItems = new Array (
		new Item("MIRC Wiki", showWiki) );

var elementsMenu = new Menu("Create New Elements", elementsItems, "elementsmenu");
var helpMenu = new Menu("Help", helpItems);

var menuBar = new MenuBar("menuBar", new Array (elementsMenu, helpMenu));
var treeManager;

window.onload = load;
window.onresize = resize;

var currentNode = null;
var currentPath = "";
var currentTagName = "";
var closeboxURL = "/closebox.gif";

var user;
var split;

function load() {
	user = new User();
	setPageHeader("Geneva Configuration Editor",
					user.name, closeboxURL, closeHandler,
					"Return to the main page");
	menuBar.display();
	split = new HorizontalSplit("left", "center", "right", true);
	resize();
	treeManager =
		new TreeManager(
			"left",
			"/editor/tree",
			"/plus.gif",
			"/minus.gif");
	treeManager.load();
	treeManager.display();
	if (openpath != "") currentNode = treeManager.expandPath(openpath);
	//else treeManager.expandAll();
	if (currentNode != null) {
		treeManager.closePaths();
		currentNode.showPath();
	};
	setEnables();
}

function setEnables() {
	menuBar.setEnable("delete", currentNode && (currentNode.name != "Server") && getSavePermission());
}

//Handlers for tree selection
//
function openElement(event) {
	var source = getSource(getEvent(event));
	currentNode = source.treenode;
	currentPath = currentNode.getPath();
	displayElement(currentPath);
	treeManager.closePaths();
	currentNode.showPath();
	menuBar.setText(currentPath);
}

function displayElement(path) {
	var req = new AJAX();
	req.GET("/editor/openElement", "path="+path+"&"+req.timeStamp(), null);
	if (req.success()) {
		var right = document.getElementById("right");
		while (right.firstChild) right.removeChild(right.firstChild);
		var xml = req.responseXML();
		var root = xml ? xml.firstChild : null;
		if (root != null) displayConfigElement(right, root);
		setEnables();
		return;
	}
	alert("The attempt to load the element contents failed.");
}

function getEditPermission(id) {
	var userString = "_" + user.name.toLowerCase() + "_";
	return user.hasRole("admin") || (id && id.toLowerCase().indexOf( userString ) != -1);
}

function getSavePermission() {
	var userString = "_" + user.name.toLowerCase() + "_";
	return user.hasRole("admin") ||
			(currentNode  && (currentNode.name.toLowerCase().indexOf( userString ) != -1));
}

function save() {
	var xml = getSavePermission() ? getCurrentNodeText() : "";
	var req = new AJAX();
	req.GET("/editor/save", "xml="+encodeURIComponent(xml) + "&"+req.timeStamp(), null);
	if (req.success()) {
		var result = req.responseText();
		if (result == "OK") alert("The configuration was saved successfully.");
		else alert("The attempt to save the configuration failed.\n"+result);
	}
	else alert("The attempt to save the configuration failed.");
}

function deleteElement() {
	if (currentNode && (currentNode.name != "Server") && getSavePermission()) {
		var req = new AJAX();
		req.GET("/editor/deleteElement", "path="+currentPath + "&"+req.timeStamp(), null);
		if (req.success()) {
			var result = req.responseText();
			if (result == "OK") {
				var right = document.getElementById("right");
				while (right.firstChild) right.removeChild(right.firstChild);
				var state = treeManager.getState();
				treeManager.load();
				treeManager.display(state);
				currentNode = null;
				currentPath = null;
				treeManager.closePaths();
				menuBar.setText("");
				return;
			}
		}
	}
	alert("Unable to delete the current element.");
}

function displayConfigElement(right, el) {
	currentTagName = el.tagName;
	var edit = getEditPermission(el.getAttribute("id"));
	if (edit) insertSaveButton(right);
	switch (el.tagName) {
		case 'config':
			showServerParams(right, el, edit);
			break;

		case 'pdqmgr':
			showDataSystemParams(right, el, edit, sysparams1, messages1);
			break;

		case 'pixmgr':
		case 'registry':
			showDataSystemParams(right, el, edit, sysparams2, messages1);
			break;

		case 'repository':
			showRepositoryParams(right, el, edit);
			break;

		case 'ehrsystem':
			showDataSystemParams(right, el, edit, sysparams3, messages2);
			showSpecialAttributes(right, el, edit, new Array("pointOfCare"));
			break;

		case 'dcmsystem':
			showDataSystemParams(right, el, edit, sysparams4, messages3);
			showSpecialAttributes(right, el, edit, new Array( "pointOfCare", "institutionName" ));
			break;

		case 'study':
			showStudyParams(right, el, edit);
			break;

		case 'docset':
			showDocSetParams(right, el, edit);
			break;

		default:
			alert("Unknown ConfigElement type")
	};
	if (edit) insertSaveButton(right);
}

function insertSaveButton(parent) {
	var center = document.createElement("CENTER");
	var input = document.createElement("INPUT");
	input.type = "button";
	input.onclick = save;
	input.value = "Save";
	center.appendChild(input);
	parent.appendChild(center);
}

var sysparams1 = new Array(
				"id",
				"enabled",
				"startupDelay",
				"hl7Version",
				"hl7URL",
				"soapVersion",
				"globalAssigningAuthority");

var sysparams2 = new Array(
				"id",
				"enabled",
				"startupDelay",
				"hl7Version",
				"hl7URL",
				"soapVersion",
				"connectionInterval",
				"globalAssigningAuthority");

var sysparams3 = new Array(
				"id",
				"enabled",
				"startupDelay",
				"hl7Version",
				"hl7URL",
				"soapVersion",
				"globalAssigningAuthority",
				"localAssigningAuthority");

var sysparams4 = new Array(
				"id",
				"enabled",
				"startupDelay",
				"hl7Version",
				"hl7URL",
				"soapVersion",
				"dcmURL",
				"repositoryID",
				"retrieveAET",
				"globalAssigningAuthority",
				"localAssigningAuthority");

var messages1 = new Array(
				"acceptsITI8withGlobalID",
				"acceptsITI8withLocalID" );

var messages2 = new Array(
				"acceptsRAD1",
				"acceptsITI8withGlobalID",
				"acceptsITI8withLocalID",
				"acceptsMessages",
				"sendsITI8withLocalID");

var messages3 = new Array(
				"acceptsRAD1",
				"acceptsRAD4",
				"acceptsITI8withGlobalID",
				"acceptsITI8withLocalID",
				"acceptsMessages",
				"sendsITI8withLocalID",
				"sendsKOS");

function showServerParams(right, el, edit) {
	insertTable("System Parameters",
				right,
				el,
				new Array(
					"serverPort",
					"logDepth",
					"askOnClose" ),
				edit);
	insertTable("Default IDs",
				right,
				el,
				new Array(
					"uidRoot",
					"globalAssigningAuthority",
					"localAssigningAuthority" ),
				edit);
	insertTable("HL7 V3 Parameters",
				right,
				el,
				new Array(
					"senderDeviceId",
					"senderDeviceName" ),
				edit);
}

function showDataSystemParams(right, el, edit, sysparams, messages) {
	insertTable("System Parameters",
				right,
				el,
				sysparams,
				edit);
	insertTable("HL7 V2 Parameters",
				right,
				el,
				new Array(
					"receivingApplication",
					"receivingFacility" ),
				edit);
	insertTable("HL7 V3 Parameters",
				right,
				el,
				new Array(
					"receiverDeviceId",
					"receiverDeviceName" ),
				edit);
	insertTable("Message Enables",
				right,
				el,
				messages,
				edit);
}

function showSpecialAttributes(right, el, edit, attributes) {
	insertTable("Special Parameters",
				right,
				el,
				attributes,
				edit);
}

function showEHRSystemParams(right, el, edit, includeConnectionInterval) {
	var sysParams =	new Array( "id", "enabled", "startupDelay", "hl7Version", "soapVersion" );
	if (includeConnectionInterval) sysParams[sysParams.length] = "connectionInterval";
	insertTable("System Parameters",
				right,
				el,
				sysParams,
				edit);
	insertTable("HL7 V2 Parameters",
				right,
				el,
				new Array(
					"receivingApplication",
					"receivingFacility" ),
				edit);
	insertTable("HL7 V3 Parameters",
				right,
				el,
				new Array(
					"receiverDeviceId",
					"receiverDeviceName" ),
				edit);
	insertTable("ITI8 Enables",
				right,
				el,
				new Array(
					"acceptsITI8withGlobalID",
					"acceptsITI8withLocalID" ),
				edit);
}

function showRepositoryParams(right, el, edit) {
	insertTable("System Parameters",
				right,
				el,
				new Array(
					"id",
					"enabled",
					"startupDelay",
					"soapVersion",
					"sendsSOAP",
					"docsetDelay",
					"globalAssigningAuthority" ),
				edit);
}

function showStudyParams(right, el, edit) {
	insertTable("System Parameters",
				right,
				el,
				new Array(
					"id",
					"enabled",
					"directory",
					"dcmsystemID"),
				edit);
	insertTable("Study Parameters",
				right,
				el,
				new Array(
					"date",
					"description",
					"bodyPartExamined"),
				edit);
	insertTable("Requested Procedure Code Sequence Parameters",
				right,
				el,
				new Array(
					"rpcsCodeValue",
					"rpcsCodingSchemeDesignator",
					"rpcsCodingSchemeVersion",
					"rpcsCodeMeaning"),
				edit);
	insertTable("Anatomic Region Code Sequence Parameters",
				right,
				el,
				new Array(
					"arcsCodeValue",
					"arcsCodingSchemeDesignator",
					"arcsCodingSchemeVersion",
					"arcsCodeMeaning"),
				edit);
	insertTable("Message Parameters",
				right,
				el,
				new Array(
					"placerOrderAuthority",
					"fillerOrderAuthority",
					"enteringOrganization",
					"procedureCode",
					"localProcedureCode"),
				edit);
}

function showDocSetParams(right, el, edit) {
	insertTable("System Parameters",
				right,
				el,
				new Array(
					"id",
					"type",
					"enabled",
					"directory",
					"repositoryID"),
				edit);
	insertTable("Document Parameters",
				right,
				el,
				new Array(
					"title",
					"date",
					"institutionName",
					"sex"),
				edit);
}

function insertTable(title, right, el, params, edit) {
	var p = document.createElement("P");
	p.appendChild(document.createTextNode(title));
	right.appendChild(p);
	var table = document.createElement("TABLE");
	var tbody = document.createElement("TBODY");
	table.appendChild(tbody);
	for (var i=0; i<params.length; i++) {
		var tr = document.createElement("TR");

		tbody.appendChild(tr);
		var td = document.createElement("TD");
		td.className  = "left";
		td.appendChild(document.createTextNode(params[i]));
		tr.appendChild(td);

		td = document.createElement("TD");
		td.className  = "right";
		var value = el.getAttribute(params[i]);
		if (!edit || !isEditable(params[i])) {
			td.appendChild(document.createTextNode(value));
		}
		else {
			var input = document.createElement("INPUT");
			input.setAttribute("type", "text");
			input.className = "text";
			input.value = value;
			td.appendChild(input);
			var helptext = help[params[i]];
			if (helptext && (helptext != "")) {
				p = document.createElement("P");
				p.className = "help";
				p.appendChild(document.createTextNode(helptext));
				td.appendChild(p);
			}
		}
		tr.appendChild(td);
	}
	right.appendChild(table);
}

//Handler for the Elements Menu
//
function newElement(event, item) {
	var state = treeManager.getState();
	var req = new AJAX();
	var url = "/editor/newElement";
	var elementName = item.name.substring(3);
	var qs = "type=" + elementName;
	req.GET(url, qs + "&"+req.timeStamp(), null);
	if (req.success()) {
		var xml = req.responseXML();
		var root = xml ? xml.firstChild : null;
		if (root != null) {
			var type = root.getAttribute("type");
			treeManager.load();
			treeManager.display(state);
			var id = root.getAttribute("id");
			var path = type + "/" + id;
			displayElement(path);
			currentNode = treeManager.expandPath(path);
			currentPath = currentNode.getPath();
			treeManager.closePaths();
			currentNode.showPath();
			menuBar.setText(currentPath);
			return;
		}
	}
	alert("Unable to create a new "+elementName+" element.");
}

//Create the XML string representing the current node
function getCurrentNodeText() {
	var xml = "";
	if (currentNode && (currentTagName != "")) {
		xml += "<" + currentTagName + "\n";
		var right = document.getElementById("right");
		var tables = right.getElementsByTagName("TABLE");
		for (var i=0; i<tables.length; i++) {
			xml += getTableData(tables[i]);
		}
		xml += "/>";
	}
	return xml;
}

function getTableData(table) {
	var xml = "";
	var rows = table.getElementsByTagName("TR");
	for (var i=0; i<rows.length; i++) {
		xml += getRowData(rows[i]);
	}
	return xml;
}

function getRowData(row) {
	var xml = "";
	var cells = row.getElementsByTagName("TD");
	if (cells.length == 2) {
		var attrName = trim( cells[0].firstChild.nodeValue );
		var value = cells[1].firstChild;
		value = (value.nodeType == 3) ? value.nodeValue : value.value;
		xml = "  " + attrName + "=\"" + escapeChars(value) + "\"\n";
	}
	return xml;
}


//Handler for closing the page
//
function closeHandler() {
	hidePopups();
	window.open("/","_self");
}

//Handlers for the Help menu
//
function showWiki(event, item) {
	window.open("http://mircwiki.rsna.org/index.php?title=Geneva_-_The_IHE_Registration_System","help");
}

//Help text for the editor fields
var help = new Object();
help.acceptsITI8withGlobalID = "Whether Geneva is to send this system an ITI8 with a global ID (yes or no).";
help.acceptsITI8withLocalID = "Whether Geneva is to send this system an ITI8 with a local ID (yes or no).";
help.acceptsMessages = "Whether Geneva is to send specially formatted messages to the system (yes or no).";
help.acceptsRAD1 = "Whether Geneva is to send this system a RAD1 (yes or no).";
help.acceptsRAD4 = "Whether Geneva is to send this system a RAD4 (yes or no).";
help.askOnClose = "Confirm when shutting down the application (yes or no). ";
help.bodyPartExamined = "The text for the BodyPartExamined element in the instances of the study.";
help.connectionInterval = "The elapsed time between connections to this system (in msec).";
help.date = "(YYYYMMDD, or * to use today's date).";
help.dcmURL = "The URL of the system for DICOM communication (in the form dicom://DestinationAET:SenderAET@IP:port).";
help.dcmsystemID = "The ID of the DICOM System to which Geneva is to transmit the study.";
help.description = "The text for the StudyDescription element in the instances of the study.";
help.directory = "The directory in which the files for inclusion in this transmission are located.";
help.docsetDelay = "The time to wait after a registration before transmitting docsets to this system (in msec).";
help.enabled = "(yes or no) (do not change).";
help.globalAssigningAuthority = "";
help.hl7URL = "The URL of the system for HL7 communication (in the form http://IP:port).";
help.hl7Version = "The version of HL7 to use with this system (2 or 3)";
help.id = "The ID of this system (do not change).";
help.institutionName = "";
help.localAssigningAuthority = "";
help.logDepth = "The size of the circular buffer of the event log.";
help.repositoryID = "The ID of the repository to which to send a KOS.";
help.retrieveAET = "The AET to be supplied in messages for retrieving objects from the system.";
help.senderDeviceId = "";
help.senderDeviceName = "";
help.sendsITI8withLocalID = "Whether Geneva is to send an ITI8 with the local ID on behalf of this system to PIXMgrs in the same globalAssigningAuthority (yes or no).";
help.sendsKOS = "Whether Geneva is to send a KOS for each study to the specified repository on behalf of this system (yes or no).";
help.sendsSOAP = "Whether Geneva is to send DocSets to this system (yes or no).";
help.serverPort = "The port number of the web server (the standard port is 80).";
help.soapURL = "The URL to which Geneva is to send SOAP messages.";
help.soapVersion = "The version of SOAP to use with this system (SOAP_1_1 or SOAP_1_2)";
help.startupDelay = "The time to wait after a registration before processing events for this system (in msec).";
help.uidRoot = "The root string for all generated UIDs.";

//Edit suppression for certain editor fields
var doNotEdit = new Object();
doNotEdit.id = true;
doNotEdit.enabled = true;
doNotEdit.type = true;

function isEditable(field) {
	return user.hasRole("admin") || !doNotEdit[field];
}

//Useful functions
function trim(text) {
	if (text == null) return "";
	text = text.replace( /^\s+/g, "" );// strip leading
	return text.replace( /\s+$/g, "" );// strip trailing
}

function escapeChars(text) {
	return	text.replace(/&/g,"&amp;")
				.replace(/>/g,"&gt;")
				.replace(/</g,"&lt;")
				.replace(/\"/g,"&quot;")
				.replace(/'/g,"&apos;");
}
