package fr.supinfo.istore.model;

import java.text.DecimalFormat;

public class InventoryLine {
    private long storeId;
    private long itemId;
    private String itemName;
    private double price;
    private int quantity;

    public InventoryLine(long storeId, long itemId, String itemName, double price, int quantity) {
        this.storeId = storeId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

    public long getStoreId() { return storeId; }
    public long getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#0.00");
        return itemName + " - " + df.format(price) + " € | stock=" + quantity;
    }
}
