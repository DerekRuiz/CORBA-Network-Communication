package store;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    final HashMap<String, String> items;

    public Customer(String customer_id) {
        this.customer_id = customer_id;
        this.budget = 1000;
        this.items = new HashMap<>();
    }
    
    public Customer(String customer_id, double budget) {
        this.customer_id = customer_id;
        this.budget = budget;
        this.items = new HashMap<>();
    }

    public boolean equals(Customer obj) {
        return this.customer_id.equals(obj.customer_id);
    }

    public void purchaseItem(String itemID, double price, String dateOfPurchase) {
        this.items.put(itemID, dateOfPurchase);
        this.budget -= Math.abs(price);
    }

    public void reimburseItem(String itemID, double price) {
        if (this.items.containsKey(itemID)) {
            this.budget += Math.abs(price);
            this.items.remove(itemID);
        }
    }

    public String getDateOfPurchase(String itemID) {
        return this.items.get(itemID);

    }
    
    public boolean hasItem(String itemID){
        return items.containsKey(itemID);
    }
    
    public boolean hasItemFromServer(String location){
        for (Map.Entry<String,String> entry: items.entrySet()){
            String item = entry.getKey();
            if (item.startsWith(location)){
                return true;
            }
        }
        return false;
    }

}
