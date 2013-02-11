import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Arrays;

/**fsc is the client. It connects to a server and allows for commands and data to be sent between the client and server.
 * @author Candidate No: 52655
 */
class fsc {

	public static void main(String args[]) throws Exception {
		boolean authorised = false;
		boolean connected = true;
		boolean titled = false;
		BufferedReader bufRead = null;

		Socket clientSocket = new Socket(args[0], Integer.parseInt(args[1]));

		InetAddress host = clientSocket.getInetAddress();
		System.out.println(host);
		
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		while (connected) {
			File dir = new File(".");
			File files[] = dir.listFiles();
			Arrays.sort(files);
			boolean fileFound = false;
			boolean escapePutLoop = false;
			
			// Server messages.
			int fromServerLineSize = Integer.parseInt(inFromServer.readLine());
			for (int i = 0; i < fromServerLineSize; i++) {
				System.out.println(inFromServer.readLine());
				}
			
			// User input.
			System.out.print("Type here: ");
			String userInput = inFromUser.readLine();
			
			// Deals with supplying password to server using SHA-256.
			if (!authorised) {
				outToServer.writeBytes(userInput + "\n");
				authorised = true;
				
			// Deals with "lls" command if password is authorised.
			} else if (authorised && userInput.equals("lls")) {
				for (int i = 0; i < files.length; i++) {
					System.out.println((files[i].toString()));
				}
				outToServer.writeBytes(userInput + "\n");
				
			// Deals with "get" command if password is authorised.	
			} else if (authorised && userInput.substring(0,3).equals("get")) {
				outToServer.writeBytes(userInput + "\n");
				String fileFromServer = inFromServer.readLine();
				if (!fileFromServer.equals("File not found!")) { //Test if file not found.
					BufferedWriter bWriter = new BufferedWriter(new FileWriter(fileFromServer)); //for file title.				
					String inFromServerString = inFromServer.readLine();
					while (!inFromServerString.equals("EOF")) {
						bWriter.write(inFromServerString + "\n");
						outToServer.writeBytes(userInput + "\n");
						inFromServerString = inFromServer.readLine();
					}
					bWriter.close();
				}
				
			// Deals with "put" command if password is authorised.
			} else if (authorised && userInput.substring(0,3).equals("put")) {
				int i = 0;
				while (i < files.length && !escapePutLoop) {
					if ((userInput.length() > 3) && (files[i].toString().equals(userInput.substring(4)))) {
						if (!titled) { //for file title.
							outToServer.writeBytes(userInput + "\n"); 
							titled = true;
						}
						if (bufRead == null) {
							bufRead = new BufferedReader(new FileReader(files[i]));
						}
						String bufReadLine = bufRead.readLine();
						while (bufReadLine != null) {
							outToServer.writeBytes(bufReadLine + "\n");
							bufReadLine = bufRead.readLine();
						} if (bufReadLine == null) {
							outToServer.writeBytes("EOF" + "\n");
							bufRead.close();
							bufRead = null;
							titled = false;
							escapePutLoop = true;
							}
						fileFound = true;
					}
					i++;
				}
				if (!fileFound) {
					outToServer.writeBytes("File not found!" + "\n");
				}
				
			// Deals with "exit" command.	
			} else if (userInput.equals("exit")) {
				try {
				clientSocket.close();
				outToServer.writeBytes(userInput + "\n");
				connected = false;
				System.out.println("Client closed!");
				}
				catch (Exception e) {
					System.out.println("Error" + e);
				}			
				
			// Deals with "rls" command and invalid commands.
			} else {
				outToServer.writeBytes(userInput + "\n");
			}
		}
	}
}
