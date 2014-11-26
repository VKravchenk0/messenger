package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import engine.ClientEngine;

public class AuthorizationWindow extends JFrame {

	private static final long serialVersionUID = 6969746126720183883L;
	private JPanel mainPanel, registrationPanel, authorizationPanel;
	private JButton confirmAuth, confirmReg, regState;
	private JTextField authIdField, regNameField;
	private JPasswordField authPasswordField, regPasswordField, confirmRegPasswordField;
	private JLabel authIDLabel, authPasswordLabel, regPasswordLabel, registrationLabel, confRegPasswordLabel, regNameLabel;
	private Box box, regBox;
	
	private boolean registrationOpened = false;
	private ClientEngine engine;
	
	
	public AuthorizationWindow(ClientEngine engine) {
		super("Welcome");
		this.engine = engine;
		mainPanel = new JPanel();
		registrationPanel = new JPanel();
		authorizationPanel = new JPanel();
		box = Box.createVerticalBox();
		
		
		// инициализация компонентов, относящихся к авторизации
		authIDLabel = new JLabel("Enter ID");
		authPasswordLabel = new JLabel("Enter password");
		authIdField = new JTextField(19);
		authPasswordField = new JPasswordField(19);
		confirmAuth = new JButton("OK");
		
		
		Box authBox = Box.createHorizontalBox();
		Box leftAuthBox = Box.createVerticalBox();
		Box rightAuthBox = Box.createVerticalBox();
		
		
		leftAuthBox.add(authIDLabel);
		leftAuthBox.add(Box.createVerticalStrut(5));
		leftAuthBox.add(authPasswordLabel);
		
		rightAuthBox.add(authIdField);
		rightAuthBox.add(Box.createVerticalStrut(5));
		rightAuthBox.add(authPasswordField);
		
		authBox.add(Box.createHorizontalStrut(5));
		authBox.add(leftAuthBox);
		authBox.add(Box.createHorizontalStrut(5));
		authBox.add(rightAuthBox);
		authBox.add(Box.createHorizontalStrut(5));
		
		registrationLabel = new JLabel("Registration");
		
		Box regLabelBox = Box.createHorizontalBox();
		regLabelBox.add(Box.createHorizontalGlue());
		regLabelBox.add(registrationLabel);
		regLabelBox.add(Box.createHorizontalGlue());
		
		// клавиша, открывающая/закрывающая панель регистрации
		regState = new JButton("Registration \u25BC");
		//regState = new JButton("Registration");
		
		
		Box buttonBox = Box.createHorizontalBox();
		
		buttonBox.add(Box.createHorizontalStrut(5));
		buttonBox.add(confirmAuth);
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(regState);
		buttonBox.add(Box.createHorizontalStrut(5));
		
		// инициализация компонентов, относящихся к регистрации
		regNameField = new JTextField(15);
		regPasswordField = new JPasswordField(15);
		confirmRegPasswordField = new JPasswordField(15);
		regNameLabel = new JLabel("Name");
		regPasswordLabel = new JLabel("Enter password");
		confRegPasswordLabel = new JLabel("Confirm password");
		confirmReg = new JButton("Register");
		
		regBox = Box.createVerticalBox();
		Box registerButtonBox = Box.createHorizontalBox();
		Box regFieldsBox = Box.createHorizontalBox();
		Box leftRegBox = Box.createVerticalBox();
		Box rightRegBox = Box.createVerticalBox();
		
		
		
		leftRegBox.add(regNameLabel);
		leftRegBox.add(Box.createVerticalStrut(5));
		leftRegBox.add(regPasswordLabel);
		leftRegBox.add(Box.createVerticalStrut(5));
		leftRegBox.add(confRegPasswordLabel);
		
		//leftRegBox.add(confirmReg);
		
		rightRegBox.add(regNameField);
		rightRegBox.add(Box.createVerticalStrut(5));
		rightRegBox.add(regPasswordField);
		rightRegBox.add(Box.createVerticalStrut(5));
		rightRegBox.add(confirmRegPasswordField);
		
		regFieldsBox.add(Box.createHorizontalStrut(5));
		regFieldsBox.add(leftRegBox);
		regFieldsBox.add(Box.createHorizontalStrut(5));
		regFieldsBox.add(rightRegBox);
		
		
		
		registerButtonBox.add(Box.createHorizontalStrut(5));
		registerButtonBox.add(confirmReg);
		registerButtonBox.add(Box.createHorizontalGlue());
		
		regBox.add(regLabelBox);
		regBox.add(Box.createVerticalStrut(10));
		regBox.add(regFieldsBox);
		regBox.add(Box.createVerticalStrut(5));
		regBox.add(registerButtonBox);
		
		
		box.add(Box.createVerticalStrut(5));
		box.add(authBox);
		box.add(Box.createVerticalStrut(10));
		box.add(buttonBox);
		//box.add(Box.createVerticalStrut(5));
		
		box.add(Box.createVerticalStrut(5));
		box.add(regBox);
		box.add(Box.createVerticalStrut(5));
		
		confirmAuth.addActionListener(actionListener);
		confirmReg.addActionListener(actionListener);
		regState.addActionListener(actionListener);
		
		authIdField.addKeyListener(new MyKeyAdapter());
		authPasswordField.addKeyListener(new MyKeyAdapter());
		regNameField.addKeyListener(new MyKeyAdapter());
		regPasswordField.addKeyListener(new MyKeyAdapter());
		confirmRegPasswordField.addKeyListener(new MyKeyAdapter());
		
		mainPanel.add(box);
		
		setContentPane(mainPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLocation(400,400);
		setResizable(false);
		regBox.setVisible(false);
		pack();
		setVisible(true);
		
		
		
		
	}
	
	
	ActionListener actionListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == regState) {
				changeState();
			} else if (e.getSource() == confirmAuth) {
				makeAuthorization();
			} else if (e.getSource() == confirmReg) {
				makeRegistration();
			}
			
		}

	};
	
	private class MyKeyAdapter extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				e.consume();
				if (e.getSource() == authIdField) {
					authPasswordField.requestFocus();
				} else if (e.getSource() == authPasswordField) {
					makeAuthorization();
				} else if (e.getSource() == regNameField) {
					regPasswordField.requestFocus();
				} else if (e.getSource() == regPasswordField) {
					confirmRegPasswordField.requestFocus();
				} else if (e.getSource() == confirmRegPasswordField) {
					makeRegistration();
				}
			}
		}
				
	}
	
	private void changeState() {

		if (!registrationOpened) {
			regState.setText("Registration \u25B2");
			registrationOpened = true;
			regBox.setVisible(true);
			pack();
		} else {
			regState.setText("Registration \u25BC");
			registrationOpened = false;
			regBox.setVisible(false);
			pack();
		}
		
	}
	
	
	protected void makeRegistration() {
		String name = regNameField.getText();
		if (name.length() < 3)  {
			regNameField.requestFocus();
			showError("Name length cannot be less than 3 characters");
			return;
		}
		if (name.length() > 30)  {
			regNameField.requestFocus();
			showError("Name length cannot be more than 30 characters");
			return;
		}
		if (Character.isDigit(name.charAt(0)))  {
			regNameField.requestFocus();
			showError("Name cannot start with a digit");
			return;
		}
		String password = new String(regPasswordField.getPassword());
		if (password.length() < 6)  {
			regPasswordField.requestFocus();
			showError("Password length cannot be less than 3 characters");
			return;
		}
		if (password.length() > 50)  {
			regPasswordField.requestFocus();
			showError("Password length cannot be more than 50 characters");
			return;
		}
		String passwordConfirmation = new String(confirmRegPasswordField.getPassword());
		if (!passwordConfirmation.equals(password)) {
			confirmRegPasswordField.requestFocus();
			showError("Passwords are not equal");
			return;
		}
		
		engine.makeRegistration(name, password);
		//engine.send("[REG]:" + name + ":" + password);
	}

	
	protected void makeAuthorization() {
		String id = authIdField.getText();
		if (id.equals(""))  {
			authIdField.requestFocus();
			return;
		}
		for (int i = 0; i < id.length(); i++) {
			if (!Character.isDigit(id.charAt(i))) {
				showError("ID cannot contain characters");
				authIdField.requestFocus();
				return;
			}
		}
		String password = new String(authPasswordField.getPassword());
		if (password.equals("")) {
			authPasswordField.requestFocus();
			return;
		}
		//engine.send("[AUTH]:" + id + ":" + password);
		engine.makeAuthorization(id, password);
		
	}
	
	public void showError(String text) {

		System.out.println(text);
		JOptionPane.showMessageDialog(this,
				text, "Error",
				JOptionPane.ERROR_MESSAGE);
		
	}
	
}
