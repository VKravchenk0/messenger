package engine;

import gui.AuthorizationWindow;
import gui.DialogWindow;
import gui.FriendInfoWindow;
import gui.InformationSettingWindow;
import gui.MainWindow;
import gui.UserSearchWindow;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

/**Main client class, responsible for server connection and handling incoming messages*/
public class ClientEngine {

	private AuthorizationWindow authWindow = null;
	private MainWindow mainWindow = null;
	private DialogWindow dialogWindow = null;
	private UserSearchWindow userSearchWindow = null;
	private InformationSettingWindow informationSettingWindow = null;
	private FriendInfoWindow friendInfoWindow = null;

	private String password = null;
	private boolean authorized = false;

	private boolean connected = false;

	// The flag that shows if engine must attempt to connect to the server.
	private Status startConnectionFlag = Status.Online;
	private Selector selector;
	private SelectionKey serverKey = null;

	private List<Friend> friendList = new CopyOnWriteArrayList<Friend>();
	private List<Friend> searchList;
	private Queue<String> messages = new ConcurrentLinkedQueue<String>();
	
	private final static String SERVER_ADDRESS = "127.0.0.1";
	//private final static String address = "77.47.220.166";
	private final static int SERVER_PORT = 8511;

	private User user = null;

	public ClientEngine() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				authWindow = new AuthorizationWindow(ClientEngine.this);
			}
		});

		while (true) {
			runConnectionHandler();
		}

	}

	private void runConnectionHandler() {
		if ((startConnectionFlag == Status.Online || startConnectionFlag == Status.AFK) && !connected) {
			startConnection();
		} else if (startConnectionFlag == Status.Offline && connected) {
			disconnect();
		}
	}

	private void startConnection() {
		SocketChannel channel;
		try {
			selector = Selector.open();
			channel = SocketChannel.open();
			channel.configureBlocking(false);

			channel.register(selector, SelectionKey.OP_CONNECT);
			channel.connect(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT));
			Iterator<SelectionKey> it = null;

			while (selector.isOpen()) {

				selector.select();
				if (selector.isOpen()) {
					it = selector.selectedKeys().iterator();
				}

				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();

					if (!key.isValid())
						continue;

					if (key.isValid() && key.isConnectable()) {
						connect(key);
					}
					if (key.isValid() && key.isReadable()) {
						read(key);
					}
					if (key.isValid() && key.isWritable()) {
						write(key);
					}
					// Engine must disconnect from server.
					if (startConnectionFlag == Status.Offline) {
						disconnect();
					}

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	private void close() {
		try {
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void connect(SelectionKey key) throws IOException {
		serverKey = key;
		SocketChannel channelToServer = (SocketChannel) key.channel();
		try {
			if (channelToServer.isConnectionPending()) {
				channelToServer.finishConnect();
			}
		} catch (ConnectException e) {
			key.cancel();
			channelToServer.close();
			selector.close();
			System.out.println("Cannot connect");
			reconnect();
		} catch (SocketException se) {
			System.out.println("socket exception. reconnecting");
			reconnect();
		}
		channelToServer.configureBlocking(false);
		channelToServer.register(selector, SelectionKey.OP_READ
				| SelectionKey.OP_WRITE);
		connected = true;
		
		if (authorized) {
			String id = String.valueOf(user.getId());
			makeAuthorization(id, password);
		}
		if (mainWindow != null) {
			mainWindow.repaint();
			mainWindow.redrawStatusBox();
		}
	}

	public void disconnect() {

		SocketChannel serverChannel = (SocketChannel) serverKey.channel();
		try {
			serverKey.cancel();
			serverChannel.close();
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		connected = false;
		if (user != null) {
			user.setStatus(Status.Offline);
			mainWindow.redrawStatusBox();

			// Setting all friends statuses to "Offline".
			for (Friend f : friendList) {
				if (f.getStatus() != Status.Unfriended) {
					f.setStatus(Status.Offline);
				}
			}
			updateFriendList();
		}
		
	}

	public void reconnect() {
		System.out.println("Start reconnection");
		while (connected == false) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			runConnectionHandler();
		}
		System.out.println("Reconnected successfully");
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer readBuffer = ByteBuffer.allocate(2048);
		readBuffer.clear();
		int bytesRead;
		try {
			bytesRead = channel.read(readBuffer);
		} catch (IOException e) {
			System.out.println("Reading problem, closing connection");
			disconnect();
			reconnect();
			return;
		}
		// End of stream, closing connection
		if (bytesRead == -1) {
			System.out.println("Nothing was read from the server");
			disconnect();
			reconnect();
			return;
		}
		readBuffer.flip();
		byte[] dataCopy = new byte[bytesRead];
		System.arraycopy(readBuffer.array(), 0, dataCopy, 0, bytesRead);
		String incomingMessage = new String(dataCopy);
		System.out.println("Raw message: " + incomingMessage);
		handleIncomingMessage(incomingMessage);

	}

	private void handleIncomingMessage(String rawMessage) {
		// In case server will send several messages in row we're just adding "~~" after each message.
		String[] messages = rawMessage.split("~~");
		for (String message : messages) {
			if (message != null && !message.equals("")) {

				String[] splittedMessage = message.split(":", 2);
				String messageType = splittedMessage[0];
				String remaining = splittedMessage[1];
				
				if (messageType.equals("[REG]")) {
					
					handleRegistration(remaining);
					
				} else if (messageType.equals("[AUTH]")) {
					
					handleAuthorization(remaining);

				} else if (messageType.equals("[MSG]")) {
					
					handleSimpleMessage(remaining);
					
				} else if (messageType.equals("[STATUS]")) {
					
					handleFriendStatusChange(remaining);
					
					
				} else if (messageType.equals("[USERSEARCH]")) {
					
					handleReceivedSearchMessage(remaining);
					
				} else if (messageType.equals("[FRIENDREQUEST]")) {
					
					handleIncomingFriendRequest(remaining);
					
				} else if (messageType.equals("[USERINFO]")) {
					//id:name:fname:lname:day^month^year:country:city:email
					String[] splittedRemaining = remaining.split(":", 2);
					int id = Integer.parseInt(splittedRemaining[0]);
					if (id == user.getId()) {
						showCurrentUserInfo(splittedRemaining[1]);
					} else {
						showFriendInfo(splittedRemaining[1]);
					}
					
				}

			}
		}

	}
	
	private void showCurrentUserInfo(String values) {
		if (informationSettingWindow != null) {
			informationSettingWindow.setValues(values);
		} 
		
	}

	private void showFriendInfo(String values) {
		if (friendInfoWindow != null) {
			friendInfoWindow.setValues(values);
		}
		
	}

	private void handleAuthorization(String remaining) {
		String[] splittedRemaining = remaining.split(":");
		if (splittedRemaining[0].equals("reject")) {
			authWindow.showError("Incorrect user id or password");
			return;
		} else if (splittedRemaining[0].equals("userAlreadyConnected")) {
			authWindow.showError("User already connected");
			return;
		} else {
			if (!splittedRemaining[2].equals("null")) {
				String friends = splittedRemaining[2];
				String friendsStatus = splittedRemaining[3];
				loadFriends(friends, friendsStatus);
			}
			if (!splittedRemaining[4].equals("null")) {
				loadUnfriendedContacts(splittedRemaining[4]);
			}
			int id = Integer.parseInt(splittedRemaining[0]);
			String name = splittedRemaining[1];
			authorized = true;
			user = new User(id, name);
			user.setStatus(startConnectionFlag);
			sendStatusNotification(startConnectionFlag.toString());
			createMainWindow();
			
		}
		
	}
	
	private void handleRegistration(String remaining) {
		String[] splittedRemaining = remaining.split(":");
		if (splittedRemaining[0].equals("reject")) {
			authWindow.showError("Cannot create user. Please try again later");
			return;
		} else {
			
			int id = Integer.parseInt(splittedRemaining[0]);
			String name = splittedRemaining[1];
			authorized = true;
			user = new User(id, name);
			user.setStatus(Status.Online);
			createMainWindow();
		}
		
	}
	
	private void handleSimpleMessage(String remaining) {
		String[] splittedRemaining = remaining.split(":");
		String msg = splittedRemaining[0];
		String[] splittedMsg = msg.split("&");
		int senderId = (int) Integer.parseInt(splittedMsg[0]);
		String messageItself = splittedMsg[1];
		if (dialogWindow != null
				&& dialogWindow.conversationExists(new Integer(
						senderId))) {
			dialogWindow.appendMessage(senderId, messageItself);
		} else {
			for (Friend fr : friendList) {
				if (fr.getId() == senderId) {
					fr.addDeferredMessage(messageItself);
				}
			}
		}
		
	}

	private void handleFriendStatusChange(String remaining) {
		String[] splittedRemaining = remaining.split(":");
		Integer id = Integer.parseInt(splittedRemaining[0]);
		String status = splittedRemaining[1];
		changeUserStatus(id, Status.valueOf(status));
		
	}

	private void handleReceivedSearchMessage(String remaining) {
		String[] splittedRemaining = remaining.split("&");
		searchList = new ArrayList<Friend>();
		int lastFoundUser = 0;
		if (remaining.length() > 0) {
			for (String user: splittedRemaining) {
				if (user != null && !user.equals("")) {
					String[] userInfo = user.split(":");
					String userId = userInfo[0];
					String name = userInfo[1];
					String fName = userInfo[2];
					String lName = userInfo[3];
					String birthDate = userInfo[4];
					System.out.println("bd = " + birthDate);
					String country = userInfo[5];
					String city = userInfo[6];
					String email = userInfo[7];
					int id = Integer.parseInt(userId);
					lastFoundUser = id;
					Friend friend = new Friend(id, name);
					friend.setFirstName(fName);
					friend.setLastName(lName);
					friend.setBirthDate(birthDate);
					friend.setCountry(country);
					friend.setCity(city);
					friend.setEmail(email);
					System.out.println(friend.getBirthDate());
					searchList.add(friend);

				}
			
			}
		} else {
			searchList.clear();
		}
		if (userSearchWindow != null) {
			userSearchWindow.setLastFoundUser(lastFoundUser);
			userSearchWindow.visualizeResult();
		}
		
	}

	private void handleIncomingFriendRequest(String remaining) {
		String[] splittedRemaining = remaining.split(":");
		int friendId = Integer.parseInt(splittedRemaining[0]);
		String name = splittedRemaining[1];
		Friend f = new Friend(friendId, name);
		f.setStatus(Status.Unfriended);
		if (!friendList.contains(f)) {
			friendList.add(f);
		}

		// if jlist wasn't loaded yet
		if (friendList.size() == 1) {
			mainWindow.loadJList();
		}
		updateFriendList();
		
	}

	private void createMainWindow() {
		authWindow.dispose();
		if (mainWindow == null || !mainWindow.isDisplayable()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					mainWindow = new MainWindow(user,
							ClientEngine.this);
					mainWindow.setStatusBoxValue(startConnectionFlag);
					updateFriendList();
					mainWindow.repaint();
				}
			});
		} else {
			
			updateFriendList();
			mainWindow.setStatusBoxValue(startConnectionFlag);
			mainWindow.repaint();
		}
	}
	
	public void updateFriendList() {
		Collections.sort(friendList, new Comparator<Friend>() {

			@Override
			public int compare(Friend f1, Friend f2) {
				if (f1.getStatus() == Status.Online && f2.getStatus() != Status.Online) {
					return -1;
				} else if (f1.getStatus() == Status.Unfriended && f2.getStatus() == Status.Offline) {
					return -1;
				} else if (f1.getStatus() == Status.Unfriended && (f2.getStatus() == Status.Online || f2.getStatus() == Status.AFK)) {
					return 1;
				} else if (f1.getStatus() == Status.Offline && f2.getStatus() != Status.Offline) {
					return 1;
				}
				
				return f1.getName().compareTo(f2.getName());
			}
			
		});
		
		if (mainWindow != null) {
			mainWindow.resetModel();
		}
		
	}

	private void changeUserStatus(Integer id, Status status) {
		Integer friendId;
		// Searching for a friend with a specified id
		for (Friend f : friendList) {
			friendId = (Integer) f.getId();
			if (friendId.equals(id)) {
				f.setStatus(status);
			}
		}
		updateFriendList();
	}

	private void loadFriends(String friends, String friendsStatus) {

		// The friends string looks like "id1&name1^id2&name2".
		String[] friendsArr = friends.split("\\^");
		for (String friend : friendsArr) {
			String[] splittedFriend = friend.split("&");
			Friend f = new Friend((int) Integer.parseInt(splittedFriend[0]),
					splittedFriend[1]);
			if (!friendList.contains(f)) {
				friendList.add(f);
			}

		}
		
		// The friendsStatus string looks like "id1&status1^id2&status2".
		String[] statuses = friendsStatus.split("\\^");
		for (String status : statuses) {
			String[] splittedStatus = status.split("&");
			for (Friend fr : friendList) {
				if (fr.getId() == (int) Integer.parseInt(splittedStatus[0])) {
					if (splittedStatus[1].equals("Online")) {
						fr.setStatus(Status.Online);
					} else if (splittedStatus[1].equals("AFK")) {
						fr.setStatus(Status.AFK);
					} else if (splittedStatus[1].equals("Offline")) {
						fr.setStatus(Status.Offline);
					}
				}
			}
		}

	}

	private void loadUnfriendedContacts(String unfriended) {

		// Adding unfriended users to friendsList
		String[] unfriendedArr = unfriended.split("\\^");
		for (String somebody : unfriendedArr) {
			String[] splittedSomebody = somebody.split("&");
			int id = Integer.parseInt(splittedSomebody[0]);
			String name = splittedSomebody[1];
			Friend f = new Friend(id, name);
			f.setStatus(Status.Unfriended);
			if (!friendList.contains(f)) {
				friendList.add(f);
			}
		}

	}

	public List<Friend> getFriendList() {
		if (friendList != null) {

			return friendList;
		} else {

			return null;
		}

	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		while (!messages.isEmpty()) {
			String message = messages.poll();
			ByteBuffer bbuffer = ByteBuffer.wrap(message.getBytes());
			channel.write(bbuffer);
		}
	}


	public void send(String message) {
		messages.add(message);
	}

	public void createDialogWindow(User user, Friend friend) {
		if ((dialogWindow == null) || (!dialogWindow.isDisplayable())) {
			dialogWindow = new DialogWindow(user, friend, this);
			dialogWindow.addConversation(friend);
			dialogWindow.pack();
			dialogWindow.setVisible(true);
			dialogWindow.setResizable(false);

		} else {
			dialogWindow.addConversation(friend);
		}

	}

	public void appendDeferredMessages(int id) {

		for (Friend f : friendList) {
			if (f.getId() == id) {
				while (f.haveDeferredMessages()) {
					String message = f.getNextMessage();
					dialogWindow.appendMessage(id, message);
				}
			}
		}

	}

	public void sendStatusNotification(String status) {
		send("[STATUS]:" + user.getId() + ":" + status);

	}

	// The method is called when UserSearchWindow is closing
	public void resetSearchList() {
		if (searchList != null) {
			searchList.clear();
			searchList = null;
		}

	}

	public User getUser() {
		return user;
	}

	public void openUserSearchWindow() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				userSearchWindow = new UserSearchWindow(ClientEngine.this);
			}
		});

	}
	
	public void openInformationSettingWindow() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				informationSettingWindow = new InformationSettingWindow(ClientEngine.this);
			}
		});

	}
	
	public void openFriendInfoWindow(final int friendId) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				friendInfoWindow = new FriendInfoWindow(ClientEngine.this, friendId);
			}
		});
	}

	public List<Friend> getSearchList() {
		if (searchList != null) {
			return searchList;
		} else {
			return null;
		}
	}

	public void makeFriendRequest(int friendId, String name) {

		Friend friend = new Friend(friendId, name);
		friend.setStatus(Status.Unfriended);
		if (!friendList.contains(friend)) {
			friendList.add(friend);
		}

		if (friendList.size() == 1) {
			mainWindow.loadJList();
		}
		mainWindow.resetModel();
		int userId = user.getId();
		String request = "[ADDFRIEND]:" + userId + ":" + friendId + ":"
				+ Math.min(userId, friendId) + ":" + Math.max(userId, friendId);
		send(request);

	}

	public void removeFriend(int id) {

		Iterator<Friend> i = friendList.iterator();
		while (i.hasNext()) {
			Friend f = i.next();
			if (f.getId() == id) {
				if (!f.getStatus().equals("Unfriended")) {
					send("[REMOVE]:" + user.getId() + ":" + f.getId() + "&"
							+ f.getName());
				}
				i.remove();
				mainWindow.resetModel();

			}
		}

	}

	public boolean friendsExists() {
		return !friendList.isEmpty();
	}

	public void makeAuthorization(String id, String password) {

		this.password = password;
		if (user == null) {
			send("[AUTH]:" + id + ":" + password + ":" + "Online");
		} else {
			String currentStatus = mainWindow.getSelectedStatus();
			send("[AUTH]:" + id + ":" + password + ":" + currentStatus);
		}

	}
	
	public void makeRegistration(String name, String password) {

		this.password = password;
		send("[REG]:" + name + ":" + password);

	}

	public boolean isConnected() {
		return connected;
	}

	public void setStartConnectionFlag(Status status) {
		startConnectionFlag = status;
	}
	
	public static void main(String[] args) {
		new ClientEngine();
	}
}
