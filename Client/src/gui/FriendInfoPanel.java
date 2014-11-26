package gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import engine.Friend;

/* The panel which displays available information about selected user */
public class FriendInfoPanel extends JPanel {
	
	private static final long serialVersionUID = -24776903223680891L;
	
	private JLabel nameLabel, fNameLabel, lNameLabel, birthdayLabel, countryLabel, cityLabel, emailLabel;
	private JTextField nameField, fNameField, lNameField, birthDate, countryField, cityField, emailField;
	private Map<Integer, String> months = new HashMap<Integer, String>(13, (float) 1.1);
	
	public FriendInfoPanel() {
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
		birthDate = new JTextField(20);
		
		nameField.setEditable(false);
		fNameField.setEditable(false);
		lNameField.setEditable(false);
		countryField.setEditable(false);
		cityField.setEditable(false);
		emailField.setEditable(false);
		birthDate.setEditable(false);
		
		Box mainBox = Box.createHorizontalBox();
		Box leftBox = Box.createVerticalBox();
		Box rightBox = Box.createVerticalBox();
		
		int fieldStrut = 5;
		int labelStrut = 9;
		
		
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
		//leftBox.add(Box.createVerticalStrut(fieldStrut));
		
		rightBox.add(Box.createVerticalStrut(7));
		rightBox.add(nameField);
		rightBox.add(Box.createVerticalStrut(fieldStrut));
		rightBox.add(fNameField);
		rightBox.add(Box.createVerticalStrut(fieldStrut));
		rightBox.add(lNameField);
		rightBox.add(Box.createVerticalStrut(fieldStrut));
		rightBox.add(birthDate);
		rightBox.add(Box.createVerticalStrut(fieldStrut));
		rightBox.add(countryField);
		rightBox.add(Box.createVerticalStrut(fieldStrut));
		rightBox.add(cityField);
		rightBox.add(Box.createVerticalStrut(fieldStrut));
		rightBox.add(emailField);
		
		mainBox.add(Box.createHorizontalStrut(10));
		mainBox.add(leftBox);
		mainBox.add(Box.createHorizontalStrut(10));
		mainBox.add(rightBox);
		mainBox.add(Box.createHorizontalStrut(10));
		
		add(mainBox);

		setBorder(BorderFactory.createLoweredBevelBorder());
		
		months.put(0, " ");
		months.put(1, "January");
		months.put(2, "February");
		months.put(3, "March");
		months.put(4, "April");
		months.put(5, "May");
		months.put(6, "June");
		months.put(7, "July");
		months.put(8, "August");
		months.put(9, "September");
		months.put(10, "October");
		months.put(11, "November");
		months.put(12, "December");
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.add(new FriendInfoPanel());
		f.setVisible(true);
		f.pack();

	}

	public void setValues(Friend friend) {
		
		String name = friend.getName();
		String fName = friend.getFirstName();
		String lName = friend.getLastName();
		String birthDate = friend.getBirthDate();
		String country = friend.getCountry();
		String city = friend.getCity();
		String email = friend.getEmail();
		
		setValues(name, fName, lName, birthDate, country, city, email);
		
	}
	
	public void setValues(String values) {
		//id:name:fname:lname:day^month^year:country:city:email
		String[] info = values.split(":");
		String name = info[0];
		String fName = info[1];
		String lName = info[2];
		String birthDate = info[3];
		String country = info[4];
		String city = info[5];
		String email = info[6];
		
		setValues(name, fName, lName, birthDate, country, city, email);
		
	}

	private void setValues(String name, String fName, String lName, String birthDate, String country, String city, String email) {
		String bDateFieldVal = "";
		String[] bDateNumbers = birthDate.split("\\^");
		int bDay = Integer.parseInt(bDateNumbers[0]);
		int bMonth = Integer.parseInt(bDateNumbers[1]);
		int bYear = Integer.parseInt(bDateNumbers[2]);
		
		if (bDay != 0) {
			bDateFieldVal = String.valueOf(bDay) + " ";
		}
		bDateFieldVal = bDateFieldVal + months.get(bMonth);
		if (bYear != 0) {
			bDateFieldVal = bDateFieldVal + " " + bYear;
		}
		
		nameField.setText(name);
		
		if (!fName.equals("null")) {
			fNameField.setText(fName);
		} else {
			fNameField.setText("");
		}
		
		if (!lName.equals("null")) {
			lNameField.setText(lName);
		} else {
			lNameField.setText("");
		}
		
		this.birthDate.setText(bDateFieldVal);
		
		if (!country.equals("null")) {
			countryField.setText(country);
		} else {
			countryField.setText("");
		}
		
		if (!city.equals("null")) {
			cityField.setText(city);
		} else {
			cityField.setText("");
		}
		
		if (!email.equals("null")) {
			emailField.setText(email);
		} else {
			emailField.setText("");
		}
		
	}

}
