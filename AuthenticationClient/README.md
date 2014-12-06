Authentication Client
=====================


 This android application provides the FaceActivity which is used by applications to do the authentication.


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

The exported property enables this activity to be called from other applications.

####How to use the application

This application is not directly used. 
We need to install this on the device to make it available for other applications to access the activities.
