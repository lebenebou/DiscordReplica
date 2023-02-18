
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable { // runnable means it can run on multiple threads
    
    private ArrayList<ConnectionHandler> connections; // list of clients that are connected
    private ServerSocket server;
    private boolean isAccepting;
    private ExecutorService pool;

    public Server(){
        connections = new ArrayList<ConnectionHandler>();
        isAccepting = true;
    }

    @Override
    public void run() {

        System.out.println("Starting server...");
        
        try{
            server = new ServerSocket(9999);
            this.pool = Executors.newCachedThreadPool();
            while(isAccepting){
            
                Socket client = server.accept();
    
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                this.pool.execute(handler);
            }
        }
        catch(IOException e){
            this.shutdown();
        }
    }

    public void shutdown(){

        if(server.isClosed()) return;
        isAccepting = false;

        try {
            server.close();
        }
        catch (IOException e) {
            System.out.println("Error in server shutdown()");
        }

        for(ConnectionHandler ch : connections){
            ch.shutdown();
        }
    }

    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ConnectionHandler(Socket c){
            client = c;
        }

        @Override
        public void run() {

            try {
                out = new PrintWriter(client.getOutputStream());
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                // Send something to client
                out.print("Hello User! Please enter your username: ");
                username = in.readLine();

                // check if username is valid ...

                System.out.println(username + " is connected!");
                broadcast(username + " joined the chatroom.");

                String user_message;
                while((user_message = in.readLine()) != null){

                    if(user_message.startsWith("/quit")){

                        broadcast(username + " has left the chatroom.");
                        this.shutdown();
                    }
                    else{
                        broadcast(username + ": " + user_message);
                    }
                }
            }
            catch (IOException e) {
                System.out.println("Error in ConnectionHandler run()");
                this.shutdown();
            }
        }

        public void sendMessage(String message){
            out.println(message);
        }

        public void broadcast(String message){

            for(ConnectionHandler ch : connections){

                if(ch == null) continue;

                ch.sendMessage(message);
            }
        }

        public void shutdown(){

            if(client.isClosed()) return;

            try {
                in.close();
                out.close();
                client.close();
            }
            catch (IOException e) {
                // ignore exception
            }
        }
    }

    public static void main(String[] args) {
        
        Server s = new Server();
        s.run();
    }
}