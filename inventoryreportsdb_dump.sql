-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: inventoryreportsdb
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `inventory`
--

DROP TABLE IF EXISTS `inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inventory` (
  `inventory_id` int(11) NOT NULL AUTO_INCREMENT,
  `sku` varchar(50) NOT NULL,
  `item_name` varchar(100) NOT NULL,
  `location_name` varchar(100) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 0,
  `reorder_level` int(11) NOT NULL DEFAULT 10,
  `unit_cost` decimal(10,2) NOT NULL DEFAULT 0.00,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`inventory_id`),
  UNIQUE KEY `unique_item_location` (`sku`,`location_name`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
INSERT INTO `inventory` VALUES (1,'SKU-1001','Logitech Wireless Mouse','Warehouse A',36,10,15.00,'2026-09-29 18:26:09'),(2,'SKU-1002','Mechanical Keyboard','Warehouse A',24,10,45.00,'2026-09-29 18:26:09'),(3,'SKU-1003','27-inch IPS Monitor','Warehouse A',15,5,150.00,'2026-09-29 18:26:09'),(4,'SKU-1004','USB-C Docking Station','Warehouse A',40,10,60.00,'2026-09-29 18:26:09'),(5,'SKU-1005','HD Web Camera 1080p','Warehouse A',17,8,35.00,'2026-09-29 18:26:09'),(6,'SKU-1006','Bluetooth Headset','Warehouse A',100,15,25.00,'2026-09-29 18:26:09'),(7,'SKU-1007','Ergonomic Office Chair','Warehouse A',11,5,120.00,'2026-09-29 18:26:09'),(8,'SKU-1008','Standing Desk Frame','Warehouse A',12,4,210.00,'2026-09-29 18:26:09'),(9,'SKU-1009','Cat6 Ethernet Cable 10m','Warehouse A',80,20,8.00,'2026-09-29 18:26:09'),(10,'SKU-1010','Surge Protector Strip','Warehouse A',60,15,12.00,'2026-09-29 18:26:09'),(11,'SKU-1006','Bluetooth Headset','Main Branch',3,15,25.00,'2026-09-29 18:26:09'),(12,'SKU-1007','Ergonomic Office Chair','Main Branch',5,5,120.00,'2026-09-29 18:26:09'),(13,'SKU-1008','Standing Desk Frame','Main Branch',2,4,210.00,'2026-09-29 18:26:09'),(14,'SKU-1009','Cat6 Ethernet Cable 10m','Main Branch',45,20,8.00,'2026-09-29 18:26:09'),(15,'SKU-1010','Surge Protector Strip','Main Branch',30,15,12.00,'2026-09-29 18:26:09'),(16,'SKU-1001','Logitech Wireless Mouse','Main Branch',14,10,15.00,'2026-09-29 20:13:58'),(22,'SKU-1005','HD Web Camera 1080p','Main Branch',8,8,35.00,'2026-10-19 15:53:58'),(23,'SKU-1002','Mechanical Keyboard','Main Branch',6,10,45.00,'2026-10-19 16:19:14'),(24,'SKU-1003','27-inch IPS Monitor','Main Branch',5,5,150.00,'2026-10-19 16:34:41');
/*!40000 ALTER TABLE `inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_movements`
--

DROP TABLE IF EXISTS `stock_movements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stock_movements` (
  `movement_id` int(11) NOT NULL AUTO_INCREMENT,
  `inventory_id` int(11) NOT NULL,
  `movement_type` enum('Stock In','Stock Out') NOT NULL,
  `beginning_quantity` int(11) NOT NULL,
  `stock_in` int(11) NOT NULL DEFAULT 0,
  `stock_out` int(11) NOT NULL DEFAULT 0,
  `resulting_quantity` int(11) NOT NULL,
  `transfer_id` int(11) DEFAULT NULL,
  `movement_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`movement_id`),
  KEY `fk_movement_inventory` (`inventory_id`),
  KEY `fk_movement_transfer` (`transfer_id`),
  KEY `idx_movement_date` (`movement_date`),
  KEY `idx_movement_type` (`movement_type`),
  CONSTRAINT `fk_movement_inventory` FOREIGN KEY (`inventory_id`) REFERENCES `inventory` (`inventory_id`),
  CONSTRAINT `fk_movement_transfer` FOREIGN KEY (`transfer_id`) REFERENCES `stock_transfers` (`transfer_id`),
  CONSTRAINT `chk_movement_quantities` CHECK (`beginning_quantity` >= 0 and `stock_in` >= 0 and `stock_out` >= 0 and `resulting_quantity` >= 0 and (`movement_type` = 'Stock In' and `stock_in` > 0 and `stock_out` = 0 and `resulting_quantity` = `beginning_quantity` + `stock_in` or `movement_type` = 'Stock Out' and `stock_out` > 0 and `stock_in` = 0 and `resulting_quantity` = `beginning_quantity` - `stock_out`))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_movements`
--

LOCK TABLES `stock_movements` WRITE;
/*!40000 ALTER TABLE `stock_movements` DISABLE KEYS */;
INSERT INTO `stock_movements` VALUES (1,3,'Stock Out',20,0,5,15,7,'2026-10-19 16:34:41','Scheduled Stock Balancing'),(2,24,'Stock In',0,5,0,5,7,'2026-10-19 16:34:41','Scheduled Stock Balancing');
/*!40000 ALTER TABLE `stock_movements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_transfers`
--

DROP TABLE IF EXISTS `stock_transfers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stock_transfers` (
  `transfer_id` int(11) NOT NULL AUTO_INCREMENT,
  `sku` varchar(50) NOT NULL,
  `from_location` varchar(100) NOT NULL,
  `to_location` varchar(100) NOT NULL,
  `qty_transferred` int(11) NOT NULL,
  `unit_cost` decimal(10,2) NOT NULL,
  `total_transfer_cost` decimal(10,2) GENERATED ALWAYS AS (`qty_transferred` * `unit_cost`) STORED,
  `transfer_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `notes` text DEFAULT NULL,
  `transfer_reason` varchar(50) NOT NULL DEFAULT 'Branch Replenishment',
  PRIMARY KEY (`transfer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_transfers`
--

LOCK TABLES `stock_transfers` WRITE;
/*!40000 ALTER TABLE `stock_transfers` DISABLE KEYS */;
INSERT INTO `stock_transfers` VALUES (1,'sku-1007','Warehouse A','Main Branch',4,120.00,480.00,'2026-09-29 20:06:15',NULL,'Branch Replenishment'),(2,'sku-1001','Warehouse A','Main Branch',9,15.00,135.00,'2026-09-29 20:13:58',NULL,'Branch Replenishment'),(3,'sku-1001','Warehouse A','Main Branch',5,15.00,75.00,'2026-10-19 15:19:28',NULL,'Branch Replenishment'),(4,'SKU-1005','Warehouse A','Main Branch',8,35.00,280.00,'2026-10-19 15:53:58','Branch Replenishment','Branch Replenishment'),(5,'SKU-1002','Warehouse A','Main Branch',11,45.00,495.00,'2026-10-19 16:19:14','Urgent Stock Request','Branch Replenishment'),(6,'SKU-1002','Main Branch','Warehouse A',5,45.00,225.00,'2026-10-19 16:20:05','Overstock Redistribution','Branch Replenishment'),(7,'SKU-1003','Warehouse A','Main Branch',5,150.00,750.00,'2026-10-19 16:34:41','Scheduled Stock Balancing','Branch Replenishment');
/*!40000 ALTER TABLE `stock_transfers` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-20  0:48:08
