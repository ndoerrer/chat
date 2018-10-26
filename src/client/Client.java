package chAT.client;

import chAT.global.*;

import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Client{
	private static DiscusserInterface findDiscusser(String host, String name, int port) {
		DiscusserInterface d = null;
		Registry registry = null;
		try {
			registry = LocateRegistry.getRegistry();
			//registry = LocateRegistry.getRegistry(host, port);
			//registry = LocateRegistry.getRegistry("rmi://" + host + ":" + port);
		} catch (RemoteException e){
			System.out.println("RemoteException: couldnt get Registry");
		}
		try{
			//d = (Discusser) registry.lookup("rmi://" + host + ":" + port + "/" + name);
			Object o = registry.lookup(name);
			System.out.println ("Found Object!");
			d = (DiscusserInterface) o;
			//d = (Discusser) registry.lookup(name);
			System.out.println ("DiscusserInterface (" + name + ") found");
		} catch (NotBoundException | RemoteException e) {
			System.out.println("NotBound- or RemoteExpcetion: couldnt lookup" + name);
			System.exit(1);
		}
		return d;
	}

	public static void main(String [] args){
		String host = "localhost";
		String name = "chAT-test";
		int port = 1099;
		String input="dummytext", myname="dummy";
		DiscusserInterface discusserI = findDiscusser(host, name, port);

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Please enter name:");
		try{
			myname = br.readLine();
		} catch (IOException e){
			System.out.println("IOException in name input!");
			return;
		}
		Message m;
		while (true){
			try{
				input = br.readLine();
			} catch (IOException e){
				System.out.println("IOException in get text input");
			}
			m = new Message(myname, input);
			try{
				System.out.println(m);
				discusserI.submitMessage(m);
			} catch (RemoteException e){
				System.out.println("RemoteException on submitMessage!\n"+ e);
			}
		}
	}
}
