package edu.cmu.ini.impli_auth.face;

/**
 * Created by CyrilLee on 11/16/14.
 */
public class GlobalVariable {
	private static GlobalVariable instance;

	// Global variable
	private static String AUTH_URL = "http://10.0.23.8:8080/CentralServer/json/";
	private static String TEST_PATH = "testImage/";


	// Restrict the constructor from being instantiated
	private GlobalVariable(){}

	public static synchronized GlobalVariable getInstance(){
		if(instance==null){
			instance=new GlobalVariable();
		}
		return instance;
	}

	public String getAuthURL() {
		return this.AUTH_URL;
	}

	public String getTestPath() {
		return this.TEST_PATH;
	}
}
