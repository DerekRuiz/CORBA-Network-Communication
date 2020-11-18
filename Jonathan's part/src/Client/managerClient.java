package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class managerClient {

    public static void main(String[] args){

        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to the Store Server interface");
        System.out.println("Please enter your manager ID:");
        String managerID = sc.nextLine();
        String branchID = managerID.substring(0,2);

        int UDPPort = 0;
        if(branchID.equals("QC"))
            UDPPort = 2000;
        else if (branchID.equals("ON"))
            UDPPort = 3000;
        else if (branchID.equals("BC"))
            UDPPort = 4000;
        else{
            System.out.println("wrong branchID");
            System.exit(0);
        }

        if(managerID.charAt(2) != 'M'){
            System.out.println("you are not a manager");
            System.exit(0);
        }

        Logger logger = Logger.getLogger(managerClient.class.getName());
        FileHandler fh;
        try {
            fh = new FileHandler("/Users/jonathanfrey/Documents/java_store_imp/src/Logs/" +managerID+ " Manager Log.txt");
            logger.setUseParentHandlers(false);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        }
        catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        String login = "Login Successfull. | Manager ID: " + managerID + " | Branch ID: " + branchID;
        logger.info(login);
        System.out.println(login);

        for(;;){

            System.out.println("OPTIONS:\n1: Add Item\n2: Remove Item\n3: List Item Availability\n4:End program");
            int option = sc.nextInt();
            if(option ==4) break;
            sc.nextLine();

            System.out.println("Enter inputs seperated by a comma:");
            String inputs = sc.nextLine();
            String m="";

            switch (option) {
                case 1:
                    m = "M1"+inputs;
                    break;
                case 2:
                    m = "M2"+inputs;
                    break;
                case 3:
                    m = "M3"+inputs;
                    break;
            }
            String result="";
            DatagramSocket aSocket = null;
            try{
                aSocket = new DatagramSocket();
                byte [] message = m.getBytes();
                InetAddress aHost = InetAddress.getByName("localhost");

                int serverPort = UDPPort;
                DatagramPacket request = new DatagramPacket(message, m.length(), aHost, serverPort);
                aSocket.send(request);

                byte [] buffer = new byte[1000];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

                aSocket.receive(reply);
                result = new String(reply.getData()).trim();
            }
            catch(SocketException e){
                System.out.println("Socket: "+e.getMessage());
            }
            catch(IOException e){
                e.printStackTrace();
                System.out.println("IO: "+e.getMessage());
            }
            finally{
                if(aSocket != null) aSocket.close();
            }

            logger.info(result);
            System.out.println(result);
        }
        System.exit(0);
    }
}
