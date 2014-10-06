package edu.cmu.ini.impli_auth.server;

import java.sql.*;
import java.util.Date;
 
public class sqlConnection {

	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
    //private static sqlConnection instance = new sqlConnection();
    public static final String URL = "jdbc:mysql://localhost:3306/AUTH?connectTimeout=3000";
    public static final String USER = "root";
    public static final String PASSWORD = "";
    public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver"; 
     
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
