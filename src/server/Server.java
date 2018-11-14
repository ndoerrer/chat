package chAT.server;

import chAT.global.*;

import java.util.Date;
import java.util.Arrays;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.net.MalformedURLException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.net.NetworkInterface;
import java.util.Enumeration;

public class Server{
	private static void checkInterfaces(){
		try {
			final Enumeration<NetworkInterface> netifs = NetworkInterface.getNetworkInterfaces();
	        while (netifs.hasMoreElements()){
				NetworkInterface netif = netifs.nextElement();
				Enumeration<InetAddress> i = netif.getInetAddresses();
				while (i.hasMoreElements())
					System.out.println(netif.getName()+": "+ i.nextElement().getHostAddress());
			}
	    } catch (Exception e) {
	        System.out.println("Exception in getNetworkInterfaces");
			System.exit(1);
	    }
	}

	public static void offer(RoomInterface r, String host, String name, int port){
		try {
			LocateRegistry.createRegistry(port);
			System.out.println("Created registry on port " + port);
		} catch (RemoteException e){
			System.out.println("RemoteExpcetion in createRegistry: registry already exists");
		}

		try {
			Naming.rebind("//" + host + ":" + port + "/" + name, r);
			System.out.println("Binding RoomInterface to rmi://"+host+":"+port+"/"+name);
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
		RoomInterface room;
		String room_name = (args.length > 0) ? args[0] : "default";
		boolean makeonetimekey = Arrays.asList(args).contains("--makeonetimekey");
		room = new Room(room_name, makeonetimekey);

		checkInterfaces();
		String host = "localhost";
		try{
			InetAddress ip = InetAddress.getLocalHost();
			host = ip.getHostAddress();
		} catch (UnknownHostException e){
			System.out.println("UnknownHostExpection in getLocalHost");
			System.exit(1);
		}
		int port = 1099;
		offer(room, host, room_name, port);
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
