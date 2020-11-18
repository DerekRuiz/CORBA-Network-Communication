
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author DRC
 */
public class ReplicaManager {

    StoreServer qcStore;
    StoreServer onStore;
    StoreServer bcStore;

    DatagramSocket replicaSocket;

    private int resendDelay = 1000;

    public ReplicaManager() {
        qcStore = new StoreServer("QC", 5461);
        onStore = new StoreServer("ON", 5462);
        bcStore = new StoreServer("BC", 5463);
        try {
            this.replicaSocket = new DatagramSocket(6789, InetAddress.getByName("HostName"));
        } catch (SocketException ex) {
            System.out.println("Replica Manager cannot start since port is already bound");
            System.exit(1);
        } catch (UnknownHostException ex) {
            System.out.println("Replica Manager cannot start since host cannot be found");
            System.exit(1);
        }
        new Thread(() -> {
            waitForRequest();
        }).start();
    }

    private void sendReceivedMessage(InetAddress requestAddress, int requestPort) {
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            byte[] resultBytes = String.format("RECEIVED").getBytes();
            DatagramPacket request = new DatagramPacket(resultBytes, resultBytes.length, requestAddress, requestPort);
            sendSocket.send(request);
        } catch (IOException ex) {
        }
    }

    private void sendAnswerMessage(String message, InetAddress requestAddress, int requestPort) {
        boolean not_received = true;
        byte[] resultBytes = message.getBytes();
        DatagramPacket request = new DatagramPacket(resultBytes, resultBytes.length, requestAddress, requestPort);
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            sendSocket.setSoTimeout(this.resendDelay);
            while (not_received) {
                sendSocket.send(request);
                try {
                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    sendSocket.receive(reply);
                    String answer = new String(reply.getData());
                    if (answer.toUpperCase().equals("RECEIVED")) {
                        not_received = false;
                    }
                } catch (SocketTimeoutException e) {
                }
            }
        } catch (IOException ex) {
        }
    }

    private void waitForRequest() {
        try {
            byte[] buffer = new byte[1000];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            replicaSocket.receive(request);
            String message = new String(request.getData());

            while (Thread.activeCount() > 20) {
                Thread.yield();
            }
            new Thread(() -> {
                waitForRequest();
            }).start();

            String[] splitMessage = message.split("|");

            String sender = splitMessage[0];
            InetAddress senderAddress = InetAddress.getByName(sender);
            int senderPort = Integer.parseInt(sender);

            String query = splitMessage[1];
            String[] args = query.split(",");

            StoreServer usersStore = null;
            if (args[1].toUpperCase().contains("QC")) {
                usersStore = qcStore;
            } else if (args[1].toUpperCase().contains("ON")) {
                usersStore = onStore;
            } else if (args[1].toUpperCase().contains("BC")) {
                usersStore = bcStore;
            } else {
                this.sendAnswerMessage("ERROR:NOT A VALID STORE", senderAddress, senderPort);
                return;
            }
            
            String result;
            switch (args[0].trim()) {
                case "ADD":
                    result = usersStore.addItem(args[1].trim(), args[2].trim(), args[3].trim(), Integer.parseInt(args[4].trim()), Double.parseDouble(args[5].trim()));
                    break;
                case "REMOVE":
                    result = usersStore.removeItem(args[1].trim(), args[2].trim(), Integer.parseInt(args[3].trim()));
                    break;
                case "LIST":
                    String[] results = usersStore.listItemAvailability(args[1].trim());
                    result = Arrays.toString(results);
                    break;
                case "PURCHASE":
                    result = usersStore.purchaseItem(args[1].trim(), args[2].trim(), args[3].trim());
                    break;
                case "FIND":
                    results = usersStore.findItem(args[1].trim(), args[2].trim());
                    result = Arrays.toString(results);
                    break;
                case "RETURN":
                    result = usersStore.returnItem(args[1].trim(), args[2].trim(), args[3].trim());
                    break;
                case "EXCHANGE":
                    result = usersStore.exchangeItem(args[1].trim(), args[2].trim(), args[3].trim(), args[4].trim());
                    break;
                default:
                    result = "ERROR:NOT A VALID METHOD";
            }
            this.sendAnswerMessage(result, senderAddress, senderPort);

        } catch (SocketException ex) {
            System.out.println("Could not connect to port, canceling server");
            System.exit(1);
        } catch (IOException ex) {
        }
    }

}
