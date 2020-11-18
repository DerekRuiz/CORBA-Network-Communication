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

public class customerClient {

    public static void main(String[] args){

        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to the Store Server interface");
        System.out.println("Please enter your customer ID");
        String customerID = sc.nextLine();
        String branchID = customerID.substring(0,2);

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

        if(customerID.charAt(2) != 'U'){
            System.out.println("you are not a customer");
            System.exit(0);
        }

        Logger logger = Logger.getLogger(customerClient.class.getName());
        FileHandler fh;
        try {
            fh = new FileHandler("/Users/jonathanfrey/Documents/java_store_imp/src/Logs/" +customerID+ " Customer Log.txt");
            logger.setUseParentHandlers(false);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        }
        catch (SecurityException | IOException e) {
            e.printStackTrace();
        }


        String login = "Login Successfull. | Customer ID: " + customerID + " | Branch ID: " + branchID;
        logger.info(login);
        System.out.println(login);

        for (; ; ) {

            System.out.println("OPTIONS:\n1: Purchase Item\n2: Find Item\n3: Return Item\n4: ExchangeItem \n5:End program");
            int option = sc.nextInt();
            if (option == 5) break;
            sc.nextLine();

            System.out.println("Enter inputs separated by a comma:");
            String inputs = sc.nextLine();
            String m = "";

            switch (option) {
                case 1:
                    m = "C1"+inputs;
                    break;
                case 2:
                    m = "C2"+inputs;
                    break;
                case 3:
                    m = "C3"+inputs;
                    break;
                case 4:
                    m = "C4"+inputs;
                    break;
            }
            String result = "";
            DatagramSocket aSocket = null;
            try {
                aSocket = new DatagramSocket();
                byte[] message = m.getBytes();
                InetAddress aHost = InetAddress.getByName("localhost");

                int serverPort = UDPPort;
                DatagramPacket request = new DatagramPacket(message, m.length(), aHost, serverPort);
                aSocket.send(request);

                byte[] buffer = new byte[1000];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

                aSocket.receive(reply);
                result = new String(reply.getData()).trim();
            } catch (SocketException e) {
                System.out.println("Socket: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("IO: " + e.getMessage());
            } finally {
                if (aSocket != null) aSocket.close();
            }
            logger.info(result);
            System.out.println(result);
        }
        System.exit(0);
    }
}
