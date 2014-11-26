package GUI;

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
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import engine.Server;



public class SetupWindow extends JFrame {

	private static final long serialVersionUID = 4406902271370887378L;
	private JLabel dbNameLabel = new JLabel("User name");
	private JLabel dbPasswordLabel = new JLabel("Password");
	private JTextField dbNameField = new JTextField(15);
	private JPasswordField dbPasswordField = new JPasswordField(15);
	
	private JLabel addressLabel = new JLabel("Address");
	private JLabel portLabel = new JLabel("Port");
	private JTextField addressField = new JTextField(15);
	private JTextField portField = new JTextField(15);
	
	private JButton ok = new JButton("OK");
	
	private ActionListener actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == ok) {
				createServer();
			}
		}
	};
	
	private class MyKeyAdapter extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				e.consume();
				if (e.getSource() == addressField) {
					portField.requestFocus();
				} else if (e.getSource() == portField) {
					dbNameField.requestFocus();
				} else if (e.getSource() == dbNameField) {
					dbPasswordField.requestFocus();
				} else if (e.getSource() == dbPasswordField) {
					createServer();
				} 
			}
		}
				
	}
	
	public SetupWindow() {
		super("Server setup");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		addressField.setText("127.0.0.1");
		portField.setText("8511");
		dbNameField.setText("root");
		
		ok.addActionListener(actionListener);
		
		MyKeyAdapter keyAdapter = new MyKeyAdapter();
		
		addressField.addKeyListener(keyAdapter);
		portField.addKeyListener(keyAdapter);
		dbNameField.addKeyListener(keyAdapter);
		dbPasswordField.addKeyListener(keyAdapter);
		
		TitledBorder connectionBorder = new TitledBorder("Connection Settings");
		TitledBorder dbBorder = new TitledBorder("Database Settings");
		
		Box connectionBox = Box.createHorizontalBox();
		Box connectionLabels = Box.createVerticalBox();
		Box connectionFields = Box.createVerticalBox();
		
		Box dbBox = Box.createHorizontalBox();
		Box dbLabels = Box.createVerticalBox();
		Box dbFields = Box.createVerticalBox();
		
		connectionLabels.add(Box.createVerticalStrut(5));
		connectionLabels.add(addressLabel);
		connectionLabels.add(Box.createVerticalStrut(5));
		connectionLabels.add(portLabel);
		connectionLabels.add(Box.createVerticalStrut(5));
		
		connectionFields.add(Box.createVerticalStrut(5));
		connectionFields.add(addressField);
		connectionFields.add(Box.createVerticalStrut(5));
		connectionFields.add(portField);
		connectionFields.add(Box.createVerticalStrut(5));
		
		connectionBox.add(Box.createHorizontalStrut(5));
		connectionBox.add(connectionLabels);
		connectionBox.add(Box.createHorizontalStrut(24));
		connectionBox.add(connectionFields);
		connectionBox.add(Box.createHorizontalStrut(5));
		
		JPanel connectionPanel = new JPanel();
		connectionPanel.add(connectionBox);
		
		connectionPanel.setBorder(connectionBorder);
		
		
		dbLabels.add(Box.createVerticalStrut(5));
		dbLabels.add(dbNameLabel);
		dbLabels.add(Box.createVerticalStrut(5));
		dbLabels.add(dbPasswordLabel);
		dbLabels.add(Box.createVerticalStrut(5));
		
		dbFields.add(Box.createVerticalStrut(5));
		dbFields.add(dbNameField);
		dbFields.add(Box.createVerticalStrut(5));
		dbFields.add(dbPasswordField);
		dbFields.add(Box.createVerticalStrut(5));
		
		dbBox.add(Box.createHorizontalStrut(5));
		dbBox.add(dbLabels);
		dbBox.add(Box.createHorizontalStrut(10));
		dbBox.add(dbFields);
		dbBox.add(Box.createHorizontalStrut(5));
		
		JPanel dbPanel = new JPanel();
		dbPanel.add(dbBox);
		
		dbPanel.setBorder(dbBorder);
		
		Box mainBox = Box.createVerticalBox();
		mainBox.add(Box.createVerticalStrut(5));
		mainBox.add(connectionPanel);
		mainBox.add(Box.createVerticalStrut(5));
		mainBox.add(dbPanel);
		mainBox.add(Box.createVerticalStrut(10));
		
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalStrut(5));
		buttonBox.add(ok);
		buttonBox.add(Box.createHorizontalGlue());
		
		mainBox.add(buttonBox);
		
		JPanel mainPanel = new JPanel();
		
		mainPanel.add(mainBox);
		
		setContentPane(mainPanel);
		
		pack();
		setVisible(true);
	}
	
	private void createServer() {
		String address = addressField.getText();
		String port = portField.getText();
		String dbName = dbNameField.getText();
		String dbPassword = new String(dbPasswordField.getPassword());
		if (address.equals("")) {
			showError("Address cannot be empty");
			return;
		}
		if (port.equals("")) {
			showError("Port cannot be empty");
			return;
		}
		int intPort = Integer.parseInt(port);
		if (intPort < 1024 || intPort > 65535) {
			showError("Port must be in range from 1025 to 65535");
			portField.setText("");
			return;
		}
		
		Server server = Server.createServer(address, intPort, dbName, dbPassword);
		
		if (server != null) {
			new Thread(server).start();
			dispose();
		} else {
			showError("Wrong database user name or password");
		}
		
		
	}
	
	public void showError(String text) {

		System.out.println(text);
		JOptionPane.showMessageDialog(this,
				text, "Error",
				JOptionPane.ERROR_MESSAGE);
		
	}
	
	

}
