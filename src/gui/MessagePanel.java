package chAT.gui;

import chAT.global.*;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import java.awt.Font;

import java.awt.Color;
import java.util.Date;

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

	public MessagePanel(JPanel content_in, RoomInterface roomI_in, String myname_in,
											Crypto crypto_in, Font font_in){
		this(content_in, roomI_in, myname_in, crypto_in, font_in, DEFAULT_DELAY);
	}

	protected void addMessage(Message m){
		m.decrypt(crypto);
		JLabel label = new JLabel(m.toString());
		label.setFont(myFont);
		if (m.getAuthor().equals(myname))
			label.setBackground(new Color(51,204,255));
		else
			label.setBackground(new Color(102,255,102));
		label.setOpaque(true);
		//System.out.println("DEBUG: adding label " + m.toString());
		content.add(label);
		content.revalidate();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {	scrollbar.setValue( scrollbar.getMaximum() ); }
		});			//seriously, java?
	}
}
