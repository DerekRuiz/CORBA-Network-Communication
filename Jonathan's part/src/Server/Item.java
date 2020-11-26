package store;

public class Item {
    String itemID;
    String name;
    int quantity;
    double price;

    public Item(String itemID, String name, int quantity2, double price2) {
        this.itemID = itemID;
        this.name = name;
        this.quantity = quantity2;
        this.price = price2;
    }

    public String toString() {
        return itemID +", " + name + ", " + quantity + ", " + price;
    }
}
