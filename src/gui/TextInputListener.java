package chAT.gui;

import chAT.global.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;

import java.rmi.RemoteException;

/**	TextInputListener class
*	This class keeps track of user text input and takes care of submitting
*	Messages and commands to the Room.
*/
public class TextInputListener implements ActionListener{
	RoomInterface roomI;
	String myname;
	Crypto crypto;
	JTextField input_field;
	MessagePanel m_panel;

	/**	TextInputListener constructor
	*	This constructor initializes a TextInputListener with a JTextField to
	*	read from, a RoomInterface to communicate with, a name, a Crypto
	*	instance and a MessagePanel.
	*	@param input_field_in: JTextField to listen to.
	*	@param roomI_in: RoomInterface to send Messages to.
	*	@param myname_in: name of the Client.
	*	@param crypto_in: Crypto instance to use for signing and encrypting.
	*	@param m_panel_in: MessagePanel to write output to.
	*/
	public TextInputListener(JTextField input_field_in, RoomInterface roomI_in,
						String myname_in, Crypto crypto_in, MessagePanel m_panel_in){
		super();
		roomI = roomI_in;
		myname = myname_in;
		crypto = crypto_in;
		input_field = input_field_in;
		m_panel = m_panel_in;
	}

	/**	actionPerformed method
	*	This method is called upon ActionEvent occuring.
	*	It submits a new message to the room or injects a command.
	*	@param evt: Event that occured (e.g. Enter Press)
	*/
	public void actionPerformed(ActionEvent evt) {
		String text = input_field.getText();
		input_field.setText("");
		Message m;
		if (!text.equals("") && text.charAt(0) == '!'){
			if (text.equals("!logout") || text.equals("!exit"))	//idea: client side command method
				System.exit(0);	
			m = new Message(myname, text.substring(1));
			System.out.println("DEBUG: injecting command: "+m);
			m.sign(crypto);
			m.encrypt(crypto);
			try{
				m = roomI.injectCommand(m);
				m_panel.addMessage(m);
			} catch (RemoteException e){
				System.out.println("RemoteException on injectCommand!\n"+ e);
				System.exit(1);
			}
		}
		if (!text.equals("") && text.charAt(0) != '!'){
			m = new Message(myname, text);
			m.sign(crypto);
			m.encrypt(crypto);
			try{
				//System.out.println("DEBUG: submiting message: "+m);
				roomI.submitMessage(m);
			} catch (RemoteException e){
				System.out.println("RemoteException on submitMessage!\n"+ e);
				System.exit(1);
			}
		}
	}
}
