/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inventoryreport;

public class InventoryItem {
    private int inventoryId;
    private String sku;
    private String itemName;
    private String locationName;
    private int quantity;
    private int reorderLevel;
    private double unitCost;
    private String status;

    public InventoryItem(int inventoryId, String sku, String itemName, String locationName, int quantity, int reorderLevel, double unitCost) {
        this.inventoryId = inventoryId;
        this.sku = sku;
        this.itemName = itemName;
        this.locationName = locationName;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.unitCost = unitCost;
        this.status = (quantity <= reorderLevel) ? "Low Stock" : "Stock On Hand";
    }

    public int getInventoryId() { return inventoryId; }
    public String getSku() { return sku; }
    public String getItemName() { return itemName; }
    public String getLocationName() { return locationName; }
    public int getQuantity() { return quantity; }
    public int getReorderLevel() { return reorderLevel; }
    public double getUnitCost() { return unitCost; }
    public String getStatus() { return status; }
}