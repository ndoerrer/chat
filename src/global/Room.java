package chAT.global;

import java.util.Vector;
import java.util.Date;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
//TODO: store/load room

public class Room extends UnicastRemoteObject implements RoomInterface{
	private Vector<Message> messages;
	private Vector<String> clients;	//TODO: at register: name must be unique!!
									// better identifier? ip+mac+random? ip+name? hash?
	private Vector<Boolean> new_message;
	private Vector<Message> client_messages;

	public Room() throws RemoteException{
		messages = new Vector<Message>();
		messages.add(new Message("system", "hello world"));
		clients = new Vector<String>();
		clients.add("system");
		new_message = new Vector<Boolean>();
		new_message.add(new Boolean(false));
		client_messages = new Vector<Message>();
		client_messages.add(null);
	}

	public boolean registerClient(String name) throws RemoteException{
		int index = clients.indexOf(name);
		if (index != -1)
			return false;
		clients.add(name);
		new_message.add(new Boolean(false));
		client_messages.add(null);
		return true;
	}//TODO: leave

	public boolean addMessages() throws RemoteException{//idea: sort client_messages copy by date
		boolean added = false;
		Message m;
		for (int i=0; i<client_messages.size(); i++){	//TODO: what if player leaves/enters
			m = client_messages.get(i);
			if (m != null){
				messages.add(new Message(m));
				client_messages.set(i, null);
				added = true;
			}
		}
		return added;
	}

	public boolean submitMessage(Message m, String name) throws RemoteException{
		int index = clients.indexOf(name);
		if (client_messages.get(index) != null)
			return false;
		else {
			client_messages.set(index, new Message(m));
			System.out.println("DEBUG: adding message " + client_messages.get(index));
			return true;
		}
	}

	public Vector<Message> requestNewMessages(Date date) throws RemoteException{
		Vector<Message> news = new Vector<Message>();
		Message m;
		for(int i=messages.size()-1; i>0; i--){
			m = messages.get(i);
			System.out.println("DEBUG: checking message " + i + ": " + m);
			if (m.getDate().after(date))
				news.add(0, m);
			else
				break;
		}
		System.out.println("DEBUG: adding news of lenght " + news.size());
		return news;
	}

	//TODO: get clients
}
