package gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import engine.ClientEngine;
import engine.Friend;

/*The window that is (surprise!) used for searching users.
* Contains the found users list, typing field, search and close buttons
* and FriendInfoPanel for displaying the users information
*/
public class UserSearchWindow extends JFrame {

	private ClientEngine eng = null;
	
	private JButton startSearching = new JButton("Search");
	private JLabel searchLabel = new JLabel("Enter user name or id");
	private JTextField searchTextField = new JTextField(20);
	private FriendInfoPanel friendInfoPanel = new FriendInfoPanel();
	private JList<String> usersList = new JList<String>();
	private int searchFromId = 0;
	private String lastQuery = null;
	private int lastFoundUser = 0;
	int pos;
	private JPopupMenu popup = null;
	
	private DefaultListModel<String> model = new DefaultListModel<String>();
	
	public UserSearchWindow(ClientEngine eng) {
		super("Search User");
		this.eng = eng;
		createGUI();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setResizable(false);
		setVisible(true);
		
	}
	
	
	ActionListener actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == startSearching) {
				startSearch();		
			}
		}
	};

	
	
	private class MyKeyAdapter extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				e.consume();
				startSearch();
			}
		}
				
	}
	
	private void startSearch() {
		String name = searchTextField.getText();
		if (!name.equals(lastQuery)) {
			searchFromId = 0;
		} else {
			searchFromId = lastFoundUser;
		}
		lastQuery = name;
		eng.send("[USERSEARCH]:" + eng.getUser().getId() + ":" + name + ":" + searchFromId);
	}
	
	ActionListener menuListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			Friend f;
			f = eng.getSearchList().get(pos);
			String command = event.getActionCommand();
			switch (command) {
				case "Add to friends":
					if (eng.getUser().getId() != f.getId()) {
						eng.makeFriendRequest(f.getId(), f.getName());
						System.out.println("Adding: " + f.getName());
					}
					break;
				default:
					break;
			}
		}
	};

	

	private void createGUI() {
		
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				eng.resetSearchList();
			}
		});
		
		startSearching.addActionListener(actionListener);
		searchTextField.addKeyListener(new MyKeyAdapter());
		usersList.addMouseListener(new MyActionAdapter());
		JScrollPane scrollPane = new JScrollPane(usersList);

		usersList.setPrototypeCellValue("MEGADESTROYER, Grigoriy, 19, Vladivostok");
		scrollPane.setSize(new Dimension(200, 250));
		
		Box labelBox = Box.createHorizontalBox();
		labelBox.add(searchLabel);
		labelBox.add(Box.createHorizontalGlue());
		Box textFieldBox = Box.createHorizontalBox();
		textFieldBox.add(searchTextField);
		textFieldBox.add(Box.createHorizontalGlue());
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(startSearching);
		buttonBox.add(Box.createHorizontalGlue());
		Box jListBox = Box.createHorizontalBox();
		jListBox.add(scrollPane);
		jListBox.add(Box.createHorizontalGlue());
		
		Box leftBox = Box.createVerticalBox();
		leftBox.add(Box.createVerticalStrut(5));
		leftBox.add(labelBox);
		leftBox.add(Box.createVerticalStrut(5));
		leftBox.add(textFieldBox);
		leftBox.add(Box.createVerticalStrut(5));
		leftBox.add(buttonBox);
		leftBox.add(Box.createVerticalStrut(5));
		
		
		leftBox.add(jListBox);
		
		Box horBox = Box.createHorizontalBox();
		horBox.add(Box.createHorizontalStrut(5));
		horBox.add(leftBox);
		horBox.add(Box.createHorizontalStrut(10));
		horBox.add(friendInfoPanel);
		horBox.add(Box.createHorizontalStrut(5));
		JPanel panel = new JPanel();
		panel.add(horBox);
		
		loadPopupMenu();
		setContentPane(panel);
		
	}
	
	public void setLastFoundUser(int id) {
		lastFoundUser = id;
	}
	
	private void resetModel() {
		model.clear();
		for (Friend f : eng.getSearchList()) {
			String firstName = f.getFirstName();
			if (firstName.equals("null")) {
				firstName = "";
			} else {
				firstName = ", " + firstName;
			}
			String age = f.getAge();
			if (age == null) {
				age = "";
			} else {
				age = ", " + age;
			}
			String city = f.getCity();
			if (city.equals("null")) {
				city = "";
			} else {
				city = ", " + city;
			}
			model.addElement(f.getName() + firstName + age + city);
		}
		usersList.setModel(model);
	}
	
	private void loadPopupMenu() {
		popup = new JPopupMenu();
		JMenuItem item;
		popup.add(item = new JMenuItem("Add to friends"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		popup.setBorder(new BevelBorder(BevelBorder.RAISED));

	}
	
	
	private class MyActionAdapter extends MouseAdapter {

		private Friend friend;

		public void mouseClicked(MouseEvent e) {
			pos = usersList.locationToIndex(e.getPoint());
			friend = eng.getSearchList().get(pos);
			 if (e.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(e)) {
				friendInfoPanel.setValues(friend);
			}
		}

		public void mouseReleased(MouseEvent e) {
			checkPopup(e);
		}

		private void checkPopup(MouseEvent e) {
			if (e.isPopupTrigger()) { // if the event shows the menu
				pos = usersList.locationToIndex(e.getPoint());
				usersList.setSelectedIndex(pos); 
				friend = eng.getSearchList().get(usersList.getSelectedIndex());
				popup.show(usersList, e.getX(), e.getY()); // and show the menu
			}
		}

	}
	

	public void visualizeResult() {
		resetModel();
	}
	
}
