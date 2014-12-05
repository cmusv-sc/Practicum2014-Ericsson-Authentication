package edu.cmu.ini.impli_auth.auth_server.geofence;

public class PassiveUser {

	public String NSSID;
	public double lat;
	public double lon;
	public int steps;
	public String device_phy_id;

	public String getNSSID() {
		return NSSID;
	}

	public void setNSSID(String NSSID) {
		this.NSSID = NSSID;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public String getDevice_Phy_ID() {
		return device_phy_id;
	}

	public void setDevice_Phy_ID(String device_phy_id) {
		this.device_phy_id = device_phy_id;
	}

	@Override
	public String toString() {
		return "Passive User [nssid=" + NSSID + "LAT=" + lat + "LON=" + lon
				+ "STEPS=" + steps + "DEVICE_ID=" + device_phy_id + "]";
	}

}