Authentication Client
=====================

####Server Configuration
 
 We need to configure the SERVER IP before running the application. You could do that by opening:

```
 Context Collector > app > src > main > java > util > GlobalVariable.java
```

Change the IP

``` 
private final static String SERVER_IP = [SERVER_IP];
private final static String PORT = "8080";
```

#### Used to register share resoruce which has this application

when you directly open this application, you can use it to register the device which has this application as share resoruce

#### Used to do authentication

 This android application provides the FaceActivity which is used by third party applications to do the authentication.


```
 Manifest file
  <activity 
      android:name="edu.cmu.ini.impli_auth.auth_client.face.FaceActivity"
      android:label="@string/app_name"
      android:screenOrientation="landscape"
      android:configChanges="keyboardHidden|orientation"
      android:exported="true">
  </activity>
```

The exported property enables this activity to be called from other third party applications.

######How to use the application for authentication

This application is not directly used for auth. 
We need to install this on the device to make it available for other applications to access the activities.
