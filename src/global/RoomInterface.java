package chAT.global;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Vector;

public interface RoomInterface extends Remote {
	public int userStatus(String name) throws RemoteException;
	public boolean registerClient(String name, String key, String passwd) throws RemoteException;
	public boolean login(String name, String passwd) throws RemoteException;
	public boolean logout(String name) throws RemoteException;
	public boolean addMessages() throws RemoteException;
	public boolean submitMessage(Message m) throws RemoteException;
	public Vector<Message> requestNewMessages(Date date) throws RemoteException;
	public String makeOneTimeKey() throws RemoteException;
	public Message injectCommand(Message m) throws RemoteException;
	public String printHelp() throws RemoteException;
}
