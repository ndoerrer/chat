package chAT.gui;

import chAT.global.*;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import java.awt.Font;

import java.awt.Color;
import java.util.Date;

/**	MessagePanel class
*	This class is used to display Messages for the users using a GUI.
*	It refreshes at a certain rate and contains all incoming message stuff.
*/
public class MessagePanel extends JScrollPane{
	private RoomInterface roomI;
	private String myname;
	private Crypto crypto;
	private int delay;
	private final static int DEFAULT_DELAY = 200;
	private MessageCollector m_collector;
	private JPanel content;
	private JScrollBar scrollbar;
	private Font myFont;

	/**	MessagePanel constructor
	*	This constructor initializes a MessagePanel instance by setting up
	*	the RoomInterface, Crypto, name, font, delay and JPanel to write to.
	*	@param content_in: JPanel to write to.
	*	@param roomI_in: RoomInterface to bind to.
	*	@param myname_in: name of the Client.
	*	@param crypto_in: Crypto to use for signing and de/encrypting
	*	@font_in: text font to use, displaying messages.
	*	@delay_in: delay that controls the MessageCollector invokation frequency
	*/
	public MessagePanel(JPanel content_in, RoomInterface roomI_in, String myname_in,
											Crypto crypto_in, Font font_in, int delay_in){
		super(content_in);
		myFont = font_in;
		content = content_in;
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollbar = getVerticalScrollBar();
		roomI = roomI_in;
		myname = myname_in;
		crypto = crypto_in;
		delay = delay_in;
		m_collector = new MessageCollector(this, roomI, myname, new Date());//TODO load date
		new Timer(delay, m_collector).start();
	}

	/**	MessagePanel reduced constructor
	*	This constructor initializes a MessagePanel instance by setting up
	*	the RoomInterface, Crypto, name, font and JPanel to write to.
	*	Delay is chosen as default.
	*	@param content_in: JPanel to write to.
	*	@param roomI_in: RoomInterface to bind to.
	*	@param myname_in: name of the Client.
	*	@param crypto_in: Crypto to use for signing and de/encrypting
	*	@font_in: text font to use, displaying messages.
	*/
	public MessagePanel(JPanel content_in, RoomInterface roomI_in, String myname_in,
											Crypto crypto_in, Font font_in){
		this(content_in, roomI_in, myname_in, crypto_in, font_in, DEFAULT_DELAY);
	}

	/**	addMessage method
	*	This method adds a Message instance to the Panel.
	*	It is set in a color depending on the author.
	*	@param m: Message to be added.
	*/
	protected void addMessage(Message m){
		m.decrypt(crypto);
		JLabel label = new JLabel(m.toString());
		label.setFont(myFont);
		if (m.getAuthor().equals(myname))
			label.setBackground(new Color(51,204,255));
		else{
			if (m.getAuthor().equals("system"))
				label.setBackground(new Color(255,51,51));
			else
				label.setBackground(new Color(102,255,102));
		}
		label.setOpaque(true);	//to also point background
		//System.out.println("DEBUG: adding label " + m.toString());
		content.add(label);
		content.revalidate();

		//JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);//new
		//topFrame.revalidate();//new

		//content.repaint();	//TODO: doesnt work for gui+console communication?
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {	scrollbar.setValue( scrollbar.getMaximum() ); }
		});			//seriously, java?
	}
}
