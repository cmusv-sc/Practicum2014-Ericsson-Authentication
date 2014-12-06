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

/**
 * Class used to send JSON messages to the server. Implement other messages here as required.
 */

public class InfoJsonSend {

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

    /**
     * Constructor for InfoJsonSend
     * @param context Application Context
     * @param ipaddressin Ipaddress to send to
     */

	public InfoJsonSend(Context context, String ipaddressin) {
		ipaddress = ipaddressin;
		cellInfo = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		IMEINumber = cellInfo.getDeviceId();

    }


    /**
     * Method used to send the messages. Constructs JSON object to send data
     *
     * @param location Location object which contains latitude and longitude
     * @param ipAddr Ipaddress to send the data
     * @param steps The step counter value obtained from the SensorService
     * @return Success or Failure
     */

    public int postMobileUsage(Location location, String ipAddr, int steps, String wifi){

        HttpResponse response = null;

        Latitude = location.getLatitude();
        Longitude = location.getLongitude();

        JSONObject userInfo = new JSONObject();

		try {
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
        if(response != null)
            System.out.println(response.getStatusLine().toString());

		return 1;
	}

}