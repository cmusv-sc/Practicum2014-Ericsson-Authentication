package edu.cmu.ini.impli_auth.auth_server.database;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.ini.impli_auth.auth_server.geofence.PassiveUser;
import edu.cmu.ini.impli_auth.auth_server.util.ConfigValue;

public class SqlConnection {

	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	private String URL;
	private String USER;
	private String PASSWORD;
	private String DRIVER_CLASS;

	public SqlConnection() {
		ConfigValue configValue = new ConfigValue();
		URL = configValue.getDBUrl();
		USER = configValue.getDBUser();
		PASSWORD = configValue.getDBPassword();
		DRIVER_CLASS = configValue.getDBDriverClass();
	}
	
	public boolean writeToAUT(int id, int auth) throws Exception {
		ResultSet result;
		int resource_id, initial_steps, fresh;
		int device_no;
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		
		System.out.println("passive_user id is " + id);
		String sql1 = String.format("select * from PASSIVE_USER where USER_ID = %d", id);
		result = statement.executeQuery(sql1);
		
		if(result.first()){
			System.out.println("the user is passive_user");
			//User exists in the Passive User table. Now we need to delete him from the PASSIVE USER and insert him into ACTIVE USER.
			resource_id = result.getInt("RESOURCE_ID");
			initial_steps = result.getInt("INITIAL_STEP");
			fresh = result.getInt("FRESH");

            String sqlCountRows = String.format("select COUNT(*) from PASSIVE_USER where USER_ID = %d", id);
			result = statement.executeQuery(sqlCountRows);
			device_no = result.getInt("COUNT");
			
			String sql2 = String.format("insert into ACTIVE_USER (USER_ID,RESOURCE_ID,INITIAL_STEPS,CURRENT_STEPS,"
													+ "MOVING,FRESH,TIMESTAMP,DEVICES_NO,AUTHENTICITY)"
													+ "values (%d,%d,%d,%d,1,%d,NOW(),%d,%d)",id,resource_id,
													initial_steps,initial_steps,fresh,1,auth);
			statement.executeUpdate(sql2);
			
			String sql3 = String.format("delete from PASSIVE_USER where USER_ID = %d", id);
			statement.executeUpdate(sql3);
			return true;
		}
		return false;		
	}
	
	/*
	 * Read Resource reads all the resources to get their location. In future the resources
	 * will be arranged based on their location. Only the resources near the query location
	 * will be considered. They will be checked for possible options for users.
	 */

	public ResultSet readResource() throws Exception {

		ResultSet returnResult;
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = "SELECT * FROM RESOURCE";
		returnResult = statement.executeQuery(sql);

		return returnResult;

	}
	
	public ResultSet readPassiveUserByUserID(int userID) throws Exception{
		ResultSet returnResult;

		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		
		String sql1 = String.format("select * from PASSIVE_USER where USER_ID = %d", userID);
		returnResult = statement.executeQuery(sql1);
		return returnResult;
	}
	
	
	/*
	 * This method is used to check if the user is already present in the PASSIVE_USER. If the 
	 * user is present in the passive user table we just update the fresh and the step counter value.
	 * This will be used in deciding the authenticity parameter.
	 */

	public ResultSet readPassiveUser(String imei) throws Exception {

		ResultSet returnResult;
		int id;
		System.out.println(imei);

		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();

		String sql = String.format("SELECT * FROM DEVICE WHERE IMEI = '%s'",
				imei);
		returnResult = statement.executeQuery(sql);
		if (returnResult.first()) {
			id = returnResult.getInt("USER_ID");
		} else
			id = -1;


		returnResult = null;
		String sql2 = String.format(
				"SELECT * FROM PASSIVE_USER WHERE USER_ID = %d", id);
		returnResult = statement.executeQuery(sql2);
		if(returnResult.next())
			return returnResult;
		else 
			return null;
		
	}
	
	/*
	 * If the above method does say that the user is present in the PASSIVE_USER
	 * table then the following method is used to update the PASSIVE_USER.
	 */

	public void updatePassiveUser(PassiveUser user, int id) throws Exception {

		ResultSet returnResult;
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();

		if (id == -1) {
			String sql = String.format(
					"SELECT USER_ID FROM DEVICE WHERE IMEI = '%s'",
					user.device_phy_id);
			System.out.println(sql);
			returnResult = statement.executeQuery(sql);
			if (returnResult.first()) {
				id = returnResult.getInt("USER_ID");
			} else
				id = -1;
		}
		System.out.println("ID in update passive user"+id);
		String sql2 = String.format(
				"UPDATE PASSIVE_USER SET INITIAL_STEP = %d, "
						+ "FRESH = FRESH + 1, " + "TIMESTAMP = NOW(), "
						+ "LATITUDE = '%s', " + "LONGITUDE = '%s', "
						+ "NSSID = '%s', " + "DEVICE_PHY_ID = '%s' "
						+ "WHERE USER_ID = %d ", user.steps, user.lat,
				user.lon, user.NSSID, user.device_phy_id, id);
		statement.executeUpdate(sql2);

	}
	
	/*
	 * If the user is not present in the PASSIVE_USER table and he triggers the geo-fence 
	 * of any resource, we will add him to the PASSIVE_USER table to track him and process
	 * the authentication request. 
	 */

	public void writePUT(PassiveUser user, int resource_id) throws Exception {

		ResultSet returnResult;
		int id;
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();

		String sql = String.format(
				"SELECT USER_ID FROM DEVICE WHERE IMEI = '%s'",
				user.device_phy_id);
		returnResult = statement.executeQuery(sql);
		if (returnResult.first()) {
			id = returnResult.getInt("USER_ID");
		} else
			id = -1;

		String sql2 = String
				.format("insert into PASSIVE_USER (user_id,resource_id,fresh,"
						+ "initial_step,latitude,longitude,nssid,timestamp,device_phy_id) "
						+ "values (%d,%d,%d,%d,'%s','%s','%s',NOW(),'%s')", id,
						resource_id, 1, user.getSteps(), user.getLat(),
						user.getLon(), user.getNSSID(), user.getDevice_Phy_ID());
		statement.executeUpdate(sql2);
		System.out.println("INSERTED TO PUT");
	}


	public void writeDataBase(int id, String picture_path) throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String
				.format("insert into USER values (%d, 'test', 'test', 'test@gmail.com', 'test', '123', '%s')",
						id, picture_path);
		statement.executeUpdate(sql);
	}

	public void updateDataBase(int id, String picture_path) throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String.format("UPDATE USER SET picture='%s' where id=%d",
				picture_path, id);
		statement.executeUpdate(sql);
	}
	
	/*
	 * The method authenticates a user by the username and password of the user. It is a 
	 * basic check on the username and the password values present in our management database
	 */

	public int authByUsernamePassword(String username, String password)
			throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String.format(
				"select ID from USER where USERNAME='%s' and PASSWORD='%s'",
				username, password);
		resultSet = statement.executeQuery(sql);
		if (resultSet.next()) {
			return resultSet.getInt("ID");
		} else
			return -1; // return -1 if auth failed.
	}
	
	public ResultSet getUserID(String userName) {
		ResultSet resultSet = null;
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName(DRIVER_CLASS);
			// setup the connection with the DB.
			connect = DriverManager.getConnection(URL, USER, PASSWORD);
			// statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// resultSet gets the result of the SQL query
			String sql = String.format("select * from USER where USERNAME = %s", userName);
			resultSet = statement.executeQuery(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultSet;
	}
	
	/*
	 * Along with authenticating the user we also add the device to our device database. This device
	 * will be the communication link between the user and the authentication server.
	 */

	public void registerUser(String username, String password, String firstName, String lastName,
			String email) throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String
				.format("insert into USER (FIRSTNAME, LASTNAME, EMAIL, USERNAME, PASSWORD) values ('%s', '%s', '%s', '%s', '%s')",
						firstName, lastName, email, username, password);
		statement.executeUpdate(sql);
	}
	
	public void registerDevice(String name, String IMEI, String credential,
			int userId) throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String
				.format("insert into DEVICE (NAME, IMEI,CREDENTIAL,USER_ID) values ('%s', '%s', '%s', %d)",
						name, IMEI, credential, userId);
		statement.executeUpdate(sql);
	}
	
	/*
	 * This function is used to authenticate the device based on its physical attributes like 
	 * its IMEI no and the Shared Key or the Credential. 
	 */

	public boolean authDevice(String IMEI, String credential) throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String.format(
				"select ID from DEVICE where IMEI='%s' and CREDENTIAL='%s'",
				IMEI, credential);
		resultSet = statement.executeQuery(sql);
		return resultSet.next();
	}
	
	/*
	 * Used to register a resource. The admin will have a screen on the resource while setting up the
	 * resource which he will use to register the resource in the start. At subsequent logins we will
	 * authenticate the resource based on its physical properties like IMEI no (equivalent) and shared 
	 * key which we sent while registering.
	 */

	public void registerResource(String name, String latitude,
			String longitude, String NSSID, String type, String SKEY, int userId)
			throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String
				.format("insert into RESOURCE (NAME,LATITUDE,LONGITUDE,CREDENTIAL,NSSID,USER_ID,TYPE) values ('%s', '%s', '%s', '%s', '%s', %d , '%s')",
						name, latitude, longitude, SKEY, NSSID, userId, type);
		statement.executeUpdate(sql);
	}

	public ResultSet getUserResources(int id) throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String
				.format("select * from RESOURCE where USER_ID=%d", id);
		resultSet = statement.executeQuery(sql);
		return resultSet;
	}
	
	/*
	 * As we mentioned we will be authenticating the resource using the physical attributes of the
	 * resource. These physical attributes are the NSSID (WiFi SSID) and the Shared Key provided during
	 * authentication.  
	 */

	public boolean authByNssidSharedKey(String NSSID, String sharedKey)
			throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String.format(
				"select ID from RESOURCE where NSSID='%s' and CREDENTIAL='%s'",
				NSSID, sharedKey);
		resultSet = statement.executeQuery(sql);
		return resultSet.next();
	}
	
	/*
	 * Management plane function to delete resources which are registered. This function can be used
	 * only by the system admin to delete a resource or even update the resource if it is being moved.
	 */

	public void deleteResourceById(int id) throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String.format("delete from resource where id=%d", id);
		statement.executeUpdate(sql);
	}

	public ResultSet getAllUser() {
		ResultSet res = null;
		try {
			res = getUser(-1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public ResultSet getUser(int id) throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName(DRIVER_CLASS);
			// setup the connection with the DB.
			connect = DriverManager.getConnection(URL, USER, PASSWORD);

			// statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// resultSet gets the result of the SQL query
			String sql = null;
			if (id < 0) {
				sql = String.format("select * from USER");
			} else {
				sql = String.format("select * from USER where ID = %d", id);
			}
			resultSet = statement.executeQuery(sql);
			// writeResultSet(resultSet);
			return resultSet;

		} catch (Exception e) {
			throw e;
		} finally {

		}
	}

	public List<Integer> getPassiveUsers(String credential) {
		
		ResultSet result;
		List<Integer> user_ids = new LinkedList<Integer>();
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName(DRIVER_CLASS);
			// setup the connection with the DB.
			connect = DriverManager.getConnection(URL, USER, PASSWORD);
			// statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// resultSet gets the result of the SQL query
			String sql1 = String.format("select ID from RESOURCE where CREDENTIAL = '%s'", credential);
			result = statement.executeQuery(sql1);
			int resource_id;
			resource_id = result.getInt("ID");

			String sql2 = String.format("select USER_ID from PASSIVE_USER where RESOURCE_ID = %d", resource_id);
			result = statement.executeQuery(sql2);
			
			while(result.next()){
				user_ids.add(result.getInt("USER_ID"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return user_ids;
	}

}
