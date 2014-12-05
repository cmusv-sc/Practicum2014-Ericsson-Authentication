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


public class InfoJsonSend {

	double Latitude;
	double Longitude;
	String jsonOutput;
	TelephonyManager cellInfo;
	String IMEINumber;
	int signalStrengthDB;
	Long tsLong;
	Long timeStamp;
	String ts;
	String ipaddress;
	Context my_context;
	MyPhoneStateListener MyListener;

	public InfoJsonSend(Context context, String ipaddressin) {

		ipaddress = ipaddressin;
		cellInfo = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		IMEINumber = cellInfo.getDeviceId();

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


	public int postMobileUsage(Location location, String ipAddr, int steps) {

		HttpResponse response = null;
		Latitude = location.getLatitude();
		Longitude = location.getLongitude();

		tsLong = System.currentTimeMillis();
		timeStamp = tsLong / 1000;

		ts = tsLong.toString();

		JSONObject userInfo = new JSONObject();

		try {

			userInfo.put("nssid", "CMU-SV-BLDG-19");
			userInfo.put("device_Phy_ID", String.valueOf(IMEINumber));
			userInfo.put("lat", Latitude);
			userInfo.put("lon", Longitude);
			userInfo.put("steps", steps);


			jsonOutput = userInfo.toString();
		} catch (JSONException e) {
			return -1;
		}


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

		System.out.println(response.getStatusLine().toString());

		return 1;
	}

}
