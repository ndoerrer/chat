package chAT.client;

import chAT.global.*;

import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.Naming;
import java.net.MalformedURLException;

import java.util.Date;

import java.io.Console;

class Shutdown {
	private Thread thread = null;
	private RoomInterface roomI;
	private String myname;
	private Date date;

	public Shutdown(RoomInterface roomI_in, String myname_in, Date date_in) {
		roomI = roomI_in;
		myname = myname_in;
		thread = new Thread("Shutdown_thread") {
			public void run() {
				while (true) {
					//System.out.println("[Sample thread] Sample thread speaking...");
					try {
						Thread.currentThread().sleep(1000);
					} catch (InterruptedException ie) {
						break;
					}
				}
			}
		};
		thread.start();
	}

	public void stopThread() {
		//TODO: save timestamp as file ".<myname>.time"
		boolean success = false;
		try{
			success = roomI.logout(myname);
		} catch (RemoteException e){
			System.out.println("RemoteException in logout!");
			thread.interrupt();
		}
		if (success)
			System.out.println("Logging out complete");
		else
			System.out.println("Logging out failed!");
		System.out.println("Shutting down");
		thread.interrupt();
	}
}

class ShutdownThread extends Thread {
	private Shutdown shutdown = null;

	public ShutdownThread(Shutdown shutdown) {
		super();
		this.shutdown = shutdown;
	}

	public void run() {
		shutdown.stopThread();
	}
}

public class Client{
	private static RoomInterface findRoom(String host, String name, int port) {
		RoomInterface roomI = null;
		try{
			Object o = Naming.lookup("rmi://" + host + ":" + port + "/" + name);
			System.out.println("Requesting Object rmi://" + host + ":" + port + "/" + name);
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
		int port = 1099;
        Console console = System.console();
		if (console == null) {
			System.out.println("Couldn't get Console instance");
			System.exit(0);
		}

		String host, input="", myname="", passwd="", key="";
		if (args.length > 0)
			host = args[0];
		else
			host = new String(console.readLine("Please enter host name: "));
		String room_name = new String(console.readLine("Please enter room name: "));
		if (room_name.equals(""))
			room_name = "default";
		RoomInterface roomI = findRoom(host, room_name, port);

		boolean pwset = false, success = false;
		int status = -1;		//-1: undefined, 0: new user, 1: registered user, 2: online user
		try{
			do {
				myname = new String(console.readLine("Please enter avatar name: "));
				status = roomI.userStatus(myname);
				switch (status){
					case 0:
						key = new String(console.readLine("Please enter registration key: "));
						passwd = new String(console.readPassword("Please enter a password for your account: "));
						pwset = true;
						if (!roomI.registerClient(myname, key, passwd)){
							System.out.println("Failed to register User " + myname);
							break;
						}
						else{
							System.out.println("Successfully registered User " + myname);
						}			//continue with login
					case 1:
						if (!pwset)
							passwd = new String(console.readPassword("Password: "));		//test it!
						success = roomI.login(myname, passwd);
						if(success)
							System.out.println("Welcome " + myname);
						else
							System.out.println("Wrong password");
						break;
					case 2:
						System.out.println("User already logged in!");
						break;
					default:		//should never happen
						System.out.println("Undefined user status!");
				}
			} while(!success);
		} catch (RemoteException e){
			System.out.println("RemoteException in registerClient");
			System.exit(1);
		}

		Date date = new Date();

		/* add shutdown hook */
		try {
			Shutdown shutdown = new Shutdown(roomI, myname, date);
			Runtime.getRuntime().addShutdownHook(new ShutdownThread(shutdown));
			System.out.println("Shutdown hook added");
		} catch (Throwable t) {
			System.out.println("Could not add Shutdown hook!");
    	}
		/* end of shutdown hook */

		Message m = new Message();
		boolean logged_in = true;
		while (logged_in){
			input = new String(console.readLine());
			if (!input.equals("")){
				if (input.charAt(0) == '!'){
					m = new Message(myname, input.substring(1));
					try{
						if (input.equals("!logout") || input.equals("!exit"))		//idea: client side command method
							System.exit(0);											//class variables for myname, etc
						System.out.println("DEBUG: injecting command: "+m);
						m = roomI.injectCommand(m);
						System.out.println(m);
					} catch (RemoteException e){
						System.out.println("RemoteException on injectCommand!\n"+ e);
						System.exit(1);
					}
				}
				else{
					m = new Message(myname, input);
					try{
						System.out.println("DEBUG: submiting message: "+m);
						roomI.submitMessage(m);
					} catch (RemoteException e){
						System.out.println("RemoteException on submitMessage!\n"+ e);
						System.exit(1);
					}
				}
				input = "";
			}
			try{
				for(Message news : roomI.requestNewMessages(date)){
					System.out.println(news);
					date = news.getDate();		//set date to newest received message date
				}
			} catch(RemoteException e){
				System.out.println("RemoteException on requestNewMessages!\n"+ e);
				System.exit(1);
			}
		}
	}
}
