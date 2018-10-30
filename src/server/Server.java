package chAT.server;

import chAT.global.*;

import java.util.List;
import java.util.Date;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.AlreadyBoundException;

import java.rmi.Naming;
import java.net.MalformedURLException;

public class Server{
	public static void offer(RoomInterface r, String host, String name, int port){
/*		Registry registry;
		try {
			registry = LocateRegistry.getRegistry();
			//registry = LocateRegistry.getRegistry("rmi://" + host + ":" + port);
			registry.rebind(name, r);
			System.out.println("Room (" + name + ") ready");
		} catch (RemoteException e) {
			System.out.println("RemoteException in offer method! [Server]");
			e.printStackTrace();
			System.exit(1);
		}*/

		try {
			LocateRegistry.createRegistry(port);
			System.out.println("Created registry on port " + port);
		} catch (RemoteException e){
			System.out.println("RemoteExpcetion in createRegistry: registry already exists");
		}

		try {
			Naming.rebind("//" + host + ":" + port + "/" + name, r);
			System.out.println("RoomInterface (" + name + ") ready");
		} catch (MalformedURLException e) {
			System.out.println("malformed URL in offer method!");
			System.exit(1);
		} catch (RemoteException e) {
			System.out.println("RemoteException in offer method!");
			//e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String [] args) throws RemoteException{		//TODO: better way??
		RoomInterface room = new Room();
	/*	} catch(RemoteException e){
			System.out.println("RemoteException in room creation");
			System.exit(1);
		}*/
		String host = "192.168.1.4";		//"localhost";		//myip
		String name = "chAT-test";
		int port = 1099;
		offer(room, host, name, port);
		Message m = new Message();
		while(true){
			try{
				room.addMessages();
			} catch (RemoteException e) {
				System.out.println("RemoteException in addMessages! [Server]");
				e.printStackTrace();
				System.exit(1);
			}
			try{
				Thread.sleep(50);	//TODO: exact time
			} catch(InterruptedException e){
				System.out.println("InterruptedException!");
			}
		}
	}
}
