package common;

import java.io.File;
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
	    	  
	    	 
	    	  
	    	  //rm1
	    	  new Thread(() -> {
	    			  
	    				  try {
	    					  Scanner fScn = new Scanner(new File("addresses.txt"));
	    				        String data;
	    				        int RMPort = 0;
	    				        InetAddress address = null;
	    				        InetAddress localAddress = null;
	    			            int localPort = 0;


	    				        while( fScn.hasNextLine() ){
	    				            data = fScn.nextLine();
	    				            
	    				            String[] token = data.split(",");
	    				            address = InetAddress.getByName(token[4]);
	    				            RMPort = Integer.parseInt(token[5]);
	    				            localAddress = InetAddress.getByName(token[0]);
	    				            localPort = Integer.parseInt(token[1]);

	    				        }
	    				        fScn.close();
	    				        
	    					  DatagramSocket socket = new DatagramSocket(RMPort, address);
	    					  while (true) {
	    					  byte[] buffer = new byte[1000];
			    	            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			    	            
			    	            
			    	            System.out.println("RM1 ready");
			    	            socket.receive(request);
			    	            System.out.println("RM1 message arrived");
			    	            sendReceivedMessage(request.getAddress(), request.getPort());
			    	            String reply = "ok";
			    	            
			    	            sendAnswerMessage(reply, localAddress, localPort);
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
	    					  
	    					  Scanner fScn = new Scanner(new File("addresses.txt"));
	    				        String data;
	    				        int RMPort = 0;
	    				        InetAddress address = null;
	    				        int localPort = 0;
	    				        InetAddress localAddress = null;

	    				        while( fScn.hasNextLine() ){
	    				            data = fScn.nextLine();
	    				            
	    				            String[] token = data.split(",");
	    				            address = InetAddress.getByName(token[6]);
	    				            RMPort = Integer.parseInt(token[7]);
	    				            localAddress = InetAddress.getByName(token[0]);
	    				            localPort = Integer.parseInt(token[1]);

	    				        }
	    				        fScn.close();
	    				        
	    					  DatagramSocket socket = new DatagramSocket(RMPort, address);
	    					  while (true) {
	    					  byte[] buffer = new byte[1000];
			    	            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			    	            
			    	            
			    	            System.out.println("RM2 ready");
			    	            socket.receive(request);
			    	            System.out.println("RM2 message arrived");
			    	            sendReceivedMessage(request.getAddress(), request.getPort());
			    	            String reply = "ok";
			    	            
			    	            sendAnswerMessage(reply, localAddress, localPort);
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
	    					  Scanner fScn = new Scanner(new File("addresses.txt"));
	    				        String data;
	    				        int RMPort = 0;
	    				        InetAddress address = null;
	    				        int localPort = 0;
	    				        InetAddress localAddress = null;

	    				        while( fScn.hasNextLine() ){
	    				            data = fScn.nextLine();
	    				            
	    				            String[] token = data.split(",");
	    				            address = InetAddress.getByName(token[8]);
	    				            RMPort = Integer.parseInt(token[9]);
	    				            localAddress = InetAddress.getByName(token[0]);
	    				            localPort = Integer.parseInt(token[1]);

	    				        }
	    				        fScn.close();
	    				        
	    					  DatagramSocket socket = new DatagramSocket(RMPort, address);
	    					  while (true) {
	    					  byte[] buffer = new byte[1000];
			    	            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			    	            
			    	            
			    	            System.out.println("RM3 ready");
			    	            socket.receive(request);
			    	            System.out.println("RM3 message arrived");
			    	            sendReceivedMessage(request.getAddress(), request.getPort());
			    	            String reply = "ok";
			    	            
			    	            sendAnswerMessage(reply, localAddress, localPort);
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
	                    String answer = new String(reply.getData()).trim();
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
