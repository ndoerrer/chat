package chAT.gui;

import chAT.global.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;

import java.rmi.RemoteException;

public class TextInputListener implements ActionListener{
	RoomInterface roomI;
	String myname;
	Crypto crypto;
	JTextField input_field;
	MessagePanel m_panel;

	public TextInputListener(JTextField input_field_in, RoomInterface roomI_in,
									String myname_in, Crypto crypto_in, MessagePanel m_panel_in){
		super();
		roomI = roomI_in;
		myname = myname_in;
		crypto = crypto_in;
		input_field = input_field_in;
		m_panel = m_panel_in;
	}

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
