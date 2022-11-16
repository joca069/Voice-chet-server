import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Main {
    public static int port=3069;
    public static int chunksize=1000000;

    public static byte[] removeZeros(byte[] original){
        int size=0;
        for(int i=original.length-1;i>=0;i--){
            if(original[i]!=0){
                size=i;
                break;
            }
        }
        byte[] ret=new byte[size];
        for(int i=0;i<ret.length;i++){
            ret[i]=original[i];
        }
        return ret;
    }
    public static void logError(Exception e){
        System.out.println(e);
    }

    public static List<User> users=new ArrayList<>();

    public static void sendToAllClients(DatagramSocket socket,int id,DatagramPacket packet){

        boolean exist=false;

        for(User user :users){


            if(id==user.id){
                exist=true;
                //update IP and port because of UDP ports gets closed after 30s
                user.ip=packet.getAddress();
                user.port=packet.getPort();
                user.resetTimer();

            }else{
                try{
                socket.send(new DatagramPacket(packet.getData(),packet.getData().length,user.ip,user.port));
                }catch (Exception eee){
                    logError(eee);
                }
            }
        }

        if(!exist){
            //create user if it doesnt exist
            User u=new User();
            u.id=id;
            u.ip=packet.getAddress();
            u.port=packet.getPort();
            users.add(u);
        }



    }


    public static void log(String s){
        System.out.println(s);
    }

    private static void server() throws Exception{
        DatagramSocket socket=new DatagramSocket(port);

        DatagramPacket packet=new DatagramPacket(new byte[chunksize],chunksize);
        while (true){
            try {
                socket.receive(packet);
                log("radi"+packet.getAddress().toString());
                byte[] data = removeZeros(packet.getData());

                //analysing data received
                byte[] idbytes = new byte[4];
                for (int i = 0; i < 4; i++) {
                    idbytes[i] = data[i];
                }
                int id = new BigInteger(idbytes).intValue();
                byte[] audio = new byte[data.length - 4];
                for (int i = 0; i < data.length - 4; i++) {
                    audio[i] = data[i + 4];
                }



                sendToAllClients(socket, id, new DatagramPacket(audio, audio.length, packet.getAddress(), packet.getPort()));

                //socket.send(new DatagramPacket(audio,audio.length,packet.getAddress(),packet.getPort()));

            }catch (Exception e){
                logError(e);
            }

        }
    }

    public static String hash(byte[] pozitive) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(pozitive);
            return Base64.getEncoder().encodeToString(messageDigest.digest());
        }catch (Exception e){
            return "error";
        }
    }

    private static byte[] encrypt(String key,byte[] text) throws Exception{
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(key.getBytes());
        messageDigest.digest();
        SecretKey aeskey =new SecretKeySpec(messageDigest.digest(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE,aeskey);
        return cipher.doFinal(text);
    }

    private static byte[] decrypt(String key,byte[] text) throws Exception{
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(key.getBytes());
        messageDigest.digest();
        SecretKey aeskey =new SecretKeySpec(messageDigest.digest(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE,aeskey);
        return cipher.doFinal(text);
    }

    public static void main(String[] args) throws Exception{

    server();



    }
}
