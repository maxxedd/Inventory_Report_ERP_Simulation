package inventoryreport;

import java.sql.Timestamp;

public class StockTransfer {
    private final int transferId;
    private final String sku;
    private final String fromLocation;
    private final String toLocation;
    private final int qtyTransferred;
    private final double unitCost;
    private final double totalTransferCost;
    private final Timestamp transferDate;
    private final String notes;

    public StockTransfer(int transferId, String sku, String fromLocation, String toLocation,
                         int qtyTransferred, double unitCost, double totalTransferCost,
                         Timestamp transferDate, String notes) {
        this.transferId = transferId;
        this.sku = sku;
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
    public String getFromLocation() { return fromLocation; }
    public String getToLocation() { return toLocation; }
    public int getQtyTransferred() { return qtyTransferred; }
    public double getUnitCost() { return unitCost; }
    public double getTotalTransferCost() { return totalTransferCost; }
    public Timestamp getTransferDate() { return transferDate; }
    public String getNotes() { return notes; }
}
