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
//import java.sql.*;
//import javax.swing.JOptionPane;
//import javax.swing.table.DefaultTableModel;

public class InventoryReport {
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            InventoryReportFRM frame = new InventoryReportFRM();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
