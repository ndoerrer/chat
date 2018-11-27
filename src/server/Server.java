package chAT.server;

import chAT.global.*;

import java.util.Date;
import java.util.Arrays;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.net.MalformedURLException;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import java.net.NetworkInterface;
import java.util.Enumeration;

class ServerShutdownThread extends Thread {
	private Room room;

	public ServerShutdownThread(Room room_in) {
		super();
		room = room_in;
	}

	public void run() {
		boolean success = false;
		success = room.shutdown();
		if (success)
			System.out.println("Sent logout to all clients");
		else
			System.out.println("Failed to send logout to clients!");
		try{
			Thread.sleep(1500);
		} catch(InterruptedException e){
			System.out.println("InterruptedException!");
		}
		System.out.println("Shutting down server");
		this.interrupt();
	}
}

public class Server{
	private static InetAddress checkInterfaces(){
		try {
			final Enumeration<NetworkInterface> netifs = NetworkInterface.getNetworkInterfaces();
	        while (netifs.hasMoreElements()){
				NetworkInterface netif = netifs.nextElement();
				Enumeration<InetAddress> i = netif.getInetAddresses();
				while (i.hasMoreElements()){
					InetAddress ia = i.nextElement();
					System.out.println(netif.getName()+": "+ ia.getHostAddress());
					if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() &&
																ia instanceof Inet4Address)
						return ia;
				}
			}
	    } catch (Exception e) {
	        System.out.println("Exception in getNetworkInterfaces");
			System.exit(1);
	    }
		return null;
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
		Room room;
		RoomInterface roomI;
		String room_name = (args.length > 0) ? args[0] : "default";
		boolean makeonetimekey = Arrays.asList(args).contains("--makeonetimekey");
		room = new Room(room_name, makeonetimekey);
		roomI = (RoomInterface) room;

		//adding ShutdownHook
		Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(room));
		System.out.println("Shutdown hook added");

		String host = checkInterfaces().getHostAddress();
		/*String host = "localhost";
		try{
			InetAddress ip = InetAddress.getLocalHost();
			host = ip.getHostAddress();
		} catch (UnknownHostException e){
			System.out.println("UnknownHostExpection in getLocalHost");
			System.exit(1);
		}*/
		int port = 1099;
		offer(roomI, host, room_name, port);
		Message m = new Message();
		while(true){
			try{
				roomI.addMessages();
			} catch (RemoteException e) {
				System.out.println("RemoteException in addMessages! [Server]");
				e.printStackTrace();
				System.exit(1);
			}
			try{
				Thread.sleep(50);	//exact time?
			} catch(InterruptedException e){
				System.out.println("InterruptedException!");
			}
		}
	}
}
