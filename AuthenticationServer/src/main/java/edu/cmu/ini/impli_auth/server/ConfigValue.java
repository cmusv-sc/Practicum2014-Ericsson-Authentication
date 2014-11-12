package edu.cmu.ini.impli_auth.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

public class ConfigValue  {
	private static String propFileName = "config.properties";
	Properties prop;
		
	public ConfigValue() {
		prop = new Properties();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		try {
			prop.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getDBUrl() {
		return prop.getProperty("url");
	}
	
	public String getDBUser() {
		return prop.getProperty("user");
	}
	
	public String getDBPassword() {
		return prop.getProperty("password");
	}
	
	public String getDBDriverClass() {
		return prop.getProperty("driver_class");
	}
	
}
