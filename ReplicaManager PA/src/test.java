
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author DRC
 */
public class test {

    DatagramSocket aSocket;

    public static void main(String[] args) throws UnknownHostException, SocketException, IOException, InterruptedException {
        ReplicaManager rm1 = new ReplicaManager(InetAddress.getLocalHost(), 6789, false, true);
        DatagramSocket aSocket = new DatagramSocket(7899, InetAddress.getLocalHost());

        test t = new test();

        new Thread(() -> {
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                try {
                    aSocket.receive(reply);
                } catch (IOException ex) {
                    Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
                }
                String answer = new String(reply.getData()).trim();
                System.out.println(answer);
                if (answer.equals("ERROR")) {
                    t.sendAnswerMessage(String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",0;ERROR,%s,%s", rm1.replicaSocket.getLocalAddress().getHostAddress(), rm1.replicaSocket.getLocalPort()), rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
                }

                t.sendReceivedMessage(reply.getAddress(), reply.getPort());
            }
        }).start();
t.sendAnswerMessage(String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",0;ADD,QCM1234,QC1234,ke,1,23"), rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
        Thread.sleep(1000);
        t.sendAnswerMessage(String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",0;CRASH,%s,%s", rm1.replicaSocket.getLocalAddress().getHostAddress(), rm1.replicaSocket.getLocalPort()), rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
        Thread.sleep(1000);
        t.sendAnswerMessage(String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",1;ADD,QCM1234,QC1234,ke,1,23"), rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
        Thread.sleep(1000);
        t.sendAnswerMessage(String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",2;ADD,QCM1234,QC1234,ke,1,23"), rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
        Thread.sleep(1000);
        t.sendAnswerMessage(String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",3;ADD,QCM1234,QC1234,ke,1,23"), rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
        Thread.sleep(1000);
        t.sendAnswerMessage(String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",4;ADD,QCM1234,QC1234,ke,1,23"), rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
        Thread.sleep(1000);
        t.sendAnswerMessage(String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",5;ADD,QCM1234,QC1234,ke,1,23"), rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
        t.sendAnswerMessage(String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",6;LIST,QCM1234"), rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
        /*byte[] resultBytes = String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",0;ADD,QCM1234,QC1234,ke,23,23").getBytes();
         DatagramPacket request = new DatagramPacket(resultBytes, resultBytes.length, rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
         aSocket.send(request);
         byte[] buffer = new byte[1000];
         DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
         try {
         aSocket.receive(reply);
         } catch (IOException ex) {
         Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
         }
         String answer = new String(reply.getData());
         System.out.println(answer);
         resultBytes = String.format("RECEIVED").getBytes();
         request = new DatagramPacket(resultBytes, resultBytes.length, reply.getAddress(), reply.getPort());
         aSocket.send(request);
        
         resultBytes = String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",1;ADD,QCM1234,QC1234,ke,23,23").getBytes();
         request = new DatagramPacket(resultBytes, resultBytes.length, rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
         aSocket.send(request);
        
         buffer = new byte[1000];
         reply = new DatagramPacket(buffer, buffer.length);
         try {
         aSocket.receive(reply);
         } catch (IOException ex) {
         Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
         }
         answer = new String(reply.getData());
         System.out.println(answer);
         resultBytes = String.format("RECEIVED").getBytes();
         request = new DatagramPacket(resultBytes, resultBytes.length, reply.getAddress(), reply.getPort());
         aSocket.send(request);
        
         resultBytes = String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",2;ADD,QCM1234,QC1234,ke,23,23").getBytes();
         request = new DatagramPacket(resultBytes, resultBytes.length, rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
         aSocket.send(request);
         buffer = new byte[1000];
         reply = new DatagramPacket(buffer, buffer.length);
         try {
         aSocket.receive(reply);
         } catch (IOException ex) {
         Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
         }
         answer = new String(reply.getData());
         System.out.println(answer);
         resultBytes = String.format("RECEIVED").getBytes();
         request = new DatagramPacket(resultBytes, resultBytes.length, reply.getAddress(), reply.getPort());
         aSocket.send(request);
        
         resultBytes = String.format(aSocket.getLocalAddress().getHostAddress() + "," + aSocket.getLocalPort() + ",3;ADD,QCM1234,QC1234,ke,23,23").getBytes();
         request = new DatagramPacket(resultBytes, resultBytes.length, rm1.replicaSocket.getLocalAddress(), rm1.replicaSocket.getLocalPort());
         aSocket.send(request);
         buffer = new byte[1000];
         reply = new DatagramPacket(buffer, buffer.length);
         try {
         aSocket.receive(reply);
         } catch (IOException ex) {
         Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
         }
         answer = new String(reply.getData());
         System.out.println(answer);
         resultBytes = String.format("RECEIVED").getBytes();
         request = new DatagramPacket(resultBytes, resultBytes.length, reply.getAddress(), reply.getPort());
         aSocket.send(request);*/
    }

    public void sendReceivedMessage(InetAddress requestAddress, int requestPort) {
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
    public void sendAnswerMessage(String message, InetAddress requestAddress, int requestPort) {
        boolean not_received = true;
        byte[] resultBytes = message.getBytes();
        DatagramPacket request = new DatagramPacket(resultBytes, resultBytes.length, requestAddress, requestPort);
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            sendSocket.setSoTimeout(1000);
            while (not_received) {
                sendSocket.send(request);
                try {
                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    sendSocket.receive(reply);
                    String answer = new String(reply.getData());
                    if (answer.trim().toUpperCase().equals("RECEIVED")) {
                        not_received = false;
                    }
                } catch (SocketTimeoutException e) {
                }
            }
        } catch (IOException ex) {
        }
    }
}
