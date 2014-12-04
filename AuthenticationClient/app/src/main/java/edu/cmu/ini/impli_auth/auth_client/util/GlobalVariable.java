package edu.cmu.ini.impli_auth.auth_client.util;

public class GlobalVariable {
	private static GlobalVariable instance;

	// Global variable
	private static final String SERVER_IP = "10.0.0.4";
	private static final String PORT = "8080";
	private static final String TEST_PATH = "testImage/";


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
}
