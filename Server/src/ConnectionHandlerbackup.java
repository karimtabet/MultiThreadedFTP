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


/**ConnectionHandler deals with separate client threads connecting to the server at the same time.
 * @author Candidate No: 52655
 */

public class ConnectionHandler extends Thread {
    Socket socket;
    boolean running = true;
    BufferedReader forbiddenReader = null;
    String password = "enterprise"; //set server password here
    MessageDigest digest; //used for hashing to SHA-256
    byte[] pwDigest; //used to store hashed password
    

    public ConnectionHandler(Socket socket) {
    this.socket = socket;
    }
    
    public void run() {
        boolean authorised = false;
        boolean titled = false;
        BufferedReader bufRead = null;

        while (this.running) {
            try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
            
            if(isForbidden(socket)){
                try{
                    outToClient.writeBytes("2" + "\n");
                    outToClient.writeBytes("This client is forbidden!" + "\n" + "Terminating connection!" +"\n");
                    wait(1000);
                    closeThread();
                }
                catch(Exception e){
                }
            }
            else{
                try{
                    outToClient.writeBytes("1" + "\n");
                    outToClient.writeBytes("Please Enter Password" + "\n");
                }
                catch(Exception e){
                }
            }
            
            while (socket.isConnected()) {
                File dir = new File(".");
                File files[] = dir.listFiles();
                Arrays.sort(files);
                boolean fileFound = false;
                
                //User messages.
                String clientInput = inFromClient.readLine();
                
                // Check password with SHA-256.
                if (!authorised) {
                    digest = MessageDigest.getInstance("SHA-256");
                    digest.update(password.getBytes("UTF-8"));
                    pwDigest = digest.digest();
                    if (clientInput.equals(pwDigest)) {
                        outToClient.writeBytes("1" + "\n");
                        outToClient.writeBytes("Password Accepted!" + "\n");
                        authorised = true;
                    } else {
                        outToClient.writeBytes("2" + "\n");
                        outToClient.writeBytes("Incorrect Password!" + "\n"
                                + "Terminating Connection!" + "\n");
                        wait(1000);
                        closeThread();
                        inFromClient.close();
                    }
                    
                // Check for command.
                } else {
                    //Deal with "rls" command.
                    if (clientInput.equals("rls")) {
                        outToClient.writeBytes(files.length + "\n");
                        for (int i = 0; i < files.length; i++) {
                            outToClient.writeBytes((files[i].toString()) + "\n");
                        }
                        
                    // Deals with "lls" command.    
                    } else if (clientInput.equals("lls")) {
                        outToClient.writeBytes("0" + "\n");
                        
                    // Deals with "get" command.
                    } else if (clientInput.substring(0,3).equals("get")) {
                        for (int i = 0; i < files.length; i++) {
                            if ((clientInput.length() > 3) && (files[i].toString().equals(clientInput.substring(4)))) {
                                if (!titled) { //for file title.
                                    outToClient.writeBytes(files[i].toString() + "\n"); 
                                    titled = true;
                                }
                                if (bufRead == null) {
                                    bufRead = new BufferedReader(new FileReader(files[i]));
                                }
                                String bufReadLine = bufRead.readLine();
                                if (bufReadLine != null) {
                                    outToClient.writeBytes(bufReadLine + "\n");
                                } else {
                                    outToClient.writeBytes("EOF" + "\n");
                                    bufRead.close();
                                    bufRead = null;
                                    titled = false;
                                    outToClient.writeBytes("1" + "\n");
                                    outToClient.writeBytes("File transfered!" + "\n");
                                }
                                fileFound = true;
                            }
                        }
                        if (!fileFound) {
                            outToClient.writeBytes("File not found!" + "\n");
                            outToClient.writeBytes("1" + "\n");
                            outToClient.writeBytes("File not found!" + "\n");
                        }
                        
                    // Deals with "put" command.
                    } else if (clientInput.substring(0,3).equals("put")) {
                        String inFromClientString = inFromClient.readLine();
                        if (!inFromClientString.equals("File not found!")) {
                            BufferedWriter bWriter = new BufferedWriter(new FileWriter(clientInput.substring(4))); //for file title.
                            while (!inFromClientString.equals("EOF")) {
                                bWriter.write(inFromClientString + "\n");
                                inFromClientString = inFromClient.readLine();
                        }
                        bWriter.close();
                        }
                        if (inFromClientString.equals("EOF")) {
                            outToClient.writeBytes("1" + "\n");
                            outToClient.writeBytes("File transfered!" + "\n");
                        }
                        
                    // Deals with file not found errors.
                    } else if (clientInput.equals("File not found!")) {
                        outToClient.writeBytes("1" + "\n");
                        outToClient.writeBytes("File not found!" + "\n");
                        
                    // Deals with exit command.
                    } else if (clientInput.equals("exit")) {
                        closeThread();
                        
                    // Deals with unknown commands.
                    } else {
                        outToClient.writeBytes("1" + "\n");
                        outToClient.writeBytes("Unknown Command!" + "\n");
                    }
                }
            }
        } catch(Exception e) {
            }
        }
    }
    
    public void wait (int n){
        long t0, t1;
        t0 =  System.currentTimeMillis();
        do{
            t1 = System.currentTimeMillis();
        }
        while (t1 - t0 < n);
    }
    
    public Boolean isForbidden(Socket socket) {
        Boolean forbidden = false;
        String line = "";
        String address = socket.getInetAddress().toString().substring(1);
        try {
            this.forbiddenReader = new BufferedReader(new FileReader("forbidden.txt"));
            line = this.forbiddenReader.readLine();
        } catch (Exception e) {
            System.out.println("Error" + e);
        }
        while (line != null) {
            try {
                String addressString = InetAddress.getByName(line).toString();
                addressString = addressString.substring(addressString.indexOf("/") + 1);
                if (address.equals(addressString)) {
                    forbidden = true;
                }
            } catch (Exception e) {
                System.out.println("Error" + e);
            }
            if (!forbidden) {
                try {
                    line = this.forbiddenReader.readLine();
                } catch (Exception e) {
                    System.out.println("Error" + e);
                }
            } else {
                line = null;
            }
        }
        return forbidden;
    }
    
    public void closeThread(){
        try{
            this.socket.close();
        }
        catch(Exception e){
        }
    }
}