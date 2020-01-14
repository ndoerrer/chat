package chAT.client;

import chAT.global.*;

import java.util.Date;
import java.rmi.RemoteException;
import java.util.Vector;

/**	ConsoleRefresher class
*	This class takes care of repeatedly at a fixed interval requesting updates
*	from the room. It also prints the news into the terminal. If a GUI is used,
*	ConsoleRefresher is not necessary.
*/
public class ConsoleRefresher extends Thread{
	private Date date;
	private RoomInterface roomI;
	private Crypto crypto;
	private String myname;
	private final int refresher_sleep = 200;

	/**	ConsoleRefresher constructor
	*	This constructor initializes a ConsoleRefresher instance.
	*	It sets myname and date according to inputs and stores references
	*	to the RoomInterface and the Crypto instance that is used.
	*	@param myname_in: nickname of the Client
	*	@param date_in: current Date (startpoint of updates)
	*	@param roomI_in: RoomInterface to bind ConsoleRefresher to
	*	@param crypto_in: Crypto instance to use
	*/
	public ConsoleRefresher(String myname_in, Date date_in, RoomInterface roomI_in,
											Crypto crypto_in){
		super();
		date = date_in;
		roomI = roomI_in;
		crypto = crypto_in;
		myname = myname_in;
	}

	/**	run method
	*	This is the threads main method and controls execution flow.
	*	New messages are requested and displayed. Afterwards it sleeps for
	*	refresher_sleep milliseconds.
	*/
	public void run(){
		while(true){
			try{
				Vector<Message> all_news = roomI.requestNewMessages(date, myname);
				if (all_news == null && roomI.userStatus(myname) == 1){//TODO: faster userStatus - also in gui
					System.out.println("The server is shutting down or you have been kicked!");
					System.exit(0);
				}
				for(Message news : all_news){
					news.decrypt(crypto);			//TODO verify signature of system
					System.out.println(news);
					if(news.getDate().after(date))
						date = news.getDate();		//set date to newest received message date
				}
			} catch(RemoteException e){
				System.out.println("RemoteException on requestNewMessages!\n"+ e);
				System.exit(1);
			}
			try{
				Thread.sleep(refresher_sleep);
			} catch(InterruptedException e){
				System.out.println("InterruptedException!");
			}
		}
	}
}
