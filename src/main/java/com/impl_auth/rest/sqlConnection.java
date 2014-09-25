package com.impl_auth.rest;

import java.sql.*;
import java.util.Date;
 
public class sqlConnection {

	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
    //private static sqlConnection instance = new sqlConnection();
    public static final String URL = "jdbc:mysql://localhost:8888/Practicum?connectTimeout=3000";
    public static final String USER = "pratibha";
    public static final String PASSWORD = "pratibha";
    public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver"; 
     
    public static void main(String[] args) throws Exception {
        sqlConnection dao = new sqlConnection();
        dao.readDataBase();
      }

    public void readDataBase() throws Exception {
        try {
        // this will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");
        // setup the connection with the DB.
        connect = DriverManager.getConnection(URL, USER, PASSWORD);
 

          // statements allow to issue SQL queries to the database
          statement = connect.createStatement();
          // resultSet gets the result of the SQL query
          resultSet = statement.executeQuery("select * from Practicum.USER");
          writeResultSet(resultSet);

          // preparedStatements can use variables and are more efficient
          preparedStatement = connect
              .prepareStatement("insert into Practicum.USER values (?, ?, ?, ? , ?, ?)");
          preparedStatement.setString(1, "dummyuser");
          preparedStatement.setString(2, "dummyuser");
          preparedStatement.setInt(3, 2);
          preparedStatement.setString(4, "bygsjf");
          preparedStatement.setInt(5, 221343);
          preparedStatement.setString(6, "dummy@gmail.com");
          preparedStatement.executeUpdate();

          preparedStatement = connect
              .prepareStatement("SELECT USERNAME, PASSWORD, ID, ADRESS, PHONE, EMAIL from USER");
          resultSet = preparedStatement.executeQuery();
          writeResultSet(resultSet);

          // remove again the insert comment
          preparedStatement = connect
          .prepareStatement("delete from  Practicum.USER where username= ? ; ");
          preparedStatement.setString(1, "dummyuser");
          preparedStatement.executeUpdate();
          
          resultSet = statement
          .executeQuery("select * from Practicum.USER");
          writeMetaData(resultSet);
          
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
              // which starts at 1
              // e.g., resultSet.getSTring(2);
              String username = resultSet.getString("USERNAME");
              String password = resultSet.getString("PASSWORD");
              int ID = resultSet.getInt("ID");
              String ADDRESS = resultSet.getString("ADRESS");
              int PHONE = resultSet.getInt("PHONE");
              String EMAIL = resultSet.getString("EMAIL");
              //print the data
              System.out.println("User name: " + username);
              System.out.println("Passowrd: " + password);
              System.out.println("ID: " + ID);
              System.out.println("ADDRESS: " + ADDRESS);
              System.out.println("PHONE NUMBER: " + PHONE);
              System.out.println("EMAIL: " + EMAIL);
            }
          }
    
}
