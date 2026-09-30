/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inventoryreport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public List<InventoryItem> getAllInventory() throws SQLException {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT inventory_id, sku, item_name, location_name, quantity, reorder_level, unit_cost FROM inventory ORDER BY inventory_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new InventoryItem(
                    rs.getInt("inventory_id"),
                    rs.getString("sku"),
                    rs.getString("item_name"),
                    rs.getString("location_name"),
                    rs.getInt("quantity"),
                    rs.getInt("reorder_level"),
                    rs.getDouble("unit_cost")
                ));
            }
        }
        return list;
    }

    public List<StockTransfer> getAllTransfers() throws SQLException {
        List<StockTransfer> list = new ArrayList<>();
        String sql = "SELECT transfer_id, sku, from_location, to_location, qty_transferred, unit_cost, total_transfer_cost, transfer_date FROM stock_transfers ORDER BY transfer_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new StockTransfer(
                    rs.getInt("transfer_id"),
                    rs.getString("sku"),
                    rs.getString("from_location"),
                    rs.getString("to_location"),
                    rs.getInt("qty_transferred"),
                    rs.getDouble("unit_cost"),
                    rs.getDouble("total_transfer_cost"),
                    rs.getTimestamp("transfer_date")
                ));
            }
        }
        return list;
    }

    public List<InventoryItem> getFilteredReport(String location, String statusFilter) throws SQLException {
        List<InventoryItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT inventory_id, sku, item_name, location_name, quantity, reorder_level, unit_cost " +
            "FROM inventory WHERE location_name = ?"
        );

        if ("Low Stock Items".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND quantity <= reorder_level");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setString(1, location);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new InventoryItem(
                    rs.getInt("inventory_id"),
                    rs.getString("sku"),
                    rs.getString("item_name"),
                    rs.getString("location_name"),
                    rs.getInt("quantity"),
                    rs.getInt("reorder_level"),
                    rs.getDouble("unit_cost")
                ));
            }
        }
        return list;
    }

    public void executeStockTransfer(String sku, String fromLoc, String toLoc, int transferQty) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); 

            String checkSql = "SELECT quantity, unit_cost FROM inventory WHERE sku = ? AND location_name = ?";
            double unitCost = 0.0;
            int currentQty = 0;

            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setString(1, sku);
                stmt.setString(2, fromLoc);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    currentQty = rs.getInt("quantity");
                    unitCost = rs.getDouble("unit_cost");
                } else {
                    conn.rollback();
                    throw climateException("SKU " + sku + " was not found at " + fromLoc + ".");
                }
            }

            if (currentQty < transferQty) {
                conn.rollback();
                throw climateException("Insufficient stock! Available: " + currentQty + " unit(s).");
            }

            String deductSql = "UPDATE inventory SET quantity = quantity - ? WHERE sku = ? AND location_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deductSql)) {
                stmt.setInt(1, transferQty);
                stmt.setString(2, sku);
                stmt.setString(3, fromLoc);
                stmt.executeUpdate();
            }

            String addSql = "UPDATE inventory SET quantity = quantity + ? WHERE sku = ? AND location_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(addSql)) {
                stmt.setInt(1, transferQty);
                stmt.setString(2, sku);
                stmt.setString(3, toLoc);
                int updated = stmt.executeUpdate();

                if (updated == 0) {
                    String insertSql = "INSERT INTO inventory (sku, item_name, location_name, quantity, reorder_level, unit_cost) " +
                                       "SELECT sku, item_name, ?, ?, reorder_level, unit_cost FROM inventory WHERE sku = ? LIMIT 1";
                    try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                        insStmt.setString(1, toLoc);
                        insStmt.setInt(2, transferQty);
                        insStmt.setString(3, sku);
                        insStmt.executeUpdate();
                    }
                }
            }

            String logSql = "INSERT INTO stock_transfers (sku, from_location, to_location, qty_transferred, unit_cost) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(logSql)) {
                stmt.setString(1, sku);
                stmt.setString(2, fromLoc);
                stmt.setString(3, toLoc);
                stmt.setInt(4, transferQty);
                stmt.setDouble(5, unitCost);
                stmt.executeUpdate();
            }

            conn.commit(); 
        }
    }

    private SQLException climateException(String msg) {
        return new SQLException(msg);
    }
}