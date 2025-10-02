-- MySQL dump 10.13  Distrib 8.0.43, for Linux (aarch64)
--
-- Host: localhost    Database: demo
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bank_accounts`
--

DROP TABLE IF EXISTS `bank_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bank_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_number` varchar(255) NOT NULL,
  `account_type` enum('CURRENT','SAVINGS') NOT NULL,
  `currency` enum('IDR','USD') NOT NULL,
  `owner_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr9gi1et82prjsig51uqxj2qm6` (`account_number`),
  KEY `FKn2kdwpw8312vj3ssowufxm8c4` (`owner_id`),
  CONSTRAINT `FKn2kdwpw8312vj3ssowufxm8c4` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bank_accounts`
--

LOCK TABLES `bank_accounts` WRITE;
/*!40000 ALTER TABLE `bank_accounts` DISABLE KEYS */;
INSERT INTO `bank_accounts` VALUES (1,'114012012212','SAVINGS','IDR',2),(2,'34555512333','SAVINGS','IDR',3);
/*!40000 ALTER TABLE `bank_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ledger`
--

DROP TABLE IF EXISTS `ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ledger` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(19,4) NOT NULL,
  `reference` varchar(255) NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `type` enum('CREDIT','DEBIT') NOT NULL,
  `account_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcbiwdr4q4ru8sssyhjmiijilo` (`account_id`),
  CONSTRAINT `FKcbiwdr4q4ru8sssyhjmiijilo` FOREIGN KEY (`account_id`) REFERENCES `bank_accounts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ledger`
--

LOCK TABLES `ledger` WRITE;
/*!40000 ALTER TABLE `ledger` DISABLE KEYS */;
INSERT INTO `ledger` VALUES (1,100.0000,'InitialValue: XXXXX','2025-10-02 17:12:32.000000','CREDIT',1),(2,1.0000,'Transfer:2','2025-10-02 17:17:03.090465','DEBIT',1),(3,1.0000,'Transfer:2','2025-10-02 17:17:03.091705','CREDIT',2),(4,23.0000,'Transfer:4','2025-10-02 18:10:38.123008','DEBIT',1),(5,23.0000,'Transfer:4','2025-10-02 18:10:38.125208','CREDIT',2),(6,1.5000,'Transfer:5','2025-10-02 18:19:36.761222','DEBIT',1),(7,1.5000,'Transfer:5','2025-10-02 18:19:36.763690','CREDIT',2);
/*!40000 ALTER TABLE `ledger` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction_setting`
--

DROP TABLE IF EXISTS `transaction_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaction_setting` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `currency` enum('IDR','USD') NOT NULL,
  `max_threshold` decimal(19,4) NOT NULL,
  `periodic_restriction` enum('PER_DAY') NOT NULL,
  `transaction_type` enum('TRANSFER') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction_setting`
--

LOCK TABLES `transaction_setting` WRITE;
/*!40000 ALTER TABLE `transaction_setting` DISABLE KEYS */;
INSERT INTO `transaction_setting` VALUES (1,'IDR',50000000.00,'PER_DAY','TRANSFER');
/*!40000 ALTER TABLE `transaction_setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transfer`
--

DROP TABLE IF EXISTS `transfer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transfer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(19,4) NOT NULL,
  `created_timestamp` bigint NOT NULL,
  `currency` enum('IDR','USD') NOT NULL,
  `from_account` varchar(255) NOT NULL,
  `status` enum('FAILED','PENDING','SUCCESS') NOT NULL,
  `to_account` varchar(255) NOT NULL,
  `updated_timestamp` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_transfer_fromaccount` (`from_account`),
  KEY `fk_transfer_toaccount` (`to_account`),
  CONSTRAINT `fk_transfer_fromaccount` FOREIGN KEY (`from_account`) REFERENCES `bank_accounts` (`account_number`),
  CONSTRAINT `fk_transfer_toaccount` FOREIGN KEY (`to_account`) REFERENCES `bank_accounts` (`account_number`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transfer`
--

LOCK TABLES `transfer` WRITE;
/*!40000 ALTER TABLE `transfer` DISABLE KEYS */;
INSERT INTO `transfer` VALUES (2,1.0000,1759425423076,'IDR','114012012212','SUCCESS','34555512333',1759425423092),(4,23.0000,1759428638073,'IDR','114012012212','SUCCESS','34555512333',1759428638126),(5,1.5000,1759429176727,'IDR','114012012212','SUCCESS','34555512333',1759429176765);
/*!40000 ALTER TABLE `transfer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transfer_log`
--

DROP TABLE IF EXISTS `transfer_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transfer_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(19,4) NOT NULL,
  `currency` enum('IDR','USD') NOT NULL,
  `error_message` varchar(255) DEFAULT NULL,
  `from_account` varchar(255) NOT NULL,
  `log_time` bigint DEFAULT NULL,
  `status` enum('FAILED','PENDING','SUCCESS') NOT NULL,
  `to_account` varchar(255) NOT NULL,
  `transfer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transfer_log`
--

LOCK TABLES `transfer_log` WRITE;
/*!40000 ALTER TABLE `transfer_log` DISABLE KEYS */;
INSERT INTO `transfer_log` VALUES (1,1.00,'IDR',NULL,'114012012212',1759425329591,'PENDING','34555512333',NULL),(2,1.00,'IDR',NULL,'114012012212',1759425329670,'PENDING','34555512333',1),(3,1.00,'IDR',NULL,'114012012212',1759425423037,'PENDING','34555512333',NULL),(4,1.00,'IDR',NULL,'114012012212',1759425423078,'PENDING','34555512333',2),(5,1.00,'IDR',NULL,'114012012212',1759425423093,'SUCCESS','34555512333',2),(6,100.00,'IDR',NULL,'114012012212',1759426302032,'PENDING','34555512333',NULL),(7,100.00,'IDR',NULL,'114012012212',1759426302137,'PENDING','34555512333',3),(8,100.00,'IDR','Source bank account insufficient balance.','114012012212',1759426302144,'FAILED','34555512333',3),(9,23.00,'IDR',NULL,'114012012212',1759428637987,'PENDING','34555512333',NULL),(10,23.00,'IDR',NULL,'114012012212',1759428638075,'PENDING','34555512333',4),(11,23.00,'IDR',NULL,'114012012212',1759428638127,'SUCCESS','34555512333',4),(12,1.50,'IDR',NULL,'114012012212',1759429176647,'PENDING','34555512333',NULL),(13,1.50,'IDR',NULL,'114012012212',1759429176729,'PENDING','34555512333',5),(14,1.50,'IDR',NULL,'114012012212',1759429176766,'SUCCESS','34555512333',5),(15,1.50,'IDR',NULL,'114012012212',1759429189097,'PENDING','34555512332',NULL),(16,1.50,'IDR','To bank account is not found.','114012012212',1759429189112,'FAILED','34555512332',NULL),(17,1.50,'IDR',NULL,'114012012211',1759429198212,'PENDING','34555512333',NULL),(18,1.50,'IDR','From bank account is not found.','114012012211',1759429198232,'FAILED','34555512333',NULL);
/*!40000 ALTER TABLE `transfer_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','CUSTOMER') DEFAULT 'CUSTOMER',
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'$2a$10$BW5uEtrVLVsKWcR2z6ZjNe.729GO8ulli3l77KjdpRjlp1H/Vk/J.','ADMIN','admin'),(2,'$2a$10$JWGQ2uBr.T4WRaUX0JE8TuF2wnSygTfSdaZdI4nsn1UwxBU6g5D8i','CUSTOMER','user1'),(3,'$2a$10$JWGQ2uBr.T4WRaUX0JE8TuF2wnSygTfSdaZdI4nsn1UwxBU6g5D8i','CUSTOMER','user3');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-02 20:35:20
