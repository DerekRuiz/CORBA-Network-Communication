import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Sequencer {
    public static void main(String args[]){
        int sequencer_port = 2000;
        int[] RM_ports = {5000,6000,7000};
        int sequence_number=0;

        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(sequencer_port);
            byte[] buffer = new byte[1000];
            while (true) {// non-terminating loop as the server is always in listening mode.
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String input = new String(request.getData());

                System.out.println("Request received from client: " + input);
                input +=sequence_number;

                for(int i=0;i<RM_ports.length;i++){
                    DatagramPacket reply = new DatagramPacket(input.getBytes(), input.length(), request.getAddress(), RM_ports[i]);
                    aSocket.send(reply);
                }

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
