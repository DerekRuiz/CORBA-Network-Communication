package implementation;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.rmi.AlreadyBoundException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.omg.CORBA.*;

import common.Tuple;
import java.util.ArrayList;

public class FrontEnd extends shared.FrontEndInterfacePOA {

	private org.omg.CORBA.ORB orb;
	
	private InetAddress localAddress = InetAddress.getLocalHost();
	private int localPort = 5100;
	private InetAddress sequencerAddress = InetAddress.getLocalHost();
	private int sequencerPort = 5123;
	private Logger log;
	private String uuid;
	
	private static int resendDelay = 1000;
	
	private DatagramSocket socket;
	
	private ArrayList<Tuple<InetAddress, Integer, String>> RMAddresses = new ArrayList<Tuple<InetAddress, Integer, String>>();
	
	public FrontEnd(ORB orb, String uuid) throws AlreadyBoundException, IOException {
			super();
		
			this.orb = orb;
			this.uuid = uuid;
			
			this.socket = new DatagramSocket(this.localPort, this.localAddress);
			
			log = initiateLogger(uuid);
			log.info("Front-end starting-up.");
			
			log.info("Front-End started up with port: " + localPort);
			
			RMAddresses.add(new Tuple<InetAddress, Integer, String>(InetAddress.getLocalHost(), 4123, "PA"));
			RMAddresses.add(new Tuple<InetAddress, Integer, String>(InetAddress.getLocalHost(), 4124, "Jonathan"));
			RMAddresses.add(new Tuple<InetAddress, Integer, String>(InetAddress.getLocalHost(), 4125, "Derek"));
	}
	
	public void shutdown() {
		this.orb.shutdown(false);
	}
	
	
	public String ReturnItem (String customerId, String itemId, String dateOfReturn) {	
		String msgArguments = "RETURN" + "," + customerId + "," + itemId + "," + dateOfReturn;
		String msg = buildMessage(localAddress, localPort, msgArguments);
		
		sendAnswerMessage(msg, sequencerAddress, sequencerPort);
		
		try {
			String reply = recieveReply(socket);
			
			return reply;
		} catch (IOException e) {
			return e.getMessage();
		}
		
	}	
	
	
	public String exchangeItem(String customerId, String newItemId, String oldItemId, String dateOfReturn) {
		String msgArguments = "EXCHANGE" + "," + customerId + "," + newItemId + "," + oldItemId + "," + dateOfReturn;
		String msg = buildMessage(localAddress, localPort, msgArguments);
		
		sendAnswerMessage(msg, sequencerAddress, sequencerPort);
		
		try {
			String reply = recieveReply(socket);
			
			return reply;
		} catch (IOException e) {
			return e.getMessage();
		}
		
	}
	
	public String PurchaseItem(String customerId, String itemId, String dateOfPurchase) {
		
		String msgArguments = "PURCHASE" + "," + customerId + "," + itemId + "," + dateOfPurchase;
		String msg = buildMessage(localAddress, localPort, msgArguments);
		
		sendAnswerMessage(msg, sequencerAddress, sequencerPort);
		
		try {
			String reply = recieveReply(socket);
			
			return reply;
		} catch (IOException e) {
			return e.getMessage();
		}
		
	}
	
	public String[] FindItem(String customerId, String itemDescription) {
		String msgArguments = "FIND" + "," + customerId + "," + itemDescription;
		String msg = buildMessage(localAddress, localPort, msgArguments);
		
		sendAnswerMessage(msg, sequencerAddress, sequencerPort);
		
		try {
			String reply = recieveReply(socket);
			String[] replyArray = reply.replace("[", "").replace("]", "").split(", ");
			return replyArray;
		} catch (IOException e) {
			return new String[1];
		}
		
	}
	
	public String AddCustomerToWaitList(String itemId, String customerId) {
		String msgArguments = "ADDTOWAITLIST" + "," + itemId + "," + customerId;
		String msg = buildMessage(localAddress, localPort, msgArguments);
		
		sendAnswerMessage(msg, sequencerAddress, sequencerPort);
		
		try {
			String reply = recieveReply(socket);
			
			return reply;
		} catch (IOException e) {
			return e.getMessage();
		}
		
	}
	
	   
  
   public String AddItem(String managerId, String itemId, String itemName, int quantity, double price) {
		String msgArguments = "ADD" + "," + managerId + "," + itemId + "," + itemName + "," + quantity + "," + price;
		String msg = buildMessage(localAddress, localPort, msgArguments);
		
		sendAnswerMessage(msg, sequencerAddress, sequencerPort);
		
		try {
			String reply = recieveReply(socket);
			
			return reply;
		} catch (IOException e) {
			return e.getMessage();
		}
   }

	public String RemoveItem(String managerId, String itemId, int quantity) {
		String msgArguments = "REMOVE" + "," + managerId + "," + itemId + "," + quantity;
		String msg = buildMessage(localAddress, localPort, msgArguments);
		
		sendAnswerMessage(msg, sequencerAddress, sequencerPort);
		
		try {
			String reply = recieveReply(socket);
			
			return reply;
		} catch (IOException e) {
			return e.getMessage();
		}
	}
	
	public String[] ListItemAvailability(String managerId){
		
		String msgArguments = "LIST" + "," + managerId;
		String msg = buildMessage(localAddress, localPort, msgArguments);
		
		sendAnswerMessage(msg, sequencerAddress, sequencerPort);
		
		try {
			String reply = recieveReply(socket);
			String[] replyArray = reply.replace("[", "").replace("]", "").split(", ");
			return replyArray;
		} catch (IOException e) {
			return new String[1];
		}
	}
	
	/**
     * Sends a received message to a process.
     *
     * @param requestAddress the InetAdress of the machine
     * @param requestPort the port to connect to
     */
	public static void sendReceivedMessage(InetAddress requestAddress, int requestPort) {
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
    
    public static void sendAnswerMessage(String message, InetAddress requestAddress, int requestPort) {
        boolean not_received = true;
        byte[] resultBytes = message.getBytes();
        DatagramPacket request = new DatagramPacket(resultBytes, resultBytes.length, requestAddress, requestPort);
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            sendSocket.setSoTimeout(resendDelay);
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
     * Waits for UDP requests. When a request is received, it returns the answer
     */
    private String waitForReply(DatagramSocket socket) {
        try {
            byte[] buffer = new byte[1000];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            socket.receive(request);
            String message = new String(request.getData());
            sendReceivedMessage(request.getAddress(), request.getPort());

            return message;
        } catch (SocketException ex) {
            System.out.println("Could not connect to port, canceling server");
            System.exit(1);
        } catch (IOException ex) {
        }
		return null;
    }
    
    private String recieveReply(DatagramSocket socket) throws IOException {
    	int expectedReplies = RMAddresses.size();
    	ArrayList<Tuple<InetAddress, Integer, String>> repliedRM = new ArrayList<Tuple<InetAddress, Integer, String>>(); 
    	boolean continueRecieving = true;
    	String answer;
    	
    	try {
    		while(continueRecieving) {
    			byte[] buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                Tuple<InetAddress, Integer, String> RMAddress = new Tuple<InetAddress, Integer, String>(request.getAddress(), request.getPort(), new String(request.getData()));
                
                if (!repliedRM.contains(RMAddress)) {
                	repliedRM.add(RMAddress);
                    sendReceivedMessage(request.getAddress(), request.getPort());
                }
                
                if (expectedReplies == repliedRM.size())
                	continueRecieving = false;
                
    		}
    		
    		answer = getMajority(repliedRM);
    		
    		return answer;
    		
        } catch (SocketException ex) {
            System.out.println("Could not connect to port, canceling server");
            System.exit(1);
        } catch (IOException ex) {
        	throw ex;
        }
    	
		return "";
    }
    
    private String getMajority(ArrayList<Tuple<InetAddress, Integer, String>> repliedRM) {
    	
    	String result = "";
		int counter = 0;
		
		for(Tuple<InetAddress, Integer, String> r: repliedRM) {
			if(counter == 0) {
				result = r.getRight();
				counter ++;
			}else if(result.equalsIgnoreCase(r.getRight())) {
				counter ++;
			}else {
				counter--;
			}
			
		}
		
		if(counter == RMAddresses.size()) {
			return result;
		}
		
		for(Tuple<InetAddress, Integer, String> r: repliedRM) {
			if(!result.equalsIgnoreCase(r.getRight())) {
				notifyFail(r.getLeft(), r.getMiddle());
				return result;
			}
			
		}
		
		return result;
    }
    
    private void notifyFail(InetAddress address, int port) {
    	
    	for (Tuple<InetAddress, Integer, String> r: RMAddresses) {
    		String msgArguments = "FAILURE" + "," + address + ", " + port;
    		String msg = buildMessage(address, port, msgArguments);
    		new Thread(() -> {
    			sendAnswerMessage(msgArguments, r.getLeft(), r.getMiddle());
            }).start();
    	}
    }
    
    
    private static String buildMessage(InetAddress address, int port, String arguments) {
		return address.toString() + "," + Integer.toString(port) + ";" + arguments;
	}
	
	
	private Logger initiateLogger(String uuid) throws IOException {
		Files.createDirectories(Paths.get("Logs/FrontEnd"));
		
		Logger logger = Logger.getLogger("Logs/FrontEnd/" + uuid + ".log");
		FileHandler fileHandler;
		
		try
		{
			fileHandler = new FileHandler("Logs/FrontEnd/" + uuid + ".log");
			
			logger.setUseParentHandlers(false);
			logger.addHandler(fileHandler);
			
			SimpleFormatter formatter = new SimpleFormatter();
			fileHandler.setFormatter(formatter);
		}
		
		catch (IOException e)
		{
			System.err.println("IO Exception " + e);
			e.printStackTrace();
		}
		
		System.out.println("FrontEnd can successfully log.");
		
		return logger;
	}
	
}





