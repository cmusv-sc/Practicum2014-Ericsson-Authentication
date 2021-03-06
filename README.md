Practicum2014-Ericsson-Authentication
=====================================

Introduction
------------
We have 5 Components (Authentication Server, Authentication Client, Personal Context Collector, Third Party App client, Third Party App server) in this project.

Except for Third Party App server program, we have Authentication Server, Authentication Client, Personal Context Collector program, and sample Third Party App client into this package.

Downloads
---------
```
git clone https://github.com/cmusv-sc/Practicum2014-Ericsson-Authentication.git
```


Authentication Server
---------------------

#### Required Software

###### Eclipse (Mars) 
Install Eclipse IDE for Java EE Developers (Mars)
```
https://eclipse.org/downloads/index-developer.php
```

###### OpenCV
Config most recent version of OpenCV into system path, here we use Homebrew to simplify things.

- add homebrew/science which is where OpenCV is located using
```
brew tap homebrew/science
```
- Go ahead and install OpenCV
```
brew install opencv
```

## Setup Tomcat on Eclipse

#### Install Tomcat 7.0
```
http://tomcat.apache.org/download-70.cgi
```
1. Open Eclipse
2. Go to Preferences > Server(left side bar) > Runtime Environments > Add..
3. Select Apache Tomcat 7.0
4. Select Tomcat 7.0 unpacked folder
5. Finish

This will start a Tomcat server from Eclipse. Easier for testing.


#### Build Instructions

1. Open Eclipse
2. Go to File > Import... > General > Existing projects into workspace > Select [PROJECT_DIRECTORY]/AuthenticationServer
3. Wait for build, and run it.

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

Download Android Studio from https://developer.android.com/sdk/installing/studio.html

###### Mobile Device

Android 4.0.3 API 15 (Minimum requirement)
- With Android Studio, Go to Tools > Android > SDK Manager, to install required SDK.

OpenCV Manager (OpenCV hardware optimized support for Android)
- When you first time lanuch camera view in Authentication Client, it will guide you to Google Play Store to install it.

###### Emulator

Android 4.0.3 API 15 (Minimum requirement)
- With Android Studio, Go to Tools > Android > SDK Manager, to install required SDK.
- Also, in SDK Manager, installing ARM EABI v7a System Image of same SDK you choose (don't use any INTEL image, but we use OpenCV native libraries, it can only run on ARM architecture image, or it would come out INSTALL_FAILED_NO_MATCHING_ABIS error)
- run your Emulator with ARM EABI v7a System Image, and also allocate memory for internal storage

OpenCV Manager (OpenCV hardware optimized support for Android)
- Go to 
  ```
  http://sourceforge.net/projects/opencvlibrary/files/opencv-android/
  ```
  , download OpenCV-2.4.9-android-sdk.zip.
- unzip OpenCV-2.4.9-android-sdk.zip in [DIRECTORY]/OpenCV-2.4.9-android-sdk
- manually install this package into Emulator : (the actually version mapping is here http://docs.opencv.org/trunk/android/service/doc/UseCases.html#how-to-select-the-proper-version-of-opencv-manager)
  ```
  adb install OpenCV-2.4.9-android-sdk/apk/OpenCV_2.4.9_Manager_2.18_armv7a-neon.apk
  ```
  
####Build Instructions

1. Open Android Studio
2. Select Import Project, select [PROJECT_DIRECTORY]/AuthenticationClient as parent folder, and click OK.
3. Go to [PROJECT_DIRECTORY]/AuthenticationClient/app/src/main/java/edu/cmu/ini/impli_auth/auth_client/util/GlobalVariable.java, and change value of Global variable SERVER_IP as AuthenticationServer IP. [If you are using your local machine as Authentication Server use IP of your machine]
4. Connect your Android device(Display Resource) in developer mode and Run the AuthenticationClient on the Phone.

Context Collector
-----------------

####Required Software
IDE, Android Studio (suggested)

###### Mobile Device

Android 4.0.3 API 15 (Minimum requirement)
- With Android Studio, Go to Tools > Android > SDK Manager, to install required SDK.

OpenCV Manager (OpenCV hardware optimized support for Android)
- When you first time lanuch camera view in Context Collector, it will guide you to Google Play Store to install it.

Google Play Services 6.1.71 (Google Play Services for Froyo)
- With Android Studio, Go to Tools > Android > SDK Manager, Open the Extras directory and select: Google Play Services for Froyo and Google Play services

###### Emulator

Android 4.0.3 API 15 (Minimum requirement)
- With Android Studio, Go to Tools > Android > SDK Manager, to install required SDK.
- Also, in SDK Manager, installing ARM EABI v7a System Image of same SDK you choose (don't use any INTEL image, but we use OpenCV native libraries, it can only run on ARM architecture image, or it would come out INSTALL_FAILED_NO_MATCHING_ABIS error)
- run your Emulator with ARM EABI v7a System Image, and also allocate memory for internal storage

OpenCV Manager (OpenCV hardware optimized support for Android)
- Go to 
  ```
  http://sourceforge.net/projects/opencvlibrary/files/opencv-android/
  ```
  , download OpenCV-2.4.9-android-sdk.zip.
- unzip OpenCV-2.4.9-android-sdk.zip in [DIRECTORY]/OpenCV-2.4.9-android-sdk
- manually install this package into Emulator : (the actually version mapping is here http://docs.opencv.org/trunk/android/service/doc/UseCases.html#how-to-select-the-proper-version-of-opencv-manager)
  ```
  adb install OpenCV-2.4.9-android-sdk/apk/OpenCV_2.4.9_Manager_2.18_armv7a-neon.apk
  ```

####Build Instructions

1. Open Android Studio
2. Select Import Project, select [PROJECT_DIRECTORY]/ContextCollector as parent folder, and click OK.
3. Go to 
[PROJECT_DIRECTORY]/ContextCollector/app/src/main/java/edu/cmu/ini/impli_auth/context_collector/util/GlobalVariable.java, and change value of Global variable SERVER_IP as AuthenticationServer IP. 
4. Connect your Android device (Personal mobile device) in developer mode and Run the ContextCollector on the Phone.

Third Party App Sample (WatchTV)
--------------------------------

####Required Software
IDE, Android Studio (suggested)

###### Mobile Device

Android 4.0.3 API 15 (Minimum requirement)
- With Android Studio, Go to Tools > Android > SDK Manager, to install required SDK.

###### Emulator

Android 4.0.3 API 15 (Minimum requirement)
- With Android Studio, Go to Tools > Android > SDK Manager, to install required SDK.
- Also, in SDK Manager, installing ARM EABI v7a System Image of same SDK you choose (don't use any INTEL image, but we use OpenCV native libraries in AuthenticationClient, and these two applications should be in the same device, native library can only run on ARM architecture image, or it would come out INSTALL_FAILED_NO_MATCHING_ABIS error)
- run your Emulator with ARM EABI v7a System Image, and also allocate memory for internal storage

####Build Instructions

1. Open Android Studio
2. Select Import Project, select [PROJECT_DIRECTORY]/WatchTV as parent folder, and click OK.
3. Connect your Android device (Personal mobile device) in developer mode and Run the WatchTV on the Phone.


