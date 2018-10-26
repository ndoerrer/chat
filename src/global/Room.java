package chAT.global;

import java.util.Vector;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Room implements RoomInterface, Serializable{
	private Vector<Message> messages;
	private int length;

	public Room(){
		length = 0;
		messages = new Vector<Message>();
	}

	public int addMessage(Message m){
		messages.add(m);
		//sending m to the clients?
		//archive if too long?
		length += 1;
		System.out.println("element added.");
		return 0;
	}

	public List<Message> getNewerThan(Date dIn){
		int index = 0, i;
		for (i=length-1; i>0; i--){
			if (messages.elementAt(i).getDate().compareTo(dIn) < 0)
				index = i+1;
		}
		if (index != length)
			System.out.println("DEBUG: index = "+index+", length = "+length);
		//if index == length... return empty
		return messages.subList(index, length);
	}

	public int getLength(){
		return length;
	}
}
