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
  `USER_ID` int(11) NOT NULL AUTO_INCREMENT,
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
-- Table structure for table `APPLICATION`
--

DROP TABLE IF EXISTS `APPLICATION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `APPLICATION` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(20) DEFAULT NULL,
  `SERVER_IP` varchar(20) DEFAULT NULL,
  `SERVER_PORT` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
-- Table structure for table `RESOURCE`
--

DROP TABLE IF EXISTS `RESOURCE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RESOURCE` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(20) DEFAULT NULL,
  `LATITUDE` varchar(20) DEFAULT NULL,
  `LONGITUDE` varchar(20) DEFAULT NULL,
  `CREDENTIAL` varchar(30) DEFAULT NULL,
  `NSSID` varchar(20) DEFAULT NULL,
  `USER_ID` int(11) DEFAULT NULL,
  `TYPE` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `USERID` (`USER_ID`),
  CONSTRAINT `RESOURCE_ibfk_1` FOREIGN KEY (`USER_ID`) REFERENCES `USER` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `USER`
--

DROP TABLE IF EXISTS `USER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `FIRSTNAME` varchar(30) DEFAULT NULL,
  `LASTNAME` varchar(30) DEFAULT NULL,
  `EMAIL` varchar(30) DEFAULT NULL,
  `USERNAME` varchar(20) DEFAULT NULL,
  `PASSWORD` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `USER_APPS`
--

DROP TABLE IF EXISTS `USER_APPS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_APPS` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `APP_ID` varchar(20) DEFAULT NULL,
  `API_KEY` varchar(30) DEFAULT NULL,
  `USER_ID` int(11) DEFAULT NULL,
  `COUNTER` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `USER_ID` (`USER_ID`),
  CONSTRAINT `USER_APPS_ibfk_1` FOREIGN KEY (`USER_ID`) REFERENCES `USER` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-11-16 17:26:53
