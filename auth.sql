-- MySQL dump 10.13  Distrib 5.6.19, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: auth_schema
-- ------------------------------------------------------
-- Server version	5.6.19-1~dotdeb.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ACTIVE_USER`
--

DROP TABLE IF EXISTS `ACTIVE_USER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ACTIVE_USER` (
  `USER_ID` int(11) NOT NULL DEFAULT '0',
  `RESOURCE_ID` int(11) NOT NULL DEFAULT '0',
  `INITIAL_STEPS` int(11) DEFAULT NULL,
  `CURRENT_STEPS` int(11) DEFAULT NULL,
  `MOVING` tinyint(1) DEFAULT NULL,
  `FRESH` int(11) DEFAULT NULL,
  `TIMESTAMP` timestamp NULL DEFAULT NULL,
  `DEVICES_NO` int(11) DEFAULT NULL,
  `AUTHENTICITY` int(11) DEFAULT NULL,
  PRIMARY KEY (`USER_ID`,`RESOURCE_ID`),
  UNIQUE KEY `uid_rid` (`USER_ID`,`RESOURCE_ID`),
  CONSTRAINT `ACTIVE_USER_ibfk_1` FOREIGN KEY (`USER_ID`) REFERENCES `USER` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ACTIVE_USER`
--

LOCK TABLES `ACTIVE_USER` WRITE;
/*!40000 ALTER TABLE `ACTIVE_USER` DISABLE KEYS */;
/*!40000 ALTER TABLE `ACTIVE_USER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `APPLICATION`
--

DROP TABLE IF EXISTS `APPLICATION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `APPLICATION` (
  `ID` int(11) NOT NULL DEFAULT '0',
  `NAME` varchar(20) DEFAULT NULL,
  `SERVER_IP` varchar(20) DEFAULT NULL,
  `SERVER_PORT` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `APPLICATION`
--

LOCK TABLES `APPLICATION` WRITE;
/*!40000 ALTER TABLE `APPLICATION` DISABLE KEYS */;
/*!40000 ALTER TABLE `APPLICATION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DEVICE`
--

DROP TABLE IF EXISTS `DEVICE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DEVICE` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(20) DEFAULT NULL,
  `IMEI` varchar(20) DEFAULT NULL,
  `CREDENTIAL` varchar(30) DEFAULT NULL,
  `USER_ID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `USER_ID` (`USER_ID`),
  CONSTRAINT `DEVICE_ibfk_1` FOREIGN KEY (`USER_ID`) REFERENCES `USER` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DEVICE`
--

LOCK TABLES `DEVICE` WRITE;
/*!40000 ALTER TABLE `DEVICE` DISABLE KEYS */;
INSERT INTO `DEVICE` VALUES (1,'Nexus 4','356489058312336','2qtcjo7e1dprc7ou',2),(14,'Nexus 5','358239054019882','1rvltvjur4g0oh7u',2);
/*!40000 ALTER TABLE `DEVICE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PASSIVE_USER`
--

DROP TABLE IF EXISTS `PASSIVE_USER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PASSIVE_USER` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `USER_ID` int(11) DEFAULT NULL,
  `RESOURCE_ID` int(11) DEFAULT NULL,
  `FRESH` int(11) DEFAULT NULL,
  `INITIAL_STEP` int(11) DEFAULT NULL,
  `LATITUDE` varchar(20) DEFAULT NULL,
  `LONGITUDE` varchar(20) DEFAULT NULL,
  `NSSID` varchar(20) DEFAULT NULL,
  `TIMESTAMP` timestamp NULL DEFAULT NULL,
  `DEVICE_PHY_ID` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uid_rid` (`USER_ID`,`RESOURCE_ID`),
  KEY `RESOURCE_ID` (`RESOURCE_ID`),
  CONSTRAINT `PASSIVE_USER_ibfk_1` FOREIGN KEY (`USER_ID`) REFERENCES `USER` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `PASSIVE_USER_ibfk_2` FOREIGN KEY (`RESOURCE_ID`) REFERENCES `RESOURCE` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PASSIVE_USER`
--

LOCK TABLES `PASSIVE_USER` WRITE;
/*!40000 ALTER TABLE `PASSIVE_USER` DISABLE KEYS */;
INSERT INTO `PASSIVE_USER` VALUES (6,2,1,1,0,'37.4125056','-122.0583326','CMU-SV-BLDG-19','2014-11-10 21:49:25','358239054019882');
/*!40000 ALTER TABLE `PASSIVE_USER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RESOURCE`
--

DROP TABLE IF EXISTS `RESOURCE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RESOURCE` (
  `ID` int(11) NOT NULL,
  `NAME` varchar(20) DEFAULT NULL,
  `IMEI` varchar(20) DEFAULT NULL,
  `LATITUDE` varchar(20) DEFAULT NULL,
  `LONGITUDE` varchar(20) DEFAULT NULL,
  `CREDENTIAL` varchar(30) DEFAULT NULL,
  `NSSID` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RESOURCE`
--

LOCK TABLES `RESOURCE` WRITE;
/*!40000 ALTER TABLE `RESOURCE` DISABLE KEYS */;
INSERT INTO `RESOURCE` VALUES (1,'Test Resource','12345','37.412591','-122.058409','CMU',NULL);
/*!40000 ALTER TABLE `RESOURCE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER`
--

DROP TABLE IF EXISTS `USER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER` (
  `ID` int(11) NOT NULL,
  `FIRSTNAME` varchar(30) DEFAULT NULL,
  `LASTNAME` varchar(30) DEFAULT NULL,
  `EMAIL` varchar(30) DEFAULT NULL,
  `USERNAME` varchar(20) DEFAULT NULL,
  `PASSWORD` varchar(20) DEFAULT NULL,
  `PICTURES` text,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER`
--

LOCK TABLES `USER` WRITE;
/*!40000 ALTER TABLE `USER` DISABLE KEYS */;
INSERT INTO `USER` VALUES (2,'Alok','Nerurkar','alok@gmail.com','alok','password','');
/*!40000 ALTER TABLE `USER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test`
--

DROP TABLE IF EXISTS `test`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `string` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test`
--

LOCK TABLES `test` WRITE;
/*!40000 ALTER TABLE `test` DISABLE KEYS */;
INSERT INTO `test` VALUES (32,'hello'),(33,'hello'),(34,'hello'),(35,'hello'),(36,'hello'),(37,'hello'),(38,'hello'),(39,'hello'),(40,'hello'),(41,'hello'),(42,'hello'),(43,'hello'),(44,'hello'),(45,'hello'),(46,'hello'),(47,'hello'),(48,'hello'),(49,'hello'),(50,'hello'),(51,'hello'),(52,'hello'),(53,'hello'),(54,'hello'),(55,'hello');
/*!40000 ALTER TABLE `test` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-11-10 13:49:30
