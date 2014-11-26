package gui;

import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import engine.ClientEngine;
import engine.Friend;
import engine.User;

/* The DialogWindow class contains JTabbedPane in which conversations(DialogPanels) are stored*/
public class DialogWindow extends JFrame {

	
	private static final long serialVersionUID = 4843272791874913599L;
	private User user;
	private ClientEngine eng;
	private Icon onlineIcon = new ImageIcon(getClass().getResource("/res/online.png"));
	private Icon offlineIcon = new ImageIcon(getClass().getResource("/res/offline.png"));
	
	private JTabbedPane tabbedPane = new JTabbedPane();
	
	private HashMap<Integer, DialogPanel> conversationList = new HashMap<Integer, DialogPanel>();
	
	// Number of currently opened tabs
	private int tabNumber = 0;
	
	public DialogWindow(User user, Friend friend, ClientEngine eng) {
				
		super("Dialog Window");
		this.user = user;
		this.eng = eng;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		add(tabbedPane);
		setLocationRelativeTo(null);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				conversationList.clear();
				resetTabNumber();
				tabbedPane.removeAll();
			}
		});
		
	}
	
	public void handleTabClosing(int i) {
		decreaseTabNumber();
		conversationList.remove(new Integer(i));
		if (tabbedPane.getTabCount() == 0) {
			dispose();
		}
	}

	public void increaseTabNumber() {
		tabNumber++;
	}

	public void decreaseTabNumber() {
		tabNumber--;
	}

	public void resetTabNumber() {
		tabNumber = 0;
	}

	private void initTabComponent(int i, JTabbedPane pane) {
		pane.setTabComponentAt(i, new ButtonTabComponent(pane, this));
	}

	public boolean containsConversationWith(Friend friend) {
		return conversationList.containsKey(new Integer(friend.getId()));
	}

	public void addConversation(Friend friend) {
		
		if (!conversationExists(new Integer(friend.getId()))) {
			DialogPanel dialogPanel = new DialogPanel(user, friend, eng);
			conversationList.put(friend.getId(), dialogPanel);
			tabbedPane.addTab(friend.getName(), dialogPanel);
			initTabComponent(tabNumber, tabbedPane);
			tabbedPane.setSelectedIndex(tabNumber);
			increaseTabNumber();
			eng.appendDeferredMessages(friend.getId());
		}
		
	}
	

	public boolean conversationExists(Integer friendId) {
		return conversationList.containsKey(friendId);
	}


	public void appendMessage(int senderId, String messageItself) {

		DialogPanel panel = conversationList.get(new Integer(senderId));
		panel.appendIncomingMessage(messageItself);
		
	}
	
}
