package chAT.global;

import java.util.Date;
import java.io.Serializable;

public class Message implements Serializable{
	private Date date;
	private String author;
	private String text;

	public Message(){
		date = new Date(); 	//current time and date
	}

	public Message(String authorIn, String textIn){
		this();
		author = authorIn;
		text = textIn;
	}

	public Message(Message m){
		this(m.getAuthor(), m.getText());
	}

	public String getText(){
		return text;
	}

	public String getAuthor(){
		return author;
	}

	public Date getDate(){
		return date;
	}

	public String toString(){
		String s = text + "(" + author + ", " + date + ")";
		return s;
	}
}
