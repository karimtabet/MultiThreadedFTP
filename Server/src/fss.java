import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;

/**fss is the server and deals opens a welcome socket and a ConnectionHandler for every connected client.
 * @author Candidate No: 52655
 */

public final class fss {
    static String password;
    static BufferedReader inFromServer = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String args[]) throws Exception {
        ServerSocket welcomeSocket = new ServerSocket(Integer.parseInt(args[0]));
        System.out.print("Set Password: ");
        password = inFromServer.readLine();
        System.out.println("Server running...");

        while (true) {
            Socket clientSocket = welcomeSocket.accept();
            ConnectionHandler cHandler = new ConnectionHandler(clientSocket, password);
            cHandler.start();
        }
    }
}
