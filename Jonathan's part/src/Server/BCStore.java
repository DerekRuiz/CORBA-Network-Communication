package store;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

public class BCStore {
    public static void main(String args[]) {

        HashMap<String, Item> BCitemList = new HashMap<>();
        Item a = new Item("BC1010", "pants", 10, 2);
        Item b = new Item("BC1011", "shirt", 10, 5);
        BCitemList.put(a.itemID,a);
        BCitemList.put(b.itemID, b);

        int BCPort = 4000;
        StoreServer BCStore = new StoreServer("BC",BCPort);

        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(BCPort);
            byte[] buffer;
            System.out.println("Server Started............");

            while (true) {

                buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);

                String request_string = new String(request.getData()).trim();
                System.out.println("Request received from client: " + request_string);
                String result="";
                String[] resultArray = null;
                DatagramPacket reply = null;

                if(request_string.startsWith("M1")) {
                    String[] inputs = request_string.substring(2).split(",");
                    result = BCStore.addItem(inputs[0],inputs[1],inputs[2],Integer.parseInt(inputs[3]),Integer.parseInt(inputs[4]));
                }
                else if(request_string.startsWith("M2")){
                    String[] inputs = request_string.substring(2).split(",");
                    result = BCStore.removeItem(inputs[0],inputs[1],Integer.parseInt(inputs[2]));
                }
                else if(request_string.startsWith("M3")){
                    String[] inputs = request_string.substring(2).split(",");
                    resultArray = BCStore.listItemAvailability(inputs[0]);
                }
                else if(request_string.startsWith("C1")) {
                    String[] inputs = request_string.substring(2).split(",");
                    result = BCStore.purchaseItem(inputs[0],inputs[1],inputs[2]);
                }
                else if(request_string.startsWith("C2")){
                    String[] inputs = request_string.substring(2).split(",");
                    resultArray = BCStore.findItem(inputs[0],inputs[1]);
                }
                else if(request_string.startsWith("C3")){
                    String[] inputs = request_string.substring(2).split(",");
                    result = BCStore.returnItem(inputs[0],inputs[1],inputs[2]);
                }
                else if(request_string.startsWith("C4")){
                    String[] inputs = request_string.substring(2).split(",");
                    result = BCStore.exchangeItem(inputs[0],inputs[1],inputs[2], null);
                }
                else if(request_string.startsWith("PFI")){
                    String[] inputs = request_string.substring(3).split(",");
                    result = BCStore.purchaseForeignItem(inputs[0],inputs[1],inputs[2]);
                }
                else if(request_string.startsWith("FFI")){
                    String input = request_string.substring(3);
                    resultArray = BCStore.findForeignItem(input);
                }
                else if(request_string.startsWith("RFI")){
                    String[] inputs = request_string.substring(3).split(",");
                    result = BCStore.returnForeignItem(inputs[0],inputs[1],inputs[2]);
                }

                if (resultArray != null) {
                	ByteArrayOutputStream out = new ByteArrayOutputStream();
					ObjectOutputStream outputStream = new ObjectOutputStream(out);
					outputStream.writeObject(resultArray);
					outputStream.close();
					out.toByteArray();
                	
                	reply = new DatagramPacket(out.toByteArray(), result.length(), request.getAddress(), request.getPort());
                }
                	
                aSocket.send(reply);
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }

    }
}
