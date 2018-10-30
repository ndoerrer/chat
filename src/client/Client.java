package chAT.client;

import chAT.global.*;

import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.net.MalformedURLException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Date;

public class Client{
	private static RoomInterface findRoom(String host, String name, int port) {
		RoomInterface roomI = null;
		Registry registry = null;
/*		try {
			//registry = LocateRegistry.getRegistry();
			//registry = LocateRegistry.getRegistry(host, port);
			//registry = LocateRegistry.getRegistry("rmi://" + host + ":" + port);
		} catch (RemoteException e){
			System.out.println("RemoteException: couldnt get Registry");
		}*/
		try{
			//d = (Discusser) registry.lookup("rmi://" + host + ":" + port + "/" + name);
			//Object o = registry.lookup(name);
			Object o = Naming.lookup("rmi://" + host + ":" + port + "/" + name);
			roomI = (RoomInterface) o;
			System.out.println ("RoomInterface (" + name + ") found");
		} catch (NotBoundException | RemoteException e) {
			System.out.println("NotBound- or RemoteExpcetion: couldnt lookup " + name);
			System.exit(1);
		} catch (MalformedURLException e){
			System.out.println("MalformedURLExpcetion: couldnt lookup " + name);
			System.exit(1);
		}
		return roomI;
	}

	public static void main(String [] args){
		String host = "192.168.1.4";		//"localhost";
		String name = "chAT-test";
		int port = 1099;
		String input="", myname="dummy";
		RoomInterface roomI = findRoom(host, name, port);

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Please enter name: ");
		try{
			myname = br.readLine();
		} catch (IOException e){
			System.out.println("IOException in name input!");
			return;
		}

		try{
			roomI.registerClient(myname);
		} catch (RemoteException e){
			System.out.println("RemoteException in registerClient");
			System.exit(1);
		}

		Date date = new Date();
		Message m = new Message();
		while (true){
			try{
				input = br.readLine();
			} catch (IOException e){
				System.out.println("IOException in get text input");
			}
			if (!input.equals("")){
				m = new Message(myname, input);
				try{
					System.out.println("submiting message: "+m);
					roomI.submitMessage(m, myname);
					input = "";
				} catch (RemoteException e){
					System.out.println("RemoteException on submitMessage!\n"+ e);
					System.exit(1);
				}
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
