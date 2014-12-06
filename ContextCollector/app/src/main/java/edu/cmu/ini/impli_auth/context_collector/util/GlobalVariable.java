package edu.cmu.ini.impli_auth.context_collector.util;

/**
 * This is singleton class for global variable recourse initialzed and access,
 * you can get only one instance for this class during whole application lifecycle.
 */
public class GlobalVariable {
	private static GlobalVariable instance;

	/**
	 * Authenitcation Server IP, port, and facical recognition, geo-fence RESTful path
	 */
	private final static String SERVER_IP = "10.0.0.4";
	private final static String PORT = "8080";
	private final static String TEST_PATH = "postUser/";
	private final static String LOCATION_PATH = "postLocation/";


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

	public String getLocationURL() {
		return LOCATION_PATH;
	}
}
