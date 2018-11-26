package chAT.gui;

import chAT.global.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.Date;

import java.rmi.RemoteException;

public class MessageCollector implements ActionListener{
	MessagePanel m_panel;
	RoomInterface roomI;
	String myname;
	Date date;


	public MessageCollector(MessagePanel m_panel_in, RoomInterface roomI_in,
							String myname_in, Date date_in){
		super();
		roomI = roomI_in;
		myname = myname_in;
		date = date_in;
		m_panel = m_panel_in;
	}

	public void actionPerformed(ActionEvent evt) {
		try{
			Vector<Message> all_news = roomI.requestNewMessages(date, myname);
			if (all_news == null && roomI.userStatus(myname) == 1){
				System.out.println("The server is shutting down or you have been kicked!");
				System.exit(0);
			}
			for(Message news : all_news){
				m_panel.addMessage(news);
				if(news.getDate().after(date))
					date = news.getDate();		//set date to newest received message date
			}
		} catch(RemoteException e){
			System.out.println("RemoteException on requestNewMessages!\n"+ e);
			System.exit(1);
		}
	}
}
