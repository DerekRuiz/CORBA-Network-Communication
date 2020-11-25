

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.rmi.AlreadyBoundException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class StoreServer{
	
	private String region;
	
	private static int port;
	private Logger log = null;
	
	private DatagramSocket socket = new DatagramSocket(getPort(region));
	
	private Map<String, Customer> customerList = new HashMap<String, Customer>();
	private Map<String, Manager> managerList = new HashMap<String, Manager>();
	private Map<String, Product> productList = new HashMap<String, Product>();
	
	private Map<String, String> blacklistedCustomers = new HashMap<String, String>();
	
	private Map<String, Queue<String>> waitList = new HashMap<String, Queue<String>>();
	
	
	public StoreServer(String _region, int _port) throws AlreadyBoundException, IOException{
			super();
		
			region = _region;
			
			port= _port;
			
			log = initiateLogger();
			log.info("Server starting-up.");
			
			
			log.info("Server started up with port: " + port);
			InitializeStore(region);
			
			Thread t = new Thread(new Runnable() {
			    private String region;
			    public Runnable init(String _region) {
			        this.region = _region;
			        return this;
			    }
			    @Override
			    public void run() {
			    	InitializeServerListener(this.region);
			    }
			}.init(region));
			
			t.start();
		
		
	}
	
	
	
	public String ReturnItem (String customerId, String itemId, String dateOfReturn) {
		
			String replyMessage = verifyReturn(customerId, itemId, dateOfReturn);
			
			if (replyMessage.equalsIgnoreCase("CanReturn")) {
				
				Customer customer = customerList.get(customerId);
				
				if (customer == null) {
					customer = new Customer(customerId, region);
					customerList.put(customerId, customer);
				}
				
				Product product = productList.get(itemId);
				if (product == null) {
					return "Product missing";
				}
				
				else {
					LinkedList<Tuple<String, Date, Double>> purchasedProducts = customer.getPurchasedProducts();
					
					for (int i = purchasedProducts.size() - 1; i >= 0; i--) {
						
						Tuple<String, Date, Double> node = purchasedProducts.get(i);
						String itemRegion = ExtractRegion(itemId);
						if (node.getLeft().equalsIgnoreCase(itemId) && itemRegion.equalsIgnoreCase(region)) {
							
							setProductQuantity(product, product.getQuantity() + 1);
				        	setCustomerBudget(customer, customer.getBudget() + node.getRight());
				        	 
				        	purchasedProducts.remove(i);
				        	try {
								UpdateWaitList(itemId);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					        return "Refunded";
						}
						
						else if(node.getLeft().equalsIgnoreCase(itemId)){
							
							replyMessage = ReturnItemAtDifferentStore(customerId, itemId);
							if (replyMessage.equalsIgnoreCase("Returned")) {
								setCustomerBudget(customer, customer.getBudget() + node.getRight());
				        	 	purchasedProducts.remove(i);
							}
							else
								return replyMessage;
						}
						
					}
					return "NotFound";
				}
			}
			else
				return replyMessage;
		}
		
	
	
	public String exchangeItem(String customerId, String newItemId, String oldItemId, String dateOfReturn) {
		String logMethod = "exchangeItem(" + customerId + ", " + newItemId + ", " + oldItemId +")";
		log.info(logMethod + " has been invoked");
		
		Customer customer = customerList.get(customerId);
		
		if (customer == null) {
			customer = new Customer(customerId, region);
			customerList.put(customerId, customer);
		}
		
		String returnMessage, purchaseMessage;
		
		returnMessage = verifyReturn(customerId, oldItemId, dateOfReturn);
		if (returnMessage.equalsIgnoreCase("CanReturn")) {
			
			double budget = customer.getBudget();
			double oldItemPrice = getProductPrice(oldItemId);
			double newItemPrice = getProductPrice(newItemId);
			
			if (oldItemPrice < 0)
				return "OldProductMissing";
			else if (newItemPrice < 0)
				return "NewProductMissing";
			else if (budget + oldItemPrice < newItemPrice)
				return "MissingFunds";
			else {
				boolean isReserved = ReserveItem(customerId, newItemId);
				if (isReserved) 
				{
					returnMessage = ReturnItem(customerId, oldItemId, dateOfReturn);
					purchaseMessage = PurchaseItem(customerId, newItemId, dateOfReturn);
					log.info(logMethod + " returns Exchanged");
					return "Exchanged";
				}
				else
					return "CannotReserve";
			}
		}
		else
			return returnMessage;
	}
	
	public String PurchaseItem(String customerId, String itemId, String dateOfPurchase) {
		
		String logMethod = "PurchaseItem(" + customerId + ", " + itemId + ", " + dateOfPurchase +")";
		log.info(logMethod + " has been invoked");
		
		String itemRegion = ExtractRegion(itemId);
		
		Customer customer = customerList.get(customerId);
		if (customer == null) {
			customer = new Customer(customerId, region);
			customerList.put(customerId, customer);
		}
		
		if (itemRegion.equalsIgnoreCase(region))
		{
			Product product = productList.get(itemId);
			
			if (product != null) {
				
				HashMap<String, String> reservations = product.getReservations();
				
				if ( (reservations.size() == 0 || product.getQuantity() <= reservations.size() || reservations.containsKey(customerId)) && product.getQuantity() != 0) {
					
					try {
						Date date = new SimpleDateFormat("dd-MM-yyyy").parse(dateOfPurchase);
						String status = CompleteTransaction(customerId, itemId, date);
						log.info(logMethod + " returns " + status);
						return status;
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				 
				else {
					log.info(logMethod + " returns Waitlist");
					return "Waitlist";
				}
			}
			else {
				log.info(logMethod + " returns ProductMissing");
				return "ProductMissing";
			}
		}
		else {
			String status = "Failed";
			try {
				status = PurchaseItemInDifferentStore(customerId, itemId, dateOfPurchase);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info(logMethod + " returns" + status);
			return status;
		}
		
		return "Failed";
	}
	
	public String[] FindItem(String customerId, String itemDescription) {
		
		try {
			
			String logMethod = "FindItem(" + customerId + ", " + itemDescription +")";
			log.info(logMethod + " has been invoked");
			
			Customer customer = customerList.get(customerId);
			
			if (customer == null) {
				customer = new Customer(customerId, region);
				customerList.put(customerId, customer);
			}
			
			else {
				
				 LinkedList<Product> matches = new LinkedList<Product>();
				
				 String[] regions = {"QC", "ON", "BC"};
				 for (int i = 0; i < regions.length; i++) {
					 if (regions[i] == this.region)
						 matches.addAll(SearchItem(itemDescription));
					 else {
						int port = getPort(regions[i]);
						InetAddress host = InetAddress.getLocalHost();
						
						DatagramSocket socket = new DatagramSocket();
						String message = "FindItem," + itemDescription + ",";
						byte[] m = message.getBytes();
						DatagramPacket request = new DatagramPacket(m, m.length , host, port);
						socket.send(request);
						
						byte[] buffer = new byte[1000];
						DatagramPacket r = new DatagramPacket(buffer, buffer.length);
						socket.receive(r);
						
						ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(r.getData()));
						LinkedList<Product> foundMatches = (LinkedList<Product>) inputStream.readObject();
						
						matches.addAll(foundMatches);
						socket.close();
					 }
					 	
		    	 }
				 
				 LinkedList<String> matchesString = new LinkedList<String>();
				 String logString = "";
				 for (int i = 0; i < matches.size(); i++) {
					 String entry = matches.get(i).getId() + " " + matches.get(i).getDescription() + " " + matches.get(i).getQuantity();
					 matchesString.add(entry);
					 
					 logString = logString + entry + "\n";
				 }
				 
				 log.info(logMethod + " returns " + logString);
				 
				 String[] array = matchesString.toArray(new String[matchesString.size()]);
				 
				return array;
			}
		}
		catch(Exception ex) {
			//throw ex;
		}
		return null;
	}
	
	public String AddCustomerToWaitList(String itemId, String customerId) {
		
		String logMethod = "AddCustomerToWaitList(" + itemId + ", " + customerId +")";
		log.info(logMethod + " has been invoked");
		
		String itemRegion = ExtractRegion(itemId);
		
		if (itemRegion.equalsIgnoreCase(region)) {
			
			Product product = productList.get(itemId);
			
			if (product != null) {
				Queue<String> queue = waitList.get(itemId);
				
				if (queue != null) {
					queue.add(customerId);
				}
				else {
					queue = new LinkedList<String>();
					queue.add(customerId);
					waitList.put(itemId, queue);
				}
				
				log.info(logMethod + " returns Waitlist");
				return "Waitlist";
			}
			else {
				log.info(logMethod + " returns ProductMissing");
				return "ProductMissing";
			}
		}
		else {
			String status = "Failed";
			try {
				status = AddCustomerToWaitListOtherStore(itemId, customerId);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			log.info(logMethod + " returns " + status);
			return status;
		}
	}
	
	   
  
	   public String AddItem(String managerId, String itemId, String itemName, int quantity, double price) {
		    String logMethod = "AddItem("+ managerId + ", " + itemId + ", " + itemName + ", " + quantity + ", " + price +")";
			log.info(logMethod + " has been invoked");
			
			Manager manager = managerList.get(managerId);
			
			if (manager == null)
				return "ERROR: Non manager accessing add item";
			
			else
			{
				String itemRegion = ExtractRegion(itemId);
				
				if (itemRegion.equalsIgnoreCase(region))
				{
					Product product = productList.get(itemId);
					
					
					if (product == null)
					{
						product = new Product(itemId, itemName, quantity, price);
						productList.put(itemId, product);
						
						log.info(logMethod + " returns Added");
					}
					
					else
					{
						if (!product.getDescription().equalsIgnoreCase(itemName))
							return String.format("ERROR: Added item name %s does not match item with id %s", itemName, itemId);
						
						else if (product.getPrice() != price)
							return String.format("ERROR: Added item price %f does not match item with id %s", price, itemId);
						else {
							int oldQuantity = product.getQuantity();
							
							setProductQuantity(product, quantity);
							
							if (oldQuantity == 0 && quantity > 0)
								try {
									UpdateWaitList(itemId);
								} catch (UnknownHostException e) {
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							
							log.info(logMethod + " returns Updated");
						}
					}
					
					return String.format("SUCCESS: Added %d item %s to store", quantity, itemId);
					
				}
				else {
					log.info(logMethod + " returns WrongItemRegion");
					return String.format("ERROR: Adding item ID %s to location %s", itemId, this.region);
				}
					
					
			}
		}
	
		public String RemoveItem(String managerId, String itemId, int quantity) {
			String logMethod = "RemoveItem(" + managerId + ", " + itemId + ", " + quantity + ")";
			log.info(logMethod + " has been invoked");
			
			Manager manager = managerList.get(managerId);
			
			if (manager == null)
				throw new SecurityException();
			
			else
			{
				String itemRegion = ExtractRegion(itemId);
							
				if (itemRegion.equalsIgnoreCase(region))
				{
					Product product = productList.get(itemId);
					if (product != null)
					{
						if (quantity < 0)
						{
							productList.remove(itemId);
							waitList.remove(itemId);
							
							log.info(logMethod + " returns ItemDeleted");
							return "ItemDeleted";
						}
						else if (product.getQuantity() > 0)
						{
							int newQuantity = product.getQuantity() - quantity > 0 ? product.getQuantity() - quantity : 0;
							setProductQuantity(product, newQuantity);
							
							log.info(logMethod + " returns ModifiedQuantity");
						   return "ModifiedQuantity";
						}
						else
						{
							log.info(logMethod + " returns ModifiedQuantity");
							return "AlreadyZero";
						}
							
					}
					
					log.info(logMethod + " returns ProductMissing");
					return "ProductMissing";
					
					
				}
				log.info(logMethod + " returns WrongItemRegion");
				return "WrongItemRegion";
			}
		}
	
		public String[] ListItemAvailability(String managerId){
			
			String logMethod = "ListItemAvailability(" + managerId + ")";
			log.info(logMethod + " has been invoked");
		
			Manager manager = managerList.get(managerId);
			
			if (manager == null)
				throw new SecurityException();
			
			else
			{
				LinkedList<String> allItems = new LinkedList<String>();
				String logString = "";
				for (Map.Entry<String, Product> map : productList.entrySet()) {
					Product product = map.getValue();
					String entry = "Id: " + product.getId() + " || Description: " + product.getDescription() + " || Quantity: " + product.getQuantity() + " || Price: " + product.getPrice() ;
					allItems.add(entry);
					logString = logString + entry + "\n";
				}
				 
				 log.info(logMethod + " returns " + logString);
				 
				 String[] array = allItems.toArray(new String[allItems.size()]);
				 
				return array;
				
			}
		}
		
	
	
	   
	   private void InitializeServerListener(String region) {
		   try {

			  
			   byte[] buffer = new byte[10000];
			   
			   while(true){
				   DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				   socket.receive(request);
				   
				   String data = new String(request.getData());
				   String[] values = data.split(",");
				   
				   String replyMessage = "";
				   byte r[] = new byte [1000];
				   
				   if (values.length > 0 && values[0].equalsIgnoreCase("PurchaseItem")) {
					   String customerId = values[1];
					   String itemId = values[2];
					   String dateOfPurchase = values[3];
					   double budget =Double.parseDouble(values[4]);
					   
					   String logMethod = "PurchaseItem(" + customerId + ", " + itemId + ", " + dateOfPurchase +")";
					   log.info(logMethod + " has been invoked");
					   
					   Product product = productList.get(itemId);
					   if (product != null) {
						   
						  
						  if (!IsCustomerBlackListed(customerId)){
							  if (budget >= product.getPrice()) {
								  HashMap<String,String> reservations = product.getReservations();
								  if ((reservations.size() == 0 || product.getQuantity() <= reservations.size() || reservations.containsKey(customerId)) && product.getQuantity() != 0) {
										Date date = new SimpleDateFormat("dd-MM-yyyy").parse(dateOfPurchase);
										
										String status = CompleteTransaction(customerId, itemId, date);
										
										blacklistedCustomers.put(customerId, customerId);
										replyMessage =  status + "," + product.getPrice();
									
								  }
								  else {
										replyMessage =  "Waitlist";
								  }
								  
							  } 
							  else  
								  replyMessage =  "MissingFunds";
								  
							
						  }
						  else
							  replyMessage = "BlackListed";
						}
						else {
							replyMessage = "ProductMissing";
						}
					   log.info(logMethod + " returns " + replyMessage);
					   r = replyMessage.getBytes();
				   }
				   
				   	else if (values.length > 0 && values[0].equalsIgnoreCase("ReturnItem")) {
					   
					   String customerId = values[1];
					   String itemId = values[2];
					  
					   String logMethod = "ReturnItem(" + customerId + ", " + itemId  +")";
					   log.info(logMethod + " has been invoked");

					   Product product = productList.get(itemId);
					   if (product != null) {
						   product.setQuantity(product.getQuantity() + 1);
						   blacklistedCustomers.remove(customerId);
						   UpdateWaitList(itemId);
						   replyMessage = "Returned";
					   }
					   else
						   replyMessage = "ProductMissing";
					   
					   log.info(logMethod + " returns " + replyMessage);
					   r = replyMessage.getBytes();
				   }
				   
				 	else if (values.length > 0 && values[0].equalsIgnoreCase("ReserveItem")) {
						   
						   String customerId = values[1];
						   String itemId = values[2];
						  
						   String logMethod = "ReserveItem(" + customerId + ", " + itemId  +")";
						   log.info(logMethod + " has been invoked");

						   boolean isReserved = ReserveItem(customerId, itemId);
						   if (isReserved)
							   replyMessage = "Reserved";
						   else
							   replyMessage = "CannoteReserve";
						   
						   log.info(logMethod + " returns " + replyMessage);
						   r = replyMessage.getBytes();
				   }
					   
				   
				   else if (values.length > 0 && values[0].equalsIgnoreCase("AddToWaitList")) {
					   
					   String customerId = values[1];
					   String itemId = values[2];
					  
					   String logMethod = "AddToWaitList(" + customerId + ", " + itemId  +")";
					   log.info(logMethod + " has been invoked");

					   
					   replyMessage = AddCustomerToWaitList( itemId, customerId);
					   
					   log.info(logMethod + " returns " + replyMessage);
					   r = replyMessage.getBytes();
				   }
				   
				   	else if (values.length > 0 && values[0].equalsIgnoreCase("GetProductPrice")) {
					   
					   String itemId = values[1];
					  
					   double price = getProductPrice(itemId);
					   
					   ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
					   byteBuffer.putDouble(price);
					   r = byteBuffer.array();
				   }
				   else if (values.length > 0 && values[0].equalsIgnoreCase("PurchaseFromWaitList")) {
					   
					   String customerId = values[1];
					   String itemId = values[2];
					   double price = Double.parseDouble(values[3]);
					  
					   String logMethod = "PurchaseFromWaitList(" + customerId + ", " + itemId  + ", " + price + ")";
					   log.info(logMethod + " has been invoked");
					   
					   Customer customer = customerList.get(customerId);
					   if (customer != null){
							double newBudget = customer.getBudget() - price;
							
							if (newBudget > 0) 
							{
								setCustomerBudget(customer, newBudget);
								LinkedList<Tuple<String, Date, Double>> customerPurchaseOrder = customer.getPurchasedProducts();
								Tuple<String, Date, Double> newPurchase = new Tuple<String, Date, Double> (itemId, new Date(), price);
								customerPurchaseOrder.add(newPurchase);
								
								replyMessage = "Completed";
							}
							
								
							else
								replyMessage = "MissingFunds";
						}
					   
					   r = replyMessage.getBytes();
				   }
				   
				   else if (values.length > 0 && values[0].equalsIgnoreCase("FindItem"))
				   {
					   String itemDescription = values[1];
					   
					   String logMethod = "FindItem(" + itemDescription + ")";
					   log.info(logMethod + " has been invoked");

					   itemDescription = itemDescription.replace("\"", "");
					   
					   LinkedList<Product> matches = SearchItem(itemDescription);
					   
					    ByteArrayOutputStream out = new ByteArrayOutputStream();
					    ObjectOutputStream outputStream = new ObjectOutputStream(out);
					    outputStream.writeObject(matches);
					    outputStream.close();
					    
					    String logString = "";
					    for (int i = 0; i < matches.size(); i++) {
						    String entry = matches.get(i).getId() + " " +matches.get(i).getDescription() + " " + matches.get(i).getQuantity();
	
						    logString = logString + entry + "\n";
					    }

					    log.info(logMethod + " returns " + logString);

					    r = out.toByteArray();
				   }
				   System.out.println("looped");
				   DatagramPacket reply = new DatagramPacket(r, r.length , request.getAddress(), request.getPort());
				   socket.send(reply);
				   
				   values = null;
			   }
		   }
		   catch(Exception ex){
			   System.out.println(ex);
		   }
	   }
	   
	
	private int getPort(String region) {
		switch (region) {
			case "QC":
				return 4005;
			case "ON":
				return 4006;
			case "BC":
				return 4007;
			default:
				return 0;
			}
			
	}
	
	private Logger initiateLogger() throws IOException {
		Files.createDirectories(Paths.get("Logs/Servers"));
		
		Logger logger = Logger.getLogger("Logs/Servers/" + region + ".log");
		FileHandler fileHandler;
		
		try
		{
			fileHandler = new FileHandler("Logs/Servers/" + region + ".log");
			
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
	
	private void InitializeStore(String region) {
		Manager manager1 = new Manager(region+ "M" + 1000, region);
		Manager manager2 = new Manager(region+ "M" + 1001, region);
		Manager manager3 = new Manager(region+ "M" + 1002, region);
		
		managerList.put(manager1.getUserId(), manager1);
		managerList.put(manager2.getUserId(), manager2);
		managerList.put(manager3.getUserId(), manager3);
		
		Customer customer1 = new Customer(region+ "U" + 1000, region);
		Customer customer2 = new Customer(region+ "U" + 1001, region);
		Customer customer3 = new Customer(region+ "U" + 1002, region);
		
		customerList.put(customer1.getUserId(), customer1);
		customerList.put(customer2.getUserId(), customer2);
		customerList.put(customer3.getUserId(), customer3);
		
		
		
	}
	
	private String AddCustomerToWaitListOtherStore(String itemId, String customerId) throws IOException {
			
			String itemRegion = ExtractRegion(itemId);
			
			int port = getPort(itemRegion);
			InetAddress host = InetAddress.getLocalHost();
			
			DatagramSocket socket = new DatagramSocket();
			String message = "AddToWaitList," + customerId + "," + itemId + ",";
			byte[] m = message.getBytes();
			DatagramPacket request = new DatagramPacket(m, m.length , host, port);
			socket.send(request);
			
			byte[] buffer = new byte[1000];
			DatagramPacket r = new DatagramPacket(buffer, buffer.length);
			socket.receive(r);
			
			String status = new String(r.getData());
			socket.close();
			return status;
		}
	
	private void UpdateWaitList(String itemId) throws IOException {
		 
		 Queue<String> queue = waitList.get(itemId);
		 if (queue != null)
		 {
			 while (!queue.isEmpty()) {
				 String customerId = queue.poll();
				 if (customerId != null)
				 {
					 String customerRegion = ExtractRegion(customerId);
					 if (customerRegion.equalsIgnoreCase(region))
					 {
						 PurchaseItem(customerId, itemId, new String());
					 }
					 else
					 {
						 Product product = productList.get(itemId);
						 if (product != null){
							String itemRegion = ExtractRegion(customerId);
							int port = getPort(itemRegion);
							InetAddress host = InetAddress.getLocalHost();
							
							DatagramSocket socket = new DatagramSocket();
							String message = "PurchaseFromWaitList," + customerId + "," + itemId + "," + product.getPrice() +",";
							byte[] m = message.getBytes();
							DatagramPacket request = new DatagramPacket(m, m.length , host, port);
							socket.send(request);
							
							byte[] buffer = new byte[10000];
							DatagramPacket r = new DatagramPacket(buffer, buffer.length);
							socket.receive(r);
							
							String replyMessage = new String(r.getData());
							replyMessage = replyMessage.trim();
							socket.close();
							if (replyMessage.equalsIgnoreCase("Completed"))
							{
								setProductQuantity(product, product.getQuantity() - 1);
							}
							
						 }
						
					 }
				 }
			 }
		 }
		 
	}
	
	
	
	private String CompleteTransaction(String customerId, String itemId, Date dateOfPurchase) {
		Product product = productList.get(itemId);
		Customer customer = customerList.get(customerId);
		
		int newQuantity = product.getQuantity() - 1;
		
		if (customer != null){
			double newBudget = customer.getBudget() - product.getPrice();
			
			if (newBudget >= 0) 
			{
				setCustomerBudget(customer, newBudget);
				LinkedList<Tuple<String, Date, Double>> customerPurchaseOrder = customer.getPurchasedProducts();
				Tuple<String, Date, Double> newPurchase = new Tuple<String, Date, Double> (itemId, dateOfPurchase, product.getPrice());
				customerPurchaseOrder.add(newPurchase);
			}
			
				
			else
				return "MissingFunds";
		}
		
		
		setProductQuantity(product, newQuantity);
		
		if (product.getReservations().containsKey(customerId))
			product.getReservations().remove(customerId);
		return "Completed";
		
	}
	
	
	private String PurchaseItemInDifferentStore(String customerId, String itemId, String dateOfPurchase) throws UnknownHostException
	{
		try {
			
			Customer customer = customerList.get(customerId);
			
			if (customer != null) {
				Date date = new SimpleDateFormat("dd-MM-yyyy").parse(dateOfPurchase);
				
				String itemRegion = ExtractRegion(itemId);
				int port = getPort(itemRegion);
				InetAddress host = InetAddress.getLocalHost();
				
				DatagramSocket socket = new DatagramSocket();
				String message = "PurchaseItem," + customerId + "," + itemId + "," + dateOfPurchase.toString() + "," + customer.getBudget() +",";
				byte[] m = message.getBytes();
				DatagramPacket request = new DatagramPacket(m, m.length , host, port);
				socket.send(request);
				
				byte[] buffer = new byte[1000];
				DatagramPacket r = new DatagramPacket(buffer, buffer.length);
				socket.receive(r);
				
				String replyMessage = new String(r.getData());
				
				String[] reply = replyMessage.split(",");
				
				socket.close();
				if (reply[0].equalsIgnoreCase("Completed")){
					
					double price = Double.parseDouble(reply[1]);
					setCustomerBudget(customer,customer.getBudget()- price);
					LinkedList<Tuple<String, Date, Double>> customerPurchaseOrder = customer.getPurchasedProducts();
					Tuple<String, Date, Double> newPurchase = new Tuple<String, Date, Double> (itemId, date, price);
					customerPurchaseOrder.add(newPurchase);
					
				}
				
				return reply[0];
			}
			else
				return "CustomerMissing";
			
			
		}
		catch(SocketException e) {
		}
		catch(IOException e) {
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	private synchronized String ReturnItemAtDifferentStore(String customerId, String itemId) {
		
		try {
			String itemRegion = ExtractRegion(itemId);
			int port = getPort(itemRegion);
			InetAddress host;
			host = InetAddress.getLocalHost();
			
			DatagramSocket socket = new DatagramSocket();
			String message = "ReturnItem," + customerId + "," + itemId + ",";
			byte[] m = message.getBytes();
			DatagramPacket request = new DatagramPacket(m, m.length , host, port);
			socket.send(request);
			
			byte[] buffer = new byte[1000];
			DatagramPacket r = new DatagramPacket(buffer, buffer.length);
			socket.receive(r);
			
			String status = new String(r.getData());
			
			socket.close();
			return status;
		}
		catch (Exception ex) {
			return "Failed";
		}
			
		
		
	}
	
	private String verifyReturn(String customerId, String itemId, String dateOfReturn) {
		String logMethod = "ReturnItem(" + customerId + ", " + itemId + ", " + dateOfReturn +")";
		log.info(logMethod + " has been invoked");
		
		Customer customer = customerList.get(customerId);
		
		if (customer == null)
			throw new SecurityException();
		
		LinkedList<Tuple<String, Date, Double>> purchasedProducts = customer.getPurchasedProducts();
		
		for (int i = purchasedProducts.size() - 1; i >= 0; i--) {
			
			Tuple<String, Date, Double> node = purchasedProducts.get(i);
			
			if (node.getLeft().equalsIgnoreCase(itemId)) {
				Date now = new Date();
				
		        Calendar c = Calendar.getInstance();
		        c.setTime(node.getMiddle());

		        c.add(Calendar.DATE, -30);

		        // convert calendar to date
		        Date minDate = c.getTime();
		        
		        if (minDate.before(now) || !minDate.after(now)) {
		        	
		        	log.info(logMethod + " returns CanReturn");
		        	return "CanReturn";
				        	 
				        }
		        else
		        {
		        	log.info(logMethod + " returns Denied");
		        	return "Denied";
		        }
				        	
			}
		}
				
		log.info(logMethod + " returns NotFound");
		return "NotFound";
	}
	
	private synchronized void setProductQuantity(Product p, int quantity) {
		p.setQuantity(quantity);
	}
	
	private synchronized void setCustomerBudget(Customer c, double budget) {
		c.setBudget(budget);
	}
	
	private LinkedList<Product> SearchItem(String itemDescription) {
		
		LinkedList<Product> matches = new LinkedList<Product>();
		
		for (Map.Entry<String, Product> map : productList.entrySet()) {
			Product product = map.getValue();
			String description = product.getDescription();
			if (description.equalsIgnoreCase(itemDescription))
				matches.add(product);
		}
		
		return matches;
	}
	
	private double getProductPrice(String itemId) {
		String itemRegion = ExtractRegion(itemId);
		
		if (itemRegion.equalsIgnoreCase(this.region)) {
			Product p = productList.get(itemId);
			if (p != null)
				return p.getPrice();
			else
				return -1;
					
		}
		else {
			try {
			int port = getPort(itemRegion);
			InetAddress host;
			host = InetAddress.getLocalHost();
			
			DatagramSocket socket = new DatagramSocket();
			String message = "GetProductPrice," + itemId + ",";
			byte[] m = message.getBytes();
			DatagramPacket request = new DatagramPacket(m, m.length , host, port);
			socket.send(request);
			
			byte[] buffer = new byte[1000];
			DatagramPacket r = new DatagramPacket(buffer, buffer.length);
			socket.receive(r);
			
			Double price = ByteBuffer.wrap(r.getData()).getDouble();
			
			socket.close();
			return price;
			}
			catch (Exception ex) {
				return -1;
			}
		}
			
	}
	
	private synchronized boolean ReserveItem(String customerId, String itemId) {
		String itemRegion = ExtractRegion(itemId);
		
		if (IsCustomerBlackListed(customerId))
			return false;
		else {
			if (itemRegion.equalsIgnoreCase(this.region)) {
				Product p = productList.get(itemId);
				
				
				HashMap<String,String> reservations = p.getReservations();
				
				if (p.getQuantity() > 0 && reservations.get(customerId) == null) {
					reservations.put(customerId, customerId);
					return true;
				}
				else
					return false;
					
			}
			else {
				try {
				int port = getPort(itemRegion);
				InetAddress host;
				host = InetAddress.getLocalHost();
				
				DatagramSocket socket = new DatagramSocket();
				String message = "ReserveItem," + customerId + "," + itemId + ",";
				byte[] m = message.getBytes();
				DatagramPacket request = new DatagramPacket(m, m.length , host, port);
				socket.send(request);
				
				byte[] buffer = new byte[1000];
				DatagramPacket r = new DatagramPacket(buffer, buffer.length);
				socket.receive(r);
				
				String status = new String(r.getData());
				
				socket.close();
				if (status.contains("Reserved"))
					return true;
				else
					return false;
				}
				catch (Exception ex) {
					return false;
				}
			}
		}
		
		
			
	}
	
	private void close() {
		socket.close();
	}


	private boolean IsCustomerBlackListed(String customerId) {
		String customer = blacklistedCustomers.get(customerId);
		return customer != null;
	}
	
	private String ExtractRegion(String id) {
		return id.substring(0,2);
	}

}
