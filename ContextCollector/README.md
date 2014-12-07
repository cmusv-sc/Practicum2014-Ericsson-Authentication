Context Collector
=================

 Android application which will be running on the user devices. This app will allow the user 
 to register with our service. After registering he can login to the app and start sending us 
 contextual information.
 
 Whenever he wants to use a resource he has to be logged in the app. He can login once in the morning 
 or he can login only when he wants to use the resource. This is completely the user choice. As we 
 mentioned about the historical location data we could potentially collect, we may ask the user to use 
 the app more so that we could learn more from him.
 
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

####How to run the Application:

```
 After initial setup you can run the application as
 
 Step 1: Connect your android phone to the pc 
        (Currently we are unable to run the app on emulator. We are hoping to solve this problem)
        
 Step 2: Select USB debugging on your phone.
        (Enable developer options. Either you will get a pop up to enable USB debugging
         or you could do it manually from Developer Options)
         
 Step 3: Select Run > Run 'app' > Select device connected
 
 Step 4: Register to the service
 
 Step 5: Login to the service
 
```
