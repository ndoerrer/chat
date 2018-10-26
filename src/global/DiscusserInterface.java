package chAT.global;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DiscusserInterface extends Remote{
	public Message requestMessage() throws RemoteException;
	public boolean submitMessage(Message m) throws RemoteException;
}
