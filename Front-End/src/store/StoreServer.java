package store;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * @author DRC
 */
public class StoreServer implements StoreServerInterface {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String location;
    private DatagramSocket aSocket;

    HashMap<String, Item> items;
    HashMap<String, Queue<String[]>> waitList;

    static HashMap<String, Manager> managers = new HashMap<>();
    static HashMap<String, Customer> customers = new HashMap<>();

    public StoreServer() {
        int UDPPort = 4321;
        this.location = "NULL";
        this.aSocket = null;
        try {
            this.aSocket = new DatagramSocket(UDPPort);
        } catch (SocketException ex) {
            System.out.println("Server " + this.location + " cannot start since port " + UDPPort + " is already bound");
            System.exit(1);
        }
        this.items = new HashMap<>();
        this.waitList = new HashMap<>();
        new Thread(() -> {
            waitForRequest(UDPPort);
        }).start();
    }

    public StoreServer(String location, int UDPPort) {
        this.location = location.toUpperCase();
        this.aSocket = null;
        try {
            this.aSocket = new DatagramSocket(UDPPort);
        } catch (SocketException ex) {
            System.out.println("Server " + this.location + " cannot start since port " + UDPPort + " is already bound");
            System.exit(1);
        }
        this.items = new HashMap<>();
        this.waitList = new HashMap<>();
        new Thread(() -> {
            waitForRequest(UDPPort);
        }).start();

        /**
         * // Initial users/items from the system
         * this.managers.put(this.location + "M1111", new Manager(this.location
         * + "M1111")); this.managers.put(this.location + "M2111", new
         * Manager(this.location + "M2111")); this.customers.put(this.location +
         * "U1110", new Customer(this.location + "U1110", 200.00));
         * this.customers.put(this.location + "U2110", new
         * Customer(this.location + "U2110")); this.items.put(this.location +
         * "1123", new Item(this.location + "1123", "lever", 6, 23.32));
         * this.items.put(this.location + "4321", new Item(this.location +
         * "4321", "bed", 2, 1230.99));
         */
    }

    @Override
    public synchronized String addItem(String managerID, String itemID, String itemName, int quantity, double price) {
        if (managerID.toUpperCase().charAt(2) != 'M') {
            this.addToLog(managerID, String.format("ERROR: Non manager accessing add item"));
            return String.format("ERROR: Non manager accessing add item");
        }
        managers.putIfAbsent(managerID, new Manager(managerID));
        if (!itemID.toUpperCase().startsWith(this.location)) {
            this.addToLog(managerID, String.format("ERROR: Adding item ID %s to location %s", itemID, this.location));
            return String.format("ERROR: Adding item ID %s to location %s", itemID, this.location);
        }

        if (items.containsKey(itemID)) {
            if (!items.get(itemID).itemName.equalsIgnoreCase(itemName)) {
                this.addToLog(managerID, String.format("ERROR: Added item name %s does not match item with id %s", itemName, itemID));
                return "ERROR: Added item name %s does not match item with id %s";
            } else if (items.get(itemID).price != price) {
                this.addToLog(managerID, String.format("ERROR: Added item price %f does not match item with id %s", price, itemID));
                return String.format("ERROR: Added item price %f does not match item with id %s", price, itemID);
            } else {
                items.get(itemID).quantity += quantity;
            }
        } else {
            items.put(itemID, new Item(itemID, itemName, quantity, price));
        }
        this.addToLog(managerID, String.format("SUCCESS: Added %d item %s to store", quantity, itemID));
        this.addToLog(this.location + "Store", String.format("SUCCESS: Manager %s added %d item %s to store", managerID, quantity, itemID));

        // Removing item for every customer is waitlist
        removeFromWaitlist(itemID, price);

        return String.format("SUCCESS: Added %d item %s to store", quantity, itemID);
    }

    @Override
    public synchronized String removeItem(String managerID, String itemID, int quantity) {
        if (managerID.toUpperCase().charAt(2) != 'M') {
            this.addToLog(managerID, String.format("ERROR: Non manager accessing remove item"));
            return String.format("ERROR: Non manager accessing remove item");
        }

        managers.putIfAbsent(managerID, new Manager(managerID));
        if (!items.containsKey(itemID)) {
            this.addToLog(managerID, String.format("ERROR: Removed item ID %s does not exist in store", itemID));
            return String.format("ERROR: Removed item ID %s does not exist in store", itemID);
        }

        if (quantity < 0) {
            Item item = items.remove(itemID);
            for (Customer customer : customers.values()) {
                if (customer.hasItem(itemID)) {
                    customer.reimburseItem(itemID, item.price);
                    this.addToLog(customer.customer_id, String.format("AUTOMATIC UPDATE: Item %s was removed from the store and was reimbursed", item.itemName));
                }
            }

            this.addToLog(managerID, String.format("SUCCESS: Item %s deleted from the store", item.itemName));
            this.addToLog(this.location + "Store", String.format("SUCCESS: Manager %s deleted item %s from the store", managerID, item.itemName));
            return String.format("SUCCESS: Item %s deleted from the store", item.itemName);
        } else {
            Item item = items.get(itemID);
            int removed = (item.quantity - quantity < 0 ? item.quantity : quantity);
            item.quantity = item.quantity - removed;

            this.addToLog(managerID, String.format("SUCCESS: Removed %d items %s from the store" + (removed == quantity ? "" : " instead of " + quantity), removed, item.itemName));
            this.addToLog(this.location + "Store", String.format("SUCCESS: Manager %s removed %d items %s from the store" + (removed == quantity ? "" : " instead of " + quantity), managerID, removed, item.itemName));
            return String.format("SUCCESS: Removed %d items %s from the store" + (removed == quantity ? "" : " instead of " + quantity), removed, item.itemName);
        }
    }

    @Override
    public synchronized String[] listItemAvailability(String managerID) {
        if (managerID.toUpperCase().charAt(2) != 'M') {
            this.addToLog(managerID, String.format("ERROR: Non manager accessing list available items"));
            return new String[]{String.format("ERROR: Non manager accessing list available items")};
        }
        managers.putIfAbsent(managerID, new Manager(managerID));

        ArrayList<String> listItems = new ArrayList<>();
        for (Map.Entry<String, Item> entry : items.entrySet()) {
            String itemID = entry.getKey();
            Item item = entry.getValue();
            listItems.add(String.format("%s %s %.2f %d", itemID, item.itemName, item.price, item.quantity));
        }
        this.addToLog(managerID, String.format("SUCCESS: Listed all available items in the server %s", this.location));
        this.addToLog(this.location + "Store", String.format("SUCCESS: Manager %s listed all available items in the server", managerID));
        return listItems.toArray(new String[listItems.size()]);
    }

    @Override
    public synchronized String purchaseItem(String customerID, String itemID, String dateOfPurchase) {
        if (customerID.contains("ADD TO WAITLIST")) {
            customerID = customerID.split(":")[0];
            String result;
            if (customerID.substring(0, 2).equalsIgnoreCase(itemID.substring(0, 2))) {
                result = waitLocalItem(customerID, itemID, dateOfPurchase);
            } else {
                result = waitForeignItem(customerID, itemID, dateOfPurchase);
            }
            return result;
        }

        if (customerID.toUpperCase().charAt(2) != 'U') {
            this.addToLog(customerID, String.format("ERROR: Non customer accessing purchase item"));
            return String.format("ERROR: Non customer accessing purchase item");
        }
        customers.putIfAbsent(customerID, new Customer(customerID));

        Customer customer = customers.get(customerID);

        // If not from this location, and 
        // customer bought item from this location or customer is on waitlist for an item from this location
        if (!customerID.substring(0, 2).equalsIgnoreCase(this.location) && (customer.hasItemFromServer(itemID.substring(0, 2).toUpperCase()) || customerInWaitlist(customerID))) {
            this.addToLog(customerID, String.format("ERROR: Cannot purchase multiple items from inter-province store"));
            return String.format("ERROR: Cannot purchase multiple items from inter-province store");
        }

        if (itemID.substring(0, 2).equalsIgnoreCase(this.location)) {
            if (!items.containsKey(itemID)) {
                this.addToLog(customerID, String.format("ERROR: Purchased item ID %s does not exist in store", itemID));
                return String.format("ERROR: Purchased item ID %s does not exist in store", itemID);
            }
            Item item = items.get(itemID);

            if (customer.budget >= item.price) {
                if (item.quantity > 0) {
                    item.quantity -= 1;
                    customer.purchaseItem(itemID, item.price, dateOfPurchase);
                    this.addToLog(customerID, String.format("SUCCESS: Purchased item %s from sever %s", itemID, this.location));
                    this.addToLog(this.location + "Store", String.format("SUCCESS: Customer %s purchased item %s from sever %s", customerID, itemID, this.location));
                    return String.format("SUCCESS: Purchased item %s from sever %s", itemID, this.location);
                } else {
                    this.addToLog(customerID, String.format("ERROR: Not enough of item %s, contacting customer", itemID));
                    customerID = customerID.split(":")[0];
                    String result;
                    if (customerID.substring(0, 2).equalsIgnoreCase(itemID.substring(0, 2))) {
                        result = waitLocalItem(customerID, itemID, dateOfPurchase);
                    } else {
                        result = waitForeignItem(customerID, itemID, dateOfPurchase);
                    }
                    return result;
                    //return String.format("WAITLIST");
                }
            } else {
                this.addToLog(customerID, String.format("ERROR: Customer could not afford item %s", itemID));
                return String.format("ERROR: Customer could not afford item %s", itemID);
            }
            // Buying from another server
        } else {
            return purchaseForeignItem(customerID, itemID, dateOfPurchase);
        }
    }

    @Override
    public synchronized String[] findItem(String customerID, String itemName) {
        if (customerID.toUpperCase().charAt(2) != 'U') {
            this.addToLog(customerID, String.format("ERROR: Non customer accessing find item"));
            return new String[]{String.format("ERROR: Non customer accessing find item")};
        }
        customers.putIfAbsent(customerID, new Customer(customerID));

        String[] local = this.findLocalItem(customerID, itemName);
        String[] foreign = this.findForeignItem(customerID, itemName);
        if (foreign.length > 0 && foreign[0].startsWith("ERROR")) {
            return foreign;
        }

        this.addToLog(customerID, String.format("SUCCESS: Found item %s from all severs", itemName));
        this.addToLog(this.location + "Store", String.format("SUCCESS: Customer %s Found item %s from all severs", customerID, itemName));

        return this.concateArray(local, foreign);
    }

    @Override
    public synchronized String returnItem(String customerID, String itemID, String dateOfReturn) {
        if (customerID.toUpperCase().charAt(2) != 'U') {
            this.addToLog(customerID, String.format("ERROR: Non customer accessing return item"));
            return String.format("ERROR: Non customer accessing return item");
        }

        if (!itemID.substring(0, 2).equalsIgnoreCase(this.location)) {
            return returnForeignItem(customerID, itemID, dateOfReturn);
        }

        customers.putIfAbsent(customerID, new Customer(customerID));
        if (!customers.get(customerID).hasItem(itemID)) {
            this.addToLog(customerID, String.format("ERROR: Returned item ID %s does not exist with customer", itemID));
            return String.format("ERROR: Returned item ID %s does not exist with customer", itemID);
        }

        Customer customer = customers.get(customerID);
        String dateOfPurchase = customer.getDateOfPurchase(itemID);
        if (dateOfPurchase != null) {
            GregorianCalendar expired = new GregorianCalendar(Integer.valueOf(dateOfPurchase.substring(4, 8)), Integer.valueOf(dateOfPurchase.substring(2, 4)) - 1, Integer.valueOf(dateOfPurchase.substring(0, 2)));
            expired.add(GregorianCalendar.DAY_OF_MONTH, 30);
            GregorianCalendar returned = new GregorianCalendar(Integer.valueOf(dateOfReturn.substring(4, 8)), Integer.valueOf(dateOfReturn.substring(2, 4)) - 1, Integer.valueOf(dateOfReturn.substring(0, 2)));

            if (returned.before(expired)) {
                customer.reimburseItem(itemID, items.get(itemID).price);
                items.get(itemID).quantity += 1;
                this.addToLog(customerID, String.format("SUCCESS: Customer returned item %s", itemID));
                this.addToLog(this.location + "Store", String.format("SUCCESS: Customer %s returned item %s", customerID, itemID));
                removeFromWaitlist(itemID, items.get(itemID).price);
                return String.format("SUCCESS: Customer returned item %s", itemID);
            } else {
                this.addToLog(customerID, String.format("ERROR: Returned item %s has passed return policy deadline", itemID));
                return String.format("ERROR: Returned item %s has passed return policy deadline", itemID);
            }
        }
        return "ERROR: Date of purchase was not found from customer";
    }

    @Override
    public String exchangeItem(String customerID, String newItemID, String oldItemID, String dateOfExchange) {
        if (customerID.toUpperCase().charAt(2) != 'U') {
            this.addToLog(customerID, String.format("ERROR: Non customer accessing return item"));
            return String.format("ERROR: Non customer accessing return item");
        }
        customers.putIfAbsent(customerID, new Customer(customerID));

        // Check if customer has old item
        Customer user = customers.get(customerID);
        if (!user.hasItem(oldItemID)) {
            this.addToLog(customerID, String.format("ERROR: Returned item ID %s does not exist with customer", oldItemID));
            return String.format("ERROR: Returned item ID %s does not exist with customer", oldItemID);
        }

        // Check if old item is passed return policy deadline
        String dateOfPurchase = user.getDateOfPurchase(oldItemID);
        GregorianCalendar expired = new GregorianCalendar(Integer.valueOf(dateOfPurchase.substring(4, 8)), Integer.valueOf(dateOfPurchase.substring(2, 4)) - 1, Integer.valueOf(dateOfPurchase.substring(0, 2)));
        expired.add(GregorianCalendar.DAY_OF_MONTH, 30);
        GregorianCalendar returned = new GregorianCalendar(Integer.valueOf(dateOfExchange.substring(4, 8)), Integer.valueOf(dateOfExchange.substring(2, 4)) - 1, Integer.valueOf(dateOfExchange.substring(0, 2)));
        if (!returned.before(expired)) {
            this.addToLog(customerID, String.format("ERROR: Returned item %s has passed return policy deadline", oldItemID));
            return String.format("ERROR: Returned item %s has passed return policy deadline", oldItemID);
        }

        // Get old item and new item as Item objects
        Item oldItem = this.itemExists(oldItemID);
        Item newItem = this.itemExists(newItemID);

        // Check if new item exists
        if (newItem == null) {
            this.addToLog(customerID, String.format("ERROR: Purchased item ID %s does not exist in store", newItemID));
            return String.format("ERROR: Purchased item ID %s does not exist in store", newItemID);
        }

        // Check if new item is available
        if (newItem.quantity == 0) {
            this.addToLog(customerID, String.format("ERROR: Not enough of new item %s", newItemID));
            return String.format("ERROR: Not enough of new item %s", newItemID);
        }

        // Check if customer has budget to exchange items
        if (user.budget + oldItem.price - newItem.price < 0) {
            this.addToLog(customerID, String.format("ERROR: Customer could not afford item %s", newItemID));
            return String.format("ERROR: Customer could not afford item %s", newItemID);
        }

        // Performs return action, unless an error occurs then returns the error
        String returnResult = this.returnItem(customerID, oldItemID, dateOfExchange);
        if (returnResult.startsWith("ERROR:")) {
            return returnResult;
        }

        // Performs purchase action, unless an error occurs then returns the error
        String purchaseResult = this.purchaseForeignItem(customerID, newItemID, dateOfExchange);
        if (purchaseResult.startsWith("ERROR:")) {
            return purchaseResult;
        }

        // Exchange complete and returns successs
        this.addToLog(this.location + "Store", String.format("SUCCESS: Customer %s exchanged item %s with item %s", customerID, oldItemID, newItemID));
        this.addToLog(customerID, String.format("SUCCESS: Customer %s exchanged item %s with item %s", customerID, oldItemID, newItemID));
        return String.format("SUCCESS: Customer %s exchanged item %s with item %s", customerID, oldItemID, newItemID);
    }

    private void removeFromWaitlist(String itemID, double price) {
        ArrayList<String[]> noBudget = new ArrayList<>();
        while (items.get(itemID).quantity > 0 && waitList.containsKey(itemID) && !waitList.get(itemID).isEmpty()) {
            String[] customerInput = waitList.get(itemID).poll();
            Customer customer = customers.get(customerInput[0]);
            if (customer.budget < price) {
                noBudget.add(customerInput);
                continue;
            }

            items.get(itemID).quantity -= 1;
            customer.purchaseItem(itemID, price, customerInput[1]);
            this.addToLog(customerInput[0], String.format("AUTOMATIC UPDATE: Item %s was avaiable and purchased", itemID));
        }
        for (String[] item : noBudget) {
            waitList.get(itemID).add(item);
        }
    }

    private boolean customerInWaitlist(String customerID) {
        for (Map.Entry<String, Queue<String[]>> entry : waitList.entrySet()) {
            for (String[] cust : entry.getValue()) {
                System.out.println(cust[0]);
                if (cust[0].equals(customerID)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String[] findLocalItem(String customerID, String itemName) {
        ArrayList<String> listItems = new ArrayList<>();
        for (Map.Entry<String, Item> entry : items.entrySet()) {
            String itemID = entry.getKey();
            Item item = entry.getValue();
            
            if (item.itemName.equalsIgnoreCase(itemName)) {
                listItems.add(String.format("%s %.2f %d", itemID, item.price, item.quantity));
            }
        }
        return listItems.toArray(new String[listItems.size()]);
    }

    private String[] findForeignItem(String customerID, String itemName) {

        DatagramSocket aSocket1 = null;
        DatagramSocket aSocket2 = null;

        try {
            aSocket1 = new DatagramSocket();
            aSocket2 = new DatagramSocket();
            DatagramPacket request1;
            DatagramPacket request2;

            byte[] resultBytes = String.format("FIND,%s,%s", customerID, itemName).getBytes();

            switch (customerID.toUpperCase().substring(0, 2)) {
                case "QC":
                    request1 = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.ONport);
                    request2 = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.BCport);

                    break;
                case "ON":
                    request1 = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.QCport);
                    request2 = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.BCport);

                    break;
                case "BC":
                    request1 = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.QCport);
                    request2 = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.ONport);
                    break;
                default:
                    return new String[]{"ERROR: Customer not located with servers"};
            }
            aSocket1.send(request1);
            aSocket2.send(request2);

            byte[] buffer1 = new byte[1000];
            byte[] buffer2 = new byte[1000];
            DatagramPacket reply1 = new DatagramPacket(buffer1, buffer1.length);
            aSocket1.receive(reply1);
            DatagramPacket reply2 = new DatagramPacket(buffer2, buffer2.length);
            aSocket2.receive(reply2);
            String items1 = new String(reply1.getData()).replace("[", "").replace("]", "").trim();
            String items2 = new String(reply2.getData()).replace("[", "").replace("]", "").trim();
            String[] arr1 = (items1.length() > 0 ? items1.split(",") : new String[]{});
            String[] arr2 = (items2.length() > 0 ? items2.split(",") : new String[]{});
            return this.concateArray(arr1, arr2);

        } catch (UnknownHostException ex) {
            System.out.println("Could not find Host");
        } catch (IOException ex) {
        } finally {
            if (aSocket1 != null) {
                aSocket1.close();
            }
            if (aSocket2 != null) {
                aSocket2.close();
            }
        }
        return null;
    }

    private String purchaseForeignItem(String customerID, String itemID, String dateOfPurchase) {
        try (DatagramSocket bSocket = new DatagramSocket()) {
            DatagramPacket request;
            byte[] resultBytes = String.format("PURCHASE,%s,%s,%s", customerID, itemID, dateOfPurchase).getBytes();

            switch (itemID.toUpperCase().substring(0, 2)) {
                case "QC":
                    request = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.QCport);
                    break;
                case "ON":
                    request = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.ONport);
                    break;
                case "BC":
                    request = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.BCport);
                    break;
                default:
                    return String.format("ERROR: No server contains the item ID %s", itemID);
            }
            bSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            bSocket.receive(reply);

            return new String(reply.getData());

        } catch (UnknownHostException ex) {
        } catch (IOException ex) {
        }
        return "ERROR: Could not connect to the server";
    }

    private String returnForeignItem(String customerID, String itemID, String dateOfReturn) {
        try (DatagramSocket bSocket = new DatagramSocket()) {
            DatagramPacket request;
            byte[] resultBytes = String.format("RETURN,%s,%s,%s", customerID, itemID, dateOfReturn).getBytes();

            switch (itemID.toUpperCase().substring(0, 2)) {
                case "QC":
                    request = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.QCport);
                    break;
                case "ON":
                    request = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.ONport);
                    break;
                case "BC":
                    request = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.BCport);
                    break;
                default:
                    return String.format("ERROR: No server contains the item ID %s", itemID);
            }
            bSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            bSocket.receive(reply);

            return new String(reply.getData());

        } catch (UnknownHostException ex) {
        } catch (IOException ex) {
        }
        return "ERROR: Could not connect to the server";
    }

    private String waitLocalItem(String customerID, String itemID, String dateOfPurchase) {
        if (!waitList.containsKey(itemID)) {
            waitList.put(itemID, new LinkedList<>());
        }

        waitList.get(itemID).add(new String[]{customerID, dateOfPurchase});

        this.addToLog(customerID, String.format("SUCCESS: Putting customer on waitlist"));
        this.addToLog(this.location + "Store", String.format("SUCCESS: Customer %s putting customer on waitlist", customerID));
        return String.format("SUCCESS: Putting customer on waitlist");
    }

    private String waitForeignItem(String customerID, String itemID, String dateOfPurchase) {
        try (DatagramSocket bSocket = new DatagramSocket()) {
            DatagramPacket request;
            byte[] resultBytes = String.format("WAIT,%s,%s,%s", customerID, itemID, dateOfPurchase).getBytes();

            switch (itemID.toUpperCase().substring(0, 2)) {
                case "QC":
                    request = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.QCport);
                    break;
                case "ON":
                    request = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.ONport);
                    break;
                case "BC":
                    request = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.BCport);
                    break;
                default:
                    return String.format("ERROR: No server contains the item ID %s", itemID);
            }
            bSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            bSocket.receive(reply);

            return new String(reply.getData());

        } catch (UnknownHostException ex) {
        } catch (IOException ex) {
        }
        return "ERROR: Could not connect to the server";
    }

    private Item itemExists(String itemID) {
        if (itemID.toUpperCase().substring(0, 2).equalsIgnoreCase(this.location)) {
            return this.items.get(itemID);
        }

        try (DatagramSocket bSocket = new DatagramSocket()) {
            DatagramPacket request;
            byte[] resultBytes = String.format("EXISTS,%s", itemID).getBytes();

            switch (itemID.toUpperCase().substring(0, 2)) {
                case "QC":
                    request = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.QCport);
                    break;
                case "ON":
                    request = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.ONport);
                    break;
                case "BC":
                    request = new DatagramPacket(resultBytes, resultBytes.length, InetAddress.getLocalHost(), Server.BCport);
                    break;
                default:
                    return null;
            }
            bSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            bSocket.receive(reply);

            String itemStr = new String(reply.getData());
            if (itemStr.trim().equalsIgnoreCase("null")) {
                return null;
            }
            String[] itemSplt = itemStr.trim().split(",");
            return new Item(itemSplt[0], itemSplt[1], Integer.valueOf(itemSplt[2]), Double.valueOf(itemSplt[3]));

        } catch (UnknownHostException ex) {
        } catch (IOException ex) {
        }
        return null;
    }

    private void waitForRequest(int UDPPort) {

        try {
            byte[] buffer = new byte[1000];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(request);
            String query = new String(request.getData());

            while (Thread.activeCount() > 20) {
                Thread.yield();
            }
            new Thread(() -> {
                waitForRequest(UDPPort);
            }).start();

            String[] args = query.split(",");
            byte[] resultBytes;
            switch (args[0].trim()) {
                case "PURCHASE":
                    String result = this.purchaseItem(args[1].trim(), args[2].trim(), args[3].trim());
                    resultBytes = result.getBytes();
                    break;
                case "FIND":
                    String[] foundItems = this.findLocalItem(args[1].trim(), args[2].trim());
                    String itemStr = Arrays.toString(foundItems);
                    resultBytes = itemStr.getBytes();
                    break;
                case "WAIT":
                    result = this.waitLocalItem(args[1].trim(), args[2].trim(), args[3].trim());
                    resultBytes = result.getBytes();
                    break;
                case "RETURN":
                    result = this.returnItem(args[1].trim(), args[2].trim(), args[3].trim());
                    resultBytes = result.getBytes();
                    break;
                case "EXISTS":
                    Item i = this.items.get(args[1].trim());
                    result = (i == null ? "null" : i.toString());
                    resultBytes = result.getBytes();
                    break;
                default:
                    resultBytes = "ERROR".getBytes();
            }

            DatagramPacket reply = new DatagramPacket(resultBytes, resultBytes.length, request.getAddress(), request.getPort());
            aSocket.send(reply);

        } catch (SocketException ex) {

        } catch (IOException ex) {
        }
    }

    private String[] concateArray(String[] arr1, String[] arr2) {
        String[] combined = new String[arr1.length + arr2.length];
        int index = 0;
        for (String item : arr1) {
            combined[index] = item;
            index++;
        }
        for (String item : arr2) {
            combined[index] = item;
            index++;
        }
        return combined;

    }

    private void addToLog(String user, String report) {
        try {
            List<String> lines = Arrays.asList(String.format("%s %s", StoreServer.dateFormat.format(new Date()), report));
            Files.write(Paths.get("Logs/" + user + "_log.txt"), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
        }
    }

    public void close() {
        aSocket.close();
    }

}
