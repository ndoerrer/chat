package chAT.global;

import java.util.Vector;
import java.util.List;
import java.util.Date;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RoomInterface extends Remote {
	public int addMessage(Message m) throws RemoteException;
	public List<Message> getNewerThan(Date dIn) throws RemoteException;
}
