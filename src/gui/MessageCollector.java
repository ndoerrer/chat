package chAT.gui;

import chAT.global.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.Date;

import java.rmi.RemoteException;

/**	MessageCollector class
*	This class requests new messages and then adds them to the messagePanel
*/
public class MessageCollector implements ActionListener{
	MessagePanel m_panel;
	RoomInterface roomI;
	String myname;
	Date date;

	/** MessageCollector constructor
	*	This constructor initializes a new MessageCollector instance.
	*	It is set to collect for a given RoomInterface and name to put the
	*	new messages into a specific MessagePanel instance. A starting date
	*	is also provided to collect all messages sent after that date.
	*	@param m_panel_in: MessagePanel instance to output through.
	*	@param roomI_in: RoomInterface to request messages from.
	*	@param myname_in: name to request messages for.
	*	@param date_in: starting date of message requesting.
	*/
	public MessageCollector(MessagePanel m_panel_in, RoomInterface roomI_in,
							String myname_in, Date date_in){
		super();
		roomI = roomI_in;
		myname = myname_in;
		date = date_in;
		m_panel = m_panel_in;
	}

	/**	actionPerformed method
	*	This method is called on any ActionEvent occuring and triggeres the
	*	request of new messages for the Client. Those are then fed into the
	*	MessagePanel
	*	@param evt: Event that occured.
	*/
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
