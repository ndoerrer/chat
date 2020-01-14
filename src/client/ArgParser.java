package chAT.client;

/** ArgParser class
*	This class represents an command line argument parser for the chat client.
*	It can extract hostname, clientname, port, roomname and whether or not
*	to run a graphical user interface.
*/
public class ArgParser{
	String hostname;	// url of the host (enclosed in [] for ipv6)
	String myname;		// nickname of the client when connecting
	String roomname;	// name of the room to connect to
	boolean graphical;	// if true, client runs a gui
	int port;			// port to reach hosts rmi-registry under

	/**	ArgParser Constructor
	*	This constructor takes the command line parameter array of strings
	*	as arguments. It extracts the parameters to parse and extracts them.
	*	@param args: array of strings as command line parameters.
	*	@throws Illegal ArgumentException: if an argument is not parsable.
	*/
	public ArgParser(String [] args) throws IllegalArgumentException{
		setDefaults();
		for(int i=0; i<args.length; i++){
			switch(args[i]){
				case "--host":
				case "-h":
					if (i == args.length-1)
						throw new IllegalArgumentException("No hostname specified!");
					hostname = args[++i];
					break;
				case "--name":
				case "-n":
					if (i == args.length-1)
						throw new IllegalArgumentException("No name specified!");
					myname = args[++i];
					break;
				case "--room":
				case "-r":
					if (i == args.length-1)
						throw new IllegalArgumentException("No room specified!");
					roomname = args[++i];
					break;
				case "--port":
				case "-p":
					if (i == args.length-1)
						throw new IllegalArgumentException("No port specified!");
					port = Integer.parseInt(args[++i]);
					break;
				case "--graphical":
				case "--gui":
				case "-g":
					graphical = true;
					break;
				default:
					System.out.println("unrecognised argument " + args[i]);
			}
		}
	}

	/**	setDefaults method
	*	This method sets default values to set paramters not specified
	*	in command line arguments.
	*/
	public void setDefaults(){
		hostname = "";			//"127.0.0.1";
		myname = "";			//"dummy";
		roomname = "default";	//"default";
		graphical = false;
		port = 1099;
	}

	/**	getHost method
	*	This method is a getter for the hostname variable.
	*	@returns hostname extracted from the args.
	*/
	public String getHost(){
		return hostname;
	}

	/**	getName method
	*	This method is a getter for the myname variable.
	*	@returns myname extracted from the args.
	*/
	public String getName(){
		return myname;
	}

	/**	getRoom method
	*	This method is a getter for the roomname variable.
	*	@returns roomname extracted from the args.
	*/
	public String getRoom(){
		return roomname;
	}

	/**	isGraphical method
	*	This method is a getter for the graphical variable.
	*	@returns graphical extracted from the args.
	*/
	public boolean isGraphical(){
		return graphical;
	}

	/**	getPort method
	*	This method is a getter for the port variable.
	*	@returns port extracted from the args.
	*/
	public int getPort(){
		return port;
	}
}
