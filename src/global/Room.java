package chAT.global;

import java.util.Vector;
import java.util.Date;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.security.PublicKey;

/**	Room class
*	This class handles all Room functionality. It extends UnicastRemoteObject
*	to be sendable via rmi.
*/
public class Room extends UnicastRemoteObject implements RoomInterface{
	private static final int DEFAULT_PORT = 1100;
	private final String room_name;
	private final String room_directory;
	private Vector<Message> messages;
	private Vector<String> clients;
	private Vector<Message> client_messages;

	private String one_time_key = null;		//not preserved on server shutdown!!
	private final String client_file;
	private final String hash_algorithm = "SHA-256";
	private final int saltlength = 16;
	private Vector<Crypto> cryptos;

	/**	Room constructor
	*	This constructor creates a Room instance with given name and for
	*	given port. The room is Initialized with System client and crypto.
	*	@param roomname_input: name of the room to create.
	*	@param port: port to call UnicastRemoteObject with.
	*	@throws RemoteException: if UnicastRemoveObject can be created.
	*/
	public Room(String roomname_input, int port) throws RemoteException{
		super(port);
		room_name = roomname_input;
		room_directory = "../data/" + room_name + "/";
		messages = new Vector<Message>();
		messages.add(new Message("system", "hello world"));
		clients = new Vector<String>();
		clients.add("system");
		client_messages = new Vector<Message>();
		client_messages.add(null);		//for system
		client_file = room_directory + "/clients.dat";
		if (!(new File(client_file)).exists()){
			System.out.println("Creating new room directory and client_file");
			try{
				(new File(room_directory)).mkdirs();		//create room directory
				(new FileWriter(client_file)).close();		//create new client file
			} catch (IOException e){
				System.out.println("IOException in creating client_file!");
			}
		}
		cryptos = new Vector<Crypto>();
		cryptos.add(null);				//for system
	}

	/**	Room constructor
	*	This constructor creates a Room instance with given name.
	*	The room is Initialized with System client and crypto.
	*	@param roomname_input: name of the room to create.
	*	@throws RemoteException: if UnicastRemoveObject can be created.
	*/
	public Room(String roomname_input) throws RemoteException{
		this(roomname_input, DEFAULT_PORT);
	}

	/**	Room constructor with makeOneTimeKey flag
	*	This constructor creates a Room instance with given name.
	*	If makeOneTimeKey is true, a single use key for client registration
	*	is created. The room is Initialized with System client and crypto.
	*	@param roomname_input: name of the room to create.
	*	@param makeOneTimeKey: if true, a key is created.
	*	@throws RemoteException: if UnicastRemoveObject can be created.
	*/
	public Room(String roomname_input, boolean makeOneTimeKey) throws RemoteException{
		this(roomname_input);
		if (makeOneTimeKey)
			System.out.println("Generating one_time_key " + makeOneTimeKey());
	}

	/**	userStatus method
	*	This method returns the status of given Client.
	*	-1: undefined, 0: unregistered, 1: registered, 2: online
	*	@param name: name of the Client to check status of.
	*	@throws RemoteException: if connection breaks.
	*	@returns status code of the Client
	*/
	public int userStatus(String name) throws RemoteException{
		System.out.println("DEBUG: trying to get status of "+ name);
		if (clients.indexOf(name) != -1)
			return 2;
		boolean found = false;
		BufferedReader br;
		try{
			br = new BufferedReader(new FileReader(client_file));
			String line;
			while ((line = br.readLine()) != null && !found)
				found = !(line.indexOf(name) == -1);
			br.close();
		} catch (IOException e) {
			System.out.println("IOException in reading client_file!");
		}
		return (found ? 1 : 0);
	}	//-1: undefined, 0: new user, 1: registered user, 2: online user

	/**	registerClient method
	*	This method returns the status of given Client.
	*	-1: undefined, 0: unregistered, 1: registered, 2: online
	*	@param name: name of the Client to register.
	*	@param key: oneTimeKey to use for registration.
	*	@param passwd: password for user (only salted hash is saved).
	*	@throws RemoteException: if connection breaks.
	*	@returns true, if registration was successfull
	*/
	public boolean registerClient(String name, String key, String passwd) throws RemoteException{
		System.out.println("DEBUG: registering client: "+name);
		if (userStatus(name) != 0)
			return false;
		if (!key.equals(one_time_key))
			return false;							//key is incorrect!			-> false

		SecureRandom srand = new SecureRandom();
		byte [] pwbytes = passwd.getBytes(StandardCharsets.UTF_8);
		byte [] saltbytes = new byte[saltlength];
		byte [] bytes = new byte[pwbytes.length+saltlength];			//size of the SHA256 hash is 32
		srand.nextBytes(saltbytes);
		for (int i=0; i<bytes.length; i++){
			if (i < pwbytes.length)
				bytes[i] = pwbytes[i];
			else
				bytes[i] = saltbytes[i - pwbytes.length];
		}
		//concatenate passwordbytes and salt
		try{
			MessageDigest digest = MessageDigest.getInstance(hash_algorithm);
			bytes = digest.digest(bytes);
		} catch (NoSuchAlgorithmException e){
			return false;
		}
		String hash = "";
		String salt = "";
        for (byte b : bytes)
            hash += String.format("%02x", b);		//conversion to hex
        for (byte b : saltbytes)
            salt += String.format("%02x", b);		//conversion to hex
		//System.out.println("DEBUG: pw hash = "+hash);
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(client_file, true));
			bw.append(name+"\t"+salt+"\t"+hash+"\n");
			bw.close();
		} catch (IOException e) {
			System.out.println("IOException in writing client_file!");
			return false;
		}
		return true;								//successfully added client	-> true
	}

	/**	login method
	*	This method attempts a login of given user with given password.
	*	TODO: move password hashing to Client (also in register).
	*	@param name: name of the Client to login.
	*	@param passwd: password for user.
	*	@param user_DHkey: public DH Key of the user (to verify signatures)
	*	@param user_RSAkey: public RSA Key of the user (to compute symmetric key)
	*	@throws RemoteException: if connection breaks.
	*	@returns true, if registration was successfull
	*/
	public PublicKey login(String name, String passwd,
								PublicKey user_DHkey, PublicKey user_RSAkey){
		String stored_hash = "", computed_hash = "", salt = "";
		BufferedReader br;
		try{
			br = new BufferedReader(new FileReader(client_file));
			String line;
			while ((line = br.readLine()) != null){
				if( !(line.indexOf(name) == -1) ){
					salt = line.split("\t")[1];
					stored_hash = line.split("\t")[2];
					break;
				}
			}
			br.close();
		} catch (IOException e) {
			System.out.println("IOException in reading client_file!");
			return null;
		}
		byte [] pwbytes = passwd.getBytes(StandardCharsets.UTF_8);
		byte [] saltbytes = new byte[saltlength];
		byte [] bytes = new byte[pwbytes.length+saltlength];			//size of the SHA256 hash is 32
		SecureRandom srand = new SecureRandom();
		srand.nextBytes(saltbytes);
		for (int i=0; i<bytes.length; i++){
			if (i < pwbytes.length)
				bytes[i] = pwbytes[i];
			else{
      			int v = Integer.parseInt(salt.substring(2*(i-pwbytes.length), 2*(i-pwbytes.length+1)), 16);
      			bytes[i] = (byte) v;
			}
		}
		System.out.println("DEBUG: client file read");
		try{
			MessageDigest digest = MessageDigest.getInstance(hash_algorithm);
			//bytes = new byte[digest.getDigestLength()];	//size of the SHA256 hash
			bytes = digest.digest(bytes);
		} catch (NoSuchAlgorithmException e){
			return null;
		}
        for (byte b : bytes) 
            computed_hash += String.format("%02x", b);		//conversion to hex
		System.out.println("DEBUG: comparing hashes");

		if (!stored_hash.equals(computed_hash) || computed_hash.equals(""))
			return null;

		clients.add(name);
		int index = clients.indexOf(name);
		client_messages.add(null);
		cryptos.add(new Crypto());

		System.out.println("DEBUG: adding crypto");
		cryptos.get(index).generateDHKeyPair();
		cryptos.get(index).computeSharedSecret(user_DHkey);
		System.out.println("DEBUG: finished DH");
		cryptos.get(index).generateRSAKeyPair();
		cryptos.get(index).setForeignRSAKey(user_RSAkey);

		System.out.println("DEBUG: finished setting up user #"+index);
		//TODO: message from system to all -> systemTAG boolean for messages?
		return cryptos.get(index).getDHPublicKey();
	}

	/**	getRSAPublicKey method
	*	This method returns the public RSA key of given user.
	*	It can be used to verify signatures of given user.
	*	@param name: name of user to get public RSA Key from.
	*	@throws RemoteException: if connection breaks.
	*	@returns public RSA key of given user
	*/
	public PublicKey getRSAPublicKey(String name) throws RemoteException{
		int index = clients.indexOf(name);
		if (index == -1)
			return null;
		return cryptos.get(index).getRSAPublicKey();
	}

	/**	logout method
	*	This method attempts to log a Client out from the Room.
	*	@param name: remove given Client from Room.
	*	@throws RemoteException: if connection breaks.
	*	@returns true, if logout was successfull
	*/
	public boolean logout(String name) throws RemoteException{
		int index = clients.indexOf(name);
		if (index == -1)
			return false;
		clients.remove(index);
		client_messages.remove(index);
		cryptos.remove(index);
		//TODO: message from system to all?
		return true;
	}

	/**	addMessages method
	*	This method extracts messages from the client_messages vector and
	*	adds them to the messages vector containing all.
	*	@throws RemoteException: if connection breaks.
	*	@returns true, if message was added
	*/
	public boolean addMessages() throws RemoteException{//idea: sort client_messages copy by date
		boolean added = false;
		Message m;
		for (int i=0; i<client_messages.size(); i++){
			m = client_messages.get(i);
			if (m != null){
				messages.add(new Message(m));
				client_messages.set(i, null);
				added = true;
			}
		}
		return added;
	}

	/**	submitMessage method
	*	This method accepts a message encrypted for system.
	*	It is decrypted and after the senders signature is verified, it
	*	is added to client messages.
	*	@param m: Message to submit
	*	@throws RemoteException: if connection breaks.
	*	@returns true, if submitting was successfull
	*/
	public boolean submitMessage(Message m) throws RemoteException{
		int index = clients.indexOf(m.getAuthor());
		if (index == -1 || client_messages.get(index) != null)
			return false;				//user not logged in!
		else {
			//System.out.println("DEBUG: encrypted message " + m);
			m.decrypt(cryptos.get(index));
			if (!m.verify(cryptos.get(index)))
				return false;
			client_messages.set(index, new Message(m));
			//System.out.println("DEBUG: adding message " + client_messages.get(index));
			return true;
		}
	}

	/**	requestNewMessages method
	*	This method is used to request all messages submitted after given date
	*	and send return them encrypted to given user.
	*/
	public Vector<Message> requestNewMessages(Date date, String name) throws RemoteException{
		Vector<Message> news = new Vector<Message>();
		int index = clients.indexOf(name);
		if (index == -1)
			return null;
		Message m;
		for(int i=messages.size()-1; i>0; i--){
			m = new Message(messages.get(i));
			//System.out.println("DEBUG: checking message " + i + ": " + m);
			if (m.getDate().after(date)){
				//TODO sign messages
				m.encrypt(cryptos.get(index));
				news.add(0, m);
			}
			else
				break;
		}
		/*DEBUG
		if (news.size() > 0)
			System.out.println("DEBUG: sending " + news.get(0).getText() + " to " + name);*/
		return news;
	}

	/**	makeOneTimeKey method
	*	This method creates a new oneTimeKey for a new Client to register.
	*	It has to be sent to the new Client on a different way though.
	*	@throws RemoteException: if connection breaks.
	*	@returns oneTimeKey as String.
	*/
	public String makeOneTimeKey() throws RemoteException{
		one_time_key = "";
		SecureRandom srand = new SecureRandom();
		byte bytes[] = new byte[16];					// = 128 Bit
		srand.nextBytes(bytes);
        for (byte b : bytes) 
            one_time_key += String.format("%02x", b);		//conversion to hex
		return one_time_key;
	}

	/**	injectCommand method
	*	This method takes a message and processes it, executing the command
	*	it contains. Possible commands are makeonetimekey, help, userlist, ...
	*	@param m: Command to execute
	*	@throws RemoteException: if connection breaks.
	*	@returns Message which is the systems answer to the command
	*/
	public Message injectCommand(Message m) throws RemoteException{
		int index = clients.indexOf(m.getAuthor());
		if (index == -1 || client_messages.get(index) != null)
			return null;				//user not logged in!
		m.decrypt(cryptos.get(clients.indexOf(m.getAuthor())));
		String command = m.getText();
		Message reply;
		switch(command){
			case "makeonetimekey":
			case "makeOneTimeKey":
				System.out.println("oneTimeKey generation called by " + m.getAuthor());
				reply = new Message("system", "new oneTimeKey: " + makeOneTimeKey());
				break;
			case "help":
				reply = new Message("system", printHelp());
				break;
			case "userlist":
			case "listusers":
			case "listUsers":
				reply = new Message("system", printUserList());
			/*case "logout":			//should be only initiated by asynchronous logout
				logout(m.getAuthor());
				return new Message("system", "logged out user " + m.getAuthor());*/
				break;
			default:
				reply = new Message("system", "invalid command: !" + command + ", try !help");
		}
		reply.encrypt(cryptos.get(index));		//TODO: sign
		return reply;
	}

	/**	printHelp method
	*	This method prints help for the possible commands.
	*	@throws RemoteException: if connection breaks.
	*	@returns help for commands as String.
	*/
	public String printHelp() throws RemoteException{
		String result = "!help: to get information about commands\n" +
					"!makeOneTimeKey: to create a key for a new user (usable only once)\n" +
					"!userlist: to show all online users";
		return result;
	}

	/**	printUserList method
	*	This method returns the list of all online users names as String.
	*	@throws RemoteException: if connection breaks.
	*	@returns names of all online users as String
	*/
	public String printUserList() throws RemoteException{
		String result = "";
		for (String user : clients)
			result += user + ", ";
		return result.substring(0, result.length()-2);
	}

	/**	shutdown method
	*	This method performs a shutdown of the server. All clients are
	*	logged out. On success, true is returned.
	*	@throws RemoteException: if connection breaks.
	*	@returns true, if shutting down didnt yield any problems.
	*/
	public boolean shutdown() throws RemoteException{
		boolean result = true;
		for(int i=1; i<clients.size(); i++){		//all users except system
			result &= logout(clients.get(i));
		}
		return result;
	}
}
