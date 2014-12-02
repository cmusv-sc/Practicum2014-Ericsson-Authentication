package edu.cmu.ini.impli_auth.context_collector.util;

/**
 * Created by CyrilLee on 11/16/14.
 */
public class GlobalVariable {
	private static GlobalVariable instance;

	// Global variable
	// private static String SERVER_IP = "192.168.1.100";
	private static String SERVER_IP = "10.0.17.239";
	private static String PORT = "8080";
	private static String TEST_PATH = "postUser/";
	private static String LOCATION_PATH = "postLocation/";


	// Restrict the constructor from being instantiated
	private GlobalVariable(){}

	public static synchronized GlobalVariable getInstance(){
		if(instance==null){
			instance=new GlobalVariable();
		}
		return instance;
	}

	public String getSERVER_IP() {
		return SERVER_IP;
	}

	public String getPORT() {
		return PORT;
	}

	public String getAuthURL() {
		return String.format("http://%s:%s/CentralServer/json/", getSERVER_IP(), getPORT());
	}

	public String getTestPath() {
		return this.TEST_PATH;
	}

	public String getLocationURL() {
		return this.LOCATION_PATH;
	}
}
