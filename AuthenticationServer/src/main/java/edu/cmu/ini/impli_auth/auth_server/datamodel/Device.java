package edu.cmu.ini.impli_auth.auth_server.datamodel;

public class Device {

	String name;
	int strength;
	String phy_attr;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public String getPhyAttr() {
		return phy_attr;
	}

	public void setPhyAttr(String id) {
		this.phy_attr = id;
	}

	@Override
	public String toString() {
		return "Device [name=" + name + ", strength=" + strength
				+ ", phy_attr=" + phy_attr + "]";
	}

}