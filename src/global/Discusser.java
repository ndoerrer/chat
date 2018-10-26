package chAT.global;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Discusser extends UnicastRemoteObject implements DiscusserInterface{
	private static long nextID = 1;
	private final long ID;
	private Message sendBuf;	//as vector?
	private Message recvBuf;	//as vector?
	private boolean wantToSend;
	private boolean received;

	public Discusser() throws RemoteException{
		ID = nextID++;
		wantToSend = false;
		received = false;
		sendBuf = null;
		recvBuf = null;
	}

	public Message requestMessage() throws RemoteException{
		if (wantToSend){
			wantToSend = false;
			return new Message(sendBuf);
		}
		else
			return null;
	}

	public boolean submitMessage(Message m) throws RemoteException{
		if (wantToSend)
			return false;
		else{
			wantToSend = true;
			sendBuf = new Message(m);
			return true;
		}
	}
}
