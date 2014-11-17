package edu.cmu.ini.impli_auth.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public static void saveImage(int userID, byte[] imageBytes) {
		String fileName = String.format("%d.jpg", System.currentTimeMillis()); 
		File outFile = new File(fileName);
		
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
	
	public static int getUserID() {
		int id = 0;
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.getAllUser();
			if(resSet != null) {
				while(resSet.next()) {
					id = resSet.getInt("ID");
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return id;
	}
}
