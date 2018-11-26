package chAT.gui;

import chAT.global.*;

import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import java.awt.Dimension;

public class Gui{
	JPanel content_pane;
	MessagePanel m_panel;
	JPanel m_space;
	JFrame frame;
	JTextField input_field;

	RoomInterface roomI;
	String myname;
	Crypto crypto;

	public Gui(RoomInterface roomI_in, String myname_in, Crypto crypto_in){
		roomI = roomI_in;
		myname = myname_in;
		crypto = crypto_in;

		m_space = new JPanel();
		m_space.setLayout(new BoxLayout(m_space, BoxLayout.Y_AXIS));		//typical java shit...
		m_panel = new MessagePanel(m_space, roomI, myname, crypto);
		m_panel.setPreferredSize(new Dimension(800, 760));	//TODO: improve

		input_field = new JTextField();
		input_field.addActionListener(new TextInputListener(input_field, roomI, myname, crypto));

		content_pane = new JPanel(new BorderLayout());
		content_pane.add(m_panel, BorderLayout.NORTH);
		content_pane.add(input_field, BorderLayout.SOUTH);
		

		frame = new JFrame("chAT gui");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(content_pane);
		frame.setSize(800, 800);
		//frame.pack(); 		//-> kills the size preferences
		frame.setVisible(true);
	}
}
