package edu.cmu.ini.impli_auth.server;

public class PassiveUser {
	
	String NSSID;
	double lat;
	double lon;
	int steps;
	
	int user_id;
	
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

	public int getUser_ID() {
		return user_id;
	}

	public void setUser_ID(int user_id) {
		this.user_id = user_id;
	}
	
	@Override
	public String toString() {
		return "Passive User [nssid=" + NSSID + ", USER_ID=" + user_id + "LAT=" + lat + "LON=" + lon +
				"STEPS=" + steps +"]";
	}

}