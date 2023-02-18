
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class Client implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isSending = true;

    @Override
    public void run() {

        System.out.println("Running client side...");
        
        try {
            client = new Socket("127.0.0.1", 9999);
            this.out = new PrintWriter(client.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();
            System.out.println("Started thread...");
            
            String message;
            while((message = in.readLine()) != null){

                System.out.println(message);
            }
            // System.out.println("Skipped While");

        } catch (IOException e) {
            System.out.println("Error in client run()");
            shutdown();
        }        
    }

    public void shutdown(){

        if(client.isClosed()) return;

        isSending = false;
        
        try {
            in.close();
            out.close();
            client.close();

        } catch (IOException e) {
            System.out.println("Error in client shutdown()");
        }
    }

    class InputHandler implements Runnable {

        @Override
        public void run() {
            
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while(isSending){

                    String message = inReader.readLine();
                    if(message.startsWith("/quit")){
                        inReader.close();
                        shutdown();
                    }
                    else{
                        out.println(message);
                    }
                }

            } catch (IOException e) {
                System.out.println("Error in InputHandler run()");
            }
        }
    }

    public static void main(String[] args) {
        
        Client c = new Client();
        c.run();
    }
}