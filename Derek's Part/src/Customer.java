
import java.util.ArrayList;
import java.util.HashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author DRC
 */
public class Customer {

    String customer_id;
    double budget;
    private final ArrayList<String> items;
    private final HashMap<String, String> dateOfPurchase;

    public Customer(String customer_id) {
        this.customer_id = customer_id;
        this.budget = 1000;
        this.items = new ArrayList<>();
        this.dateOfPurchase = new HashMap<>();
    }
    
    public Customer(String customer_id, double budget) {
        this.customer_id = customer_id;
        this.budget = budget;
        this.items = new ArrayList<>();
        this.dateOfPurchase = new HashMap<>();
    }

    public boolean equals(Customer obj) {
        return this.customer_id.equals(obj.customer_id);
    }

    public void purchaseItem(String itemID, double price, String dateOfPurchase) {
        this.items.add(itemID);
        this.dateOfPurchase.put(itemID, dateOfPurchase);
        this.budget -= Math.abs(price);
    }

    public void reimburseItem(String itemID, double price) {
        if (this.items.remove(itemID)) {
            this.budget += Math.abs(price);
            this.dateOfPurchase.remove(itemID);
        }
    }

    public String getDateOfPurchase(String itemID) {
        return this.dateOfPurchase.get(itemID);

    }
    
    public boolean hasItem(String itemID){
        return items.contains(itemID);
    }
    
    public boolean hasItemFromServer(String location){
        for (String item: items){
            if (item.startsWith(location)){
                return true;
            }
        }
        return false;
    }

}
