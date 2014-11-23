package edu.cmu.ini.impli_auth.auth_server.geofence;

public class ActiveUser {

	int id;
	String NSSID;
	String lat;
	String lon;
	int user_id;
	
	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}
	
	public String getNSSID() {
		return NSSID;
	}

	public void setNSSID(String NSSID) {
		this.NSSID = NSSID;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String name) {
		this.lat = name;
	}
	
	public String getLon() {
		return lon;
	}

	public void setLon(String name) {
		this.lon = name;
	}

	public int getUser_ID() {
		return user_id;
	}

	public void setUser_ID(int user_id) {
		this.user_id = user_id;
	}
	
	@Override
	public String toString() {
		return "Active User [nssid=" + NSSID + ", USER_ID=" + user_id + "]";
	}

}