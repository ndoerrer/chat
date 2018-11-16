package chAT.global;

import java.util.Date;
import java.io.Serializable;

public class Message implements Serializable{
	private Date date;
	private String author;
	private String text;
	private boolean signed;
	private String signature;

	public Message(){
		date = new Date(); 	//current time and date
		signature = "";
		signed = false;
	}

	public Message(String authorIn, String textIn){
		this();
		author = authorIn;
		text = textIn;
	}

	public Message(Message m){
		this(m.getAuthor(), m.getText());
		signed = m.isSigned();
		signature = m.getSignature();
	}

	public boolean sign(Crypto crypto){
		signature = crypto.getSignature(getText());
		if (signature != "")
			signed = true;
		return signed;
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

	public String getSignature(){
		return signature;
	}

	public void clear(){
		text = "";
	}

	public boolean isSigned(){
		return signed;
	}

@Override
	public String toString(){
		String s = text + "\t(" + author + ", " + date + ")";
		return s;
	}
}
