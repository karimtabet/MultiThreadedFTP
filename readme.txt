Multi Threaded Byte Level File Transfer Protocol
Author: Karim Tabet
Version: 1.0 26/05/2011
  SERVER PASSWORD: enterprise
  
  How to use the protocol:

  1. Compile and run fss.java on a specific port (e.g. java fss 1420)

  2. Server will ask to set password. Enter password now.

  3. Compile and run fsc.java on the servers address and port. (e.g. java fsc localhost 1420)

  4. If connection is made, server will prompt for password. Enter password now.

  5. Enter 1 of 5 commands into fsc:
  	  i.   lls: list contents of local (fsc) directory.
	  ii.  rls: list contents of remote (fss) directory.
	  iii. put [name]: takes the  file [name] from the local directory (fsc) and saves it in the remote server's current directory (fss).
	  iv.  get [name]: gets the file [name] from the remote directory (fss) and saves it in the local server's current directory (fsc).
	  v.   exit: closes the client.


  The fss.java file can be found in cd Server/src, this is also the remote server's current directory.
  The fsc.java file can be found in cd Client/src, this is also the local server's current directory.

  NOTE! forbidden.txt is a file in Server/src that contains a list of addresses that the server will NOT allow to connect! Please check this
  file if you experience problems with connectivity to ensure your address is not listed or if you wish to block any connecting addresses!

  Enjoy! :)
