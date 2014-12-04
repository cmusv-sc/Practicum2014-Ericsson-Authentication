package edu.cmu.ini.impli_auth.context_collector.util;


public class GlobalVariable {
	private static GlobalVariable instance;

	// Global variable
	private final static String SERVER_IP = "10.0.17.239";
	private final static String PORT = "8080";
	private final static String TEST_PATH = "postUser/";
	private final static String LOCATION_PATH = "postLocation/";


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
		return TEST_PATH;
	}

	public String getLocationURL() {
		return LOCATION_PATH;
	}
}
