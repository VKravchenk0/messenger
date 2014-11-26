package engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
/*Class that contains information about every single user and methods that are used to manipulate with user information in database*/
public class User {

	private static String dbName = "messenger";
	private static String dbUserName = null;
	private static String dbPassword = null;
	private static String url = "jdbc:mysql://localhost/mysql"
			+ "?autoReconnect=true&useUnicode=true&characterEncoding=utf8";

	private Queue<String> messages = new LinkedList<String>();
	private Integer id = null;
	private boolean authorized = false;
	private List<Integer> friendsIdList = new ArrayList<Integer>();
	private Status status;

	public int getID() {
		if (authorized) {
			return id;
		} else {
			return -1;
		}
		
	}

	public boolean isAuthorized() {
		return authorized;
	}

	public void addMessage(String msg) {
		messages.add(msg);
	}

	public String getNextMessage() {
		return messages.poll();
	}

	public boolean haveMessages() {
		return !messages.isEmpty();
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public Status getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "User [id=" + id + "]";
	}
	/**************/
	public static boolean initDBConnection(String dbUserName, String dbPassword) {
		User.dbUserName = dbUserName;
		User.dbPassword = dbPassword;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Cannot find driver");
		}
		boolean connected = checkDB();
		if (connected) {
			return true;
		} else {
			return false;
		}
	}

	// Checking if database exists. If not - creating it.
	private static boolean checkDB() {
		String update = "CREATE DATABASE IF NOT EXISTS " + dbName
				+ " CHARACTER SET utf8 COLLATE utf8_general_ci";

		String creatingUsersTable = "CREATE TABLE  IF NOT EXISTS `users` ("
				+ "  `id` int(6) NOT NULL auto_increment,"
				+ "  `name` varchar(30)," + "  `password` varchar(50),"
				+ "  `fName` varchar(30) default NULL,"
				+ "  `lName` varchar(30) default NULL,"
				+ "  `birth_date` date default NULL,"
				+ "  `country` varchar(30) default NULL,"
				+ "  `city` varchar(30) default NULL,"
				+ "  `email` varchar(50) default NULL,"
				+ "  `friends` text default NULL,"
				+ "  `deferredMessages` text default NULL,"
				+ "  PRIMARY KEY  (`id`)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

		String creatingFriendsRequestsTable = "CREATE TABLE IF NOT EXISTS `friendsRequests` ("
				+ "`initializer` int(6), `request` varchar(10)) engine=InnoDB DEFAULT CHARSET=utf8;";
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeUpdate(update);
			st.executeQuery("use " + dbName);
			st.executeUpdate(creatingUsersTable);
			st.executeUpdate(creatingFriendsRequestsTable);
			return true;
		} catch (SQLException e) {
			return false;
		}

	}

	public String registerUser(String name, String password) {

		String update = "INSERT INTO users " + "(`name`, `password`, `birth_date`) VALUES"
				+ "('" + name + "', '" + password + "', STR_TO_DATE('00,00,0000', '%d,%m,%Y'));";
		String addedIDQuery = "SELECT LAST_INSERT_ID()";

		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {

			st.executeQuery("use " + dbName);

			st.executeUpdate(update);

			ResultSet rs = st.executeQuery(addedIDQuery);

			while (rs.next()) {
				authorized = true;
				int addedID = rs.getInt(1);
				setID(addedID);
				return ((Integer) id).toString() + ":" + name;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "reject";
	}

	public String authorizeUser(int id, String password, Map<Integer, User> users) {
		if (users.containsKey(new Integer(id))) {
			return "userAlreadyConnected";
		}
		String query = "SELECT * FROM users WHERE id = " + id
				+ " and password = '" + password + "';";
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			ResultSet rs = st.executeQuery(query);
			while (rs.next()) {
				authorized = true;
				setID(id);
				String authorizationInfo = getAuthorizationInfo();
				if (authorizationInfo != null) {
					return authorizationInfo;
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "reject";

	}

	private void setID(int id) {
		this.id = id;
	}

	private String getAuthorizationInfo() {
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			ResultSet rs = st.executeQuery("SELECT * FROM users WHERE id = "
					+ id + ";");
			while (rs.next()) {
				// int id = rs.getInt("id");
				String name = rs.getString("name");
				String fName = rs.getString("fName");
				String lName = rs.getString("lName");
				String country = rs.getString("country");
				String city = rs.getString("city");
				String friends = rs.getString("friends");
				String returnedString = id + ":" + name + ":"
						+ (friends != null ? friends : "null") + ":";

				return returnedString;

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}

	private void clearDeferredMessages() {
		String update = "UPDATE `users` SET deferredMessages = NULL WHERE id = "
				+ id;
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public String getFriendsId() {
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			ResultSet rs = st.executeQuery("SELECT * FROM users WHERE id = "
					+ id + ";");
			while (rs.next()) {
				String friends = rs.getString("friends");
				if (friends != null) {
					String[] friendsArray = friends.split("\\^");
					StringBuilder returnedString = new StringBuilder(
							friends.length());
					for (String friend : friendsArray) {
						String friendId = friend.split("&")[0];
						returnedString.append(friendId + "^");
					}
					return returnedString.toString();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static String getUserInfo(int userId) {
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			ResultSet rs = st.executeQuery("SELECT *, DAY(birth_date), MONTH(birth_date), YEAR(birth_date) FROM users WHERE id = "
					+ userId + ";");
			while (rs.next()) {
				String name = rs.getString("name");
				String fName = rs.getString("fName");
				String lName = rs.getString("lName");
				String birthDay = rs.getString("DAY(birth_date)");
				String birthMonth = rs.getString("MONTH(birth_date)");
				String birthYear = rs.getString("YEAR(birth_date)");
				
				String country = rs.getString("country");
				String city = rs.getString("city");
				String email = rs.getString("email");
				String returnedString = userId + ":" + name + ":"
						+ (fName != null ? fName : "null") + ":"
						+ (lName != null ? lName : "null") + ":"
						+ birthDay + "^" + birthMonth + "^" + birthYear + ":"
						+ (country != null ? country : "null") + ":"
						+ (city != null ? city : "null") + ":"
						+ (email != null ? email : "null");
				return returnedString;
			}
			

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;

	}

	public static void addDeferredMessage(int id, String message) {
		String update = "UPDATE users SET deferredMessages"
				+ " = CONCAT_WS('', deferredMessages, '" + message
				+ "^') where id = " + id + ";";
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void makeFriends(int id1, String name1, int id2, String name2) {
		String friend1 = id1 + "&" + name1;
		String friend2 = id2 + "&" + name2;
		String update1 = "UPDATE users SET friends"
				+ " = CONCAT_WS('', friends, '" + friend1 + "^') where id = "
				+ id2 + ";";
		String update2 = "UPDATE users SET friends"
				+ " = CONCAT_WS('', friends, '" + friend2 + "^') where id = "
				+ id1 + ";";

		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);

			st.executeUpdate(update1);
			st.executeUpdate(update2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadDeferredMessages() {
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			ResultSet rs = st
					.executeQuery("SELECT deferredMessages FROM users WHERE id = "
							+ id + ";");
			while (rs.next()) {
				String deferredMessages = rs.getString("deferredMessages");
				if (deferredMessages != null && !deferredMessages.equals("")) {
					String[] splittedMessages = deferredMessages.split("\\^");
					for (String message : splittedMessages) {
						if (!message.equals("") && message != null) {
							addMessage(message);
						}
					}

				}

			}
			clearDeferredMessages();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void removeFriend(int userId, String friend) {

		String update = "UPDATE `users` SET `friends` = replace(`friends`, '"
				+ friend + "^', '') where id = " + userId + ";";

		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeUpdate("use " + dbName);
			st.executeUpdate(update);

			ResultSet rs = st
					.executeQuery("SELECT friends FROM users where id = "
							+ userId + ";");
			String friends = null;
			while (rs.next()) {
				friends = rs.getString("friends");
			}
			if (friends.equals("")) {
				st.executeUpdate("UPDATE users SET friends = null WHERE id = "
						+ userId + ";");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void reloadFriendList() {
		friendsIdList.clear();
		String friendsIdString = getFriendsId();
		String[] friendsArr = friendsIdString.split("\\^");
		int count = 0;
		int count2 = 0;
		for (String friendId : friendsArr) {
			count2++;
			if (friendId != null && !friendId.equals("")) {
				Integer id = Integer.parseInt(friendId);
				friendsIdList.add(id);
				count++;
			}
		}
	}

	public List<Integer> getFriendsIdList() {
		reloadFriendList();
		return friendsIdList;
	}

	/*
	 * returns next 20 users starting from startingId with specified name or
	 * first name or one user with specified id (if string `name` contains id)
	 */
	public static String searchUsers(String name, String startingId) {
		String users = null;
		Character firstChar = name.charAt(0);
		if (Character.isDigit(firstChar)) {
			// If user is searching by id
			int id = Integer.parseInt(name);
			users = searchUserById(id);
		} else {
			// If user is searching by name
			int stId = Integer.parseInt(startingId);
			users = searchUserByName(name, stId);
		}

		return users;
	}

	private static String searchUserByName(String name, int startingId) {

		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);

			ResultSet rs = st
					.executeQuery("SELECT id, name, fName, lName, DAY(birth_date), MONTH(birth_date), YEAR(birth_date), country, city, email FROM users WHERE (name = '"
							+ name
							+ "' or fName = '"
							+ name
							+ "') and id > "
							+ startingId + " limit 20;");
			StringBuilder sb = new StringBuilder();
			while (rs.next()) {
				String id = rs.getString("id");
				String nameFromDb = rs.getString("name");
				String fName = rs.getString("fName");
				String lName = rs.getString("lName");
				String birthDay = rs.getString("DAY(birth_date)");
				String birthMonth = rs.getString("MONTH(birth_date)");
				String birthYear = rs.getString("YEAR(birth_date)");
				String country = rs.getString("country");
				String city = rs.getString("city");
				String email = rs.getString("email");
				sb.append(id + ":" + nameFromDb + ":"
						+ (fName != null ? fName : "null") + ":"
						+ (lName != null ? lName : "null") + ":"
						+ birthDay + "^" + birthMonth + "^" + birthYear + ":"
						+ (country != null ? country : "null") + ":"
						+ (city != null ? city : "null") + ":"
						+ (email != null ? email : "null") + "&");

			}
			return sb.toString();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static String searchUserById(int id) {
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			ResultSet rs = st
					.executeQuery("SELECT name, fName, lName, DAY(birth_date), MONTH(birth_date), YEAR(birth_date), country, city, email FROM users WHERE id = "
							+ id + ";");
			StringBuilder sb = new StringBuilder();
			while (rs.next()) {
				String name = rs.getString("name");
				String fName = rs.getString("fName");
				String lName = rs.getString("lName");
				String birthDay = rs.getString("DAY(birth_date)");
				String birthMonth = rs.getString("MONTH(birth_date)");
				String birthYear = rs.getString("YEAR(birth_date)");
				String country = rs.getString("country");
				String city = rs.getString("city");
				String email = rs.getString("email");
				sb.append(id + ":" + name + ":"
						+ (fName != null ? fName : "null") + ":"
						+ (lName != null ? lName : "null") + ":"
						+ birthDay + "^" + birthMonth + "^" + birthYear + ":"
						+ (country != null ? country : "null") + ":"
						+ (city != null ? city : "null") + ":"
						+ (email != null ? email : "null") + "&");
			}
			return sb.toString();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;

	}

	/*
	 * Request is a string like "min(requesterId, requestedId):max(requesterId,
	 * requestedId)". The friendsRequest table in db consists of two columns:
	 * initializer (requester) and request
	 */
	public static boolean makeFriendRequest(int initializer, String request) {
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			String query = "SELECT initializer, request FROM friendsrequests WHERE request = '"
					+ request + "';";
			ResultSet rs = st.executeQuery(query);
			if (rs.first()) {
				int id = rs.getInt("initializer");
				if (id != initializer) {
					/*
					 * If request was not sent by the people who has already
					 * sent this request, we must make add those two users to one
					 * anoter's friends lists
					 */
					ResultSet rs1 = st
							.executeQuery("SELECT name FROM users WHERE id = "
									+ id + ";");
					String name1 = null;
					while (rs1.next()) {
						name1 = rs1.getString("name");
					}

					rs1 = st.executeQuery("SELECT name FROM users WHERE id = "
							+ initializer + ";");
					String name2 = null;
					while (rs1.next()) {
						name2 = rs1.getString("name");
					}
					String deleteQuery = "DELETE FROM friendsrequests WHERE request = '"
							+ request + "';";
					// Adding users to one another's friends list in database
					makeFriends(id, name1, initializer, name2);
					st.executeUpdate(deleteQuery);
					return true;

				}
			} else {
				// There are no such request in database
				String update = "INSERT INTO friendsrequests SET initializer = "
						+ initializer + ", request = '" + request + "';";
				st.executeUpdate(update);
			}

			return false;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public String getName() {
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			ResultSet rs = st.executeQuery("SELECT name FROM users WHERE id = "
					+ id + ";");
			while (rs.next()) {
				String name = rs.getString("name");
				return name;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String getFriendsRequests() {

		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			ResultSet rs = st
					.executeQuery("SELECT request FROM friendsrequests WHERE request like \"%"
							+ id + "%\";");
			StringBuilder requestsBuilder = new StringBuilder();
			while (rs.next()) {
				// Friend request is like "someId1:someId2", so we need to find all the requests with current user id.
				String request = rs.getString("request");
				// Now we need to remove current user's id from this string.
				String[] arrRequest = request.split(":");
				for (String id : arrRequest) {
					if (!id.equals(String.valueOf((int) this.id)) && id != null) {
						requestsBuilder.append(id + "^");
					}
				}
				if (requestsBuilder.toString().equals("")) {
					requestsBuilder.append("null");
				}
				// returned string is now looks like "id1^id2^id3^" or "null" if there are no requests.
			}
			if (!requestsBuilder.toString().equals("")) {
				/* Now we must add users names to the string, so the returned string will look like
				 * "id1&name1^id2&name2^id3&name3^"
				 */
				StringBuilder returnedString = new StringBuilder();
				String[] requestsIds = requestsBuilder.toString().split("\\^");
				for (String id : requestsIds) {
					int intId = Integer.parseInt(id);
					String friendName = getName(intId);
					returnedString.append(intId + "&" + friendName + "^");
				}
				return returnedString.toString();
			} 

		} catch (SQLException e) {
			return "null";
		}

		return "null";
	}

	public static String getName(int id) {
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			ResultSet rs = st.executeQuery("SELECT name FROM users WHERE id = "
					+ id + ";");
			while (rs.next()) {
				String name = rs.getString("name");
				return name;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void changeInformation(String remaining) {
		String[] information = remaining.split(":");
		String id = information[0];
		String name = information[1];
		String lName = information[2];
		String fName = information[3];
		String date = information[4];
		String country = information[5];
		String city = information[6];
		String email = information[7];
		
		String[] splittedDate = date.split("\\^");
		String birthDay = splittedDate[0];
		String birthMonth = splittedDate[1];
		String birthYear = splittedDate[2];
		String update = "UPDATE users SET name = '" + name + "', "
				+ "fName = '" + fName + "', "
				+ "lName = '" + lName + "', "
				+ "birth_date = STR_TO_DATE('" + birthDay + "," + birthMonth + "," + birthYear + "', '%d,%m,%Y'), "
				+ "country = '" + country + "', "
				+ "city = '" + city + "', "
				+ "email = '" + email + "' "
				+ "WHERE id=" + id +";";
		try (Connection connection = DriverManager.getConnection(url,
				dbUserName, dbPassword);
				Statement st = connection.createStatement()) {
			st.executeQuery("use " + dbName);
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}