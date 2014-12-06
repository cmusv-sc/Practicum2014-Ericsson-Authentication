package edu.cmu.ini.impli_auth.context_collector.geofence;

import android.app.*;
import android.content.*;
import android.location.*;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.util.*;
import android.widget.*;

import com.google.android.gms.common.*;
import com.google.android.gms.location.*;
import com.google.android.gms.location.LocationListener;

import edu.cmu.ini.impli_auth.context_collector.util.*;

public class GPSTracker extends Service implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

    // Object for locationClient
    LocationClient mLocationClient;
    // Do we want debug toast messages?
    boolean isDebugMsg;
    // Do we want dynamic location updates?
    boolean isDynamic;
    // Object for InfoJsonSend class defined in the same package used to send JSON messages
    InfoJsonSend jsonOutput;
    // Ipaddress of Server
	String ipAddress;
	IBinder mBinder = new LocalBinder();
    // Location Request object
	LocationRequest mLocationRequest;
	boolean mUpdatesRequested;
    // Intent for StepCounter service
    Intent stepCounterService;

    WifiManager wifiManager;
    WifiInfo wifiInfo;
    String wifiSsid = "";

    private static final long MIN_DISTANCE = 10;
    // Boolean to check if service is running
    private boolean serviceNotRunning = true;
    // Broadcast Receiver object to get direction updates
    private SensorServiceReceiver sensorReceiverDirection;
    // Broadcast Receiver object to get step updates
    private SensorServiceReceiver sensorReceiverStep;
    public int stepCounter;
    public int angle;
    private GlobalVariable gv = GlobalVariable.getInstance();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	ipAddress = gv.getAuthURL() + gv.getLocationURL();
    	int updateInterval = 10;
    	jsonOutput = new InfoJsonSend(this, ipAddress);
        mLocationClient = new LocationClient(this, this, this);
        
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(updateInterval*1000);


        if(isDynamic){
            mLocationRequest.setSmallestDisplacement(MIN_DISTANCE);
        }
        else {
            mLocationRequest.setSmallestDisplacement(0);
            mLocationRequest.setFastestInterval(updateInterval*1000);
        }
        mUpdatesRequested = true;
        mLocationClient.connect();

        startService();

        return Service.START_STICKY;
    }

    @Override
    public void onLocationChanged(Location location) {
    	Log.i("cellPhoneInfo", "Location Changed");
        if(isDebugMsg){
		    Toast.makeText(this, "New Location " + mLocationClient.getLastLocation().toString() + " posted to server", Toast.LENGTH_SHORT).show();
        }
    }
    

    @Override
    public void onDestroy(){
    	Log.i("GPSTracker", "stopped");
    	if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
        }
    	mLocationClient.disconnect();
        stopService();
    	Toast.makeText(this, "Logging stopped", Toast.LENGTH_SHORT).show();
    }
 
 
    @Override
    public IBinder onBind(Intent arg0) {

        return mBinder;
    }

    public class LocalBinder extends Binder {
//    	  public GPSTracker getServerInstance() {
//    	   return GPSTracker.this;
//    	  }
    	 }


	@Override
	public void onConnectionFailed(ConnectionResult result) {

		Log.d("GPSTracker", "Connection Failed");
		Toast.makeText(this, "Google Play Services connection failed", Toast.LENGTH_SHORT).show();

	}

    /**
     * Play services when connected can get the location updates. Hence this method is effectively
     * called first. We start the async task here. After this the task is executed onLocationChanged
     * method.
     * @param connectionHint Bundle of data provided to clients by Google Play services.
     *                       May be null if no content is provided by the service.
     */

	@Override
	public void onConnected(Bundle connectionHint) {

		Log.d("GPSTracker", mLocationClient.getLastLocation().toString());
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		new SendPost().execute();
		Log.d("GPSTracker", mLocationRequest.toString());
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Logging stopped", Toast.LENGTH_SHORT).show();

	}

    /**
     * Async task to sent post messages. Network messages cannot be sent from the UI thread.
     * So we create an async task to send the messages. Debug print from step counter.
     */

    private class SendPost extends AsyncTask<Void,Integer,Double>{
        @Override
        protected Double doInBackground(Void... voids) {
            wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
            wifiInfo = wifiManager.getConnectionInfo();

            if(wifiInfo.getSSID() != null)
                wifiSsid = wifiInfo.getSSID();

            jsonOutput.postMobileUsage(mLocationClient.getLastLocation(),ipAddress,stepCounter,
                                                                                    wifiSsid);
            return null;
        }
    }
            
    /**
     * Have to unregister receivers everytime the service stops. Hence done separately.
     */

	private void stopService() {
		if (!serviceNotRunning) {
			unregisterReceiver(sensorReceiverDirection);
			unregisterReceiver(sensorReceiverStep);
			stopService(stepCounterService);
			serviceNotRunning = true;


    /*
    Starting the SensorService. We have to register receivers and hence starting the service is done
    separately.
     */

	private void startService() {
		if (serviceNotRunning) {
			stepCounterService = new Intent(GPSTracker.this, SensorService.class);
			startService(stepCounterService);
			registerBroadCastReceivers();
			serviceNotRunning = false;
		}
    }

    /**
     * Registering the broadcast receiver for SensorService. This receiver is supposed to listen on
     * updates from the SensorService service. The intent filters are defined in the Manifest file.
     * One if for step counter and one for orientation.
     */
    
	private void registerBroadCastReceivers() {
		IntentFilter directionFilter = new IntentFilter(SensorService.DIRECTION_UPDATE);
		sensorReceiverDirection = new SensorServiceReceiver();
		registerReceiver(sensorReceiverDirection, directionFilter);
		IntentFilter stepsFilter = new IntentFilter(SensorService.STEP_UPDATE);
		sensorReceiverStep = new SensorServiceReceiver();
		registerReceiver(sensorReceiverStep, stepsFilter);
	}

    /**
     * Broadcast Receiver for Sensor Service to get the value of step counter and orientation.
     * They are explicitly sent from the SensorService as intents.
     */


	public class SensorServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(SensorService.STEP_UPDATE))
				stepCounter = intent.getIntExtra(SensorService.STEPS, 0);
			else if (intent.getAction().equals(SensorService.DIRECTION_UPDATE))
				angle = intent.getIntExtra(SensorService.ANGLE, 0);
			System.out.println(stepCounter + " " + angle);
		}
	}

}