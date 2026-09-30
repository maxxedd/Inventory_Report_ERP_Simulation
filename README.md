Markdown
# Inventory & Stock Transfer System

A Java Swing desktop application for managing warehouse stock, executing inter-branch stock transfers, and generating inventory status reports using a MySQL/MariaDB backend.

---

## Features

* **Real-Time Stock Management:** Displays current inventory items, stock levels, reorder thresholds, and unit costs.
* **Transactional Stock Transfers:** Transfer inventory between locations (e.g., Warehouse A to Main Branch) with automated stock level updates and transaction logging.
* **Audit Trail & Financial Reporting:** View full history of stock transfers along with total transfer valuations and aggregate totals.
* **Filtered Inventory Reports:** Filter inventory items based on specific location and stock condition (Stock On Hand vs. Low Stock Alerts).

---

## Project Structure

```text
src/
└── inventoryreport/
    ├── InventoryItem.java        # POJO representing an inventory item
    ├── StockTransfer.java        # POJO representing a stock transfer transaction
    ├── InventoryDAO.java         # Data Access Object (Database queries & transactions)
    ├── InventoryReportFRM.java   # Swing GUI Form (View & Controller logic)
    └── InventoryReport.java      # Application entry point (main method)
Prerequisites
Java Development Kit (JDK): Version 8 or higher

Database: MySQL or MariaDB

Driver: MySQL Connector/J (mysql-connector-j-x.x.x.jar)

IDE: NetBeans (recommended), Eclipse, or IntelliJ IDEA

Database Setup
Open your database management tool (e.g., phpMyAdmin, MySQL Workbench).

Create a new database:

SQL
CREATE DATABASE inventory_db;
USE inventory_db;
Run the schema script to create the required tables (inventory and stock_transfers).

Update your database connection credentials in InventoryDAO.java:

Java
private static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
private static final String USER = "root";
private static final String PASSWORD = "";
How to Run
Clone or download this repository.

Open the project in NetBeans IDE.

Add mysql-connector-j.jar to your project's Libraries folder.

Clean and Build the project (Shift + F11).

Run InventoryReport.java (Shift + F6) or click Run Project.
