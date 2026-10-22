/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inventoryreport;

/**
 *
 * @author Acer
 */

import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class InventoryReportFRM extends javax.swing.JFrame {

    /**
     * Creates new form InventoryReportFRM
     */
private final InventoryDAO inventoryDAO = new InventoryDAO();

    public InventoryReportFRM() {
        initComponents();
        setupInventoryRowSelection();

        // All four search fields update their corresponding table while typing.
        addLiveSearchListener(jTextField1, this::applyInventoryFilters);
        addLiveSearchListener(jTextField2, this::loadTransferTable);
        addLiveSearchListener(jTextField3, this::loadStockMovementTable);
        addLiveSearchListener(jTextField4, this::generateReport);

        // These filters must work together with the stock movement name search.
        location.addActionListener(e -> loadStockMovementTable());
        movement.addActionListener(e -> loadStockMovementTable());

        refreshAllViews();
    }

    /** Attach a DocumentListener so a table reloads on insert, deletion, or replacement. */
    private void addLiveSearchListener(javax.swing.JTextField field, Runnable refreshAction) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshAction.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshAction.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshAction.run();
            }
        });
    }

    private void refreshAllViews() {
        loadInventoryTable();
        loadTransferTable();
        loadStockMovementTable();
        generateReport();
    }

    private void setupInventoryRowSelection() {
        // Allow the user to select a row in the inventory table.
        inventoryTable.setRowSelectionAllowed(true);
        inventoryTable.setSelectionMode(
            javax.swing.ListSelectionModel.SINGLE_SELECTION
        );

        // Automatically populate skuInput when a row is selected.
        inventoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            int selectedRow = inventoryTable.getSelectedRow();

            if (selectedRow >= 0) {
                // Convert the selected row in case the table is sorted.
                int modelRow = inventoryTable.convertRowIndexToModel(selectedRow);

                // Column index 1 contains the SKU.
                Object skuValue = inventoryTable.getModel()
                        .getValueAt(modelRow, 1);

                if (skuValue != null) {
                    skuInput.setText(skuValue.toString());
                }
            }
        });
    }


    private void performStockTransfer() {
        String sku = skuInput.getText().trim();
        String qtyStr = quantityInput.getText().trim();
        Object fromSelection = transferFromDD.getSelectedItem();
        Object toSelection = transferToDD.getSelectedItem();
        Object notesSelection = transferReasonDD.getSelectedItem();
        if (fromSelection == null || toSelection == null || notesSelection == null) {
            JOptionPane.showMessageDialog(this, "Please select source, destination, and transfer reason.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String fromLoc = fromSelection.toString().trim();
        String toLoc = toSelection.toString().trim();
        String notes = notesSelection.toString().trim();

        if (sku.isEmpty() || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both SKU and Quantity.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (fromLoc.equals(toLoc)) {
            JOptionPane.showMessageDialog(this, "Source and Destination locations must be different.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();

            inventoryDAO.executeStockTransfer(sku, fromLoc, toLoc, qty, notes);

            JOptionPane.showMessageDialog(this, "Stock transfer of " + qty + " unit(s) successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            skuInput.setText("");
            quantityInput.setText("");

            refreshAllViews();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity must be a positive integer.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Transfer Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadInventoryTable() {
        applyInventoryFilters();

       }
    
    private void applyInventoryFilters() {
        DefaultTableModel model =
                (DefaultTableModel) inventoryTable.getModel();

        model.setRowCount(0);

        String keyword = jTextField1.getText().trim();

        Object selectedLocation = jComboBox1.getSelectedItem();

        String location = selectedLocation == null
                ? "All Locations"
                : selectedLocation.toString().trim();

        try {
            List<InventoryItem> list =
                    inventoryDAO.getFilteredInventory(keyword, location);

            for (InventoryItem item : list) {
                model.addRow(new Object[]{
                    item.getInventoryId(),
                    item.getSku(),
                    item.getItemName(),
                    item.getLocationName(),
                    item.getQuantity(),
                    item.getReorderLevel(),
                    String.format("%.2f", item.getUnitCost())
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load inventory: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /** Loads transfer records matching the current live search and updates the filtered total. */
    private void loadTransferTable() {
        DefaultTableModel model = (DefaultTableModel) transferRecord.getModel();
        model.setRowCount(0);
        double grandTotal = 0.0;
        String keyword = jTextField2.getText().trim();

        try {
            List<StockTransfer> list = inventoryDAO.getFilteredTransfers(keyword);
            for (StockTransfer st : list) {
                grandTotal += st.getTotalTransferCost();
                model.addRow(new Object[]{
                    st.getTransferId(),
                    st.getSku(),
                    st.getItemName(),
                    st.getFromLocation(),
                    st.getToLocation(),
                    st.getQtyTransferred(),
                    String.format("%.2f", st.getUnitCost()),
                    String.format("%.2f", st.getTotalTransferCost()),
                    st.getTransferDate(),
                    st.getNotes()
                });
            }
            // The displayed total always reflects the rows currently shown.
            grandTotalOutput.setText(String.format("%.2f", grandTotal));
        } catch (SQLException e) {
            grandTotalOutput.setText("0.00");
            JOptionPane.showMessageDialog(this,
                    "Unable to load stock transfer records: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Loads stock movements using the live item-name search and both dropdown filters. */
    private void loadStockMovementTable() {
        DefaultTableModel model = (DefaultTableModel) stockMovement.getModel();
        model.setRowCount(0);

        Object selectedLocation = location.getSelectedItem();
        Object selectedMovement = movement.getSelectedItem();
        String locationFilter = selectedLocation == null
                ? "All Location" : selectedLocation.toString().trim();
        String movementFilter = selectedMovement == null
                ? "All" : selectedMovement.toString().trim();
        String keyword = jTextField3.getText().trim();

        try {
            List<StockMovement> records = inventoryDAO.getFilteredStockMovements(
                    locationFilter, movementFilter, keyword);

            for (StockMovement record : records) {
                model.addRow(new Object[]{
                    record.getMovementId(),
                    record.getInventoryId(),
                    record.getItemName(),
                    record.getMovementType(),
                    record.getBeginningQuantity(),
                    record.getStockIn(),
                    record.getStockOut(),
                    record.getResultingQuantity(),
                    record.getTransferId() == null ? "" : record.getTransferId(),
                    record.getMovementDate(),
                    record.getNotes()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load stock movement records: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateReport() {
        Object selectedLocation = locationDD.getSelectedItem();
        Object selectedStatus = statusDD.getSelectedItem();

        String location = (selectedLocation == null)
                ? "All Locations"
                : selectedLocation.toString().trim();

        String status = (selectedStatus == null)
                ? "Stock On Hand"
                : selectedStatus.toString().trim();

        DefaultTableModel model =
                (DefaultTableModel) inventoryReportTbl.getModel();

        model.setRowCount(0);

        try {
            String keyword = jTextField4.getText().trim();
            List<InventoryItem> list =
                    inventoryDAO.getFilteredReport(location, status, keyword);

            for (InventoryItem item : list) {
                model.addRow(new Object[]{
                    item.getInventoryId(),
                    item.getSku(),
                    item.getItemName(),
                    item.getQuantity(),
                    item.getReorderLevel(),
                    item.getStatus()
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to generate inventory report: "
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }                                     

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        inventoryTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        transferFromDD = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        transferToDD = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        skuInput = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        quantityInput = new javax.swing.JTextField();
        transferButton = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        transferReasonDD = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        transferRecord = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        grandTotalOutput = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        stockMovement = new javax.swing.JTable();
        jLabel14 = new javax.swing.JLabel();
        location = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        movement = new javax.swing.JComboBox();
        jLabel18 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        inventoryReportTbl = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        locationDD = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        statusDD = new javax.swing.JComboBox();
        jLabel19 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setMinimumSize(new java.awt.Dimension(1209, 628));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setText("Inventory Report Simulation");

        inventoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Inventory ID", "SKU", "Name", "Location", "Quantity", "Reorder Level", "Unit Cost"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        inventoryTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(inventoryTable);
        if (inventoryTable.getColumnModel().getColumnCount() > 0) {
            inventoryTable.getColumnModel().getColumn(0).setResizable(false);
            inventoryTable.getColumnModel().getColumn(1).setResizable(false);
            inventoryTable.getColumnModel().getColumn(2).setResizable(false);
            inventoryTable.getColumnModel().getColumn(3).setResizable(false);
            inventoryTable.getColumnModel().getColumn(4).setResizable(false);
            inventoryTable.getColumnModel().getColumn(5).setResizable(false);
            inventoryTable.getColumnModel().getColumn(6).setResizable(false);
        }

        jLabel2.setText("Transfer Stocks");

        jLabel3.setText("From:");

        transferFromDD.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Warehouse A", "Main Branch" }));
        transferFromDD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transferFromDDActionPerformed(evt);
            }
        });

        jLabel4.setText("To:");

        transferToDD.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Warehouse A", "Main Branch" }));
        transferToDD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transferToDDActionPerformed(evt);
            }
        });

        jLabel5.setText("Item SKU:");

        skuInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skuInputActionPerformed(evt);
            }
        });

        jLabel6.setText("Quantity:");

        quantityInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quantityInputActionPerformed(evt);
            }
        });

        transferButton.setText("Transfer");
        transferButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transferButtonActionPerformed(evt);
            }
        });

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel8.setText("Search Item (Enter Item SKU/Name)");

        jLabel12.setText("Location: ");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All Location", "Warehouse A", "Main Branch" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel15.setText("Notes: ");

        transferReasonDD.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Branch Replenishment", "Low Stock at Destination", "High Demand", "Overstock Redistribution", "Scheduled Stock Balancing", "Urgent Stock Request", "Others" }));
        transferReasonDD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transferReasonDDActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel8)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(transferFromDD, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(transferToDD, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(skuInput, javax.swing.GroupLayout.PREFERRED_SIZE, 367, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(quantityInput)
                            .addComponent(transferReasonDD, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(36, 36, 36)
                        .addComponent(transferButton, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(249, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(skuInput, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(quantityInput, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(transferButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(transferFromDD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(transferToDD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(transferReasonDD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41))
        );

        jTabbedPane1.addTab("Stock Management & Transfer Forms", jPanel5);

        transferRecord.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Trasfer ID", "SKU", "Name", "From", "To", "Quantity", "Unit Cost", "Total Cost", "Date", "Notes"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        transferRecord.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(transferRecord);
        if (transferRecord.getColumnModel().getColumnCount() > 0) {
            transferRecord.getColumnModel().getColumn(0).setResizable(false);
            transferRecord.getColumnModel().getColumn(1).setResizable(false);
            transferRecord.getColumnModel().getColumn(2).setResizable(false);
            transferRecord.getColumnModel().getColumn(3).setResizable(false);
            transferRecord.getColumnModel().getColumn(4).setResizable(false);
            transferRecord.getColumnModel().getColumn(5).setResizable(false);
            transferRecord.getColumnModel().getColumn(6).setResizable(false);
            transferRecord.getColumnModel().getColumn(7).setResizable(false);
            transferRecord.getColumnModel().getColumn(8).setResizable(false);
            transferRecord.getColumnModel().getColumn(9).setResizable(false);
        }

        jLabel7.setText("Grand Total: ");

        grandTotalOutput.setEditable(false);
        grandTotalOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grandTotalOutputActionPerformed(evt);
            }
        });

        jLabel17.setText("Search Item (Enter Item SKU/Name)");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1097, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(grandTotalOutput, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(51, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(grandTotalOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33))
        );

        jTabbedPane1.addTab("Stock Transfer Record", jPanel2);

        stockMovement.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Movement ID", "Inventory ID", "Name", "Movement Type", "Beginning Quantity", "Stock IN", "Stock OUT", "Resulting Quantity", "Transfer ID", "Movement Date", "Notes"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(stockMovement);
        if (stockMovement.getColumnModel().getColumnCount() > 0) {
            stockMovement.getColumnModel().getColumn(0).setResizable(false);
            stockMovement.getColumnModel().getColumn(1).setResizable(false);
            stockMovement.getColumnModel().getColumn(2).setResizable(false);
            stockMovement.getColumnModel().getColumn(3).setResizable(false);
            stockMovement.getColumnModel().getColumn(4).setResizable(false);
            stockMovement.getColumnModel().getColumn(5).setResizable(false);
            stockMovement.getColumnModel().getColumn(6).setResizable(false);
            stockMovement.getColumnModel().getColumn(7).setResizable(false);
            stockMovement.getColumnModel().getColumn(8).setResizable(false);
            stockMovement.getColumnModel().getColumn(9).setResizable(false);
            stockMovement.getColumnModel().getColumn(10).setResizable(false);
        }

        jLabel14.setText("Location: ");

        location.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All Location", "Warehouse A", "Main Branch" }));

        jLabel16.setText("Movement: ");

        movement.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "Stock In", "Stock Out" }));

        jLabel18.setText("Search Item by Name:");

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1121, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(location, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(71, 71, 71)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(movement, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField3)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(location, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(movement, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(71, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Stock Movement Records", jPanel3);

        inventoryReportTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Inventory ID", "SKU", "Name", "Quantity on Hand", "Reorder Level", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(inventoryReportTbl);
        if (inventoryReportTbl.getColumnModel().getColumnCount() > 0) {
            inventoryReportTbl.getColumnModel().getColumn(0).setResizable(false);
            inventoryReportTbl.getColumnModel().getColumn(1).setResizable(false);
            inventoryReportTbl.getColumnModel().getColumn(2).setResizable(false);
            inventoryReportTbl.getColumnModel().getColumn(3).setResizable(false);
            inventoryReportTbl.getColumnModel().getColumn(4).setResizable(false);
            inventoryReportTbl.getColumnModel().getColumn(5).setResizable(false);
        }

        jLabel10.setText("Location:");

        locationDD.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All Location ", "Warehouse A", "Main Branch" }));
        locationDD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationDDActionPerformed(evt);
            }
        });

        jLabel11.setText("Status:");

        statusDD.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Stock On Hand", "Low Stock Items" }));
        statusDD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusDDActionPerformed(evt);
            }
        });

        jLabel19.setText("Search Item (Enter Item SKU/Name):");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(locationDD, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(68, 68, 68)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(statusDD, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField4)))
                        .addGap(0, 564, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(57, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(locationDD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(statusDD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
        );

        jTabbedPane1.addTab("Inventory Reports", jPanel4);

        jLabel13.setFont(new java.awt.Font("Tahoma", 2, 10)); // NOI18N
        jLabel13.setText("by: Calbario");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel13))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1126, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(448, 448, 448)
                        .addComponent(jLabel1)))
                .addGap(0, 51, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addGap(26, 26, 26)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel13))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void skuInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skuInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_skuInputActionPerformed

    private void transferFromDDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transferFromDDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_transferFromDDActionPerformed

    private void transferToDDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transferToDDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_transferToDDActionPerformed

    private void quantityInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quantityInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_quantityInputActionPerformed

    private void grandTotalOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grandTotalOutputActionPerformed
        // TODO add your handling code here:
        loadTransferTable();

    }//GEN-LAST:event_grandTotalOutputActionPerformed

    private void locationDDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationDDActionPerformed
        generateReport();
    }//GEN-LAST:event_locationDDActionPerformed

    private void statusDDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusDDActionPerformed
        generateReport();
    }//GEN-LAST:event_statusDDActionPerformed

    private void generateReportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateReportButtonActionPerformed
        // TODO add your handling code here:
        generateReport();
    }//GEN-LAST:event_generateReportButtonActionPerformed

    private void transferButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transferButtonActionPerformed
         // TODO add your handling code here:
         performStockTransfer();
    }//GEN-LAST:event_transferButtonActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
        applyInventoryFilters();

    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void transferReasonDDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transferReasonDDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_transferReasonDDActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(InventoryReportFRM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InventoryReportFRM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InventoryReportFRM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InventoryReportFRM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InventoryReportFRM().setVisible(true);
            }
        });
    }
    public javax.swing.JTable getInventoryTable() { return inventoryTable; }
    public javax.swing.JTable getTransferRecord() { return transferRecord; }
    public javax.swing.JTable getInventoryReportTbl() { return inventoryReportTbl; }
    public javax.swing.JTextField getSkuInput() { return skuInput; }
    public javax.swing.JTextField getQuantityInput() { return quantityInput; }
    public javax.swing.JTextField getGrandTotalOutput() { return grandTotalOutput; }
    public javax.swing.JComboBox getTransferFromDD() { return transferFromDD; }
    public javax.swing.JComboBox getTransferToDD() { return transferToDD; }
    public javax.swing.JComboBox getLocationDD() { return locationDD; }
    public javax.swing.JComboBox getStatusDD() { return statusDD; }
    public javax.swing.JTable getStockMovement() { return stockMovement; }
    public javax.swing.JComboBox getMovementLocationDD() { return location; }
    public javax.swing.JComboBox getMovementTypeDD() { return movement; }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField grandTotalOutput;
    private javax.swing.JTable inventoryReportTbl;
    private javax.swing.JTable inventoryTable;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JComboBox location;
    private javax.swing.JComboBox locationDD;
    private javax.swing.JComboBox movement;
    private javax.swing.JTextField quantityInput;
    private javax.swing.JTextField skuInput;
    private javax.swing.JComboBox statusDD;
    private javax.swing.JTable stockMovement;
    private javax.swing.JButton transferButton;
    private javax.swing.JComboBox transferFromDD;
    private javax.swing.JComboBox transferReasonDD;
    private javax.swing.JTable transferRecord;
    private javax.swing.JComboBox transferToDD;
    // End of variables declaration//GEN-END:variables
}
