package chAT.global;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Vector;

public interface RoomInterface extends Remote {
	public boolean registerClient(String name) throws RemoteException;
	public boolean addMessages() throws RemoteException;
	public boolean submitMessage(Message m, String name) throws RemoteException;
	public Vector<Message> requestNewMessages(Date date) throws RemoteException;
}
