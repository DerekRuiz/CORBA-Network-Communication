package FE.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.UUID;

import org.omg.CORBA.ORB;

import clients.ManagerClient;

public class TestServer {

	 public static void main(String args[]) {
	      try {
	    	  
	    	  //sequencer
	    	  new Thread(() -> {
	    		 
	    			 
	    				  try {
	    					  DatagramSocket socket = new DatagramSocket(5123, InetAddress.getLocalHost());
	    					  while(true) {
		    				  byte[] buffer = new byte[1000];
			    	            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			    	            
			    	            
			    	            System.out.println("Sequencer ready");
			    	            socket.receive(request);
			    	            System.out.println("Sequencer message arrived");
			    	            sendReceivedMessage(request.getAddress(), request.getPort());
			    	            
			    	            DatagramPacket forward1 = new DatagramPacket(request.getData(), request.getData().length, InetAddress.getLocalHost(), 4123);
			    	            socket.send(forward1);
			    	            
			    	            DatagramPacket forward2 = new DatagramPacket(request.getData(), request.getData().length, InetAddress.getLocalHost(), 4124);
			    	            socket.send(forward2);
			    	            
			    	            DatagramPacket forward3 = new DatagramPacket(request.getData(), request.getData().length, InetAddress.getLocalHost(), 4125);
			    	            socket.send(forward3);
	    					  }
	    					  
			    	        } catch (SocketException ex) {
			    	            System.out.println("Sequencer failed with " + ex.getMessage());
			    	        } catch (IOException ex) {
			    	        }
	          }).start();
	    	  
	    	  
	    	  //rm1
	    	  new Thread(() -> {
	    			  
	    				  try {
	    					  DatagramSocket socket = new DatagramSocket(4123, InetAddress.getLocalHost());
	    					  while (true) {
	    					  byte[] buffer = new byte[1000];
			    	            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			    	            
			    	            
			    	            System.out.println("RM1 ready");
			    	            socket.receive(request);
			    	            System.out.println("RM1 message arrived");
			    	            
			    	            String reply = "ok";
			    	            
			    	            sendAnswerMessage(reply, InetAddress.getLocalHost(), 5100);
	    					  }
	    				  }
	    				  catch (SocketException ex) {
	  	    	            System.out.println("RM1 failed with " + ex.getMessage());
	  	    	        } catch (IOException ex) {
	  	    	        }
	    				
	    			  
	    	            
	          }).start();
	    	  
	    	  
	    	//rm2
	    	  new Thread(() -> {
	    			  
	    				  try {
	    					  DatagramSocket socket = new DatagramSocket(4124, InetAddress.getLocalHost());
	    					  while (true) {
	    					  byte[] buffer = new byte[1000];
			    	            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			    	            
			    	            
			    	            System.out.println("RM2 ready");
			    	            socket.receive(request);
			    	            System.out.println("RM2 message arrived");
			    	            
			    	            String reply = "ok";
			    	            
			    	            sendAnswerMessage(reply, InetAddress.getLocalHost(), 5100);
	    					  }
	    				  }
	    				  catch (SocketException ex) {
	  	    	            System.out.println("RM2 failed with " + ex.getMessage());
	  	    	        } catch (IOException ex) {
	  	    	        }
	    				
	    			  
	    	            
	          }).start();
	    	  
	    	  
	    	//rm3
	    	  new Thread(() -> {
	    			  
	    				  try {
	    					  DatagramSocket socket = new DatagramSocket(4125, InetAddress.getLocalHost());
	    					  while (true) {
	    					  byte[] buffer = new byte[1000];
			    	            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			    	            
			    	            
			    	            System.out.println("RM3 ready");
			    	            socket.receive(request);
			    	            System.out.println("RM3 message arrived");
			    	            
			    	            String reply = "ok";
			    	            
			    	            sendAnswerMessage(reply, InetAddress.getLocalHost(), 5100);
	    					  }
	    				  }
	    				  catch (SocketException ex) {
	  	    	            System.out.println("RM3 failed with " + ex.getMessage());
	  	    	        } catch (IOException ex) {
	  	    	        }
	    				
	    			  
	    	            
	          }).start();
	         
	      } 
	      catch (Exception ex) {
	         ex.printStackTrace( );
	      } 
	   } 
	 
	 public static void sendAnswerMessage(String message, InetAddress requestAddress, int requestPort) {
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
	                    if (answer.equalsIgnoreCase("RECEIVED")) {
	                        not_received = false;
	                    }
	                } catch (SocketTimeoutException e) {
	                }
	            }
	        } catch (IOException ex) {
	        }
	    }
	 
	 private static void sendReceivedMessage(InetAddress requestAddress, int requestPort) {
	        try (DatagramSocket sendSocket = new DatagramSocket()) {
	        	String r = "RECEIVED";
	            byte[] resultBytes = r.getBytes();
	            DatagramPacket request = new DatagramPacket(resultBytes, resultBytes.length, requestAddress, requestPort);
	            sendSocket.send(request);
	        } catch (IOException ex) {
	        }
	    }
}
