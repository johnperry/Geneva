/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.panels;

import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.elements.*;
import org.rsna.util.FileUtil;
import org.rsna.util.XmlUtil;

/**
 * A JPanel to provide a user interface for setting the system props.
 */
public class ControlPanel extends JPanel {

	SelectorPanel selectorPanel;
	FooterPanel footerPanel;
	Properties props;
	JScrollPane jsp;
	Profiles profiles;
	ProfileSaver profileSaver;
	ProfileLoader profileLoader;
	ProfileDeleter profileDeleter;

    /**
     * Class constructor; creates a user interface and loads it with values
     * from the props file.
     */
    public ControlPanel() {
		super();
		setLayout(new BorderLayout());
		Configuration config = Configuration.getInstance();
		config.setControlPanel(this);
		props = config.getProperties();
		profiles = new Profiles();
		profileLoader = new ProfileLoader();
		profileSaver = new ProfileSaver();
		profileDeleter = new ProfileDeleter();
		selectorPanel = new SelectorPanel();
		jsp = new JScrollPane();
		jsp.setViewportView(selectorPanel);
		this.add(jsp,BorderLayout.CENTER);
		footerPanel = new FooterPanel();
		this.add(footerPanel,BorderLayout.SOUTH);
		jsp.getVerticalScrollBar().setUnitIncrement(25);
		jsp.getVerticalScrollBar().setBlockIncrement(25);
    }

    public void reload() {
		this.remove(jsp);
		selectorPanel = new SelectorPanel();
		jsp.setViewportView(selectorPanel);
		this.add(jsp,BorderLayout.CENTER);
	}

	class Popup extends JPopupMenu {
		public Popup() {
			super();
			String[] profileNames = profiles.getNames();
			JMenuItem item;

			//Make the load menu
			JMenu loadMenu = new JMenu("Load profile");
			loadMenu.setEnabled(profiles.size() > 0);
			addProfiles(loadMenu, profileNames, profileLoader);
			this.add(loadMenu);
			this.addSeparator();

			//put in a dummy item
			item = new JMenuItem();
			item.setEnabled(false);
			this.add(item);
			this.addSeparator();

			//Make the save menu
			JMenu saveMenu = new JMenu("Save profile");
			item = new JMenuItem("New...");
			item.addActionListener(profileSaver);
			saveMenu.add(item);
			saveMenu.addSeparator();
			addProfiles(saveMenu, profileNames, profileSaver);
			this.add(saveMenu);
			this.addSeparator();

			//put in a dummy item
			item = new JMenuItem();
			item.setEnabled(false);
			this.add(item);
			this.addSeparator();

			//Make the delete menu
			JMenu deleteMenu = new JMenu("Delete profile");
			deleteMenu.setEnabled(profiles.size() > 0);
			addProfiles(deleteMenu, profileNames, profileDeleter);
			this.add(deleteMenu);
		}
		private void addProfiles(JMenu menu, String[] profileNames, ActionListener listener) {
			for (int i=0; i<profileNames.length; i++) {
				JMenuItem item = new JMenuItem(profileNames[i]);
				item.addActionListener(listener);
				menu.add(item);
			}
		}
	}

	class PopupListener extends MouseAdapter {
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				Popup popup = new Popup();
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	class Profiles {
		Hashtable<String,Profile> table;
		String filename = "profiles.xml";
		public Profiles() {
			table = new Hashtable<String,Profile>();
			load();
		}
		public String[] getNames() {
			String[] names = new String[table.size()];
			names = table.keySet().toArray(names);
			Arrays.sort(names);
			return names;
		}
		public Profile getProfile(String name) {
			Profile p = table.get(name);
			if (p == null) p = new Profile(name);
			return p;
		}
		public int size() {
			return table.size();
		}
		public void add(Profile profile) {
			table.put(profile.name, profile);
			save();
		}
		public void delete(String name) {
			table.remove(name);
			save();
		}
		private void load() {
			File file = new File(filename);
			if (file.exists()) {
				try {
					Document doc = XmlUtil.getDocument(file);
					Element root = doc.getDocumentElement();
					Node child = root.getFirstChild();
					while (child != null) {
						if (child.getNodeType() == Node.ELEMENT_NODE) {
							Profile p = new Profile((Element)child);
							table.put(p.name, p);
						}
						child = child.getNextSibling();
					}
				}
				catch (Exception ignore) { }
			}
		}
		private void save() {
			try {
				Document doc = XmlUtil.getDocument();
				Element root = doc.createElement("profiles");
				doc.appendChild(root);
				Enumeration<String> keys = table.keys();
				while (keys.hasMoreElements()) {
					table.get(keys.nextElement()).appendTo(root);
				}
				FileUtil.setFileText(
					new File(filename),
					FileUtil.utf8,
					XmlUtil.toString(doc));
			}
			catch (Exception ignore) { }
		}
	}

	class Profile {
		public String name;
		HashSet<String> ids;
		public Profile(Element el) {
			name = el.getAttribute("name");
			ids = new HashSet<String>();
			Node child = el.getFirstChild();
			while (child != null) {
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					ids.add( ((Element)child).getAttribute("id") );
				}
				child = child.getNextSibling();
			}
		}
		public Profile(String name) {
			this.name = name;
			ids = new HashSet<String>();
		}
		public void add(String id) {
			ids.add(id);
		}
		public boolean has(String id) {
			return ids.contains(id);
		}
		public void appendTo(Element el) {
			Document doc = el.getOwnerDocument();
			Element p = doc.createElement("profile");
			p.setAttribute("name",name);
			Iterator<String> it = ids.iterator();
			while (it.hasNext()) {
				Element child = doc.createElement("enable");
				child.setAttribute("id", it.next());
				p.appendChild(child);
			}
			el.appendChild(p);
		}
	}

	//The selector panel with a checkbox for each system.
	class SelectorPanel extends JPanel {
		public SelectorPanel() {
			super();
			Configuration config = Configuration.getInstance();
			this.setLayout(new ColumnLayout(20));
			this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

			add(new CPLabel("PIX Managers"));
			ConfigElement[] elements = config.getPIXMgrs();
			for (int i=0; i<elements.length; i++) {
				add(new CPCheckBox(elements[i]));
			}

			add(new CPLabel("Registries"));
			elements = config.getRegistries();
			for (int i=0; i<elements.length; i++) {
				add(new CPCheckBox(elements[i]));
			}

			add(new CPLabel("PDQ Managers"));
			elements = config.getPDQMgrs();
			for (int i=0; i<elements.length; i++) {
				add(new CPCheckBox(elements[i]));
			}

			add(new CPLabel("Repositories"));
			elements = config.getRepositories();
			for (int i=0; i<elements.length; i++) {
				add(new CPCheckBox(elements[i]));
			}

			add(new CRLF());

			add(new CPLabel("EHR Systems"));
			elements = config.getEHRSystems();
			for (int i=0; i<elements.length; i++) {
				add(new CPCheckBox(elements[i]));
			}

			add(new CPLabel("DCM Systems"));
			elements = config.getDCMSystems();
			for (int i=0; i<elements.length; i++) {
				add(new CPCheckBox(elements[i]));
			}

			add(new CPLabel("Studies"));
			elements = config.getStudies();
			for (int i=0; i<elements.length; i++) {
				add(new CPCheckBox(elements[i]));
			}

			add(new CPLabel("DocSets"));
			elements = config.getDocSets();
			for (int i=0; i<elements.length; i++) {
				add(new CPCheckBox(elements[i]));
			}

			add(new CPLabel("Messages"));
			elements = config.getMessages();
			for (int i=0; i<elements.length; i++) {
				add(new CPCheckBox(elements[i]));
			}

			//Set the MouseAdapter to catch popup triggers.
			this.addMouseListener(new PopupListener());
		}
	}

	class ProfileSaver implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof JMenuItem) {
				JMenuItem item = (JMenuItem)e.getSource();
				String name = item.getText();
				if (name.equals("New...")) {
					name = JOptionPane.showInputDialog(selectorPanel,"Enter a name for the new profile.");
				}
				if ((name == null) || name.trim().equals("")) return;
				Profile profile = new Profile(name);
				Component[] components = selectorPanel.getComponents();
				for (int i=0; i<components.length; i++) {
					Component comp = components[i];
					if (comp instanceof CPCheckBox) {
						CPCheckBox scb = (CPCheckBox)comp;
						String id = scb.element.id;
						if (scb.isSelected()) profile.add(id);
					}
				}
				profiles.add(profile);
			}
		}
	}

	class ProfileLoader implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof JMenuItem) {
				JMenuItem item = (JMenuItem)e.getSource();
				String name = item.getText();
				Profile profile = profiles.getProfile(name);
				Component[] components = selectorPanel.getComponents();
				for (int i=0; i<components.length; i++) {
					Component comp = components[i];
					if (comp instanceof CPCheckBox) {
						CPCheckBox scb = (CPCheckBox)comp;
						String id = scb.element.id;
						boolean enb = profile.has(id);
						scb.setState(enb);
					}
				}
			}
		}
	}

	class ProfileDeleter implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof JMenuItem) {
				JMenuItem item = (JMenuItem)e.getSource();
				String name = item.getText();
				profiles.delete(name);
			}
		}
	}

	class FooterPanel extends JPanel {
		public ControllerButton enbAll;
		public JButton disAll;
		public FooterPanel() {
			super();
			enbAll = new ControllerButton(true,"Enable All");
			disAll = new ControllerButton(false,"Disable All");
			this.add(enbAll);
			this.add(disAll);
		}
	}

	class ControllerButton extends JButton implements ActionListener {
		boolean enb;
		public ControllerButton(boolean enb, String label) {
			super(label);
			this.enb = enb;
			this.addActionListener(this);
		}
		public void actionPerformed(ActionEvent event) {
			Component[] components = selectorPanel.getComponents();
			for (int i=0; i<components.length; i++) {
				Component comp = components[i];
				if (comp instanceof CPCheckBox) {
					CPCheckBox scb = (CPCheckBox)comp;
					scb.setState(enb);
				}
			}
		}
	}

	Font labelFont = new Font("Dialog", Font.BOLD, 18);
	class CPLabel extends JLabel {
		public CPLabel(String text) {
			super(text);
			this.setForeground(Color.blue);
			this.setFont(labelFont);
		}
	}

	class CPCheckBox extends JCheckBox implements ActionListener {
		public ConfigElement element;
		public CPCheckBox(ConfigElement element) {
			super(element.id);
			this.element = element;
			String enabled = props.getProperty(element.id);
			setState((enabled == null) || !enabled.equals("false"));
			this.addActionListener(this);
		}
		public void actionPerformed(ActionEvent event) {
			setState(isSelected());
		}
		public void setState(boolean enb) {
			element.enabled = enb;
			setSelected(enb);
			props.setProperty(element.id, ""+enb);
			Configuration.getInstance().setChanged(true);
		}
	}

	class CRLF extends JComponent {
		public CRLF() {
			super();
			setVisible(false);
		}
	}

	//Layout Manager for the selector panel.
	class ColumnLayout implements LayoutManager {
		private int horizontalGap;

		public ColumnLayout(int horizontalGap) {
			this.horizontalGap = horizontalGap;
		}

		public void addLayoutComponent(String name,Component component) { }
		public void removeLayoutComponent(Component component) { }

		public Dimension preferredLayoutSize(Container parent) {
			return getLayoutSize(parent,horizontalGap,false);
		}

		public Dimension minimumLayoutSize(Container parent) {
			return getLayoutSize(parent,horizontalGap,false);
		}

		public void layoutContainer(Container parent) {
			getLayoutSize(parent,horizontalGap,true);
		}

		private Dimension getLayoutSize(Container parent, int hGap, boolean layout) {
			Insets insets = parent.getInsets();
			int lineMaxX = 0;
			int lineY = 0;
			int currentY = 0;
			int currentX = 0;
			int maxY = 0;
			int maxX = 0;
			int topMargin = 0;
			int leftMargin = 0;
			Dimension d;
			Component[] components = parent.getComponents();
			for (int i=0; i<components.length; i++) {
				if (components[i] instanceof CPLabel) {
					topMargin = 20;
					leftMargin = 0;
					currentY = lineY;
					currentX = lineMaxX + hGap;
					lineMaxX = currentX;
				}
				else if (components[i] instanceof CPCheckBox) {
					topMargin = 0;
					leftMargin = 0;
				}
				if (components[i] instanceof CRLF) {
					lineY = maxY;
					lineMaxX = 0;
				}
				else {
					//It's not a CRLF, lay it out.
					d = components[i].getPreferredSize();
					if (layout) {
						components[i].setBounds(
											insets.left + leftMargin + currentX,
											insets.top + topMargin + currentY,
											d.width,
											d.height);
					}
					currentY += topMargin + d.height;
					lineMaxX = Math.max(lineMaxX, leftMargin + currentX + d.width);
					maxX = Math.max(maxX, lineMaxX);
					maxY = Math.max(maxY, topMargin + currentY + d.height);
				}
			}
			return new Dimension(insets.left + maxX + insets.right,
								 insets.top + maxY + insets.bottom);
		}
	}

}
