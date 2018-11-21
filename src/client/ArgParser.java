package chAT.client;

public class ArgParser{
	String hostname;
	String myname;
	String roomname;
	boolean graphical;
	int port;

	public void setDefaults(){
		hostname = "";			//"127.0.0.1";
		myname = "";			//"dummy";
		roomname = "default";	//"default";
		graphical = false;
		port = 1099;
	}

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

	public String getHost(){
		return hostname;
	}

	public String getName(){
		return myname;
	}

	public String getRoom(){
		return roomname;
	}

	public boolean isGraphical(){
		return graphical;
	}

	public int getPort(){
		return port;
	}
}
