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

/**
 * 
 * Contains several utility methods to deal with file store or database object
 * access, facial recognition probability transformation, and Geo-Fence distance
 * logic
 *
 */
public class Util {

	/**
	 * create directory named with user id for containing user's face image
	 * 
	 * @param userID
	 *            user id
	 */
	public static void createDir(int userID) {
		File file = new File("user-" + userID);
		if (!file.exists()) {
			if (file.mkdir()) {
				System.out.println("Directory is created!");
			}
		} else {
			System.out.println("Directory has already existed");
		}
	}

	/**
	 * save user registration face image into directory named with their id
	 * 
	 * @param userID
	 *            user id
	 * @param imageBytes
	 *            byte array of image
	 * @param fileName
	 *            image file name
	 */
	public static void saveImage(int userID, byte[] imageBytes, String fileName) {
		File outFile = new File(String.format("user-%d/%s.jpg", userID,
				fileName));
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

	/**
	 * get all user list
	 * 
	 * @return list of user id
	 */
	public static List<Integer> getAllUserList() {
		List<Integer> result = new ArrayList<Integer>();
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.getAllUser();
			if (resSet != null) {
				while (resSet.next()) {
					result.add(resSet.getInt("ID"));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * get map of training images and their corresponding labels
	 * 
	 * @param userList
	 *            list of target users will be used to train
	 * @return map of training images and their corresponding labels
	 */
	public static Map<File, Integer> getUserImageMap(List<Integer> userList) {

		Map<File, Integer> map = new HashMap<File, Integer>();
		for (int id : userList) {
			File root = new File("user-" + id);
			FilenameFilter imgFilter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					name = name.toLowerCase();
					return name.endsWith(".jpg");
				}
			};

			File[] imageFiles = root.listFiles(imgFilter);
			for (File file : imageFiles) {
				map.put(file, id);
			}
		}

		return map;
	}

	/**
	 * get user id by username
	 * 
	 * @param username
	 *            username
	 * @return user id
	 */
	public static int getUserID(String username) {
		int id = 0;
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.getAllUser();
			if (resSet != null) {
				while (resSet.next()) {
					if (username.equals(resSet.getString("USERNAME"))) {
						id = resSet.getInt("ID");
						return id;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	/**
	 * get list of ID of users who is going to use this resource
	 * 
	 * @param Credential
	 *            share resource(display) credential
	 * @return list of ID of users who is going to use this resource
	 */
	public static List<Integer> getPassiveUserList(String Credential) {

		List<Integer> result_list = new LinkedList<Integer>();

		SqlConnection dao = new SqlConnection();
		result_list = dao.getPassiveUsers(Credential);

		if (result_list != null) {
			return result_list;
		}

		else
			return null;
	}

	/**
	 * get username by user id
	 * 
	 * @param id
	 *            user id
	 * @return username
	 */
	public static String getUserName(int id) {
		String userName = "";
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.getAllUser();
			if (resSet != null) {
				while (resSet.next()) {
					if (id == resSet.getInt("ID")) {
						userName = resSet.getString("USERNAME");
						return userName;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return userName;
	}

	/**
	 * know this resource is public one or private one for user
	 * 
	 * @param userID
	 *            user id
	 * @param credential
	 *            resource's credential
	 * @return "private" or "public"
	 */
	public static String getResourceAccessStatus(int userID, String credential) {
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.readResource();
			if (resSet != null) {
				while (resSet.next()) {
					if (credential.equals(resSet.getString("CREDENTIAL"))) {
						return (userID == resSet.getInt("USER_ID")) ? "private"
								: "public";
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "public";
	}

	/**
	 * know which resource(display) user is near now
	 * 
	 * @param userID
	 *            user id
	 * @return resource id
	 */
	public static int getResourceID(int userID) {
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.readPassiveUserByUserID(userID);
			if (resSet != null) {
				while (resSet.next()) {
					return resSet.getInt("RESOURCE_ID");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * verify whether specific user is a specific resource's owner
	 * 
	 * @param userID
	 *            user id
	 * @param resourceID
	 *            share resource(display) id
	 * @return yes or no
	 */
	public static boolean isUserResource(int userID, int resourceID) {
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.readResource();
			if (resSet != null) {
				while (resSet.next()) {
					if (resourceID == resSet.getInt("ID")) {
						return (userID == resSet.getInt("USER_ID"));
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Segment distance value into different range to perform uniform
	 * distribution probability transformation
	 * 
	 * @param distance
	 *            distance from a predicted label
	 * @return probability as a predicted user
	 */
	public static double genProb(double distance) {
		if (distance <= 50) {
			return 100;
		}
		if (distance <= 80) {
			return Util.uniformDist(50, 80, 70, 100, distance);
		}
		return Util.uniformDist(80, 90, 0, 70, distance);
	}

	/**
	 * Based on probability uniform distribution assumption, transforming given
	 * range of distance value for the predicted label into probability as a
	 * predicted user
	 * 
	 * @param sourceStart
	 *            lower bound of distance value
	 * @param sourceEnd
	 *            upper bound of distance value
	 * @param targetStart
	 *            lower bound of probability
	 * @param targetEnd
	 *            upper bound of probability
	 * @param dist
	 *            given distance value
	 * @return probability as a predicted user
	 */
	public static double uniformDist(int sourceStart, int sourceEnd,
			int targetStart, int targetEnd, double dist) {
		return targetStart
				+ ((targetEnd - targetStart) / (sourceEnd - sourceStart))
				* ((sourceEnd - sourceStart) - (dist - sourceStart));
	}

	/**
	 * This method is used to obtain the physical distance between two
	 * coordinates. It is a standard math formula. For the exact formula please
	 * refer the documentation.
	 * 
	 * The distance value obtained is a scalar. What that means, is source and
	 * destination can be interchanged and we will still get the same value. It
	 * is basically the distance between two points.
	 * 
	 * @param lat1
	 *            first set of latitude
	 * @param lon1
	 *            first set of longitude
	 * @param lat2
	 *            second set of latitude
	 * @param lon2
	 *            second set of latitude
	 * @return distance
	 */
	public static double distance(double lat1, double lon1, double lat2,
			double lon2) {

		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515 * 1.609344;

		return (dist);
	}

	/**
	 * Standard functions to convert angle values from degrees to radians and
	 * vice versa. These are values needed in the above trigonometric functions.
	 * 
	 * @param deg
	 *            input in degrees
	 * @return radian output in radian
	 */
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/**
	 * Reverse function
	 * 
	 * @param rad
	 *            input in radian
	 * @return output in degrees
	 */
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

	/**
	 * since OpenCV facial recognition can't do training or testing for less
	 * than two labels, we check whether can start facial recognition from
	 * current amount of users
	 * 
	 * @return yes or no
	 */
	public static boolean canStartFaceReg() {
		SqlConnection dao = new SqlConnection();
		try {
			return dao.getUserAmount() > 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
