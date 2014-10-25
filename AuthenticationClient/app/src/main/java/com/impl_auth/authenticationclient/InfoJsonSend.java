package com.impl_auth.authenticationclient;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;


public class InfoJsonSend{

	double Latitude;
	double Longitude;
	String latitudeStr;
	String longitudeStr;
	String jsonOutput;
	TelephonyManager cellInfo;
	String IMEINumber;
	String IMSINumber;
	int signalStrengthDB;
	Long tsLong;
	Long timeStamp;
	String ts;
	String ipaddress;
	Context my_context;
	MyPhoneStateListener    MyListener;
	public InfoJsonSend(Context context, String ipaddressin){

		ipaddress = ipaddressin;
		cellInfo = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		IMEINumber = cellInfo.getDeviceId();
		
		IMSINumber = cellInfo.getSubscriberId();
		my_context = context;
		signalStrengthDB = 0;
		 
		 MyListener = new MyPhoneStateListener();
		 cellInfo.listen(MyListener , PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		 
		 
		
		}


    private class MyPhoneStateListener extends PhoneStateListener
    {
      /* Get the Signal strength from the provider, each time there is an update */
      @Override
      public void onSignalStrengthsChanged(SignalStrength signalStrength)
      {
         super.onSignalStrengthsChanged(signalStrength);
         int signalStrengthInt = signalStrength.getGsmSignalStrength();
         if(signalStrengthInt == 99){
        	 signalStrengthDB = -1;
         }
         else
        	 signalStrengthDB = -113 + signalStrengthInt*2;

      }

    }/* End of private Class */


    public int postMobileUsage(Location location, String ipAddr){

        HttpResponse response = null;
        Latitude = location.getLatitude();
        Longitude = location.getLongitude();

        latitudeStr = String.valueOf(Latitude);
        longitudeStr = String.valueOf(Longitude);

        tsLong = System.currentTimeMillis();
        timeStamp = tsLong/1000;

        ts = tsLong.toString();

        JSONObject userInfo = new JSONObject();

        try{

            userInfo.put("id", 1);
            userInfo.put("lat", latitudeStr);
            userInfo.put("lon", longitudeStr);
            userInfo.put("nssid", "CMU-SV-BLDG-19");
            userInfo.put("user_ID", 2);
            userInfo.put("imei",String.valueOf(IMEINumber));

            jsonOutput = userInfo.toString();
        }
        catch (JSONException e){
            return -1;
        }


        URI theURI;
        try{
            theURI = new URI(ipAddr);
        }
        catch (URISyntaxException e)
        {
            return -1;
        }

        HttpPost post = new HttpPost(theURI);

        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonOutput);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        post.setEntity(entity);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            response = httpClient.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(response.getStatusLine().toString());

        return 1;
    }

}
