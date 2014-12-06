package edu.cmu.ini.impli_auth.context_collector.geofence;

import android.content.*;
import android.location.*;
import android.telephony.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;
import org.json.*;

import java.io.*;
import java.net.*;

<<<<<<< HEAD
/**
 * Class used to send JSON messages to the server. Implement other messages here as required.
 */
public class InfoJsonSend{
=======

public class InfoJsonSend {
>>>>>>> FETCH_HEAD

    // Latitude value
	double Latitude;
    // Longitude value
	double Longitude;
    // Storing jsonOutput to send
	String jsonOutput;
	TelephonyManager cellInfo;
    // Storing IMEI no
	String IMEINumber;
	// Storing IP address
	String ipaddress;
<<<<<<< HEAD

    /**
     * Constructor for InfoJsonSend
     * @param context Application Context
     * @param ipaddressin Ipaddress to send to
     */
	public InfoJsonSend(Context context, String ipaddressin){
=======
	Context my_context;
	MyPhoneStateListener MyListener;

	public InfoJsonSend(Context context, String ipaddressin) {
>>>>>>> FETCH_HEAD

		ipaddress = ipaddressin;
		cellInfo = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		IMEINumber = cellInfo.getDeviceId();

<<<<<<< HEAD
    }
=======
		my_context = context;
		signalStrengthDB = 0;

        /*
        This piece of code is used to get the Network signal strength. Phone state listener.
         */
		MyListener = new MyPhoneStateListener();
		cellInfo.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}


	private class MyPhoneStateListener extends PhoneStateListener {
		/* Get the Signal strength from the provider, each time there is an update */
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			int signalStrengthInt = signalStrength.getGsmSignalStrength();
			if (signalStrengthInt == 99) {
				signalStrengthDB = -1;
			} else
				signalStrengthDB = -113 + signalStrengthInt * 2;

		}

	}/* End of private Class */
>>>>>>> FETCH_HEAD

    /**
     * Method used to send the messages. Constructs JSON object to send data
     *
     * @param location Location object which contains latitude and longitude
     * @param ipAddr Ipaddress to send the data
     * @param steps The step counter value obtained from the SensorService
     * @return Success or Failure
     */

<<<<<<< HEAD
    public int postMobileUsage(Location location, String ipAddr, int steps, String wifi){

        HttpResponse response = null;

        Latitude = location.getLatitude();
        Longitude = location.getLongitude();

        JSONObject userInfo = new JSONObject();
=======
	public int postMobileUsage(Location location, String ipAddr, int steps) {

		HttpResponse response = null;
		Latitude = location.getLatitude();
		Longitude = location.getLongitude();

		tsLong = System.currentTimeMillis();
		timeStamp = tsLong / 1000;

		ts = tsLong.toString();

		JSONObject userInfo = new JSONObject();
>>>>>>> FETCH_HEAD

		try {

<<<<<<< HEAD
            userInfo.put("nssid", wifi);
            userInfo.put("device_Phy_ID",String.valueOf(IMEINumber));
            userInfo.put("lat", Latitude);
            userInfo.put("lon", Longitude);
            userInfo.put("steps",steps);

            jsonOutput = userInfo.toString();
        }
        catch (JSONException e){
            return -1;
        }
=======
			userInfo.put("nssid", "CMU-SV-BLDG-19");
			userInfo.put("device_Phy_ID", String.valueOf(IMEINumber));
			userInfo.put("lat", Latitude);
			userInfo.put("lon", Longitude);
			userInfo.put("steps", steps);


			jsonOutput = userInfo.toString();
		} catch (JSONException e) {
			return -1;
		}
>>>>>>> FETCH_HEAD


		URI theURI;
		try {
			theURI = new URI(ipAddr);
		} catch (URISyntaxException e) {
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

<<<<<<< HEAD
        if(response != null)
            System.out.println(response.getStatusLine().toString());
=======
		System.out.println(response.getStatusLine().toString());
>>>>>>> FETCH_HEAD

		return 1;
	}

}