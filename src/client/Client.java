package chAT.client;

import chAT.global.*;

import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.Naming;
import java.net.MalformedURLException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Date;

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
		//String host = "10.196.230.245";		//"localhost";
		int port = 1099;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String host, input="", myname="", name = "chAT-test";		//idea: also as input?
		if (args.length > 0)
			host = args[0];
		else {
			System.out.print("Please enter host name: ");
			try{
				host = br.readLine();
			} catch (IOException e){
				System.out.println("IOException in host input!");
				return;
			}
		}
		RoomInterface roomI = findRoom(host, name, port);

		boolean success = false;
		try{
			do {
				System.out.print("Please enter avatar name: ");
				try{
					myname = br.readLine();
				} catch (IOException e){
					System.out.println("IOException in name input!");
					return;
				}
				success = roomI.registerClient(myname);
				if (!success)
					System.out.println("rejected!");
			} while(success == false);
			System.out.println("Successfully registered client");
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
					System.out.println("DEBUG: submiting message: "+m);
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
