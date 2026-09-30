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


public class InventoryReportFRM extends javax.swing.JFrame {

    /**
     * Creates new form InventoryReportFRM
     */
private final InventoryDAO inventoryDAO = new InventoryDAO();

    public InventoryReportFRM() {
        initComponents();
        refreshAllViews();
    }

    private void refreshAllViews() {
        loadInventoryTable();
        loadTransferTable();
        generateReport();
    }


    private void performStockTransfer() {
        String sku = skuInput.getText().trim();
        String qtyStr = quantityInput.getText().trim();
        String fromLoc = transferFromDD.getSelectedItem().toString();
        String toLoc = transferToDD.getSelectedItem().toString();

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

            inventoryDAO.executeStockTransfer(sku, fromLoc, toLoc, qty);

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
           DefaultTableModel model = (DefaultTableModel) inventoryTable.getModel();
           model.setRowCount(0);
           try {
               List<InventoryItem> list = inventoryDAO.getAllInventory();
               for (InventoryItem item : list) {
                   model.addRow(new Object[]{
                       item.getInventoryId(), item.getSku(), item.getItemName(),
                       item.getLocationName(), item.getQuantity(), item.getReorderLevel(),
                       String.format("%.2f", item.getUnitCost())
                   });
               }
           } catch (SQLException e) {
               JOptionPane.showMessageDialog(this, "DAO Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
           }
       }

    private void loadTransferTable() {
        DefaultTableModel model = (DefaultTableModel) transferRecord.getModel();
        model.setRowCount(0);
        double grandTotal = 0.0;
        try {
            List<StockTransfer> list = inventoryDAO.getAllTransfers();
            for (StockTransfer st : list) {
                grandTotal += st.getTotalTransferCost();
                model.addRow(new Object[]{
                    st.getTransferId(), st.getSku(), st.getFromLocation(),
                    st.getToLocation(), st.getQtyTransferred(),
                    String.format("%.2f", st.getUnitCost()),
                    String.format("%.2f", st.getTotalTransferCost()),
                    st.getTransferDate()
                });
            }
            grandTotalOutput.setText(String.format("%.2f", grandTotal));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DAO Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateReport() {
        String loc = locationDD.getSelectedItem().toString();
        String status = statusDD.getSelectedItem().toString();
        DefaultTableModel model = (DefaultTableModel) inventoryReportTbl.getModel();
        model.setRowCount(0);

        try {
            List<InventoryItem> list = inventoryDAO.getFilteredReport(loc, status);
            for (InventoryItem item : list) {
                model.addRow(new Object[]{
                    item.getInventoryId(), item.getSku(), item.getItemName(),
                    item.getQuantity(), item.getReorderLevel(), item.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DAO Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        transferRecord = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        grandTotalOutput = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        inventoryReportTbl = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        locationDD = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        statusDD = new javax.swing.JComboBox();
        generateReportButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
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
                        .addComponent(skuInput)))
                .addGap(32, 32, 32)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(quantityInput, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(transferButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(189, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(skuInput, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(quantityInput, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(transferFromDD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(transferToDD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(transferButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 63, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Stock Management & Transfer Forms", jPanel5);

        transferRecord.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Trasfer ID", "SKU", "From", "To", "Quantity", "Unit Cost", "Total Cost", "Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
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
        }

        jLabel7.setText("Grand Total: ");

        grandTotalOutput.setEditable(false);
        grandTotalOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grandTotalOutputActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 846, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(grandTotalOutput, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(grandTotalOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(176, 176, 176))
        );

        jTabbedPane1.addTab("Stock Transfer Record", jPanel2);

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

        jLabel9.setText("Generate Report ");

        jLabel10.setText("Location:");

        locationDD.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Warehouse A", "Main Branch" }));
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

        generateReportButton.setText("Generate Report");
        generateReportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateReportButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(locationDD, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(68, 68, 68)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(statusDD, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 289, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 635, Short.MAX_VALUE)
                        .addComponent(generateReportButton, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(locationDD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(statusDD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(generateReportButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(53, 53, 53)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(87, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Inventory Reports", jPanel4);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 851, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(360, 360, 360)
                        .addComponent(jLabel1)))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(37, 37, 37)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
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
        // TODO add your handling code here:
    }//GEN-LAST:event_locationDDActionPerformed

    private void statusDDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusDDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_statusDDActionPerformed

    private void generateReportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateReportButtonActionPerformed
        // TODO add your handling code here:
        generateReport();
    }//GEN-LAST:event_generateReportButtonActionPerformed

    private void transferButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transferButtonActionPerformed
         // TODO add your handling code here:
         performStockTransfer();
    }//GEN-LAST:event_transferButtonActionPerformed

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton generateReportButton;
    private javax.swing.JTextField grandTotalOutput;
    private javax.swing.JTable inventoryReportTbl;
    private javax.swing.JTable inventoryTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox locationDD;
    private javax.swing.JTextField quantityInput;
    private javax.swing.JTextField skuInput;
    private javax.swing.JComboBox statusDD;
    private javax.swing.JButton transferButton;
    private javax.swing.JComboBox transferFromDD;
    private javax.swing.JTable transferRecord;
    private javax.swing.JComboBox transferToDD;
    // End of variables declaration//GEN-END:variables
}
