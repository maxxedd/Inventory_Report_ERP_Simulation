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
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
INSERT INTO `inventory` VALUES (1,'SKU-1001','Logitech Wireless Mouse','Warehouse A',41,10,15.00,'2026-09-29 18:26:09'),(2,'SKU-1002','Mechanical Keyboard','Warehouse A',30,10,45.00,'2026-09-29 18:26:09'),(3,'SKU-1003','27-inch IPS Monitor','Warehouse A',20,5,150.00,'2026-09-29 18:26:09'),(4,'SKU-1004','USB-C Docking Station','Warehouse A',40,10,60.00,'2026-09-29 18:26:09'),(5,'SKU-1005','HD Web Camera 1080p','Warehouse A',25,8,35.00,'2026-09-29 18:26:09'),(6,'SKU-1006','Bluetooth Headset','Warehouse A',100,15,25.00,'2026-09-29 18:26:09'),(7,'SKU-1007','Ergonomic Office Chair','Warehouse A',11,5,120.00,'2026-09-29 18:26:09'),(8,'SKU-1008','Standing Desk Frame','Warehouse A',12,4,210.00,'2026-09-29 18:26:09'),(9,'SKU-1009','Cat6 Ethernet Cable 10m','Warehouse A',80,20,8.00,'2026-09-29 18:26:09'),(10,'SKU-1010','Surge Protector Strip','Warehouse A',60,15,12.00,'2026-09-29 18:26:09'),(11,'SKU-1006','Bluetooth Headset','Main Branch',3,15,25.00,'2026-09-29 18:26:09'),(12,'SKU-1007','Ergonomic Office Chair','Main Branch',5,5,120.00,'2026-09-29 18:26:09'),(13,'SKU-1008','Standing Desk Frame','Main Branch',2,4,210.00,'2026-09-29 18:26:09'),(14,'SKU-1009','Cat6 Ethernet Cable 10m','Main Branch',45,20,8.00,'2026-09-29 18:26:09'),(15,'SKU-1010','Surge Protector Strip','Main Branch',30,15,12.00,'2026-09-29 18:26:09'),(16,'SKU-1001','Logitech Wireless Mouse','Main Branch',9,10,15.00,'2026-09-29 20:13:58');
/*!40000 ALTER TABLE `inventory` ENABLE KEYS */;
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
  PRIMARY KEY (`transfer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_transfers`
--

LOCK TABLES `stock_transfers` WRITE;
/*!40000 ALTER TABLE `stock_transfers` DISABLE KEYS */;
INSERT INTO `stock_transfers` VALUES (1,'sku-1007','Warehouse A','Main Branch',4,120.00,480.00,'2026-09-29 20:06:15',NULL),(2,'sku-1001','Warehouse A','Main Branch',9,15.00,135.00,'2026-09-29 20:13:58',NULL);
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

-- Dump completed on 2026-09-30  4:20:50
