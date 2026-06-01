package ChatSystem.server;
import java.net.*;
import java.io.*;
import ChatSystem.shared.*;
import ChatSystem.server.service.*;

public class ClientHandler {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private MessageRouter messageRouter;
    


    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException ex){
            System.err.println("Error sending message: " + ex.getMessage());
        
        }
    }
    
    public ClientHandler(Socket socker, MessageRouter messageRouter){
        this.socket = socker;
        this.messageRouter = messageRouter;
        try{
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());
        }
        catch(IOException ex){
                System.err.println("Error initializing streams: " + ex.getMessage());
            }
    }

    public void listen(){
        if (in == null || out == null) {
            System.err.println("Error: Streams not initialized. Connection rejected.");
            return;
        }
        try{
            while(true){
                Message message = (Message) in.readObject();
                if(message == null) break;
                messageRouter.routeMessage(message, this);
            }
        } catch (IOException | ClassNotFoundException ex){
            System.err.println("Error: " + ex.getMessage());
        }
    }

}