package engine;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

public class Friend {

	private Queue<String> deferredMessages = new LinkedList<String>();

	private int id;
	private String name = null;
	private Status status;
	private String firstName = null;
	private String lastName = null;
	private String country = null;
	private String city = null;
	private String email = null;
	private String birthDate = null;

	public Friend(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public void addDeferredMessage(String messageItself) {
		deferredMessages.add(messageItself);
	}

	public String getNextMessage() {
		return deferredMessages.poll();
	}

	public boolean haveDeferredMessages() {
		return !deferredMessages.isEmpty();
	}

	public void setFirstName(String fName) {
		firstName = fName;
	}

	public void setLastName(String lName) {
		lastName = lName;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setCity(String city) {
		this.city = city;
	}
	
	public void setEmail(String email) {
		this.email  = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getCountry() {
		return country;
	}

	public String getCity() {
		return city;
	}
	
	public String getEmail() {
		return email;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}
	
	public String getBirthDate() {
		return birthDate;
	}
	
	public String getAge() {
		String[] numbers = birthDate.split("\\^");
		int bDay = Integer.parseInt(numbers[0]);
		int bMonth = Integer.parseInt(numbers[1]);
		int bYear = Integer.parseInt(numbers[2]);
		if (bDay != 0 && bMonth != 0 && bYear != 0) {
			long currentTime = System.currentTimeMillis();
			Calendar now = Calendar.getInstance();
			now.setTimeInMillis(currentTime);
			int years = now.get(Calendar.YEAR) - bYear;
			int currMonth = now.get(Calendar.MONTH) + 1;
			int month = currMonth - bMonth;
			if (month < 0) {
				years--;
			}
			return String.valueOf(years);
		}
		return null;
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Friend other = (Friend) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
