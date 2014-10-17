package com.impl_auth.authenticationclient;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class GPSTracker extends Service implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{

 
    // flag for GPS status
    boolean canGetLocation = false;

    
    LocationClient mLocationClient;

    // Do we want debug toast messages?
    boolean isDebugMsg;

    // Do we want dynamic location updates?
    boolean isDynamic;

    InfoJsonSend jsonOutput;
	String ipAddress, ipAddressMobile;
	IBinder mBinder = new LocalBinder();
	LocationRequest mLocationRequest;
	boolean mUpdatesRequested;


    private static final long MIN_DISTANCE = 10; // 0 meters
    
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 10;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
 
    // The minimum time between updates in milliseconds

    private boolean serviceNotRunning = true;
    private SensorServiceReceiver sensorReceiverDirection;
    private SensorServiceReceiver sensorReceiverStep;
    public int stepCounter;
    public int angle;

    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	ipAddress=intent.getExtras().getString("IP");
    	int updateInterval = intent.getExtras().getInt("updateInterval");
    	jsonOutput = new InfoJsonSend(this, ipAddress);
        mLocationClient = new LocationClient(this, this, this);	

        //jsonOutput.postToServer(location);	
       
        //mLocationClient.requestLocationUpdates()
        
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(updateInterval*1000);


        if(isDynamic){
            mLocationRequest.setSmallestDisplacement(MIN_DISTANCE);
            //mLocationRequest.setFastestInterval(updateInterval*1000/2);
        }
        else {
            mLocationRequest.setSmallestDisplacement(0);
            mLocationRequest.setFastestInterval(updateInterval*1000);
        }
        mUpdatesRequested = true;
        mLocationClient.connect();

        startService();

        return Service.START_NOT_STICKY;
    }
  
     
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
     
    
 
    @Override
    public void onLocationChanged(Location location) {
    	Log.i("cellPhoneInfo", "Location Changed");
		//jsonOutput.postToServer(mLocationClient.getLastLocation());
        if(isDebugMsg){
		    Toast.makeText(this, "New Location " + mLocationClient.getLastLocation().toString() + " posted to server", Toast.LENGTH_SHORT).show();
        }
    }
    

    @Override
    public void onDestroy(){
    	Log.i("GPSTracker", "stopped");
    	//removeLocationUpdates(this);
    	if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            mLocationClient.removeLocationUpdates((LocationListener)this);
        }
    	mLocationClient.disconnect();
    	Toast.makeText(this, "Logging stopped", Toast.LENGTH_SHORT).show();
    }
 
 
    @Override
    public IBinder onBind(Intent arg0) {

        return mBinder;
    }


    public class LocalBinder extends Binder {
    	  public GPSTracker getServerInstance() {
    	   return GPSTracker.this;
    	  }
    	 }

	@Override
	public void onConnectionFailed(ConnectionResult result) {

		Log.d("GPSTracker", "Connection Failed");
		Toast.makeText(this, "Google Play Services connection failed", Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {

		Log.d("GPSTracker", mLocationClient.getLastLocation().toString());
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		//jsonOutput.postMobileUsage(mLocationClient.getLastLocation(),ipAddress);
        new SendPost().execute();
        Log.d("GPSTracker", mLocationRequest.toString());
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Logging stopped", Toast.LENGTH_SHORT).show();
		
	}

    public Location getLocation(){
        return mLocationClient.getLastLocation();
    }



    private class SendPost extends AsyncTask<Void,Integer,Double>{
        @Override
        protected Double doInBackground(Void... voids) {
            jsonOutput.postMobileUsage(mLocationClient.getLastLocation(),ipAddress);
            System.out.println(stepCounter);
            return null;
        }
    }

    private void startService() {
        if (serviceNotRunning) {
            startService(new Intent(GPSTracker.this, SensorService.class));
            regsiterBroadCastReceivers();
            serviceNotRunning = false;
        }

    }

    private void regsiterBroadCastReceivers() {
        IntentFilter directionFilter = new IntentFilter(SensorService.DIRECTION_UPDATE);
        sensorReceiverDirection = new SensorServiceReceiver();
        registerReceiver(sensorReceiverDirection, directionFilter);
        IntentFilter stepsFilter = new IntentFilter(SensorService.STEP_UPDATE);
        sensorReceiverStep = new SensorServiceReceiver();
        registerReceiver(sensorReceiverStep, stepsFilter);
    }

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




