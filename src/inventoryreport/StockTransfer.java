package inventoryreport;

import java.sql.Timestamp;

/** Represents a completed stock transfer for display in the transfer history. */
public class StockTransfer {
    private final int transferId;
    private final String sku;
    private final String itemName;
    private final String fromLocation;
    private final String toLocation;
    private final int qtyTransferred;
    private final double unitCost;
    private final double totalTransferCost;
    private final Timestamp transferDate;
    private final String notes;

    /** Backward-compatible constructor for code that does not provide itemName. */
    public StockTransfer(int transferId, String sku, String fromLocation, String toLocation,
                         int qtyTransferred, double unitCost, double totalTransferCost,
                         Timestamp transferDate, String notes) {
        this(transferId, sku, sku, fromLocation, toLocation, qtyTransferred,
             unitCost, totalTransferCost, transferDate, notes);
    }

    public StockTransfer(int transferId, String sku, String itemName,
                         String fromLocation, String toLocation, int qtyTransferred,
                         double unitCost, double totalTransferCost,
                         Timestamp transferDate, String notes) {
        this.transferId = transferId;
        this.sku = sku;
        this.itemName = itemName;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.qtyTransferred = qtyTransferred;
        this.unitCost = unitCost;
        this.totalTransferCost = totalTransferCost;
        this.transferDate = transferDate;
        this.notes = notes;
    }

    public int getTransferId() { return transferId; }
    public String getSku() { return sku; }
    public String getItemName() { return itemName; }
    public String getFromLocation() { return fromLocation; }
    public String getToLocation() { return toLocation; }
    public int getQtyTransferred() { return qtyTransferred; }
    public double getUnitCost() { return unitCost; }
    public double getTotalTransferCost() { return totalTransferCost; }
    public Timestamp getTransferDate() { return transferDate; }
    public String getNotes() { return notes; }
}
