
public class Server implements Runnable { // runnable means it can run on threads
    
    @Override
    public void run() {
        System.out.println("This is the run function");
    }
    
    public static void main(String[] args) {
        
        System.out.println("This is the main function");
    }
}