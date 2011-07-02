/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.main;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;
import org.rsna.geneva.panels.*;
import org.rsna.geneva.misc.*;
import org.rsna.server.*;
import org.rsna.servlets.*;
import org.rsna.geneva.servlets.*;
import org.rsna.util.*;
import org.rsna.geneva.hl7.IHETransactionTransformerFactory;

/**
 * The Registration System program for IHE demonstrations,
 * connectathons, and internet tests.
 */
public class Geneva extends JFrame implements RegSysListener {

    private Configuration			config;
    private EventLog				eventLog;
    private RegistrationDatabase 	regDB;

    private StatusPanel				statusPanel;
    private ControlPanel			controlPanel;
    private BatchPanel				batchPanel;
    private ConfigurationPanel		configPanel;
    private LogPanel				logPanel;
    private HtmlJPanel 				helpPanel;

	private HttpServer				httpServer = null;

	static final File libraries = new File("libraries");
	static final String mainClass = "org.rsna.geneva.main.Geneva";
	static final String jarName = "Geneva.jar";

    private static final String	windowTitle = "Geneva: IHE Registration Server";
    private static final int version 		= 2010;

    public static final String log4jFN	= "log4j.properties";
    public static final String helpFN	= "ROOT/help.html";

    String[] userRoles = { "company" };

    public static Color background = Color.getHSBColor(0.5833f, 0.17f, 0.95f);

	@SuppressWarnings("unchecked")
    public static void main(String args[]) {
		//Make sure the libraries directory is present
		libraries.mkdirs();

		//Get a JarClassLoader pointing to this program plus the libraries directory
		JarClassLoader cl = JarClassLoader.getInstance(new File[] { new File(jarName), libraries });

		//Set the context classloader to the JarClassLoader
		Thread.currentThread().setContextClassLoader(cl);

		//Load the class and instantiate it
		try {
			Class theClass = cl.loadClass(mainClass);
			theClass.getConstructor( new Class[0] ).newInstance( new Object[0] );
		}
		catch (Exception unable) { unable.printStackTrace(); }
    }

	/**
	 * Class constructor; creates the program main class, loads the
	 * configuration, displays the GUI, and starts the HTTP Server.
	 */
    public Geneva() {

		File logs = new File("logs");
		logs.mkdirs();
		File logProps = new File("log4j.properties");
		PropertyConfigurator.configure(logProps.getAbsolutePath());

		config = Configuration.getInstance();
		Users.getInstance("org.rsna.server.UsersXmlFileImpl", userRoles);

		eventLog = new EventLog(config.getLogDepth());
		eventLog.addRegSysListener(this);
		config.setEventLog(eventLog);

		setTitle(windowTitle + " - version " + version);
		addWindowListener(new WindowCloser(this,config.getAskOnClose()));

        IHETransactionTransformerFactory ttf
            = new IHETransactionTransformerFactory(
                config.iti44XsltTemplateFile);

		//Create the UI.
		statusPanel = new StatusPanel();
		configPanel = new ConfigurationPanel();
		controlPanel = new ControlPanel();
		batchPanel = new BatchPanel();
		logPanel = new LogPanel(eventLog,background);
		helpPanel = new HtmlJPanel(new File(helpFN));
		MainPanel mainPanel =
			new MainPanel (
					statusPanel,
					configPanel,
					controlPanel,
					batchPanel,
					logPanel,
					helpPanel);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		pack();
		centerFrame();
		setVisible(true);

		//Start the server.
		startHttpServer(getServletSelector());
    }

	//Create the ServletSelector
	private ServletSelector getServletSelector() {
		ServletSelector selector = new ServletSelector(new File("ROOT"), false);
		selector.addServlet("users",		UserManagerServlet.class);
		selector.addServlet("user",			UserServlet.class);
		selector.addServlet("logs",			LogServlet.class);
		selector.addServlet("configuration",ConfigurationServlet.class);
		selector.addServlet("system",		SysPropsServlet.class);

		selector.addServlet("login",		LoginServlet.class);
		selector.addServlet("password",		PasswordServlet.class);
		selector.addServlet("editor",		ConfigEditorServlet.class);
		selector.addServlet("controlpanel",	ControlPanelServlet.class);
		selector.addServlet("dashboard",	DashboardServlet.class);
		selector.addServlet("pdq",			PDQServlet.class);
		selector.addServlet("registration",	RegistrationServlet.class);

		return selector;
	}

	/**
	 * The RegSysListener implementation.
	 */
    public void regsysEventOccurred(RegSysEvent event) {
		logPanel.updateText();
		statusPanel.setText(
			event.getTime() + " " + event.getType() + " event (" + event.getStatus() + ")");
	}

	//Set up and start the HTTP Server
	private void startHttpServer(ServletSelector selector) {
		int port = Configuration.getInstance().getServerPort();
		try {
			httpServer = new HttpServer(false, port, selector);
			httpServer.start();
			eventLog.append(
				RegSysEvent.getOKEvent(
					this,"HTTP Server started on port "+port));
		}
		catch (Exception ex) {
			eventLog.append(
				RegSysEvent.getErrorEvent(
					this,"Unable to create the HTTP Server on port "+port));
		}
	}

    //Position and size the application's JFrame on startup.
    private void centerFrame() {
		Toolkit t = getToolkit();
		Dimension scr = t.getScreenSize ();
		setSize(scr.width*7/10, scr.height*4/5);
		setLocation (
			new Point (
				(scr.width-getSize().width)/2,
				(scr.height-getSize().height)/2));
    }

	//GUI panels

	class MainPanel extends JPanel implements ChangeListener {
		JTabbedPane tabbedPane;
		LogPanel logPanel;
		ConfigurationPanel configPanel;

		public MainPanel(StatusPanel statusPanel,
						 ConfigurationPanel configPanel,
						 JPanel controlPanel,
						 JPanel batchPanel,
						 LogPanel logPanel,
						 JPanel helpPanel) {
			super();
			this.setLayout(new BorderLayout());
			this.add(statusPanel,BorderLayout.SOUTH);
			this.logPanel = logPanel;
			this.configPanel = configPanel;
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.addTab("Configuration", configPanel);
			tabbedPane.addTab("Control Panel", controlPanel);
			tabbedPane.addTab("Batch Registration", batchPanel);
			tabbedPane.addTab("Event Log", logPanel);
			tabbedPane.addTab("Help", helpPanel);
			this.add(tabbedPane,BorderLayout.CENTER);
			tabbedPane.addChangeListener(this);
		}
		public void stateChanged(ChangeEvent event) {
			Component c = tabbedPane.getSelectedComponent();
			if (c.equals(logPanel)) {
				logPanel.setDisplayed(true);
				logPanel.updateText();
			}
			else logPanel.setDisplayed(false);
			if (c.equals(configPanel)) {
				configPanel.display();
			}
		}
	}

	class StatusPanel extends JPanel {
		public JLabel status;
		public StatusPanel() {
			super();
			this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			this.setBackground(background);
			status = new JLabel("Ready");
			Font font = new Font(status.getFont().getFontName(),Font.BOLD,14);
			status.setFont(font);
			this.add(status);
		}
		public void setText(String text) {
			status.setText(text);
		}
	}

    //Class to capture a window close event and give the
    //user a chance to change his mind. This class also
    //saves the configuration on exit.
    class WindowCloser extends WindowAdapter {
		private Component parent;
		private boolean askOnClose;
		public WindowCloser(JFrame parent, boolean askOnClose) {
			this.parent = parent;
			this.askOnClose = askOnClose;
			parent.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}
		public void windowClosing(WindowEvent evt) {
			config.save();
			if (!askOnClose ||
				(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
												parent,
												"Are you sure you want to stop the program?",
												"Are you sure?",
												JOptionPane.YES_NO_OPTION))) {
				Logger.getLogger(Geneva.class).info("Normal Shutdown\n\n");
				System.exit(0);
			}
		}
    }

}
