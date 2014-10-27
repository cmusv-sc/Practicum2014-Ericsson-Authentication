package edu.cmu.ini.impli_auth.server;

import java.sql.*;
import java.util.Date;
 
public class sqlConnection {

	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
    //private static sqlConnection instance = new sqlConnection();
    public static final String URL = "jdbc:mysql://10.0.21.163:3306/auth_schema?connectionTimeout=3000";
    public static final String USER = "central";
    public static final String PASSWORD = "pass";
    public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver"; 
    
    /* 
     * ############
     * ############
     */
     
    public void writeToAUT(ActiveUser user) throws Exception {
   	 Class.forName("com.mysql.jdbc.Driver");
   	 connect = DriverManager.getConnection(URL, USER, PASSWORD);
   	 statement = connect.createStatement();
   	 String sql = String.format("insert into ACTIVE_USER (id, nssid, latitude, longitude, "
   	 					+ "user_id) values (%d,'%s','%s','%s',%d)", 
   	 					user.getID(), user.getNSSID(),user.getLat(),user.getLon(),user.getUser_ID());
   	 statement.executeUpdate(sql);
   }

    public ResultSet readResource() throws Exception {
		
    	ResultSet returnResult;
    	Class.forName("com.mysql.jdbc.Driver");
      	connect = DriverManager.getConnection(URL, USER, PASSWORD);
      	statement = connect.createStatement();
      	String sql = "SELECT * FROM RESOURCE";
      	returnResult = statement.executeQuery(sql);
    	
    	return returnResult;
    	
    }
    
    public ResultSet readPassiveUser(String imei) throws Exception {
		
    	ResultSet returnResult;
    	int id;
    	System.out.println(imei);
    	
    	Class.forName("com.mysql.jdbc.Driver");
      	connect = DriverManager.getConnection(URL, USER, PASSWORD);
      	statement = connect.createStatement();
      	
      	
      	String sql = String.format("SELECT * FROM DEVICE WHERE IMEI = '%s'", imei);
      	returnResult = statement.executeQuery(sql);
      	if(returnResult.first()){
      		id = returnResult.getInt("USER_ID");
          	//returnResult.deleteRow();
      	}
      	else
      		id = -1;
      	
      	System.out.println("ID = "+id);
      	
      	String sql2 = String.format("SELECT * FROM PASSIVE_USER WHERE USER_ID = %d", id);
      	returnResult = statement.executeQuery(sql2);
    	System.out.println("READ");
    	if(returnResult.next())
    		return returnResult;
    	else
    		return null;
    }
    
    public void updatePassiveUser(PassiveUser user, int id) throws Exception {

    	ResultSet returnResult;
    	Class.forName("com.mysql.jdbc.Driver");
      	connect = DriverManager.getConnection(URL, USER, PASSWORD);
      	statement = connect.createStatement();
      	
      	if(id == -1){
      		String sql = String.format("SELECT USER_ID FROM DEVICE WHERE IMEI = '%s'", user.device_phy_id);
      		returnResult = statement.executeQuery(sql);
      		if(returnResult.first()){
          		id = returnResult.getInt("USER_ID");
          	}
          	else
          		id = -1;
      	}
      	
      	
      	String sql2 = String.format("UPDATE PASSIVE_USER SET INITIAL_STEP = %d, "
      									+ "FRESH = FRESH + 1, "
      									+ "TIMESTAMP = NOW(), "
      									+ "LATITUDE = '%s', "
      									+ "LONGITUDE = '%s', "
      									+ "NSSID = '%s', "
      									+ "DEVICE_PHY_ID = '%s' "
      									+ "WHERE USER_ID = %d ", user.steps, user.lat, 
      															user.lon , user.NSSID,
      															user.device_phy_id, id);
      	statement.executeUpdate(sql2);
    	
    }
    
    public void writePUT(PassiveUser user, int resource_id) throws Exception {
      	
    	ResultSet returnResult;
    	int id;
    	Class.forName("com.mysql.jdbc.Driver");
      	connect = DriverManager.getConnection(URL, USER, PASSWORD);
      	statement = connect.createStatement();
      	 
      	String sql = String.format("SELECT USER_ID FROM DEVICE WHERE IMEI = '%s'", user.device_phy_id);
      	returnResult = statement.executeQuery(sql);
      	if(returnResult.first()){
      		id = returnResult.getInt("USER_ID");
      	}
      	else id = -1;
      	      	
      	
      	 String sql2 = String.format("insert into PASSIVE_USER (user_id,resource_id,fresh,"
      	 		+ "initial_step,latitude,longitude,nssid,timestamp,device_phy_id) "
      	 		+ "values (%d,%d,%d,%d,'%s','%s','%s',NOW(),'%s')", 
      	 					id, resource_id, 1, user.getSteps(), user.getLat(), user.getLon(), user.getNSSID(), user.getDevice_Phy_ID());
      	 statement.executeUpdate(sql2);
      	 System.out.println("INSERTED TO PUT");
      }
    
    public void writeTEST(String string) throws Exception {
     	 Class.forName("com.mysql.jdbc.Driver");
     	 connect = DriverManager.getConnection(URL, USER, PASSWORD);
     	 statement = connect.createStatement();
     	 String sql = String.format("insert into test (string) "
     	 		+ "values ('%s')", 
     	 					string);
     	 statement.executeUpdate(sql);
     	 System.out.println("INSERTED");
     }
    
    public String readTEST()throws Exception {
    	
    	ResultSet returnResult;
    	Class.forName("com.mysql.jdbc.Driver");
      	connect = DriverManager.getConnection(URL, USER, PASSWORD);
      	statement = connect.createStatement();
      	String sql = String.format("select * from PASSIVE_USER");
      	returnResult = statement.executeQuery(sql);
      	
      	String result="";
      	if(returnResult.next())
      		result = String.valueOf(returnResult.getInt(2));
      	
      	return result;
    	
		
	}
    
    
    /* 
     * ############
     * ############
     */
    
    public void writeDataBase(int id, String picture_path) throws Exception {
    	 Class.forName("com.mysql.jdbc.Driver");
    	 connect = DriverManager.getConnection(URL, USER, PASSWORD);
    	 statement = connect.createStatement();
    	 String sql = String.format("insert into USER values (%d, 'test', 'test', 'test@gmail.com', 'test', '123', '%s')", id, picture_path);
    	 statement.executeUpdate(sql);
    }
    
    public void updateDataBase(int id, String picture_path) throws Exception {
    	Class.forName("com.mysql.jdbc.Driver");
   	 	connect = DriverManager.getConnection(URL, USER, PASSWORD);
   	 	statement = connect.createStatement();
   	 	String sql = String.format("UPDATE USER SET picture='%s' where id=%d", picture_path, id);
   	 	statement.executeUpdate(sql);
    }
    
    public int authByUsernamePassword(String username, String password) throws Exception{
    	Class.forName("com.mysql.jdbc.Driver");
    	connect = DriverManager.getConnection(URL, USER, PASSWORD);
    	statement = connect.createStatement();
    	String sql = String.format("select id from user where username='%s' and password='%s'", username, password);
    	resultSet = statement.executeQuery(sql);
    	if(resultSet.next()){
    		return resultSet.getInt("ID");
    	} else return -1; //return -1 if auth failed.
    }
    
    public void registerResource(String name, String latitude, String longitude, String NSSID, String type, String SKEY, int userId) throws Exception {
   	 	Class.forName("com.mysql.jdbc.Driver");
   	 	connect = DriverManager.getConnection(URL, USER, PASSWORD);
   	 	statement = connect.createStatement();
   	 	String sql = String.format("insert into resource (name,latitude,longitude,NSSID,type,SKEY,USERID) values ('%s', '%s', '%s', '%s', '%s', '%s', %d)", name,latitude,longitude,NSSID,type,SKEY,userId);
   	 	statement.executeUpdate(sql);
    }

    public ResultSet getUserResources(int id) throws Exception{
    	Class.forName("com.mysql.jdbc.Driver");
    	connect = DriverManager.getConnection(URL, USER, PASSWORD);
    	statement = connect.createStatement();
    	String sql = String.format("select * from resource where userid=%d", id);
    	resultSet = statement.executeQuery(sql);
    	return resultSet;
    }
    
    public boolean authByNssidSharedKey(String NSSID, String sharedKey) throws Exception{
    	Class.forName("com.mysql.jdbc.Driver");
    	connect = DriverManager.getConnection(URL, USER, PASSWORD);
    	statement = connect.createStatement();
    	String sql = String.format("select id from resource where nssid='%s' and skey='%s'", NSSID, sharedKey);
    	resultSet = statement.executeQuery(sql);
    	return resultSet.next();
    }
    
    public void deleteResourceById(int id) throws Exception {
    	Class.forName("com.mysql.jdbc.Driver");
   	 	connect = DriverManager.getConnection(URL, USER, PASSWORD);
   	 	statement = connect.createStatement();
   	 	String sql = String.format("delete from resource where id=%d", id);
   	 	statement.executeUpdate(sql);
    }
    
    public ResultSet readDataBase(int id) throws Exception {
        try {
        // this will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");
        // setup the connection with the DB.
        connect = DriverManager.getConnection(URL, USER, PASSWORD);
 

          // statements allow to issue SQL queries to the database
          statement = connect.createStatement();
          // resultSet gets the result of the SQL query
          String sql = null;
          if(id < 0) {
        	  sql = String.format("select * from USER");
          }
          else { 
        	  sql = String.format("select * from USER where ID = %d", id);
          }
          resultSet = statement.executeQuery(sql);
          //writeResultSet(resultSet);
          return resultSet;
          
    } catch (Exception e) {
          throw e;
        } finally {
          
        }
    }
        private void writeMetaData(ResultSet resultSet) throws SQLException {
            // now get some metadata from the database
            System.out.println("The columns in the table are: ");
            System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
            for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
              System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
            }
          }

          private void writeResultSet(ResultSet resultSet) throws SQLException {
            // resultSet is initialised before the first data set
            while (resultSet.next()) {
              // it is possible to get the columns via name
              // also possible to get the columns via the column number
              int id = resultSet.getInt("ID");
              String picture = resultSet.getString("PICTURE");
             
              //print the data
              System.out.println("User id: " + id);
              System.out.println("Picure path: " + picture);
            }
          }
    
}
