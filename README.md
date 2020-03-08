# chAT

## A	General information

This is a lightweight chat client and server.
It utilizes java rmi calls to manage communication over network channels
(ipv4 or ipv6).

## B	Installation

To install both the client and the server simply clone the repository using

	git clone https://github.com/ndoerrer/chat

afterwards enter the directory chat and call

	ant

which is a java building tool.

## C	Setting up a Server

A server can be run calling the executable server class from the build directory
additionally specifying the hostname of your server (has to be reachable from
outside - take care of port forwarding etc...) and the argument --makeonetimekey
to initialize it for first use.

	java -Djava.rmi.server.hostname=<hostname> chAT.server.Server default --makeonetimekey

In case of an ipv6 hostname, the address must be enclosed in brackets [].

## D	Starting the Client and logging in to Server

From the build directory, the client ca be started with or without graphical
user interface (if without, simply omit the --gui):

	java chAT.client.Client --gui

You will be asked for a hostname to enter and an avatar name for you.
Afterwards you are asked for a onetimekey if your username is not known to the
server. You can copy it from the command line output of the server when it was
started with --makeonetimekey option. Then you need to register with a passphrase
that has to be repeated once (it is only stored as salted hash). After you
registered or simply logged in (if the user is already known to the server) you
can start chatting with anyone present using either the terminal or the gui.

## E	Commands from Client side

	!makeonetimekey			generates a new onetimekey for registration of a single new user
	!userlist				prints a list of all users currently logged in
	!help					prints some information about commands etc
	!exit					logs out from the server and exits the client
