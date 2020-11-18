

import java.io.*;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import shared.ServerInterface;
import shared.ServerInterfaceHelper;

import org.omg.CORBA.*;

import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* This class represents the object client for a distributed
* object of class SomeImpl, which implements the remote 
* interface SomeInterface.
*/

public class ManagerClient {
	
	private static ManagerClient client;
	
	private static String userId;
	
	private static ORB orb;
	
	private static Logger log;
	
	private static int QCPORT = 4000;
	private static int ONPORT = 4001;
	private static int BCPORT = 4002;
	
	public ManagerClient(ORB orb) throws MalformedURLException {
        
        this.orb = orb;
	}
	
	public void AddItem(String managerId, String itemId, String itemName, int quantity, double price) {
		
		try {
			String region = ExtractRegion(managerId);
			
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");  	 
	    	NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			ServerInterface server = ServerInterfaceHelper.narrow(ncRef.resolve_str(region));
			
			if (server != null)
			{
				String logMethod = "AddItem(" + managerId +", " + itemId + ", " + itemName + ", " + quantity + ", " + price + ")";
				
				log.info(logMethod);
				
				String status = server.AddItem(managerId, itemId, itemName, quantity, price);
				
				System.out.println(status);
				log.info(logMethod + " has replied with " + status);
				
			}
		}
		catch (Exception e) {
	         System.out.println("Exception in Client: " + e);
	    } 
		
	}
	
	
	public void RemoveItem(String managerId, String itemId, int quantity) throws  SecurityException {
		try {
			String region = ExtractRegion(managerId);
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");  	 
	    	NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			ServerInterface server = ServerInterfaceHelper.narrow(ncRef.resolve_str(region));
			
			if (server != null)
			{
				String logMethod = "RemoveItem(" + managerId +", " + itemId + "," + quantity + ")";
				log.info(logMethod);
				
				String status = server.RemoveItem(managerId, itemId, quantity);
				
				System.out.println(status);
				log.info(logMethod + " has replied with " + status);
			}
		}
		catch (Exception e) {
	         System.out.println("Exception in Client: " + e);
	    } 
	}
	
	public void ListItemAvailability(String managerId) throws SecurityException  {
		try {
			
			String region = ExtractRegion(managerId);
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");  	 
	    	NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			ServerInterface server = ServerInterfaceHelper.narrow(ncRef.resolve_str(region));
			
			if (server != null)
			{
				String logMethod = "RemoveItem(" + managerId + ")";
				log.info(logMethod);
				
				String[] matches = server.ListItemAvailability(managerId);
				
				String reply = "";
				if (matches.length > 0)
					for (int i = 0; i < matches.length; i++) {
						reply = reply + matches[i] + "\n";
						
					}
				else
					reply = "No items";
				System.out.println(reply);
				log.info(logMethod + " has replied with " + reply);
				
			}
		}
		catch (Exception e) {
	         System.out.println("Exception in Client: " + e);
	    } 
	}
		
	
	private String ExtractRegion(String id) {
		return id.substring(0,2);
	}

	
	private static boolean ValidateId(String id) {
		Pattern p = Pattern.compile("(QC|ON|BC)(M)([0-9]){4}");
   	 	Matcher m = p.matcher(id);
   	 	boolean b = m.matches();
   	 	
   	 	return b;
	}
	
	private static boolean ValidateItemId(String id) {
		Pattern p = Pattern.compile("(QC|ON|BC)([0-9]){4}");
   	 	Matcher m = p.matcher(id);
   	 	boolean b = m.matches();
   	 	
   	 	return b;
	}
	
	
	private static void EnterSystem() throws SecurityException {
		boolean loop = true;
		Scanner scanner = new Scanner(System.in);
		System.out.println("Valid Id. Welcome.");
		System.out.println("Here are your options");
		
		
		while (loop)
		{
			PrintInstructions();
			int choice = scanner.nextInt();
			
			switch (choice) {
			case 1:
				InputAddItem();
				break;
			case 2:
				InputRemoveItem();
				break;
			case 3:
				client.ListItemAvailability(userId);
				break;
			
			case 4: 
				loop = false;
				break;
				
			case 5:
				System.exit(0);
			
			default:
				System.out.println("Incorrect value");
			}
			
		}
	}
	
	private static void InputAddItem() {
		Scanner scanner = new Scanner(System.in);
		
		String itemId;
		String itemName;
		int quantity;
		double price;
		
		while (true)
		{
			System.out.println("Enter itemId");
			itemId = scanner.nextLine();
			if (ValidateItemId(itemId))
				break;
			else
				System.out.println("Invalid itemId");
		}
		
		System.out.println("Enter itemName");
		itemName = scanner.nextLine();
		
		System.out.println("Enter itemQuantity");
		quantity = scanner.nextInt();
		
		System.out.println("Enter itemPrice");
		price = scanner.nextDouble();
		
		try {
			client.AddItem(userId, itemId, itemName, quantity, price);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	private static void InputRemoveItem() {
		Scanner scanner = new Scanner(System.in);
		
		String itemId;
		String itemName;
		int quantity;
		double price;
		
		while (true)
		{
			System.out.println("Enter itemId");
			itemId = scanner.nextLine();
			if (ValidateItemId(itemId))
				break;
			else
				System.out.println("Invalid itemId");
		}
		
		System.out.println("Enter itemQuantity");
		quantity = scanner.nextInt();
		
		
		try {
			client.RemoveItem(userId, itemId, quantity);
		}  catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	private static void PrintInstructions() {
		System.out.println("Please enter the number of the option you wish to choose: \n" +
				"1: Add a new item or update inventory \n" +
				"2: Remove an item or remove inventory \n" +
				"3: Check available items \n" +
				"4: Change userID \n" +
				"5: Exit system");
	}
	
	private static Logger initiateLogger() throws IOException {
		Files.createDirectories(Paths.get("Logs/Users"));
		
		Logger logger = Logger.getLogger("Logs/Users/" + userId + ".log");
		FileHandler fileHandler;
		
		try
		{
			fileHandler = new FileHandler("Logs/Users/" + userId + ".log");
			
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
		
		System.out.println("Server can successfully log.");
		
		return logger;
	}

	
    public static void main(String args[]) {
      try {
    		ORB orb = ORB.init(args, null);
		    
         client = new ManagerClient(orb);
         while (true)
         { 
        	 Scanner scanner = new Scanner(System.in);
        	 System.out.println("Welcome. Please enter your id");
        	
        	 String id = scanner.nextLine();
        	 
        	 if (ValidateId(id)) {
        		 userId = id;
        		 log = initiateLogger();
        		 log.info(userId+ " has logged in");
        		 EnterSystem();
        	 }
        	 else
        		 System.out.println("Oops, bad id");
        	 
        	 
        	 
         }
         
         //client.AddItem("QCM1002", "QC1000", "Bananas", 20, 10.00);
         //client.AddItem("ONM1002", "ON1000", "Apples", 20, 10.00);
         //client.AddItem("ONM1002", "ON1001", "Bananas", 20, 10.00);
         //client.RemoveItem("ONM1002", "ON1001", 20);
         
         //client.ListItemAvailability("QCM1002");
         
         
      } 
      catch (Exception ex) {
         ex.printStackTrace( );
      } 
   } 
    

   
}