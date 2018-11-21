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

//TODO: store/load room -> shutdownhook
public class Room extends UnicastRemoteObject implements RoomInterface{
	private final String room_name;
	private final String room_directory;
	private Vector<Message> messages;
	private Vector<String> clients;
	private Vector<Message> client_messages;

	private String one_time_key = null;						//not preserved on server shutdown!!
	private final String client_file;
	private final String hash_algorithm = "SHA-256";
	private Vector<Crypto> cryptos;

	public Room(String roomname_input) throws RemoteException{
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

	public Room(String roomname_input, boolean makeOneTimeKey) throws RemoteException{
		this(roomname_input);
		if (makeOneTimeKey)
			System.out.println("Generating one_time_key " + makeOneTimeKey());
	}

	public int userStatus(String name) throws RemoteException{
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

	public boolean registerClient(String name, String key, String passwd) throws RemoteException{
		if (userStatus(name) != 0)
			return false;
		if (!key.equals(one_time_key))
			return false;							//key is incorrect!			-> false
		byte [] bytes = new byte[1];				//size of the SHA256 hash is 32
		try{
			MessageDigest digest = MessageDigest.getInstance(hash_algorithm);
			bytes = digest.digest(passwd.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e){
			return false;
		}
		String hash = "";
        for (byte b : bytes) 
            hash += String.format("%02x", b);		//conversion to hex
		//System.out.println("DEBUG: pw hash = "+hash);
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(client_file, true));
			bw.append(name+"\t"+hash+"\n");
			bw.close();
		} catch (IOException e) {
			System.out.println("IOException in writing client_file!");
			return false;
		}
		return true;								//successfully added client	-> true
	}

	public PublicKey login(String name, String passwd,
								PublicKey user_DHkey, PublicKey user_RSAkey){
		String stored_hash = "", computed_hash = "";
		BufferedReader br;
		try{
			br = new BufferedReader(new FileReader(client_file));
			String line;
			while ((line = br.readLine()) != null){
				if( !(line.indexOf(name) == -1) ){
					stored_hash = line.split("\t")[1];
					break;
				}
			}
			br.close();
		} catch (IOException e) {
			System.out.println("IOException in reading client_file!");
			return null;
		}

		System.out.println("DEBUG: client file read");
		byte [] bytes = new byte[1];					//size of the SHA256 hash is 32
		try{
			MessageDigest digest = MessageDigest.getInstance(hash_algorithm);
			bytes = new byte[digest.getDigestLength()];	//size of the SHA256 hash
			bytes = digest.digest(passwd.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e){
			return null;
		}
        for (byte b : bytes) 
            computed_hash += String.format("%02x", b);		//conversion to hex
		System.out.println("DEBUG: comparing hashes");

		if (!stored_hash.equals(computed_hash) || computed_hash.equals(""))
			return null;

		clients.add(name);
		client_messages.add(null);

		System.out.println("DEBUG: adding crypto");
		cryptos.add(new Crypto());
		cryptos.get(cryptos.size()-1).generateDHKeyPair();
		cryptos.get(cryptos.size()-1).computeSharedSecret(user_DHkey);
		System.out.println("DEBUG: finished DH");
		cryptos.get(cryptos.size()-1).generateRSAKeyPair();
		cryptos.get(cryptos.size()-1).setForeignRSAKey(user_RSAkey);

		//TODO: message from system to all -> systemTAG boolean for messages?
		return cryptos.get(cryptos.size()-1).getDHPublicKey();
	}

	public PublicKey getRSAPublicKey(String name) throws RemoteException{
		int index = clients.indexOf(name);
		if (index == -1)
			return null;
		return cryptos.get(index).getRSAPublicKey();
	}

	public boolean logout(String name){		// logout after some idletime?
		int index = clients.indexOf(name);
		if (index == -1)
			return false;
		clients.remove(index);
		client_messages.remove(index);
		//TODO: message from system to all?
		return true;
	}

	public boolean addMessages() throws RemoteException{//idea: sort client_messages copy by date
		boolean added = false;
		Message m;
		for (int i=0; i<client_messages.size(); i++){	//TODO: what if player leaves/enters
			m = client_messages.get(i);
			if (m != null){
				messages.add(new Message(m));
				client_messages.set(i, null);
				added = true;
			}
		}
		return added;
	}

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

	public Vector<Message> requestNewMessages(Date date, String name) throws RemoteException{
		Vector<Message> news = new Vector<Message>();
		int index = clients.indexOf(name);
		if (index == -1)
			return null;
		Message m;
		for(int i=messages.size()-1; i>0; i--){
			m = messages.get(i);
			//System.out.println("DEBUG: checking message " + i + ": " + m);
			if (m.getDate().after(date)){
				m.encrypt(cryptos.get(index));
				news.add(0, m);
			}
			else
				break;
		}
		//System.out.println("DEBUG: adding news of lenght " + news.size());
		return news;
	}

	public String makeOneTimeKey() throws RemoteException{
		one_time_key = "";
		SecureRandom srand = new SecureRandom();
		byte bytes[] = new byte[16];					// = 128 Bit
		srand.nextBytes(bytes);
        for (byte b : bytes) 
            one_time_key += String.format("%02x", b);		//conversion to hex
		return one_time_key;
	}

	public Message injectCommand(Message m) throws RemoteException{
		int index = clients.indexOf(m.getAuthor());
		if (index == -1 || client_messages.get(index) != null)
			return null;				//user not logged in!
		m.decrypt(cryptos.get(clients.indexOf(m.getAuthor())));
		String command = m.getText();
		switch(command){
			case "makeonetimekey":
			case "makeOneTimeKey":
				System.out.println("oneTimeKey generation called by " + m.getAuthor());
				return new Message("system", "new oneTimeKey: " + makeOneTimeKey());
			case "help":
				return new Message("system", printHelp());
			case "userlist":
			case "listusers":
			case "listUsers":
				return new Message("system", printUserList());
			/*case "logout":			//should be only initiated by asynchronous logout
				logout(m.getAuthor());
				return new Message("system", "logged out user " + m.getAuthor());*/
			default:
				return new Message("system", "invalid command: !" + command + "\ttry !help");
		}
	}

	public String printHelp() throws RemoteException{
		String result = "!help: to get information about commands\n" +
					"!makeOneTimeKey: to create a key for a new user (usable only once)\n" +
					"!userlist: to show all online users";
		return result;
	}

	public String printUserList() throws RemoteException{
		String result = "";
		for (String user : clients)
			result += user + "\n";
		return result.substring(0, result.length()-1);
	}
}
