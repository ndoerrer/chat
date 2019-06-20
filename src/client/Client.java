package chAT.client;

import chAT.global.*;
import chAT.gui.*;

import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.Naming;
import java.net.MalformedURLException;

import java.util.Date;
import java.io.Console;

import java.security.PublicKey;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

class ClientShutdownThread extends Thread {
	private RoomInterface roomI;
	private String myname;
	private Date date;
	private String roomname;

	public ClientShutdownThread(RoomInterface roomI_in, String myname_in,
									Date date_in, String roomname_in) {
		super();
		roomI = roomI_in;
		myname = myname_in;
		date = date_in;
		roomname = roomname_in;
	}	//TODO: DNS retranslation?

	private void storeTimestamp(){
		String timestamp_file = "../data/" + roomname + "/" + myname + ".time";
		if (!(new File(timestamp_file)).exists()){
			System.out.println("Creating new room directory and timestampfile");
			try{
				(new File(roomname)).mkdirs();				//create room directory
				(new FileWriter(timestamp_file)).close();	//create new client file
			} catch (IOException e){
				System.out.println("IOException in creating timestamp file!");
			}
		}
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(timestamp_file));
			bw.write( ""+(new Date()).getTime() );
			bw.close();
		} catch (IOException e) {
			System.out.println("IOException in writing timestamp file!");
		}
	}

	public void run() {
		storeTimestamp();
		boolean success = false;
		try{
			success = roomI.logout(myname);
		} catch (RemoteException e){
			System.out.println("RemoteException in logout!");
			this.interrupt();
		}
		if (success)
			System.out.println("Logging out complete");
		else
			System.out.println("Logging out failed!");
		System.out.println("Shutting down");
		this.interrupt();
	}
}

public class Client{
	private static Date loadDate(String roomname, String myname){
		String timestamp_file = "../data/" + roomname + "/" + myname + ".time";
		long milliseconds = 0;
		if ( (new File(timestamp_file)).exists() ){
			try{
				BufferedReader br = new BufferedReader(new FileReader(timestamp_file));
				milliseconds = Long.parseLong(br.readLine());
				br.close();
			} catch (IOException e){
				System.out.println("IOException in reading timestamp file!");
			}
			return new Date(milliseconds);
		}
		return new Date();
	}

	private static RoomInterface findRoom(String host, String name, int port) {
		RoomInterface roomI = null;
		try{
			System.out.println("Requesting Object rmi://" + host + ":" + port + "/" + name);
			Object o = Naming.lookup("rmi://" + host + ":" + port + "/" + name);
			roomI = (RoomInterface) o;
			System.out.println ("RoomInterface (" + name + ") found");
		} catch (NotBoundException e) {
			System.out.println("NotBoundException: couldnt lookup " + name);
			System.exit(1);
		} catch (RemoteException e) {
			System.out.println("RemoteExpcetion: couldnt lookup " + name);
			System.exit(2);
		} catch (MalformedURLException e){
			System.out.println("MalformedURLExpcetion: couldnt lookup " + name);
			System.exit(3);
		}
		return roomI;
	}

	public static void main(String [] args){
        Console console = System.console();
		if (console == null) {
			System.out.println("Couldn't get Console instance");
			System.exit(0);
		}

		ArgParser arg_parser = new ArgParser(args);
		String host="", myname="", roomname="", passwd="", key="";
		int port = 0;
		boolean graphical = false;

		try{
			host = arg_parser.getHost();
			myname = arg_parser.getName();
			roomname = arg_parser.getRoom();
			port = arg_parser.getPort();
			graphical = arg_parser.isGraphical();
		} catch(IllegalArgumentException e){
			System.out.println(e);
			System.exit(1);
		}
		while (host.equals(""))
			host = new String(console.readLine("Please enter host name: "));
		while (roomname.equals(""))
			roomname = new String(console.readLine("Please enter room name: "));
		while (myname.equals(""))
			myname = new String(console.readLine("Please enter avatar name: "));

		RoomInterface roomI = findRoom(host, roomname, port);
		Crypto crypto = new Crypto();
		System.out.print("DEBUG: creating Crypto Keys...");
		crypto.generateDHKeyPair();
		crypto.generateRSAKeyPair();
		PublicKey room_RSA_public_key, room_DH_public_key;
		System.out.println(" done");

		boolean pwset = false, success = false;
		int status = -1;		//-1: undefined, 0: new user, 1: registered user, 2: online user
		try{
			do {
				System.out.print("DEBUG: checking user status...");
				status = roomI.userStatus(myname);
				System.out.println(" done");
				switch (status){
					case 0:
						key = new String(console.readLine("Please enter registration key: "));
						passwd = new String(console.readPassword(
												"Please enter a password for your account: "));
						pwset = true;
						if (!roomI.registerClient(myname, key, passwd)){
							System.out.println("Failed to register User " + myname);
							break;
						}
						else{
							System.out.println("Successfully registered User " + myname);
						}			//continue with login -> no break
					case 1:
						if (!pwset)
							passwd = new String(console.readPassword("Password: "));
						//System.out.println("DEBUG: DHpubKey:"+crypto.getDHPublicKey());
						//System.out.println("DEBUG: RSApubKey:"+crypto.getRSAPublicKey());
						room_DH_public_key = roomI.login(myname, passwd, crypto.getDHPublicKey(),
															crypto.getRSAPublicKey());
						success = (room_DH_public_key != null);
						if(success){
							crypto.computeSharedSecret(room_DH_public_key);
							crypto.setForeignRSAKey(roomI.getRSAPublicKey(myname));
							System.out.println("Welcome " + myname);
						}
						else
							System.out.println("Wrong password");
						break;
					case 2:
						System.out.println("User already logged in!");
						break;
					default:		//should never happen
						System.out.println("Undefined user status!");
				}
				if (!success)
					myname = new String(console.readLine("Please enter avatar name: "));
			} while(!success);
		} catch (RemoteException e){
			System.out.println("RemoteException in registerClient or login or userStatus" + e);
			System.exit(1);
		}

		Date date = loadDate(roomname, myname);	//TODO: check dates -> test

		//adding ShutdownHook
		Runtime.getRuntime().addShutdownHook(new ClientShutdownThread(
											roomI, myname, date, roomname));
		System.out.println("Shutdown hook added");

		Gui gui;
		if (graphical)
			gui = new Gui(roomI, myname, crypto);
		else{
			//adding ConsoleRefresher
			ConsoleRefresher refresher = new ConsoleRefresher(myname, date, roomI, crypto);
			refresher.start();
	
			String input = "";
			Message m = new Message();
			while (true){
				input = new String(console.readLine());
				if (!input.equals("") && input.charAt(0) == '!'){
					if (input.equals("!logout") || input.equals("!exit")) //idea: client side command method
						System.exit(0);
					m = new Message(myname, input.substring(1));
					m.sign(crypto);			//XXX:or crypto.encrypt(m)? also TextInputListener
					m.encrypt(crypto);		//XXX:only encrypts message+hash, not author+date
					try{
						//System.out.println("DEBUG: injecting command: "+m);
						m = roomI.injectCommand(m);
						System.out.println(m);
					} catch (RemoteException e){
						System.out.println("RemoteException on injectCommand!\n"+ e);
						System.exit(1);
					}
				}
				if (!input.equals("") && input.charAt(0) != '!'){
					m = new Message(myname, input);
					m.sign(crypto);			//XXX:or crypto.encrypt(m)?
					m.encrypt(crypto);		//XXX:only encrypts message+hash, not author+date
					try{
						//System.out.println("DEBUG: submiting message: "+m);
						roomI.submitMessage(m);
					} catch (RemoteException e){
						System.out.println("RemoteException on submitMessage!\n"+ e);
						System.exit(1);
					}
				}
			}
		}
	}
}
