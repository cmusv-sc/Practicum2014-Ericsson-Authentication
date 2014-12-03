Practicum2014-Ericsson-Authentication
=====================================

Introduction
------------
We have 5 Components (Authentication Server, Authentication Client, Personal Context Collector, Third Party App client, Third Party App server) in this project.

Except for Third Party App server program, we have Authentication Server, Authentication Client, Personal Context Collector program, and sample Third Party App client into this package.


Authentication Server
---------------------

#### Required Software

Authentication Server is supposed to run over TomCat servlet container and JBoss module.

#### Build Instructions

MySQL Database
--------------

#### Required Software

Above MySQL 5.5 (can use Homebrew to install)

- ensure that Homebrew is update to date and ready to brew
```
brew update
brew doctor
brew upgrade
```

- install MySQL
```
brew install mysql
```

- Start MySQL
```
mysql.server restart
```
#### Build Instructions

######  In MySQL db
1. pull db script [PROJECT_DIRECTORY]/auth_schema.sql from git
2. login MySQL with cmd line.
```
mysql -u <user name> -p <password>
```
3. create & use database:
```
create database <db name>;
use <db name>;
```
4. populate the script
```
source <script name>;
```
5. grant a user with the right of accessing the database with password
```
GRANT ALL ON <db name>.* TO 'user'@'localhost' IDENTIFIED BY 'some_password';
FLUSH PRIVILEGES;
```
###### In AuthenticationServer/src/main/resources/config.properties

Config Your own MySQL server IP, port, database name, username and password
```
ex.
srvip=localhost
port=3306
db=auth_db
user=root
password=root
```


Authentication Client
---------------------

####Required Software
IDE, Android Studio (suggested)

Android 4.0.3 API 15 (Minimum requirement)
- With Android Studio, Go to Tools > Android > SDK Manager, to install required SDK.

####Build Instructions

1. Open Android Studio
2. Go to File > Import Project, select [PROJECT_DIRECTORY]/AuthenticationClient as parent folder, and click OK.
3. Go to [PROJECT_DIRECTORY]/AuthenticationClient/app/src/main/java/edu/cmu/ini/impli_auth/auth_client/util/GlobalVariable.java, and change value of Global variable SERVER_IP as AuthenticationServer IP. 

Context Collector
-----------------

####Required Software
IDE, Android Studio (suggested)

Android 4.0.3 API 15 (Minimum requirement)
- With Android Studio, Go to Tools > Android > SDK Manager, to install required SDK.

####Build Instructions

1. Open Android Studio
2. Go to File > Import Project, select [PROJECT_DIRECTORY]/ContextCollector as parent folder, and click OK.
3. Go to 
[PROJECT_DIRECTORY]/ContextCollector/app/src/main/java/edu/cmu/ini/impli_auth/context_collector/util/GlobalVariable.java, and change value of Global variable SERVER_IP as AuthenticationServer IP. 

Third Party App Sample (WatchTV)
--------------------------------

####Required Software
IDE, Android Studio (suggested)

Android 4.0.3 API 15 (Minimum requirement)
- With Android Studio, Go to Tools > Android > SDK Manager, to install required SDK.

####Build Instructions

1. Open Android Studio
2. Go to File > Import Project, select [PROJECT_DIRECTORY]/WatchTV as parent folder, and click OK.


