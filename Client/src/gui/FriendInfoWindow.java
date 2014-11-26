package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import engine.ClientEngine;
import engine.Friend;

/* The window that contains FriendInfoPanel and close button*/
public class FriendInfoWindow extends JFrame {
	
	JButton closeButton = new JButton("Close");
	FriendInfoPanel panel = new FriendInfoPanel();
	ClientEngine eng;
	
	public FriendInfoWindow(ClientEngine eng, int friendId) {
		super("Id" + friendId);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		closeButton.addActionListener(actionListener);
		
		Box mainBox = Box.createVerticalBox();
		mainBox.add(panel);
		mainBox.add(Box.createVerticalStrut(5));
		
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalStrut(5));
		buttonBox.add(closeButton);
		buttonBox.add(Box.createHorizontalGlue());
		
		mainBox.add(buttonBox);
		mainBox.add(Box.createVerticalStrut(5));
		
		JPanel mainPanel = new JPanel();
		mainPanel.add(mainBox);
		setContentPane(mainPanel);
		pack();
		setVisible(true);
		
		this.eng = eng;
		
		this.eng.send("[USERINFO]:" + eng.getUser().getId() + ":" + friendId);
	}
	
	ActionListener actionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	};
	
	public void setValues(String val) {
		
		panel.setValues(val);
	}

}
