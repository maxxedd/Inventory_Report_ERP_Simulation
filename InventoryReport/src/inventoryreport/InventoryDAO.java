package inventoryreport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access for current inventory, stock transfers, and stock movement history.
 */
public class InventoryDAO {

    public List<InventoryItem> getAllInventory() throws SQLException {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT inventory_id, sku, item_name, location_name, quantity, reorder_level, unit_cost "
                   + "FROM inventory ORDER BY inventory_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapInventoryItem(rs));
            }
        }
        return list;
    }

    /** Filters the main inventory table by SKU/name and location. */
    public List<InventoryItem> getFilteredInventory(String keyword, String location) throws SQLException {
        List<InventoryItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT inventory_id, sku, item_name, location_name, quantity, reorder_level, unit_cost "
              + "FROM inventory WHERE 1 = 1");

        boolean filterKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean filterLocation = isSpecificLocation(location);
        if (filterKeyword) {
            sql.append(" AND (sku LIKE ? OR item_name LIKE ?)");
        }
        if (filterLocation) {
            sql.append(" AND location_name = ?");
        }
        sql.append(" ORDER BY inventory_id ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            if (filterKeyword) {
                String pattern = "%" + keyword.trim() + "%";
                pstmt.setString(parameterIndex++, pattern);
                pstmt.setString(parameterIndex++, pattern);
            }
            if (filterLocation) {
                pstmt.setString(parameterIndex, location.trim());
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInventoryItem(rs));
                }
            }
        }
        return list;
    }

    public List<StockTransfer> getAllTransfers() throws SQLException {
        List<StockTransfer> list = new ArrayList<>();
        String sql = "SELECT transfer_id, sku, from_location, to_location, qty_transferred, unit_cost, "
                   + "total_transfer_cost, transfer_date, notes "
                   + "FROM stock_transfers ORDER BY transfer_id DESC";
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
                    rs.getTimestamp("transfer_date"),
                    rs.getString("notes")
                ));
            }
        }
        return list;
    }

    /**
     * Returns stock movement rows filtered by the item's location and/or movement type.
     * Valid location filters: All Location, All Locations, Warehouse A, Main Branch.
     * Valid movement filters: All, Stock In, Stock Out.
     */
    public List<StockMovement> getFilteredStockMovements(String locationFilter,
                                                          String movementTypeFilter) throws SQLException {
        List<StockMovement> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT sm.movement_id, sm.inventory_id, sm.movement_type, "
              + "sm.beginning_quantity, sm.stock_in, sm.stock_out, sm.resulting_quantity, "
              + "sm.transfer_id, sm.movement_date, sm.notes, i.sku, i.location_name "
              + "FROM stock_movements sm "
              + "INNER JOIN inventory i ON i.inventory_id = sm.inventory_id "
              + "WHERE 1 = 1");

        boolean filterLocation = isSpecificLocation(locationFilter);
        boolean filterMovementType = movementTypeFilter != null
                && !movementTypeFilter.trim().isEmpty()
                && !"All".equalsIgnoreCase(movementTypeFilter.trim())
                && !"All Movements".equalsIgnoreCase(movementTypeFilter.trim());

        if (filterLocation) {
            sql.append(" AND i.location_name = ?");
        }
        if (filterMovementType) {
            sql.append(" AND sm.movement_type = ?");
        }
        sql.append(" ORDER BY sm.movement_date DESC, sm.movement_id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            if (filterLocation) {
                pstmt.setString(parameterIndex++, locationFilter.trim());
            }
            if (filterMovementType) {
                pstmt.setString(parameterIndex, movementTypeFilter.trim());
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int rawTransferId = rs.getInt("transfer_id");
                    Integer transferId = rs.wasNull() ? null : Integer.valueOf(rawTransferId);
                    list.add(new StockMovement(
                        rs.getInt("movement_id"),
                        rs.getInt("inventory_id"),
                        rs.getString("movement_type"),
                        rs.getInt("beginning_quantity"),
                        rs.getInt("stock_in"),
                        rs.getInt("stock_out"),
                        rs.getInt("resulting_quantity"),
                        transferId,
                        rs.getTimestamp("movement_date"),
                        rs.getString("notes"),
                        rs.getString("sku"),
                        rs.getString("location_name")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Reports inventory at one location or all locations. Low Stock Items means
     * quantity <= reorder_level; Stock On Hand means quantity > reorder_level.
     */
    public List<InventoryItem> getFilteredReport(String location, String statusFilter) throws SQLException {
        List<InventoryItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT inventory_id, sku, item_name, location_name, quantity, reorder_level, unit_cost "
              + "FROM inventory WHERE 1 = 1");

        boolean filterLocation = isSpecificLocation(location);
        if (filterLocation) {
            sql.append(" AND location_name = ?");
        }
        if ("Low Stock Items".equalsIgnoreCase(statusFilter == null ? "" : statusFilter.trim())) {
            sql.append(" AND quantity <= reorder_level");
        } else {
            sql.append(" AND quantity > reorder_level");
        }
        sql.append(" ORDER BY location_name ASC, item_name ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            if (filterLocation) {
                pstmt.setString(1, location.trim());
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInventoryItem(rs));
                }
            }
        }
        return list;
    }

    private InventoryItem mapInventoryItem(ResultSet rs) throws SQLException {
        return new InventoryItem(
            rs.getInt("inventory_id"),
            rs.getString("sku"),
            rs.getString("item_name"),
            rs.getString("location_name"),
            rs.getInt("quantity"),
            rs.getInt("reorder_level"),
            rs.getDouble("unit_cost")
        );
    }

    /**
     * Transfers stock and writes two stock movement records (Stock Out at the source
     * and Stock In at the destination) in the same database transaction.
     */
    public void executeStockTransfer(String sku, String fromLoc, String toLoc,
                                     int transferQty, String notes) throws SQLException {
        if (sku == null || sku.trim().isEmpty()) {
            throw new SQLException("SKU is required.");
        }
        if (transferQty <= 0) {
            throw new SQLException("Transfer quantity must be greater than zero.");
        }
        if (fromLoc == null || toLoc == null || fromLoc.trim().isEmpty() || toLoc.trim().isEmpty()
                || fromLoc.trim().equalsIgnoreCase(toLoc.trim())) {
            throw new SQLException("Source and destination locations must be different.");
        }

        String cleanSku = sku.trim();
        String cleanFrom = fromLoc.trim();
        String cleanTo = toLoc.trim();
        String cleanNotes = (notes == null || notes.trim().isEmpty()) ? null : notes.trim();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                SourceInventory source = getSourceInventoryForUpdate(conn, cleanSku, cleanFrom);
                if (source.quantity < transferQty) {
                    throw new SQLException("Insufficient stock at " + cleanFrom
                            + ". Available: " + source.quantity + " unit(s).");
                }

                DestinationInventory destination = getDestinationInventoryForUpdate(conn, cleanSku, cleanTo);
                int destinationBeginning = (destination == null) ? 0 : destination.quantity;
                int sourceResulting = source.quantity - transferQty;
                int destinationResulting = destinationBeginning + transferQty;

                String deductSql = "UPDATE inventory SET quantity = quantity - ? "
                                 + "WHERE inventory_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deductSql)) {
                    stmt.setInt(1, transferQty);
                    stmt.setInt(2, source.inventoryId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Could not update source inventory row.");
                    }
                }

                int destinationInventoryId;
                if (destination != null) {
                    String addSql = "UPDATE inventory SET quantity = quantity + ? WHERE inventory_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(addSql)) {
                        stmt.setInt(1, transferQty);
                        stmt.setInt(2, destination.inventoryId);
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("Could not update destination inventory row.");
                        }
                    }
                    destinationInventoryId = destination.inventoryId;
                } else {
                    String insertDestinationSql = "INSERT INTO inventory "
                            + "(sku, item_name, location_name, quantity, reorder_level, unit_cost) "
                            + "VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertDestinationSql,
                            Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, cleanSku);
                        stmt.setString(2, source.itemName);
                        stmt.setString(3, cleanTo);
                        stmt.setInt(4, transferQty);
                        stmt.setInt(5, source.reorderLevel);
                        stmt.setDouble(6, source.unitCost);
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("Could not create destination inventory row.");
                        }
                        try (ResultSet keys = stmt.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException("Could not retrieve the new destination inventory ID.");
                            }
                            destinationInventoryId = keys.getInt(1);
                        }
                    }
                }

                int transferId = insertTransferRecord(conn, cleanSku, cleanFrom, cleanTo,
                        transferQty, source.unitCost, cleanNotes);

                insertStockMovement(conn, source.inventoryId, "Stock Out",
                        source.quantity, 0, transferQty, sourceResulting,
                        Integer.valueOf(transferId), cleanNotes);
                insertStockMovement(conn, destinationInventoryId, "Stock In",
                        destinationBeginning, transferQty, 0, destinationResulting,
                        Integer.valueOf(transferId), cleanNotes);

                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Connection is about to be closed by try-with-resources.
                }
            }
        }
    }

    /**
     * Records a standalone Stock In or Stock Out. This DAO method can be called
     * from a stock-adjustment form/button if the UI later provides one.
     * Stock In can create a location row if the SKU already exists elsewhere.
     */
    public void recordStockAdjustment(String sku, String location, String movementType,
                                      int quantity, String notes) throws SQLException {
        if (sku == null || sku.trim().isEmpty()) {
            throw new SQLException("SKU is required.");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new SQLException("Location is required.");
        }
        if (quantity <= 0) {
            throw new SQLException("Quantity must be greater than zero.");
        }
        if (movementType == null
                || (!"Stock In".equalsIgnoreCase(movementType.trim())
                    && !"Stock Out".equalsIgnoreCase(movementType.trim()))) {
            throw new SQLException("Movement type must be Stock In or Stock Out.");
        }

        String type = movementType.trim().equalsIgnoreCase("Stock In") ? "Stock In" : "Stock Out";
        String cleanSku = sku.trim();
        String cleanLocation = location.trim();
        String cleanNotes = (notes == null || notes.trim().isEmpty()) ? null : notes.trim();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                AdjustmentInventory item = getInventoryForAdjustment(conn, cleanSku, cleanLocation);
                int beginning;
                int resulting;
                int inventoryId;
                if (item == null) {
                    if (!"Stock In".equals(type)) {
                        throw new SQLException("SKU " + cleanSku + " was not found at " + cleanLocation + ".");
                    }
                    SourceInventory template = getAnyInventoryForSku(conn, cleanSku);
                    String insertSql = "INSERT INTO inventory "
                            + "(sku, item_name, location_name, quantity, reorder_level, unit_cost) "
                            + "VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, cleanSku);
                        stmt.setString(2, template.itemName);
                        stmt.setString(3, cleanLocation);
                        stmt.setInt(4, quantity);
                        stmt.setInt(5, template.reorderLevel);
                        stmt.setDouble(6, template.unitCost);
                        stmt.executeUpdate();
                        try (ResultSet keys = stmt.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException("Could not retrieve the new inventory ID.");
                            }
                            inventoryId = keys.getInt(1);
                        }
                    }
                    beginning = 0;
                    resulting = quantity;
                } else {
                    inventoryId = item.inventoryId;
                    beginning = item.quantity;
                    if ("Stock Out".equals(type) && beginning < quantity) {
                        throw new SQLException("Insufficient stock at " + cleanLocation
                                + ". Available: " + beginning + " unit(s).");
                    }
                    resulting = "Stock In".equals(type) ? beginning + quantity : beginning - quantity;
                    String updateSql = "UPDATE inventory SET quantity = ? WHERE inventory_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                        stmt.setInt(1, resulting);
                        stmt.setInt(2, inventoryId);
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("Could not update inventory quantity.");
                        }
                    }
                }

                int stockIn = "Stock In".equals(type) ? quantity : 0;
                int stockOut = "Stock Out".equals(type) ? quantity : 0;
                insertStockMovement(conn, inventoryId, type, beginning, stockIn, stockOut,
                        resulting, null, cleanNotes);
                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Connection is about to be closed by try-with-resources.
                }
            }
        }
    }

    private SourceInventory getSourceInventoryForUpdate(Connection conn, String sku,
                                                          String location) throws SQLException {
        String sql = "SELECT inventory_id, item_name, quantity, reorder_level, unit_cost "
                   + "FROM inventory WHERE sku = ? AND location_name = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sku);
            stmt.setString(2, location);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("SKU " + sku + " was not found at " + location + ".");
                }
                return new SourceInventory(rs.getInt("inventory_id"), rs.getString("item_name"),
                        rs.getInt("quantity"), rs.getInt("reorder_level"), rs.getDouble("unit_cost"));
            }
        }
    }

    private DestinationInventory getDestinationInventoryForUpdate(Connection conn, String sku,
                                                                   String location) throws SQLException {
        String sql = "SELECT inventory_id, quantity FROM inventory "
                   + "WHERE sku = ? AND location_name = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sku);
            stmt.setString(2, location);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new DestinationInventory(rs.getInt("inventory_id"), rs.getInt("quantity"));
            }
        }
    }

    private AdjustmentInventory getInventoryForAdjustment(Connection conn, String sku,
                                                            String location) throws SQLException {
        String sql = "SELECT inventory_id, quantity FROM inventory "
                   + "WHERE sku = ? AND location_name = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sku);
            stmt.setString(2, location);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new AdjustmentInventory(rs.getInt("inventory_id"), rs.getInt("quantity"));
            }
        }
    }

    private SourceInventory getAnyInventoryForSku(Connection conn, String sku) throws SQLException {
        String sql = "SELECT inventory_id, item_name, quantity, reorder_level, unit_cost "
                   + "FROM inventory WHERE sku = ? ORDER BY inventory_id ASC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("SKU " + sku + " does not exist in inventory at any location.");
                }
                return new SourceInventory(rs.getInt("inventory_id"), rs.getString("item_name"),
                        rs.getInt("quantity"), rs.getInt("reorder_level"), rs.getDouble("unit_cost"));
            }
        }
    }

    private int insertTransferRecord(Connection conn, String sku, String fromLoc, String toLoc,
                                     int quantity, double unitCost, String notes) throws SQLException {
        String sql = "INSERT INTO stock_transfers "
                   + "(sku, from_location, to_location, qty_transferred, unit_cost, notes) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, sku);
            stmt.setString(2, fromLoc);
            stmt.setString(3, toLoc);
            stmt.setInt(4, quantity);
            stmt.setDouble(5, unitCost);
            if (notes == null) {
                stmt.setNull(6, Types.VARCHAR);
            } else {
                stmt.setString(6, notes);
            }
            if (stmt.executeUpdate() != 1) {
                throw new SQLException("Could not save the stock transfer record.");
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Could not retrieve the new transfer ID.");
                }
                return keys.getInt(1);
            }
        }
    }

    private void insertStockMovement(Connection conn, int inventoryId, String movementType,
                                     int beginningQuantity, int stockIn, int stockOut,
                                     int resultingQuantity, Integer transferId, String notes) throws SQLException {
        String sql = "INSERT INTO stock_movements "
                   + "(inventory_id, movement_type, beginning_quantity, stock_in, stock_out, "
                   + "resulting_quantity, transfer_id, notes) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, inventoryId);
            stmt.setString(2, movementType);
            stmt.setInt(3, beginningQuantity);
            stmt.setInt(4, stockIn);
            stmt.setInt(5, stockOut);
            stmt.setInt(6, resultingQuantity);
            if (transferId == null) {
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setInt(7, transferId.intValue());
            }
            if (notes == null) {
                stmt.setNull(8, Types.VARCHAR);
            } else {
                stmt.setString(8, notes);
            }
            stmt.executeUpdate();
        }
    }

    private boolean isSpecificLocation(String location) {
        return location != null
                && !location.trim().isEmpty()
                && !"All Location".equalsIgnoreCase(location.trim())
                && !"All Locations".equalsIgnoreCase(location.trim())
                && !"All".equalsIgnoreCase(location.trim());
    }

    private static final class SourceInventory {
        private final int inventoryId;
        private final String itemName;
        private final int quantity;
        private final int reorderLevel;
        private final double unitCost;

        private SourceInventory(int inventoryId, String itemName, int quantity,
                                int reorderLevel, double unitCost) {
            this.inventoryId = inventoryId;
            this.itemName = itemName;
            this.quantity = quantity;
            this.reorderLevel = reorderLevel;
            this.unitCost = unitCost;
        }
    }

    private static final class DestinationInventory {
        private final int inventoryId;
        private final int quantity;

        private DestinationInventory(int inventoryId, int quantity) {
            this.inventoryId = inventoryId;
            this.quantity = quantity;
        }
    }

    private static final class AdjustmentInventory {
        private final int inventoryId;
        private final int quantity;

        private AdjustmentInventory(int inventoryId, int quantity) {
            this.inventoryId = inventoryId;
            this.quantity = quantity;
        }
    }
}
