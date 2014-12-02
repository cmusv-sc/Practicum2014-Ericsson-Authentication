package edu.cmu.ini.impli_auth.auth_server.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.cmu.ini.impli_auth.auth_server.database.SqlConnection;

public class Util {
	public static void createDir(int userID) {
		File file = new File("user-" + userID);
		if (!file.exists()) {
			if (file.mkdir()) {
				System.out.println("Directory is created!");
			}
		}
		else {
			System.out.println("Directory has already existed");
		}
	}
	
	public static void saveImage(int userID, byte[] imageBytes, String fileName) {
		File outFile = new File(String.format("user-%d/%s.jpg", userID, fileName)); 
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(outFile);
			outStream.write(imageBytes); 
			outStream.flush(); 
			outStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static List<Integer> getAllUserList() {
		List<Integer> result = new ArrayList<Integer>();
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.getAllUser();
			if(resSet != null) {
				while(resSet.next()) {
					result.add(resSet.getInt("ID"));
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public static Map<File, Integer> getUserImageMap(List<Integer> userList) {
		
		Map<File, Integer> map = new HashMap<File, Integer>();
		for(int id : userList) {
			File root = new File("user-" + id);
			FilenameFilter imgFilter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					name = name.toLowerCase();
					return name.endsWith(".jpg");
				}
			};

			File[] imageFiles = root.listFiles(imgFilter);
			for(File file : imageFiles) {
				map.put(file, id);
			}
		}
		
		return map;
	}
	
	public static int getUserID(String username) {
		int id = 0;
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.getAllUser();
			if(resSet != null) {
				while(resSet.next()) {
					if(username.equals(resSet.getString("USERNAME"))) {
						id = resSet.getInt("ID");
						return id;
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return id;
	}
	

	public static List<Integer> getPassiveUserList(String Credential){
		
		List<Integer> result_list = new LinkedList<Integer>();
		
		SqlConnection dao = new SqlConnection();
		result_list = dao.getPassiveUsers(Credential);
		
		if(result_list != null){
			return result_list;
		}
		
		else
			return null;
	}
	
	public static String getUserName(int id) {
		String userName = "";
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.getAllUser();
			if(resSet != null) {
				while(resSet.next()) {
					if(id == resSet.getInt("ID")) {
						userName = resSet.getString("USERNAME");
						return userName;
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userName;
	}
	
	public static String getResourceAccessStatus(int userID, String credential) {
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.readResource();
			if(resSet != null) {
				while(resSet.next()) {
					if(credential.equals(resSet.getString("CREDENTIAL"))) {
						return (userID == resSet.getInt("USER_ID")) ? "private" : "public";
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "public";
	}
	
	public static int getResourceID(int userID) {
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.readPassiveUserByUserID(userID);
			if(resSet != null) {
				while(resSet.next()) {
					return resSet.getInt("RESOURCE_ID");
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public static boolean isUserResource(int userID, int resourceID) {
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.readResource();
			if(resSet != null) {
				while(resSet.next()) {
					if(resourceID == resSet.getInt("ID")) {
						return (userID == resSet.getInt("USER_ID"));
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public static double genProb(double prob) {
		if(prob <= 50) {
			return 100;
		}
		if(prob <= 80) {
			return Util.normalDist(50, 80, 70, 100, prob);
		}
		return Util.normalDist(80, 90, 0, 70, prob);
	}
	
	public static double normalDist(int sourceStart, int sourceEnd, int targetStart, int targetEnd, double p) {
		return targetStart + ((targetEnd - targetStart) / (sourceEnd - sourceStart)) * 
				((sourceEnd - sourceStart) - (p - sourceStart));
	}
}
