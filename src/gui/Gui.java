package chAT.gui;

import chAT.global.*;

import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Font;

/**	Gui class
*	The Gui class represents a possible user frontend for the Client class.
*	It handles input and output using threads.
*/
public class Gui{
	JPanel content_pane;
	MessagePanel m_panel;
	JPanel m_space;
	JFrame frame;
	JTextField input_field;

	RoomInterface roomI;
	String myname;
	Crypto crypto;

	/**	Gui constructor
	*	This constructor initializes the Gui with a RoomInterface to bind to,
	*	a Clients name and a crypto Instance.
	*	@param roomI_in: RoomInterface to bind to.
	*	@param myname_in: Name of the Client
	*	@param crypto_in: Crypto instance to use for signing and en/decryption.
	*/
	public Gui(RoomInterface roomI_in, String myname_in, Crypto crypto_in){
		roomI = roomI_in;
		myname = myname_in;
		crypto = crypto_in;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Font myFont = new Font("Serif", Font.PLAIN, 12);
		if (screenSize.getWidth() > 1500)
			myFont = new Font("Serif", Font.PLAIN, 24);
		if (screenSize.getWidth() > 3000)
			myFont = new Font("Serif", Font.PLAIN, 36);

		m_space = new JPanel();
		m_space.setLayout(new BoxLayout(m_space, BoxLayout.Y_AXIS));		//typical java shit...
		m_panel = new MessagePanel(m_space, roomI, myname, crypto, myFont);
		m_panel.setPreferredSize(screenSize);
		m_panel.setFont(myFont);

		input_field = new JTextField();
		input_field.setFont(myFont);
		input_field.addActionListener(new TextInputListener(input_field, roomI, myname, crypto, m_panel));

		content_pane = new JPanel();
		content_pane.setLayout(new BoxLayout(content_pane, BoxLayout.Y_AXIS));		//typical java shit...

		content_pane.add(m_panel);
		content_pane.add(input_field);
		

		frame = new JFrame("chAT gui");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(content_pane);
		frame.setSize(800, 800);
		//frame.pack(); 		//-> kills the size preferences
		frame.setVisible(true);
	}
}
