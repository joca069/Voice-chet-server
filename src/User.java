import javax.swing.*;
import java.net.InetAddress;

public class User {

    public static int timeout=10000;
    public int id;

    //this thread removes this object from list of users (because after some time(30sec) client UDP port gets closed)
    private Thread delete;
    public int port;
    public InetAddress ip;


    public User(){
       generateDeleteThread();
        delete.start();
    }

    private int getIndex(){
        return Main.users.indexOf(this);
    }

    private void generateDeleteThread(){
        delete=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeout);
                    int index=getIndex();
                    Main.users.remove(index);
                }catch (Exception eee){
                }

                }
        });
    }
    public void resetTimer()  {
        delete.interrupt();
        generateDeleteThread();
        delete.start();
        }
}
