/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inventoryreport;

import java.sql.Timestamp;

public class StockTransfer {
    private int transferId;
    private String sku;
    private String fromLocation;
    private String toLocation;
    private int qtyTransferred;
    private double unitCost;
    private double totalTransferCost;
    private Timestamp transferDate;

    public StockTransfer(int transferId, String sku, String fromLocation, String toLocation, int qtyTransferred, double unitCost, double totalTransferCost, Timestamp transferDate) {
        this.transferId = transferId;
        this.sku = sku;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.qtyTransferred = qtyTransferred;
        this.unitCost = unitCost;
        this.totalTransferCost = totalTransferCost;
        this.transferDate = transferDate;
    }

    public int getTransferId() { return transferId; }
    public String getSku() { return sku; }
    public String getFromLocation() { return fromLocation; }
    public String getToLocation() { return toLocation; }
    public int getQtyTransferred() { return qtyTransferred; }
    public double getUnitCost() { return unitCost; }
    public double getTotalTransferCost() { return totalTransferCost; }
    public Timestamp getTransferDate() { return transferDate; }
}