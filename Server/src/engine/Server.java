package engine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import GUI.SetupWindow;

/*Class that incapsulates server thread which handles users connections and incoming messages*/
public class Server implements Runnable {

	private String address = null;
	private int port;
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private HashMap<Integer, User> users = new HashMap<Integer, User>();

	private Server(String address, int port, String dbUserName,
			String dbPassword) {
		this.address = address;
		this.port = port;
	}

	public static Server createServer(String address, int port,
			String dbUserName, String dbPassword) {
		Server server = new Server(address, port, dbUserName, dbPassword);

		// attempting to connect to database.
		boolean connectedToDB = User.initDBConnection(dbUserName, dbPassword);
		if (connectedToDB) {
			return server;
		} else {
			return null;
		}

	}

	public void run() {
		if (selector != null || serverChannel != null)
			return;

		try {
			selector = Selector.open();

			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(new InetSocketAddress(address, port));

			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Waiting for connections");

		SelectionKey currentKey = null;
		try {
			while (selector.isOpen()) {
				selector.select();

				Iterator<SelectionKey> it = selector.selectedKeys().iterator();

				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
					currentKey = key;
					if (!key.isValid())
						continue;

					if (key.isValid() && key.isAcceptable()) {
						// Accepting connection.
						accept(key);
						continue;
					}

					if (key.isValid() && key.isReadable()) {
						// Reading from client.
						read(key);
						continue;
					}

					// If key have attached user, then we checking if this user
					// have information to be send to. Otherwise we skip this
					// step.
					if (key.isValid()
							&& key.isWritable()
							&& (haveAttachedUser(key) ? ((User) key
									.attachment()).haveMessages() : false)) {
						// Writing to client.
						write(key);

					}

				}
			}
		} catch (IOException e) {
			System.out.println("IOE in run");
			disconnect(currentKey);
		} finally {
			closeConnection();
		}

	}

	private boolean haveAttachedUser(SelectionKey key) {
		return key.attachment() != null;
	}

	private void disconnect(SelectionKey key) {
		System.out.println("Disconnecting user: " + key.channel().toString());
		if (haveAttachedUser(key)) {
			Integer removedUserId = ((User) key.attachment()).getID();
			if (!removedUserId.equals(-1)) {
				sendStatusNotification(removedUserId, "Offline");
				users.remove(removedUserId);
			}
		}
		displayUsers();

		try {
			key.channel().close();
			key.cancel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
				.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);
		socketChannel.register(selector, SelectionKey.OP_READ
				| SelectionKey.OP_WRITE);

	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer readBuffer = ByteBuffer.allocate(2048);
		readBuffer.clear();
		int bytesRead;
		try {
			// Reading bytes to readBuffer.
			bytesRead = channel.read(readBuffer);
		} catch (IOException e) {
			System.out.println("IOEX in read");
			disconnect(key);
			return;
		}

		// End of stream, disconnecting user.
		if (bytesRead == -1) {
			System.out.println("End of stream");
			disconnect(key);
			return;
		}
		readBuffer.flip();
		byte[] dataCopy = new byte[bytesRead];
		System.arraycopy(readBuffer.array(), 0, dataCopy, 0, bytesRead);
		String msg = new String(dataCopy);
		handleIncomingMessage(key, msg);

	}

	private void displayUsers() {
		System.out.println("-----------Users online:------------");
		for (Map.Entry<Integer, User> entry : users.entrySet()) {
			System.out.println(entry.getKey());
		}
		System.out.println("------------------------------------");
	}

	// Sending information to user.
	private void write(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		User user = (User) key.attachment();

		while (user.haveMessages()) {
			String message = user.getNextMessage() + "~~";
			ByteBuffer bbuffer = ByteBuffer.wrap(message.getBytes());
			channel.write(bbuffer);

		}

	}

	private void closeConnection() {
		System.out.println("Closing server down");
		if (selector != null) {
			try {
				selector.close();
				serverChannel.socket().close();
				serverChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Received messages have format like [message type]:remaining message
	 */
	private void handleIncomingMessage(SelectionKey key, String incoming)
			throws IOException {
		String[] splitted = incoming.split(":", 2);
		String messageType = splitted[0];
		String remaining = splitted[1];
		if (messageType.equals("[MSG]")) {

			handleTextMessage(remaining);

		} else if (messageType.equals("[AUTH]")) {

			handleAuthorization(remaining, key);

		} else if (messageType.equals("[REG]")) {

			handleRegistration(remaining, key);

		} else if (messageType.equals("[STATUS]")) {
			
			handleUserStatusChange(remaining);
			
		} else if (messageType.equals("[USERSEARCH]")) {
			
			handleUserSearch(remaining);
			
		} else if (messageType.equals("[REMOVE]")) {
			
			handleFriendRemoving(remaining);
			
		} else if (messageType.equals("[ADDFRIEND]")) {

			handleFriendAdding(remaining);
			
		} else if (messageType.equals("[INFOCHANGE]")) {
			
			handleUserInfoChange(remaining);
			
		} else if (messageType.equals("[USERINFO]")) {
			
			String[] splittedRemaining = remaining.split(":");
			int requesterId = Integer.parseInt(splittedRemaining[0]);
			int userId = Integer.parseInt(splittedRemaining[1]);
			String info = User.getUserInfo(userId);
			if (isOnline(requesterId)) {
				((User) key.attachment()).addMessage("[USERINFO]:" + info);
			}
			
			
			
		}

	}

	private void handleUserInfoChange(String remaining) {
		User.changeInformation(remaining);
	}

	// Incoming message is just a text we need to send to addressee
	private void handleTextMessage(String remaining) {
		// Remaining string is "addresseID:message".
		String[] splittedRemaining = remaining.split(":", 2);
		Integer addressee = Integer.parseInt(new String(splittedRemaining[0]));
		String message = "[MSG]:" + splittedRemaining[1];
		if (isOnline(addressee)) {
			// Sending message right now.
			users.get(addressee).addMessage(message);
		} else {
			// Addressee is offline. Adding message to user`s deferred messages.
			addDeferredMessage(addressee, message);
		}

	}
	
	// Incoming message is an authorization attempt.
	private void handleAuthorization(String remaining, SelectionKey key) {
		String[] splittedRemaining = remaining.split(":", 3);
		Integer id = Integer.parseInt(splittedRemaining[0]);
		String password = splittedRemaining[1];
		String status = splittedRemaining[2];
		User user = new User();
		// To have ability to get user by SelectionKey.
		key.attach(user);
		// Trying to authorize user.
		String authInfo = "[AUTH]:" + user.authorizeUser(id, password, users);
		if (user.isAuthorized()) {
			// Authorization successful.
			users.put(user.getID(), user);
			user.setStatus(Status.valueOf(status));
			// Getting friends from database.
			String friendsId = user.getFriendsId();
			
			if (friendsId != null && !friendsId.isEmpty()) {
				// Appending friends statuses
				String friendsStatus = getFriendsStatus(friendsId);
				authInfo = authInfo + friendsStatus;
			}
			// Appending friends request.
			authInfo = authInfo + ":" + user.getFriendsRequests();
			user.addMessage(authInfo);
			// Loading deferred messages to user`s messages list
			user.loadDeferredMessages();
			// Sending notification to friends.
			sendStatusNotification(user.getID(), status);

		} else {
			// If authorization is failed, we just sending "reject" returned by User.authorizeUser method.
			user.addMessage(authInfo);
		}
	}

	private void handleRegistration(String remaining, SelectionKey key) {
		String[] splittedRemaining = remaining.split(":", 2);
		String name = splittedRemaining[0];
		String password = splittedRemaining[1];
		User user = new User();
		// To have ability to get user by SelectionKey.
		key.attach(user);
		String regInfo = "[REG]:" + user.registerUser(name, password);
		user.addMessage(regInfo);
		if (user.isAuthorized()) {
			// User registered and authorized.
			users.put(user.getID(), user);
			user.setStatus(Status.Online);
			// Sending notification to friends
			sendStatusNotification(user.getID(), "Online");
		}
		user.addMessage(regInfo);
	}
	
	// User changed his status
	private void handleUserStatusChange(String remaining) {
		String[] splittedRemaining = remaining.split(":", 2);
		int id = (int) Integer.parseInt(splittedRemaining[0]);
		
		String status = splittedRemaining[1];
		User u = users.get((Integer) id);
		u.setStatus(Status.valueOf(status));
		// Sending notification to friends
		sendStatusNotification(id, status);
	}
	
	// User is searching somebody.
	private void handleUserSearch(String remaining) {
		String[] splittedRemaining = remaining.split(":", 3);
		String requesterId = splittedRemaining[0];
		String name = splittedRemaining[1];
		String startingId = splittedRemaining[2];
		String usersFound = User.searchUsers(name, startingId);
		if (usersFound != null) {
			this.users.get(Integer.parseInt(requesterId)).addMessage(
					"[USERSEARCH]:" + usersFound);
		}
	}
	
	private void handleFriendRemoving(String remaining) {
		String[] splittedRemaining = remaining.split(":", 2);
		int user = Integer.parseInt(splittedRemaining[0]);
		String friend = splittedRemaining[1];
		User.removeFriend(user, friend);
	}
	
	/* The format of remaining string is:
	 * requesterId:requestedId:min(requesterId,requestedId):max(requesterId:requestedId)
	 * min(requesterId,requestedId):max(requesterId:requestedId) is a request
	 */
	private void handleFriendAdding(String remaining) {
		String[] splittedRemaining = remaining.split(":", 3);
		int id1 = Integer.parseInt(splittedRemaining[0]); 
		int id2 = Integer.parseInt(splittedRemaining[1]);
		String request = splittedRemaining[2];
		boolean success = User.makeFriendRequest(id1, request);
		if (success) {
			User user1 = users.get(new Integer(id1));
			User user2 = users.get(new Integer(id2));
			if (!isOnline(id1)) {
				if (isOnline(id2)) {
					user2.addMessage("[STATUS]:" + id1 + ":" + "Offline");
				} 
			} else {
				if (isOnline(id2)) {
					user2.addMessage("[STATUS]:" + id1 + ":" + user1.getStatus().toString());
				}
			}
			if (!isOnline(id2)) {
				if (isOnline(id1)) {
					user1.addMessage("[STATUS]:" + id2 + ":" + "Offline");
				}
			} else {
				if (isOnline(id1)) {
					user1.addMessage("[STATUS]:" + id2 + ":" + user2.getStatus().toString());
				} 
			}
		} else {
			// Sending request message to id2
			sendFriendRequest(id1, id2);
		}
	}
	
	private String getFriendsStatus(String friendsId) {
		String[] ar = friendsId.split("\\^");
		StringBuilder friendsStatus = new StringBuilder();
		for (String strId : ar) {
			if (!strId.equals("")) {
				Integer friendId = Integer.parseInt(strId);
				String userStatus = null;
				if (!isOnline((int) friendId)) {
					userStatus = "Offline";
				} else {
					User u = users.get(friendId);
					userStatus = u.getStatus().toString();
				}
				friendsStatus.append(friendId + "&"
						+ userStatus + "^");
			}
		}
		return friendsStatus.toString();
	}
	
	private void sendFriendRequest(int id1, int id2) {
		if (users.containsKey(new Integer(id2))) {
			String name1 = User.getName(id1);
			User user2 = users.get(new Integer(id2));
			String name2 = user2.getName();
			if (name2 != null) {
				user2.addMessage("[FRIENDREQUEST]" + ":" + id1 + ":" + name1);
			}

		}

	}

	// Sending status notification to all online friends
	private void sendStatusNotification(int id, String status) {
		User u = users.get(new Integer(id));
		// get friends
		List<Integer> friendsId = u.getFriendsIdList();
		for (Integer i : friendsId) {
			if (isOnline(i)) {
				User friend = users.get(i);
				friend.addMessage("[STATUS]:" + id + ":" + status);
			} 
		}

	}

	private void addDeferredMessage(int addressee, String message) {
		User.addDeferredMessage(addressee, message);
	}
	
	private boolean isOnline(int id) {
		return users.containsKey(id);
	}
	
	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new SetupWindow();
			}
		});
		

	}

}