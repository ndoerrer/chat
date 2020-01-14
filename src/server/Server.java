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
import java.net.Inet6Address;
import java.net.UnknownHostException;

import java.net.NetworkInterface;
import java.util.Enumeration;

/**	ServerShutdownThread class
*	This class is meant to be run as a Thread as shutdown hook for the
*	running Server program.
*	It takes care of a clean shutdown.
*/
class ServerShutdownThread extends Thread {
	private Room room;

	/**	ServerShutdownThread constructor
	*	This constructor creates a ServerShutdownThread instance
	*	bound to one specific Room.
	*	@param room_in: Room to bind to.
	*/
	public ServerShutdownThread(Room room_in) {
		super();
		room = room_in;
	}

	/**	run method
	*	This is the Threads main method. It performs a clean shutdown of
	*	the Room. The room is ordered to log out all clients. Afterwards
	*	this Thread waits for two seconds, then terminates.
	*/
	public void run() {
		boolean success = false;
		try {
			success = room.shutdown();
		} catch(RemoteException e){
			System.out.println("RemoteException in server shutdown");
		}
		if (success)
			System.out.println("Sent logout to all clients");
		else
			System.out.println("Failed to send logout to clients!");
		try{
			Thread.sleep(2000);
		} catch(InterruptedException e){
			System.out.println("InterruptedException!");
		}
		System.out.println("Shutting down server");
		this.interrupt();
	}
}

/**	Server class
*	This class handles all things on server side. It is meant to be run
*	as an executable class - program.
*	It creates a Room and an according RoomInterface, offers it via RMI
*	to Clients and keeps it running.
*/
public class Server{
	/**	checkInterfaces method
	*	This method tries to find all available network interfaces. Then
	*	it returns the "most appropriate" address.
	*	@returns address of the Server as String.
	*/
	private static String checkInterfaces(){
		String host="";
		boolean loopback = true;
		boolean renew = false;
		try {
			final Enumeration<NetworkInterface> netifs = NetworkInterface.getNetworkInterfaces();
	        while (netifs.hasMoreElements()){
				NetworkInterface netif = netifs.nextElement();
				System.out.println("checking interface " + netif.getName());
				Enumeration<InetAddress> i = netif.getInetAddresses();
				while (i.hasMoreElements()){
					InetAddress ia = i.nextElement();
					System.out.println("\tfound address: "+ ia.getHostAddress());
					System.out.println("\t\tlinklocal: "+ ia.isLinkLocalAddress());
					System.out.println("\t\tloopback: "+ ia.isLoopbackAddress());
					if (ia instanceof Inet6Address){ //local loopback < (nonlocal loopback) < local nonloopback < nonlocal nonloopback
						if (host.equals(""))
							renew = true;
						if (!ia.isLoopbackAddress() && loopback)
							renew = true;
						if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress())
							renew = true;
						if(renew){
							host = ia.getHostAddress();
							int percent_index = host.indexOf("%");
							host = "["+host.substring(0, percent_index)+"]";
							if (!ia.isLoopbackAddress())
								loopback = false;
							renew = false;
						}
					}
				}
			}
	    } catch (Exception e) {
	        System.out.println("Exception in getNetworkInterfaces");
			System.exit(1);
	    }
		System.out.println("could only find linklocal or loopback addresses");
		return host;
	}

	/**	offer method
	*	This method gets a RoomInterface, host, name and port and creates a
	*	RMI-registry at the port to then make the RoomInterface accessible
	*	over the RMI-registry at given host, port and name.
	*	@param r: RoomInterface to offer.
	*	@param host: host name of the rmi-registry (probably own).
	*	@param name: name of the Room to offer.
	*	@param port: port to offer the RoomInterface on.
	*/
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

	/**	main method
	*	This method handles the main execution flow of the Server program.
	*	@param args: Command line arguments (args[0] is name)
	*/
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

		String host = checkInterfaces();		//TODO: network interface as parameter
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
