package edu.cmu.ini.impli_auth.auth_client.util;

/**
 * This is singleton class for global variable recourse initialzed and access,
 * you can get only one instance for this class during whole application lifecycle.
 */
public class GlobalVariable {
	private static GlobalVariable instance;

	/**
	 * Authenitcation Server IP, port, and facical recognition RESTful path
	 */
	private static final String SERVER_IP = "10.0.2.2";
	private static final String PORT = "8080";
	private static final String TEST_PATH = "testImage/";


	// Restrict the constructor from being instantiated
	private GlobalVariable() {
	}

	/**
	 * The method for getting instance of this class.
	 */
	public static synchronized GlobalVariable getInstance() {
		if (instance == null) {
			instance = new GlobalVariable();
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
