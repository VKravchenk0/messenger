package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import engine.ClientEngine;
import engine.Friend;
import engine.Status;
import engine.User;

/*Class name speaks for itself. The main window that is opened right after 
* successful authorization or registration. Contains buttons that are leading to
* InformationSettingWindow or UserSearchWindow, contact list and status box
*/ 
public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private Icon onlineIcon = new ImageIcon(getClass().getResource(
			"/res/online.png"));
	private Icon offlineIcon = new ImageIcon(getClass().getResource(
			"/res/offline.png"));
	private Icon unfriendedIcon = new ImageIcon(getClass().getResource(
			"/res/unfriended.png"));
	private Icon afkIcon = new ImageIcon(getClass().getResource(
			"/res/afk.png"));

	// Кнопка поиска пользователей
	private JButton searchButton = new JButton("Search");
	private JButton settings = new JButton("Settings");
	private JButton userInfoButton = new JButton("User info");

	// Список контактов
	private JList<String> jlist = new JList<String>();
	private DefaultListModel<String> model = new DefaultListModel<String>();;

	// Всплывающее окно в списке контактов
	private JPopupMenu friendPopup = null;
	// Всплывающее окно для пользователей, которые еще не добавлены в друзья
	private JPopupMenu unfriendedPopup = null;

	private JComboBox<?> statusBox = null;
	
	// Индекс выбранного в списке пользователя
	private int index;
	private ClientEngine eng = null;
	/*private String name = null;
	private int id;*/
	private User user;
	private MyActionAdapter ma = new MyActionAdapter();

	private String[] statusValues = { "Online", "AFK", "Offline" };

	public MainWindow(User user, ClientEngine engine) {

		super(user.getId() + " : " + user.getName());
		jlist.setPrototypeCellValue("11111111111111111");
		eng = engine;
		this.user = user;
		if (!eng.getFriendList().isEmpty()) {
			loadJList();
		}
		
		loadPopupMenu();
		loadStatus();
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		userInfoButton.addActionListener(actionListener);
		searchButton.addActionListener(actionListener);
		JPanel mainPanel = new JPanel();
		Box mainBox = Box.createVerticalBox();
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalStrut(5));
		buttonBox.add(userInfoButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(searchButton);
		buttonBox.add(Box.createHorizontalStrut(5));

		mainBox.add(Box.createVerticalStrut(5));
		mainBox.add(buttonBox);
		mainBox.add(Box.createVerticalStrut(5));
		JScrollPane scrollPane = new JScrollPane(jlist);
		scrollPane.setPreferredSize(new Dimension(120, 250));
		mainBox.add(scrollPane);
		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(statusBox);

		mainPanel.add(mainBox);
		setContentPane(mainPanel);

		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane
						.showConfirmDialog(MainWindow.this,
								"Are you sure you want to exit?",
								"Really Closing?", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					System.exit(0);
				} 
			}
		});

		pack();
		setLocationByPlatform(true);
		setVisible(true);
		setResizable(false);
		

	}

	private ActionListener actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == searchButton) {
				eng.openUserSearchWindow();
			} else if (e.getSource() == settings) {
				System.out.println("Lately here will be settings function");
			} else if (e.getSource() == userInfoButton) {
				//[USERINFO]:user_who_made_request:another_user
				eng.openInformationSettingWindow();
				//eng.send("[USERINFO]:" + eng.getUser().getId() + ":" + eng.getUser().getId());
			}
			
		}
	};

	private class MyActionAdapter extends MouseAdapter {

		private Friend friend;

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && eng.friendsExists()) {
				int pos = jlist.locationToIndex(e.getPoint());
				friend = eng.getFriendList().get(pos);
				eng.createDialogWindow(user, friend);
				
				
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (eng.friendsExists()) {
				checkPopup(e);
			}
			
		}

		private void checkPopup(MouseEvent e) {
			if (e.isPopupTrigger()) { 
				index = jlist.locationToIndex(e.getPoint());
				jlist.setSelectedIndex(index); 
				friend = eng.getFriendList().get(jlist.getSelectedIndex());
				if (friend.getStatus() != Status.Unfriended) {
					friendPopup.show(jlist, e.getX(), e.getY()); 
				} else {
					unfriendedPopup.show(jlist, e.getX(), e.getY());
				}
			}
		}

	}

	ActionListener friendPopupListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			Friend f;
			f = eng.getFriendList().get(index);
			String command = event.getActionCommand();
			switch (command) {
				case "Write":
					//System.out.println("Write command: " + f.getName());
					break;
				case "Show information":
					//System.out.println("Show information command " + f.getName());
					eng.openFriendInfoWindow(f.getId());
					break;
				case "Remove":
					//System.out.println("Remove command " + f.getName());
					eng.removeFriend(f.getId());
					break;
				default:
					break;
			}
		}
	};
	
	ActionListener unfriendedPopupListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			Friend f;
			f = eng.getFriendList().get(index);
			String command = event.getActionCommand();
			switch (command) {
				case "Add":
					eng.makeFriendRequest(f.getId(), f.getName());
					break;
				case "Remove":
					//System.out.println("Remove command " + f.getName());
					eng.removeFriend(f.getId());
					break;
				default:
					break;
			}
		}
	};

	public void resetModel() {
		model.clear();
		for (Friend f : eng.getFriendList()) {
			model.addElement(f.getName());
		}
		jlist.setModel(model);
		jlist.repaint();
	}
	
	

	public void loadJList() {
		
		jlist.addMouseListener(ma);
		jlist.setCellRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component component = super.getListCellRendererComponent(list,
						value, index, isSelected, cellHasFocus);
				JLabel label = (JLabel) component;
				Friend friend = eng.getFriendList().get(index);
				
				Icon icon = null;
				
				if (friend.getStatus() == Status.Online) {
					icon = onlineIcon;
				} else if (friend.getStatus() == Status.Offline) {
					icon = offlineIcon;
				} else if (friend.getStatus() == Status.Unfriended) {
					icon = unfriendedIcon;
				} else if (friend.getStatus() == Status.AFK) {
					icon = afkIcon;
				}
				label.setIcon(icon);
				return label;
			}
		});
		resetModel();
		repaint();
	}

	private void loadPopupMenu() {
		friendPopup = new JPopupMenu();
		JMenuItem item;
		friendPopup.add(item = new JMenuItem("Write"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(friendPopupListener);
		friendPopup.add(item = new JMenuItem("Show information"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(friendPopupListener);
		friendPopup.add(item = new JMenuItem("Remove"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(friendPopupListener);

		friendPopup.setBorder(new BevelBorder(BevelBorder.RAISED));
		
		unfriendedPopup = new JPopupMenu();
		JMenuItem item1;
		unfriendedPopup.add(item1 = new JMenuItem("Add"));
		item1.setHorizontalTextPosition(JMenuItem.RIGHT);
		item1.addActionListener(unfriendedPopupListener);
		unfriendedPopup.add(item1 = new JMenuItem("Remove"));
		item1.setHorizontalTextPosition(JMenuItem.RIGHT);
		item1.addActionListener(unfriendedPopupListener);

		unfriendedPopup.setBorder(new BevelBorder(BevelBorder.RAISED));
	}

	private void loadStatus() {

		statusBox = new JComboBox<Object>(statusValues);
		statusBox.setSelectedItem(statusValues[0]);
		statusBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					final Object item = e.getItem();
					//System.out.println("Connected: " + eng.isConnected());
					//System.out.println("Current user`s status: " + eng.getUser().getStatus());
					User user = eng.getUser();
					if ((user.getStatus() == Status.Online || user.getStatus() == Status.AFK) && item.toString().equals("Offline")) {
						eng.setStartConnectionFlag(Status.Offline);
					} else if (user.getStatus() == Status.Online && item.toString().equals("AFK")) {
						user.setStatus(Status.AFK);
						//System.out.println("calling sendStatusNotification(AFK)");
						eng.sendStatusNotification("AFK");
						redrawStatusBox();
					} else if (user.getStatus() == Status.AFK && item.toString().equals("Online")) {
						user.setStatus(Status.Online);
						//System.out.println("calling sendStatusNotification(AFK)");
						eng.sendStatusNotification("Online");
						redrawStatusBox();
					} else if (user.getStatus() == Status.Offline && item.toString().equals("Online")) {
						eng.setStartConnectionFlag(Status.Online);
					} else if (user.getStatus() == Status.Offline && item.toString().equals("AFK")) {
						eng.setStartConnectionFlag(Status.AFK);
					}
				}
			}
		});
		statusBox.setRenderer(new DefaultListCellRenderer() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(
						list, value, index, isSelected, cellHasFocus);

				Icon icon = null;
				if (value.toString().equals("Online")) {
					icon = onlineIcon;
				} else if (value.toString().equals("Offline")) {
					icon = offlineIcon;
				} else if (value.toString().equals("AFK")) {
					icon = afkIcon;
				}
				label.setIcon(icon);
				return label;
			}
		});

	}

	public void redrawStatusBox() {
		if (eng.getUser().getStatus() == Status.Online) {
			statusBox.setSelectedItem(statusValues[0]);
		} else if (eng.getUser().getStatus() == Status.AFK){
			statusBox.setSelectedItem(statusValues[1]);
		} else if (eng.getUser().getStatus() == Status.Offline){
			statusBox.setSelectedItem(statusValues[2]);
		}
		statusBox.repaint();
	}

	public String getCurrentStatus() {
		
		return (String) statusBox.getSelectedItem();
	}

	public void setStatusBoxValue(Status value) {
		if (value == Status.Online) {
			statusBox.setSelectedIndex(0);
		} else if (value == Status.AFK) {
			statusBox.setSelectedIndex(1);
		}
		statusBox.repaint();
		
	}

	public String getSelectedStatus() {
		String selectedStatus = (String) statusBox.getSelectedItem();
		//System.out.println("SelectedStatus now is " + selectedStatus);
		return selectedStatus;
	}

}
