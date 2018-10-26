package chAT.server;

import chAT.global.*;

import java.util.List;
import java.util.Date;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.AlreadyBoundException;

public class Server{
	public static void offer(Discusser d, String host, String name, int port){
		Registry registry;
		try {
			LocateRegistry.createRegistry(port);
		} catch (RemoteException e){
			System.out.println("registry for port " + port + " already exists");
		}
		try {
			registry = LocateRegistry.getRegistry();
			//registry = LocateRegistry.getRegistry("rmi://" + host + ":" + port);
			registry.bind(name, d);
			System.out.println("Discusser (" + name + ") ready");
		} catch (AlreadyBoundException e) {
			System.out.println("Already bound exception! [Server]");
			System.exit(1);
		} catch (RemoteException e) {
			System.out.println("RemoteException in offer method! [Server]");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String [] args){
		Room room = new Room();
		Discusser discusser = null;
		try{
			discusser = new Discusser();
		} catch (RemoteException e){
			System.out.println("RemoteException on Discusser constructor!");
		}
		String host = "localhost";
		String name = "chAT-test";
		int port = 1099;
		offer(discusser, host, name, port);
		Message m = new Message();
		while(true){
			try{
				m = discusser.requestMessage();
			} catch (RemoteException e){
				System.out.println("RemoteException on requestMessage!");
			}
			if(m != null){
				room.addMessage(m);
				System.out.println(m);
			}
			try{
				Thread.sleep(1000);
			} catch(InterruptedException e){
				System.out.println("InterruptedException!");
			}
		}
	}
}
