package inventoryreport;

import java.sql.Timestamp;

/** Represents one stock movement at a single inventory location. */
public class StockMovement {
    private final int movementId;
    private final int inventoryId;
    private final String movementType;
    private final int beginningQuantity;
    private final int stockIn;
    private final int stockOut;
    private final int resultingQuantity;
    private final Integer transferId;
    private final Timestamp movementDate;
    private final String notes;
    private final String sku;
    private final String locationName;
    private final String itemName;

    /** Backward-compatible constructor. */
    public StockMovement(int movementId, int inventoryId, String movementType,
                         int beginningQuantity, int stockIn, int stockOut,
                         int resultingQuantity, Integer transferId, Timestamp movementDate,
                         String notes, String sku, String locationName) {
        this(movementId, inventoryId, movementType, beginningQuantity, stockIn,
             stockOut, resultingQuantity, transferId, movementDate, notes,
             sku, locationName, null);
    }

    public StockMovement(int movementId, int inventoryId, String movementType,
                         int beginningQuantity, int stockIn, int stockOut,
                         int resultingQuantity, Integer transferId, Timestamp movementDate,
                         String notes, String sku, String locationName, String itemName) {
        this.movementId = movementId;
        this.inventoryId = inventoryId;
        this.movementType = movementType;
        this.beginningQuantity = beginningQuantity;
        this.stockIn = stockIn;
        this.stockOut = stockOut;
        this.resultingQuantity = resultingQuantity;
        this.transferId = transferId;
        this.movementDate = movementDate;
        this.notes = notes;
        this.sku = sku;
        this.locationName = locationName;
        this.itemName = itemName;
    }

    public int getMovementId() { return movementId; }
    public int getInventoryId() { return inventoryId; }
    public String getMovementType() { return movementType; }
    public int getBeginningQuantity() { return beginningQuantity; }
    public int getStockIn() { return stockIn; }
    public int getStockOut() { return stockOut; }
    public int getResultingQuantity() { return resultingQuantity; }
    public Integer getTransferId() { return transferId; }
    public Timestamp getMovementDate() { return movementDate; }
    public String getNotes() { return notes; }
    public String getSku() { return sku; }
    public String getLocationName() { return locationName; }
    public String getItemName() { return itemName; }
}
