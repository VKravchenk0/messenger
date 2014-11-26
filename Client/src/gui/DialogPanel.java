package gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.text.DefaultCaret;

import engine.ClientEngine;
import engine.Friend;
import engine.User;




public class DialogPanel extends JPanel{
	/* The dialog panel that is stored inside tab in the DialogWindow.
	*  The conversation with every friend has its own tab and dialog panel
	*  Contains dialog fields, buttons and friend info
	*/
	private static final long serialVersionUID = 8877916585121453634L;
	
	private User user;
	private Friend friend;
	private JTextArea conversationArea = new JTextArea(10, 50);
	private JTextArea typeArea = new JTextArea(7, 50);
	private JButton send = new JButton("Send");
	private JScrollPane topScrollPane;
	private JScrollPane bottomScrollPane;
	private JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false);
	private JLabel name;
	private JLabel id;
	private JLabel nameLabel;
	private ClientEngine eng;
	
	public DialogPanel(User user, Friend friend, ClientEngine eng){
		this.user = user;
		this.friend = friend; 
		this.eng = eng;
		Box vertBox = Box.createVerticalBox();
		Box horBox1 = Box.createHorizontalBox();
		Box horBox2 = Box.createHorizontalBox();
		Box horBox3 = Box.createHorizontalBox();
		nameLabel = new JLabel("Name");
		String friendName = friend.getFirstName();
		if (friendName != null) {
			name = new JLabel(friendName);
		} else {
			name = new JLabel(" ");
		}
		id = new JLabel(String.valueOf(friend.getId()));
		name.setFont(new Font("Dialog", Font.PLAIN, 14));
		name.setFont(new Font("Dialog", Font.PLAIN, 14));
		name.setPreferredSize(new Dimension(150, 30));
		name.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		id.setFont(new Font("Dialog", Font.PLAIN, 14));
		id.setPreferredSize(new Dimension(150, 30));
		id.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		typeArea.addKeyListener(new MyKeyAdapter());
		send.addActionListener(actionListener);
		conversationArea.setLineWrap(true);
		conversationArea.setWrapStyleWord(true);
		conversationArea.setEditable(false);
		typeArea.setLineWrap(true);
		typeArea.setWrapStyleWord(true);
		typeArea.setMaximumSize(new Dimension(600,400));
		typeArea.setFont(new Font("Dialog", Font.PLAIN, 14));
		conversationArea.setFont(new Font("Dialog", Font.PLAIN, 14));
		
		//Auto scroll to bottom after appending message
		DefaultCaret caret = (DefaultCaret) conversationArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		topScrollPane = new JScrollPane(conversationArea);
		topScrollPane.setPreferredSize(new Dimension(550, 200));
		topScrollPane.setSize(new Dimension(550, 200));
		topScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		topScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		bottomScrollPane = new JScrollPane(typeArea);
		bottomScrollPane.setPreferredSize(new Dimension(550, 150));
		bottomScrollPane.setSize(new Dimension(550, 150));
		bottomScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		bottomScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		splitPane.setTopComponent(topScrollPane);
		splitPane.setBottomComponent(bottomScrollPane);
		
		/* The panel consists of three horizontal boxes
		*  top box is for friend info
		*  middle is for text fields
		*  bottom box is for buttons
		*/
		vertBox.add(Box.createVerticalStrut(5));
		horBox1.add(Box.createHorizontalStrut(10));
		horBox1.add(nameLabel);
		horBox1.add(Box.createHorizontalStrut(15));
		horBox1.add(name);
		horBox1.add(Box.createHorizontalStrut(15));
		horBox1.add(id);
		horBox1.add(Box.createHorizontalGlue());
		vertBox.add(horBox1);
		vertBox.add(Box.createVerticalStrut(5));
		horBox2.add(Box.createHorizontalStrut(5));
		horBox2.add(splitPane);
		horBox2.add(Box.createHorizontalStrut(5));
		vertBox.add(horBox2);
		vertBox.add(Box.createVerticalStrut(5));
		horBox3.add(Box.createHorizontalStrut(5));
		horBox3.add(send);
		horBox3.add(Box.createHorizontalGlue());
		vertBox.add(horBox3);
		vertBox.add(Box.createVerticalStrut(5));
	
		add(vertBox);
		typeArea.requestFocus();
		
	}
	
	
	
	private class MyKeyAdapter extends KeyAdapter {
		
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				e.consume();
				doClick();
				
			}
		}
	}
	
	ActionListener actionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == send){
				doClick();
			}
		}
	};
	
	private void doClick() {
		sendText();
		
	}
	
	private void sendText(){
		String text = typeArea.getText();
		if (!text.equals("")) {
			eng.send("[MSG]:" + friend.getId() + ":" + user.getId() + "&" + text);
			conversationArea.append(user.getName() + " ("  + getCurrentDate() + ")" + "\n");
			conversationArea.append(text.trim() + "\n" + "\n");
			typeArea.setText("");
		}
		typeArea.requestFocus();
		
	}
	
	public void appendIncomingMessage(String message) {
		conversationArea.append(friend.getName() + " ("  + getCurrentDate() + ")" + "\n");
		conversationArea.append(message.trim() + "\n" + "\n");
	}
	
	private String getCurrentDate() {
		DateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yy");
		Calendar calobj = Calendar.getInstance();
		return df.format(calobj.getTime());
	}
	
	
}
