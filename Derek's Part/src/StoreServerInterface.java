
import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author DRC
 */
public interface StoreServerInterface extends Remote{
    public String addItem(String managerID, String itemID, String itemName, int quantity, double price) throws RemoteException;
    
    public String removeItem(String managerID, String itemID, int quantity) throws RemoteException;

    public String[] listItemAvailability(String managerID) throws RemoteException;
    
    public String purchaseItem(String customerID, String itemID, String dateOfPurchase) throws RemoteException;

    public String[] findItem(String customerID, String itemName) throws RemoteException;
    
    public String returnItem(String customerID, String itemID, String dateOfReturn) throws RemoteException;
    
    public String exchange(String customerID, String newItemID, String oldItemID) throws RemoteException;
    
}
