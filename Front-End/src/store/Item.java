package store;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author DRC
 */
public class Item {

        public String itemID;
        public String itemName;
        public int quantity;
        public double price;

        public Item(String itemID, String itemName, int quantity, double price) {
            this.itemID = itemID;
            this.itemName = itemName;
            this.quantity = quantity;
            this.price = price;
        }

        @Override
        public String toString() {
            return String.format("%s,%s,%d,%f", this.itemID, this.itemName, this.quantity, this.price);
        }

    }
