package edu.cmu.ini.impli_auth.auth_server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * Deal with database configuration in file "config.properties"
 *
 */
public class ConfigValue {
	private static String propFileName = "config.properties";
	Properties prop;

	public ConfigValue() {
		prop = new Properties();
		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream(propFileName);
		try {
			prop.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getDBUrl() {
		return String.format("jdbc:mysql://%s:%s/%s?connectionTimeout=3000",
				prop.getProperty("srvip"), prop.getProperty("port"),
				prop.getProperty("db"));
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
