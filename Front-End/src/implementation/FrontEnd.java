package implementation;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.rmi.AlreadyBoundException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import shared.FrontEndInterface;
import shared.FrontEndInterfaceHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.UUID;

public class FrontEnd extends shared.FrontEndInterfacePOA {

	private org.omg.CORBA.ORB orb = null;
	
	private static int port;
	private Logger log = null;
	private String uuid;
	
	
	public FrontEnd(ORB orb, String uuid) throws AlreadyBoundException, IOException {
			super();
		
			this.orb = orb;
			this.uuid = uuid;
			
			log = initiateLogger(uuid);
			log.info("Front-end starting-up.");
			
			log.info("Front-End started up with port: " + port);
			
	}
	
	public void shutdown() {
		this.orb.shutdown(false);
	}
	
	
	public String ReturnItem (String customerId, String itemId, String dateOfReturn) {	
		return "Apple";
	}	
	
	
	public String exchangeItem(String customerId, String newItemId, String oldItemId, String dateOfReturn) {
		return "Apple";
	}
	
	public String PurchaseItem(String customerId, String itemId, String dateOfPurchase) {
		return "Apple";
		
	}
	
	public String[] FindItem(String customerId, String itemDescription) {
		String[] r = {"Apple"};
		
		return r;
		
	}
	
	public String AddCustomerToWaitList(String itemId, String customerId) {
		return "Apple";
		
	}
	
	   
  
   public String AddItem(String managerId, String itemId, String itemName, int quantity, double price) {
	   return "Apple";
   }

	public String RemoveItem(String managerId, String itemId, int quantity) {
		return "Apple";
	}
	
	public String[] ListItemAvailability(String managerId){
			
		String[] r = {"Apple"};
		
		return r;
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





