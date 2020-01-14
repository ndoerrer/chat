package chAT.global;

import java.util.Date;
import java.io.Serializable;

/**	Message class
*	This class implements the functionality of a Message that can be exchanged.
*	It contains a text, an author, a date
*	and a signature (optional but encouraged).
*/
public class Message implements Serializable{
	private Date date;
	private String author;
	private String text;
	private boolean signed;
	private String signature;

	/**	Message default constructor
	*	This constructor creates a new dummy Message. It has the current
	*	date, no text, no author and no signature.
	*/
	public Message(){
		date = new Date(); 	//current time and date
		signature = "";
		author = "";
		text = "";
		signed = false;
	}

	/**	Message parameter constructor
	*	This constructor creates a new Message. author and text are given
	*	as parameters.
	*	@param authorIn: author to set for the new message.
	*	@param textIn: text of the message.
	*/
	public Message(String authorIn, String textIn){
		this();
		author = authorIn;
		text = textIn;
	}

	/**	Message copy constructor
	*	This constructor creates a new Message, copying another one.
	*	@param m: message to duplicate.
	*/
	public Message(Message m){
		this(m.getAuthor(), m.getText());
		date = m.getDate();
		signed = m.isSigned();
		signature = m.getSignature();
	}

	/**	sign method
	*	This method creates a signature for the message text and sets the
	*	signed flag accordingly.
	*	@param crypto: Crypto instance to get indentity for signing from.
	*	@returns true if signing was successful.
	*/
	public boolean sign(Crypto crypto){
		signature = crypto.getSignature(getText());
		if (signature != "")
			signed = true;
		return signed;
	}

	/**	verify method
	*	This method verifies ownership of a message instance.
	*	@param crypto: Crypto instance to check ownership for.
	*	@returns true if ownership was verified.
	*/
	public boolean verify(Crypto crypto){
		return crypto.verifySignature(text, signature, crypto.getForeignRSAKey());
	}

	/**	encrypt method
	*	This method encrypts the text and signature of a message
	*	(not author and date!!).
	*	@param crypto: Crypto instance to get keys for encryption from.
	*	@returns true if message was successfully encrypted.
	*/
	public boolean encrypt(Crypto crypto){
		text = crypto.encrypt(text);
		if (signed)
			signature = crypto.encrypt(signature);
		if (text == null || signature == null)
			return false;
		return true;
	}

	/**	decrypt method
	*	This method decrypts the text of a message.
	*	@param crypto: Crypto instance to get keys for decryption from.
	*	@returns true if message was successfully decrypted.
	*/
	public boolean decrypt(Crypto crypto){
		text = crypto.decrypt(text);
		if (signed)
			signature = crypto.decrypt(signature);
		if (text == null || signature == null)
			return false;
		return true;
	}

	/**	getText method
	*	This method returns the current text of the message (may be encrypted).
	*	@returns text of the message.
	*/
	public String getText(){
		return text;
	}

	/**	getAuthor method
	*	This method returns the author of the message.
	*	@returns author of the message.
	*/
	public String getAuthor(){
		return author;
	}

	/**	getDate method
	*	This method returns the date of the message.
	*	@returns date of the message.
	*/
	public Date getDate(){
		return date;
	}

	/**	getSignature method
	*	This method returns the signature of the message (may be encrypted).
	*	@returns signature of the message.
	*/
	public String getSignature(){
		return signature;
	}

	/**	clear method
	*	This method deletes the text of a message, leaving author and date
	*	intact. Signature is also deleted if any.
	*/
	public void clear(){
		text = "";
		if (signed)
			signature = "";
	}

	/**	isSigned method
	*	This method returns true, if the message is signed, false otherwise.
	*	@returns true, if message is signed.
	*/
	public boolean isSigned(){
		return signed;
	}

	/**	toString method
	*	This method returns a string representation of the message instance.
	*	It consists of the text followed by author and date.
	*	@returns String representation of the message.
	*/
@Override
	public String toString(){
		String s = text + "\t(" + author + ", " + date + ")";
		return s;
	}
}
