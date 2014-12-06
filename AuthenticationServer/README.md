Authentication Server
=====================

Jersey Server implementation of our Central Service. The server maintains the database and services all the authentication requests from resources as well as collects context information from the devices. 

####MySQL configuration
You can find the MySQL database file in the
```
CentralServer/src/main/resources
```

You can change the config to use a particular database server

```
# MySQL database configuration

srvip=localhost
port=3306
db=auth_db
user=root
password=root
driver_class=com.mysql.jdbc.Driver
```

####How to run the application

After initial setup and installing the server adapters in Eclipse you could directly run the server
through Eclipse itself

```
Step 1: Select Run...

Step 2: In the Run As dialog select Run on Server

Step 3: Choose an existing server or manually define a new server
        This server can be running locally or by configuring a remote server.
```
