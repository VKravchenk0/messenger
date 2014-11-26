package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import engine.ClientEngine;

/*Current class incapsulates window in which user can change personal information*/
public class InformationSettingWindow extends JFrame {

	private static final long serialVersionUID = -5311518707773141516L;
	private JLabel nameLabel, fNameLabel, lNameLabel, birthdayLabel, countryLabel, cityLabel, emailLabel;
	private JTextField nameField, fNameField, lNameField, countryField, cityField, emailField;
	private JComboBox<String> birthDay, birthMonth, birthYear;
	private String[] day, year, biggerMonth, smallerMonth, leapFebruary, february;
	private String[] month = {"-", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	private JButton okButton, cancelButton;
	
	private ClientEngine eng;
	
	public InformationSettingWindow(ClientEngine eng) {
		super("Info");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.eng = eng;
		
		nameLabel = new JLabel("Name: ");
		fNameLabel = new JLabel("First name: ");
		lNameLabel = new JLabel("Last name: ");
		birthdayLabel = new JLabel("Birth date: ");
		countryLabel = new JLabel("Country: ");
		cityLabel = new JLabel("City: ");
		emailLabel = new JLabel("Email: ");
		
		nameField = new JTextField(20);
		fNameField = new JTextField(20);
		lNameField = new JTextField(20);
		countryField = new JTextField(20);
		cityField = new JTextField(20);
		emailField = new JTextField(20);
		
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		okButton.addActionListener(actionListener);
		cancelButton.addActionListener(actionListener);
		
		initializeBirthdateArrays();
		
		birthDay = new JComboBox<String>();
		birthMonth = new JComboBox<String>();
		birthYear = new JComboBox<String>();
		
		Box mainBox = Box.createHorizontalBox();
		Box leftBox = Box.createVerticalBox();
		Box rightBox = Box.createVerticalBox();
		
		int fieldStrut = 5;
		int labelStrut = 10;
		
		
		leftBox.add(Box.createVerticalStrut(6));
		leftBox.add(nameLabel);
		leftBox.add(Box.createVerticalStrut(labelStrut));
		leftBox.add(fNameLabel);
		leftBox.add(Box.createVerticalStrut(labelStrut));
		leftBox.add(lNameLabel);
		leftBox.add(Box.createVerticalStrut(labelStrut));
		leftBox.add(birthdayLabel);
		leftBox.add(Box.createVerticalStrut(labelStrut));
		leftBox.add(countryLabel);
		leftBox.add(Box.createVerticalStrut(labelStrut));
		leftBox.add(cityLabel);
		leftBox.add(Box.createVerticalStrut(labelStrut));
		leftBox.add(emailLabel);
		leftBox.add(Box.createVerticalStrut(2*fieldStrut));
		leftBox.add(okButton);
		
		day = biggerMonth;
		birthDay = new JComboBox<String>(day);
		birthMonth = new JComboBox<String>(month);
		birthYear = new JComboBox<String>(year);
		
		birthMonth.addActionListener(actionListener);
		birthYear.addActionListener(actionListener);
		
		Box birthDateBox = Box.createHorizontalBox();
		birthDateBox.add(birthDay);
		birthDateBox.add(Box.createHorizontalStrut(5));
		birthDateBox.add(birthMonth);
		birthDateBox.add(Box.createHorizontalStrut(5));
		birthDateBox.add(birthYear);
		
		rightBox.add(Box.createVerticalStrut(7));
		rightBox.add(nameField);
		rightBox.add(Box.createVerticalStrut(fieldStrut));
		rightBox.add(fNameField);
		rightBox.add(Box.createVerticalStrut(fieldStrut));
		rightBox.add(lNameField);
		rightBox.add(Box.createVerticalStrut(fieldStrut));
		rightBox.add(birthDateBox);
		rightBox.add(Box.createVerticalStrut(fieldStrut));
		rightBox.add(countryField);
		rightBox.add(Box.createVerticalStrut(fieldStrut));
		rightBox.add(cityField);
		rightBox.add(Box.createVerticalStrut(fieldStrut));
		rightBox.add(emailField);
		rightBox.add(Box.createVerticalStrut(2*fieldStrut));
		rightBox.add(cancelButton);
		
		mainBox.add(Box.createHorizontalStrut(10));
		mainBox.add(leftBox);
		mainBox.add(Box.createHorizontalStrut(10));
		mainBox.add(rightBox);
		mainBox.add(Box.createHorizontalStrut(10));
		
		JPanel mainPanel = new JPanel();
		mainPanel.add(mainBox);
		
		setContentPane(mainPanel);
		
		setVisible(true);
		setResizable(false);
		pack();
		eng.send("[USERINFO]:" + eng.getUser().getId() + ":" + eng.getUser().getId());
		
	}
	
	ActionListener actionListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof JComboBox) {
				int selectedDay = birthDay.getSelectedIndex();
				// index is a number of the month, where "-" have the number of zero, and january have 1
				int monthIndex = birthMonth.getSelectedIndex();
				String bYear = (String) birthYear.getSelectedItem();
				int birthYear;
				if (!bYear.equals("-")) {
					birthYear = Integer.parseInt(bYear);
				} else {
					birthYear = 0;
				}
				validateBirthDateBoxes(monthIndex, birthYear);
				birthDay.setModel(new DefaultComboBoxModel<String>(day));
				if (selectedDay > day.length - 1) {
					birthDay.setSelectedIndex(day.length - 1); 
				} else {
					birthDay.setSelectedIndex(selectedDay);
				}
				birthDay.repaint();
				
			}
			
			if (e.getSource() instanceof JButton) {
				JButton button = (JButton) e.getSource();
				if (button.equals(okButton)) {
					String birthDay = (String) InformationSettingWindow.this.birthDay.getSelectedItem();
					String birthMonth = (String) InformationSettingWindow.this.birthMonth.getSelectedItem();
					String birthYear = (String) InformationSettingWindow.this.birthYear.getSelectedItem();
					Calendar now = Calendar.getInstance();
					Calendar choosedDate = Calendar.getInstance();
					String date;
					if (!birthDay.equals("-")) {
						int day = Integer.parseInt(birthDay);
						choosedDate.set(Calendar.DAY_OF_MONTH, day);
						date = day + "^";
					} else {
						date = "00^";
					}
					
					
					
					if (!birthMonth.equals("-")) {
						int month = (int) InformationSettingWindow.this.birthMonth.getSelectedIndex();
						choosedDate.set(Calendar.MONTH, month - 1);
						date = date + month + "^";
					} else {
						date = date + "00^";
					}
					
					if (!birthYear.equals("-")) {
						int year = Integer.parseInt(birthYear);
						choosedDate.set(Calendar.YEAR, year);
						date = date + birthYear;
					} else {
						date = date + "0000";
					}
					
					if (choosedDate.after(now)) {
						showError("You can't sit here, you haven't even born yet");
						return;
					} 
					date = date + ":";					
					
					String name = nameField.getText();
					String fName = fNameField.getText();
					String lName = lNameField.getText();
					String country = countryField.getText();
					String city = cityField.getText();
					String email = emailField.getText();
					StringBuilder sb = new StringBuilder();
					
					if (name.equals("")) {
						sb.append("null");
					} else if (name.length() < 3) {
						showError("Name length cannot be less than 3 characters");
						return;
					} else if (name.length() > 30) {
						showError("Name length cannot be more than 30 characters");
						return;
					} else if (haveSpecialSymbols(name)) {
						showError("Name cannot contain symbols: ^&:");
						return;
					} else if (Character.isDigit(name.charAt(0)))  {
						showError("Name cannot start with a digit");
						return;
					}
					sb.append(name + ":");
					if (!fName.equals("")) {
						if (fName.length() > 30) {
							showError("First name length cannot be more than 30 characters");
							return;
						} else {
							sb.append(fName + ":");
						}
					} else {
						sb.append("null:");
					}
					
					if (!lName.equals("")) {
						if (lName.length() > 30) {
							showError("Last name length cannot be more than 30 characters");
							return;
						} else {
							sb.append(lName + ":");
						}
					} else {
						sb.append("null:");
					}
					
					sb.append(date);
					
					if (!country.equals("")) {
						if (country.length() > 30) {
							showError("Country name length cannot be more than 30 characters");
							return;
						} else {
							sb.append(country + ":");
						}
					} else {
						sb.append("null:");
					}
					
					if (!city.equals("")) {
						if (city.length() > 30) {
							showError("City name length cannot be more than 30 characters");
							return;
						} else {
							sb.append(city + ":");
						}
					} else {
						sb.append("null:");
					}
					
					if (!email.equals("")) {
						if (email.length() > 50) {
							showError("Email length cannot be more than 50 characters");
							return;
						} else if (!email.contains("@")) {
							showError("Email is invalid");
							return;
						} else {
							sb.append(email);
						}
					} else {
						sb.append("null");
					}
					
					String msg = sb.toString();
					//[INFOCHANGE]:13:Vasya:Vasiliiy:Petrov:1^June^2010:Ukraine:Kiev:lalka@mail.ru
					eng.send("[INFOCHANGE]:" + eng.getUser().getId() + ":" + msg);
					
				} else if (button.equals(cancelButton)) {
					dispose();
				}
			}
			
			
		}
		
	};
	
	private boolean haveSpecialSymbols(String s) {
		return (s.contains(":") || s.contains("^") || s.contains("&"));
	}
	
	public void showError(String text) {

		JOptionPane.showMessageDialog(this,
				text, "Error",
				JOptionPane.ERROR_MESSAGE);
		
	}
	
	
	
	private void initializeBirthdateArrays() {
		biggerMonth = new String[32];
		smallerMonth = new String[31];
		leapFebruary = new String[30];
		february = new String[29];
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		year = new String[71];
		year[0] = "-";
		biggerMonth[0] = "-";
		smallerMonth[0] = "-";
		leapFebruary[0] = "-";
		february[0] = "-";
		for (int i = year.length - 1; i >= 1; i--) {
			year[i] = Integer.toString(currentYear - i + 1);
		}
				
		for (int i = 1; i < biggerMonth.length; i++) {
			biggerMonth[i] = Integer.toString(i);
		}
		for (int i = 1; i < smallerMonth.length; i++) {
			smallerMonth[i] = Integer.toString(i);
		}
		for (int i = 1; i < leapFebruary.length; i++) {
			leapFebruary[i] = Integer.toString(i);
		}
		for (int i = 1; i < february.length; i++) {
			february[i] = Integer.toString(i);
		}
		
	}

	public void setValues(String val) {
		//name:fname:lname:day^month^year:country:city:email
		String[] values = val.split(":");
		String name = values[0];
		String fName = values[1];
		String lName = values[2];
		String birthDate = values[3];
		
		String[] bd = birthDate.split("\\^");
		String birthDay = bd[0];
		String birthMonth = bd[1];
		String birthYear = bd[2];
		
		String country = values[4];
		String city = values[5];
		String email = values[6];
		
		nameField.setText(name);
		
		if (!fName.equals("null")) {
			fNameField.setText(fName);
			fNameField.repaint();
		}
		
		if (!lName.equals("null")) {
			lNameField.setText(lName);
		}
		

		if (!country.equals("null")) {
			countryField.setText(country);
		}
		
		if (!city.equals("null")) {
			cityField.setText(city);
			cityField.repaint();
		}
		
		if (!email.equals("null")) {
			emailField.setText(email);
		}
		int bDay = Integer.parseInt(birthDay);
		int bMonth = Integer.parseInt(birthMonth);
		int bYear = Integer.parseInt(birthYear);
		if (bYear == 0) {
			this.birthYear.setSelectedIndex(bYear);
		} else {
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			int yearIndex = currentYear - bYear + 1;
			this.birthYear.setSelectedIndex(yearIndex);
		}
		
		this.birthMonth.setSelectedIndex(bMonth);

		validateBirthDateBoxes(bMonth, bYear);
		
		
		this.birthYear.repaint();
		this.birthMonth.repaint();
		this.birthDay.setModel(new DefaultComboBoxModel<String>(day));
		this.birthDay.setSelectedIndex(bDay);
		this.birthDay.repaint();
	
	}

	// Metod is used for setting correct number of days to birthDay comboBox with respect to current month and year
	private void validateBirthDateBoxes(int bMonth, int bYear) {
		if (bMonth == 1 || bMonth == 3 || bMonth == 5 || bMonth == 7 || bMonth == 8 || bMonth == 10 || bMonth == 12) {
			day = biggerMonth;
		} else if (bMonth == 4 || bMonth == 6 || bMonth == 9 || bMonth == 11) {
			day = smallerMonth;
		} else if (bMonth == 2) { // February
			if (bYear == 0) { // No year choosed
				day = leapFebruary;
			} else {
				if ((bYear % 4 == 0 && bYear % 100 != 0) || (bYear % 400 == 0)) {
					System.out.println("leap");
					day = leapFebruary;
				} else {
					System.out.println("not leap");
					day = february;
				}
			}
			
		}
		
	}

}
