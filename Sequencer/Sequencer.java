import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class Sequencer {

    DatagramSocket sequencerSocket;
    DatagramSocket validitySocket;
    InetAddress address;
    int sequencer_port =2000;
    int sequence_number = 0;
    HashMap<InetAddress, Integer> RM_adresses = new HashMap<>();


    public Sequencer() throws UnknownHostException, SocketException {

        this.address = InetAddress.getLocalHost();
        this.sequencerSocket = new DatagramSocket(this.sequencer_port, this.address);
        this.validitySocket = new DatagramSocket(30000, this.address);

        RM_adresses.put(InetAddress.getByName(""),0);
        RM_adresses.put(InetAddress.getByName(""),2);
        RM_adresses.put(InetAddress.getByName(""),3);

    }

    public void run() throws IOException {

        boolean not_received;
        byte[] buffer;

        while (true) {

            buffer = new byte[1000];
            DatagramPacket FE_request = new DatagramPacket(buffer, buffer.length);
            sequencerSocket.receive(FE_request);
            String request = new String(FE_request.getData());

            String[] splitMessage = request.split("|");
            String header = splitMessage[0] +","+ sequence_number ;
            sequence_number++;

            String response = header + "|" + splitMessage[1];
            not_received =true;

            while (not_received) {

                for (InetAddress key : RM_adresses.keySet()) {
                    DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), key, RM_adresses.get(key));
                    sequencerSocket.send(reply);
                }
                try{
                    validitySocket.setSoTimeout(1000);
                    byte[] buff = new byte[1000];

                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    validitySocket.receive(reply);
                    String answer = new String(reply.getData());
                    if (answer.toUpperCase().equals("RECEIVED")) {
                        not_received = false;
                    }
                } catch (SocketTimeoutException e) {
                }
            }

        }
    }

}
