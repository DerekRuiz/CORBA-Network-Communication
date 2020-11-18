
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

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

    private static final int resendDelay = 1000;
    private int nextProcessID;
    private PriorityQueue<String> messageQueue;

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

        nextProcessID = 1;
        messageQueue = new PriorityQueue<>(new MessageStringComparator());
        new Thread(() -> {
            while (true) {
                waitForRequest();
            }
        }).start();
        new Thread(() -> {
            while (true) {
                while (messageQueue.isEmpty()) {
                    Thread.yield();
                }
                processRequest();
            }
        }).start();
    }

    /**
     * Sends a received message to a process.
     *
     * @param requestAddress the InetAdress of the machine
     * @param requestPort the port to connect to
     */
    private void sendReceivedMessage(InetAddress requestAddress, int requestPort) {
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            byte[] resultBytes = String.format("RECEIVED").getBytes();
            DatagramPacket request = new DatagramPacket(resultBytes, resultBytes.length, requestAddress, requestPort);
            sendSocket.send(request);
        } catch (IOException ex) {
        }
    }

    /**
     * Sends a reliable UDP request to another machine. This is reliable since
     * it sends the message, and if no response it returned in
     * 'ReplicaManager.resendDelay' amount of time then the message is resent.
     *
     * @param message the message to be sent through UDP request
     * @param requestAddress the InetAddress of the machine
     * @param requestPort the port to connect to
     */
    private void sendAnswerMessage(String message, InetAddress requestAddress, int requestPort) {
        boolean not_received = true;
        byte[] resultBytes = message.getBytes();
        DatagramPacket request = new DatagramPacket(resultBytes, resultBytes.length, requestAddress, requestPort);
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            sendSocket.setSoTimeout(ReplicaManager.resendDelay);
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

    /**
     * Waits for UDP requests. When a request is received, it added it to the
     * queue and sends a response that it was received.
     */
    private void waitForRequest() {
        try {
            byte[] buffer = new byte[1000];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            replicaSocket.receive(request);
            String message = new String(request.getData());
            this.sendReceivedMessage(request.getAddress(), request.getPort());
            messageQueue.add(message);
        } catch (SocketException ex) {
            System.out.println("Could not connect to port, canceling server");
            System.exit(1);
        } catch (IOException ex) {
        }
    }

    /**
     * Gets the next message in the queue and processes it. If the queue is
     * empty nothing is processed. If the message was already processed, meaning
     * the sequencer ID is less than the next ID to be processed, then the
     * message is not processed. If the message is too early, meaning the
     * sequencer ID is greater than the next id to be processed, then add it
     * back to the queue.
     */
    private void processRequest() {
        try {
            String message = messageQueue.poll();
            if (message == null) {
                return;
            }
            String[] splitMessage = message.split("|");

            String sender = splitMessage[0];
            int processID = Integer.parseInt(sender);
            
            // Ensures that no duplicate message is processed.
            if (processID <= this.nextProcessID) {
                return;
            } else if (processID > this.nextProcessID + 1) {
                messageQueue.add(message);
                return;
            }

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
            this.nextProcessID++;
        } catch (IOException ex) {
        }
    }

    private static class MessageStringComparator implements Comparator<String> {

        @Override
        public int compare(String str1, String str2) {
            int i1 = Integer.parseInt(str1.split(",")[0]);
            int i2 = Integer.parseInt(str2.split(",")[0]);
            return Integer.compare(i1, i2);
        }
    }

}
