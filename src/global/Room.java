package chAT.global;

import java.util.Vector;
import java.util.Date;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

//TODO: store/load room
public class Room extends UnicastRemoteObject implements RoomInterface{
	private Vector<Message> messages;
	private Vector<String> clients;
	private Vector<Message> client_messages;

	private String oneTimeKey = null;		//maybe not in clear text?
	private final String clientFile = "../data/clients.dat";
	private final String hash_algorithm = "SHA-256";

	public Room() throws RemoteException{
		messages = new Vector<Message>();
		messages.add(new Message("system", "hello world"));
		clients = new Vector<String>();
		clients.add("system");
		client_messages = new Vector<Message>();
		client_messages.add(null);
	}

	public Room(boolean makeOneTimeKey) throws RemoteException{
		this();
		if (makeOneTimeKey)
			System.out.println("Generating oneTimeKey " + makeOneTimeKey());
	}

	public int userStatus(String name) throws RemoteException{
		if (clients.indexOf(name) != -1)
			return 2;
		boolean found = false;
		BufferedReader br;
		try{
			br = new BufferedReader(new FileReader(clientFile));
			String line;
			while ((line = br.readLine()) != null && !found)
				found = !(line.indexOf(name) == -1);
			br.close();
		} catch (IOException e) {
			System.out.println("IOException in reading clientFile!");
		}
		return (found ? 1 : 0);
	}	//-1: undefined, 0: new user, 1: registered user, 2: online user

	public boolean registerClient(String name, String key, String passwd) throws RemoteException{
		if (userStatus(name) != 0)
			return false;
		if (!key.equals(oneTimeKey))
			return false;							//key is incorrect!			-> false
		byte [] bytes = new byte[1];				//size of the SHA256 hash is 32
		try{
			MessageDigest digest = MessageDigest.getInstance(hash_algorithm);
			bytes = new byte[digest.getDigestLength()];	//size of the SHA256 hash
			bytes = digest.digest(passwd.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e){
			return false;
		}
		String hash = "";
        for (byte b : bytes) 
            hash += String.format("%02x", b);		//conversion to hex
		System.out.println(hash);
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(clientFile, true));
			bw.append(name+"\t"+hash+"\n");
			bw.close();
		} catch (IOException e) {
			System.out.println("IOException in writing clientFile!");
			return true;
		}
		return true;								//successfully added client	-> true
	}

	public boolean login(String name, String passwd){
		String stored_hash = "", computed_hash = "";
		BufferedReader br;
		try{
			br = new BufferedReader(new FileReader(clientFile));
			String line;
			while ((line = br.readLine()) != null){
				if( !(line.indexOf(name) == -1) ){
					stored_hash = line.split("\t")[1];
					break;
				}
			}
			br.close();
		} catch (IOException e) {
			System.out.println("IOException in reading clientFile!");
			return false;
		}

		byte [] bytes = new byte[1];				//size of the SHA256 hash is 32
		try{
			MessageDigest digest = MessageDigest.getInstance(hash_algorithm);
			bytes = new byte[digest.getDigestLength()];	//size of the SHA256 hash
			bytes = digest.digest(passwd.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e){
			return false;
		}
        for (byte b : bytes) 
            computed_hash += String.format("%02x", b);		//conversion to hex

		if (!stored_hash.equals(computed_hash) || computed_hash.equals(""))
			return false;

		clients.add(name);
		client_messages.add(null);
		//TODO: message from system to all -> systemTAG boolean for messages?
		return true;
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
		if (client_messages.get(index) != null)
			return false;				//user not logged in!
		else {
			client_messages.set(index, new Message(m));
			System.out.println("DEBUG: adding message " + client_messages.get(index));
			return true;
		}
	}

	public Vector<Message> requestNewMessages(Date date) throws RemoteException{
		Vector<Message> news = new Vector<Message>();
		Message m;
		for(int i=messages.size()-1; i>0; i--){
			m = messages.get(i);
			System.out.println("DEBUG: checking message " + i + ": " + m);
			if (m.getDate().after(date))
				news.add(0, m);
			else
				break;
		}
		System.out.println("DEBUG: adding news of lenght " + news.size());
		return news;
	}

	public String makeOneTimeKey() throws RemoteException{
		oneTimeKey = "";
		SecureRandom srand = new SecureRandom();
		byte bytes[] = new byte[16];					// = 128 Bit
		srand.nextBytes(bytes);
        for (byte b : bytes) 
            oneTimeKey += String.format("%02x", b);		//conversion to hex
		return oneTimeKey;
	}

	public Message injectCommand(Message m) throws RemoteException{
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
			/*case "logout":
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

	//TODO: get registered users function, request+store hashed password on disk
}
