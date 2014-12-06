package edu.cmu.ini.impli_auth.auth_server.database;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.ini.impli_auth.auth_server.datamodel.PassiveUser;
import edu.cmu.ini.impli_auth.auth_server.util.ConfigValue;

/**
 * This class contains methods to update database.
 *
 */
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

	/**
	 * 
	 * @param id
	 *            User_id to insert into active user
	 * @param auth
	 *            Probability output of the authentication
	 * @return Success/failure
	 * @throws Exception
	 *             throws SQL Exception
	 */

	public boolean writeToAUT(int id, int auth) throws Exception {
		ResultSet result;
		int resource_id, initial_steps, fresh;
		int device_no;
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();

		System.out.println("passive_user id is " + id);
		String sql1 = String.format(
				"select * from PASSIVE_USER where USER_ID = %d", id);
		result = statement.executeQuery(sql1);

		if (result.first()) {
			System.out.println("the user is passive_user");
			// User exists in the Passive User table. Now we need to delete him
			// from the PASSIVE USER and insert him into ACTIVE USER.
			resource_id = result.getInt("RESOURCE_ID");
			initial_steps = result.getInt("INITIAL_STEP");
			fresh = result.getInt("FRESH");

			String sqlCountRows = String
					.format("select COUNT(*) as DEVICE_NO from PASSIVE_USER where USER_ID = %d",
							id);
			result = statement.executeQuery(sqlCountRows);
			result.next();
			device_no = result.getInt("DEVICE_NO");

			String sql2 = String.format(
					"insert into ACTIVE_USER (USER_ID,RESOURCE_ID,INITIAL_STEPS,CURRENT_STEPS,"
							+ "MOVING,FRESH,TIMESTAMP,DEVICES_NO,AUTHENTICITY)"
							+ "values (%d,%d,%d,%d,1,%d,NOW(),%d,%d)", id,
					resource_id, initial_steps, initial_steps, fresh,
					device_no, auth);

			statement.executeUpdate(sql2);

			String sql3 = String.format(
					"delete from PASSIVE_USER where USER_ID = %d", id);
			statement.executeUpdate(sql3);
			return true;
		}
		return false;
	}

	/**
	 * Read Resource reads all the resources to get their location. In future
	 * the resources will be arranged based on their location. Only the
	 * resources near the query location will be considered. They will be
	 * checked for possible options for users.
	 * 
	 * @return The result of the resource list.
	 * @throws Exception
	 *             Throws SQL exception.
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

	/**
	 * 
	 * @param userID
	 *            User_ID to read the passive user table
	 * @return ResultSet of the query
	 * @throws Exception
	 *             throws SQL exception
	 */

	public ResultSet readPassiveUserByUserID(int userID) throws Exception {
		ResultSet returnResult;

		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();

		String sql1 = String.format(
				"select * from PASSIVE_USER where USER_ID = %d", userID);
		returnResult = statement.executeQuery(sql1);
		return returnResult;
	}

	/**
	 * This method is used to check if the user is already present in the
	 * PASSIVE_USER. If the user is present in the passive user table we just
	 * update the fresh and the step counter value. This will be used in
	 * deciding the authenticity parameter.
	 * 
	 * @param imei
	 *            IMEI number of user context collector.
	 * @return Result set of getting passive user.
	 * @throws Exception
	 *             Throws SQL exception.
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
		if (returnResult.next())
			return returnResult;
		else
			return null;

	}

	/**
	 * If the above method does say that the user is present in the PASSIVE_USER
	 * table then the following method is used to update the PASSIVE_USER.
	 * 
	 * @param user
	 *            Passive User object. Used to capture all fields needed for
	 *            Passive_user
	 * @param id
	 *            User_id used to update passive_user
	 * @throws Exception
	 *             SQL exception
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
		System.out.println("ID in update passive user" + id);
		String sql2 = String.format(
				"UPDATE PASSIVE_USER SET INITIAL_STEP = %d, "
						+ "FRESH = FRESH + 1, " + "TIMESTAMP = NOW(), "
						+ "LATITUDE = '%s', " + "LONGITUDE = '%s', "
						+ "NSSID = '%s', " + "DEVICE_PHY_ID = '%s' "
						+ "WHERE USER_ID = %d ", user.steps, user.lat,
				user.lon, user.NSSID, user.device_phy_id, id);
		statement.executeUpdate(sql2);

	}

	/**
	 * If the user is not present in the PASSIVE_USER table and he triggers the
	 * geo-fence of any resource, we will add him to the PASSIVE_USER table to
	 * track him and process the authentication request.
	 * 
	 * @param user
	 * @param resource_id
	 * @throws Exception
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

	/**
	 * The method authenticates a user by the username and password of the user.
	 * It is a basic check on the username and the password values present in
	 * our management database
	 * 
	 * @param username
	 *            Username.
	 * @param password
	 *            Password.
	 * @return The user id if authenticated. -1 if failed.
	 * @throws Exception
	 *             Throws SQL exception.
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

	/**
	 * Get the user entry by username.
	 * 
	 * @param userName
	 *            The username to find the user.
	 * @return The result of the search.
	 */
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
			String sql = String.format(
					"select * from USER where USERNAME = %s", userName);
			resultSet = statement.executeQuery(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultSet;
	}

	/**
	 * Along with authenticating the user we also add the device to our device
	 * database. This device will be the communication link between the user and
	 * the authentication server.
	 * 
	 * @param username
	 *            Username.
	 * @param password
	 *            Password.
	 * @param firstName
	 *            User first name.
	 * @param lastName
	 *            User last name.
	 * @param email
	 *            User email.
	 * @throws Exception
	 *             Throw SQL exception.
	 */
	public void registerUser(String username, String password,
			String firstName, String lastName, String email) throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String
				.format("insert into USER (FIRSTNAME, LASTNAME, EMAIL, USERNAME, PASSWORD) values ('%s', '%s', '%s', '%s', '%s')",
						firstName, lastName, email, username, password);
		statement.executeUpdate(sql);
	}

	/**
	 * Register a device under certain user.
	 * 
	 * @param name
	 *            Device description name.
	 * @param IMEI
	 *            IMEI number of the device. Used as a unique identifier.
	 * @param credential
	 *            The device credential to put into the db.
	 * @param userId
	 *            Device owner id in User table.
	 * @throws Exception
	 *             Throws SQL exception.
	 */
	public void registerDevice(String name, String IMEI, String credential,
			int userId) throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String.format("select ID from DEVICE where IMEI='%s'",
				IMEI);
		resultSet = statement.executeQuery(sql);
		if (resultSet.next()) {
			// Update when exist.
			sql = String.format(
					"replace into DEVICE values (%d, '%s', '%s', '%s', %d)",
					resultSet.getInt("ID"), name, IMEI, credential, userId);
			statement.executeUpdate(sql);
		} else {
			// Insert if not.
			sql = String
					.format("insert into DEVICE (NAME, IMEI,CREDENTIAL,USER_ID) values ('%s', '%s', '%s', %d)",
							name, IMEI, credential, userId);
			statement.executeUpdate(sql);
		}

	}

	/**
	 * Authenticate a device with device credential.
	 * 
	 * @param IMEI
	 *            Device IMEI number.
	 * @param credential
	 *            Device credential.
	 * @return A boolean value indicating authentication result.
	 * @throws Exception
	 *             Throws SQL exception.
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

	/**
	 * Used to register a resource. The admin will have a screen on the resource
	 * while setting up the resource which he will use to register the resource
	 * in the start. At subsequent logins we will authenticate the resource
	 * based on its physical properties like IMEI no (equivalent) and shared key
	 * which we sent while registering.
	 * 
	 * @param name
	 *            Descriptive name of resource.
	 * @param latitude
	 *            Latitude of device location.
	 * @param longitude
	 *            Longitude of device location.
	 * @param NSSID
	 *            The NSSID of the wifi module on the resource. Used a unique
	 *            identifier of the resource.
	 * @param type
	 *            Descriptive type of the resource.
	 * @param SKEY
	 *            Resource credential.
	 * @param userId
	 *            Owner id in User table.
	 * @throws Exception
	 *             Throws SQL exception.
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

	/**
	 * Get all resources belongs to a user.
	 * 
	 * @param id
	 *            User id.
	 * @return A result set of all resources under the user.
	 * @throws Exception
	 *             Throws SQL exceptions.
	 */
	public ResultSet getUserResources(int id) throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String.format("select * from RESOURCE where USER_ID=%d",
				id);
		resultSet = statement.executeQuery(sql);
		return resultSet;
	}

	/**
	 * As we mentioned we will be authenticating the resource using the physical
	 * attributes of the resource. These physical attributes are the NSSID (WiFi
	 * SSID) and the Shared Key provided during authentication.
	 * 
	 * @param NSSID
	 *            NSSID of resource.
	 * @param sharedKey
	 *            The shared key created on register.
	 * @return Return a boolean value indicating authentication result.
	 * @throws Exception
	 *             Throws SQL excetpion.
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

	/**
	 * Management plane function to delete resources which are registered. This
	 * function can be used only by the system admin to delete a resource or
	 * even update the resource if it is being moved.
	 * 
	 * @param id
	 *            Resource id.
	 * @throws Exception
	 *             Throws SQL exception.
	 */
	public void deleteResourceById(int id) throws Exception {
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();
		String sql = String.format("delete from resource where id=%d", id);
		statement.executeUpdate(sql);
	}

	/**
	 * Get all user from user table.
	 * 
	 * @return Result set of all users.
	 */
	public ResultSet getAllUser() {
		ResultSet res = null;
		try {
			res = getUser(-1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Get the user by id. Get all user if id < 0.
	 * 
	 * @param id
	 *            User id.
	 * @return Result set of user.
	 * @throws Exception
	 *             Throws SQL exception.
	 */
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

	/**
	 * get list of ID of users who is going to use this resource
	 * 
	 * @param credential
	 *            share resource(display) credential
	 * @return list of ID of users who is going to use this resource
	 */
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
			String sql1 = String.format(
					"select ID from RESOURCE where CREDENTIAL = '%s'",
					credential);
			result = statement.executeQuery(sql1);
			int resource_id;
			resource_id = result.getInt("ID");

			String sql2 = String.format(
					"select USER_ID from PASSIVE_USER where RESOURCE_ID = %d",
					resource_id);
			result = statement.executeQuery(sql2);

			while (result.next()) {
				user_ids.add(result.getInt("USER_ID"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return user_ids;
	}

	/**
	 * get current amount of user in this system
	 * 
	 * @return
	 * @throws Exception
	 *             Throws SQL exception or ClassNotFound exception
	 */
	public int getUserAmount() throws Exception {
		ResultSet result;
		Class.forName(DRIVER_CLASS);
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connect.createStatement();

		String sqlCountRows = "select COUNT(*) as USER_NO from USER";
		result = statement.executeQuery(sqlCountRows);
		result.next();
		return result.getInt("USER_NO");
	}

}
