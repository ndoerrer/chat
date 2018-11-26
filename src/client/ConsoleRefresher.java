package chAT.client;

import chAT.global.*;

import java.util.Date;
import java.rmi.RemoteException;
import java.util.Vector;

public class ConsoleRefresher extends Thread{
	private Date date;
	private RoomInterface roomI;
	private Crypto crypto;
	private String myname;
	private final int refresher_sleep = 100;

	public ConsoleRefresher(String myname_in, Date date_in, RoomInterface roomI_in,
											Crypto crypto_in){
		super();
		date = date_in;
		roomI = roomI_in;
		crypto = crypto_in;
		myname = myname_in;
	}

	public void run(){
		while(true){
			try{
				Vector<Message> all_news = roomI.requestNewMessages(date, myname);
				if (all_news == null && roomI.userStatus(myname) == 1){//TODO: faster userStatus
					System.out.println("The server is shutting down or you have been kicked!");
					System.exit(0);
				}
				for(Message news : all_news){
					news.decrypt(crypto);
					System.out.println(news);
					if(news.getDate().after(date))
						date = news.getDate();		//set date to newest received message date
				}
			} catch(RemoteException e){
				System.out.println("RemoteException on requestNewMessages!\n"+ e);
				System.exit(1);
			}
			try{
				Thread.sleep(refresher_sleep);	//exact time?
			} catch(InterruptedException e){
				System.out.println("InterruptedException!");
			}
		}
	}
}
