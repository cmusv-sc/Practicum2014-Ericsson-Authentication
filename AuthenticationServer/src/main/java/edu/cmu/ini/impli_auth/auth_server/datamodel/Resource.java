package edu.cmu.ini.impli_auth.auth_server.datamodel;

public class Resource {

	String name;
	String lat;
	String lon;
	String NSSID;
	String type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getNSSID() {
		return NSSID;
	}

	public void setNSSID(String name) {
		this.NSSID = name;
	}

	public String getType() {
		return name;
	}

	public void setType(String name) {
		this.type = name;
	}

	@Override
	public String toString() {
		return "Resource [name=" + name + ", lat=" + lat + "]";
	}

}