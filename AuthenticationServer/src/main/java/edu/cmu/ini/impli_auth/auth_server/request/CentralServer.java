package edu.cmu.ini.impli_auth.auth_server.request;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.SecureRandom;
import java.math.BigInteger;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import edu.cmu.ini.impli_auth.auth_server.database.SqlConnection;
import edu.cmu.ini.impli_auth.auth_server.face.FaceTestResult;
import edu.cmu.ini.impli_auth.auth_server.face.LBPHFaceRecognizer;
import edu.cmu.ini.impli_auth.auth_server.geofence.PassiveUser;
import edu.cmu.ini.impli_auth.auth_server.util.Util;

@Path("/json")
public class CentralServer {
	
	//private LBPHFaceRecognizer faceRecognizer;
	private static int width = 128, height = 128;
	
	//public CentralServer() {
		//faceRecognizer = new LBPHFaceRecognizer(width, height);
		//faceRecognizer.train();
	//}
	
	/*
	 * This method is used to obtain the physical distance between two coordinates.
	 * It is a standard math formula. For the exact formula please refer the documentation.
	 * 
	 * The distance value obtained is a scalar. What that means, is source and destination 
	 * can be interchanged and we will still get the same value. It is basically the distance 
	 * between two points.
	 */
	private double distance(double lat1, double lon1, double lat2, double lon2) {
		  
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * 
								Math.sin(deg2rad(lat2)) + 
								Math.cos(deg2rad(lat1)) * 
								Math.cos(deg2rad(lat2)) * 
								Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515 * 1.609344;
		
		return (dist);
	}
	

	/*
	 * Standard functions to convert angle values from degrees to radians and vice versa. 
	 * These are values needed in the above trigonometric functions. The latitude and 
	 * longitude values are nothing but angles and we need to convert them to radians in 
	 * order to use the trigonometric functions. Then we restore the final value into degrees
	 * and calculate distance
	 */
	
	private double deg2rad(double deg) {
		  return (deg * Math.PI / 180.0);
	}
		
	private double rad2deg(double rad) {
		  return (rad * 180 / Math.PI);
	}


	@GET
	@Path("/db")
	public void getdata() {
		
		SqlConnection dao = new SqlConnection();
		try {
			ResultSet resSet = dao.getAllUser();
			if(resSet != null) {
				while(resSet.next()) {
					System.out.print(resSet.getString("email"));
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@GET
	@Path("/getUserResources/{id}")
	//@Produces("application/json")
	public Response getUserResources(@PathParam("id")int id) {

		SqlConnection dao = new SqlConnection();
		ResultSet resultSet = null;
		try {
			resultSet = dao.getUserResources(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<table><tr><td>name</td><td>delete</td></tr>");
		String result = "Failed to get result";
		if(resultSet != null) {
			try {
				while(resultSet.next()){
					sb.append("<tr><td>");
					sb.append(resultSet.getString("name"));
					sb.append("</td><td>");
					sb.append(String.format("<form action=\"/CentralServer/json/deleteResourceById\" method=\"POST\"><input type=\"hidden\" name=\"id\" value=\"%s\"><input type=\"submit\" value=\"Submit\"></form>", resultSet.getString("id")));
					sb.append("</td></tr>");
					//System.out.println("Get a resource");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb.append("</table>");
			result = sb.toString();
		}
		return Response.status(200).entity(result).build(); 
	}
	
	@POST
	@Path("/deleteResourceById")
	//@Produces("application/json")
	public void deleteResourceById(@FormParam("id") int id) {
		SqlConnection dao = new SqlConnection();
		try {
			dao.deleteResourceById(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

    @POST
	@Path("/postUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createUser(@FormParam("Username") String username, @FormParam("Password") String password,
			@FormParam("FirstName") String firstName, @FormParam("LastName") String lastName,
			@FormParam("Email") String email, @FormParam("image1") String image1, @FormParam("image2") String image2, 
			@FormParam("image3") String image3, @FormParam("image4") String image4, 
			@FormParam("image5") String image5) {
    	
		SqlConnection dao = new SqlConnection();
		int result=0;
		try {
			dao.registerUser(username, password, firstName, lastName, email);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = -1;
		}
		
		if(result != -1) {
	    	int userID = Util.getUserID(username);
	    	Util.createDir(userID);
	    	byte[] imageBytes = DatatypeConverter.parseBase64Binary(image1);
	    	Util.saveImage(userID, imageBytes, "image1");
	    	byte[] imageBytes1 = DatatypeConverter.parseBase64Binary(image2);
	    	Util.saveImage(userID, imageBytes1, "image2");
	    	byte[] imageBytes2 = DatatypeConverter.parseBase64Binary(image3);
	    	Util.saveImage(userID, imageBytes2, "image3");
	    	byte[] imageBytes3 = DatatypeConverter.parseBase64Binary(image4);
	    	Util.saveImage(userID, imageBytes3, "image4");
	    	byte[] imageBytes4 = DatatypeConverter.parseBase64Binary(image5);
	    	Util.saveImage(userID, imageBytes4, "image5");
	    	//faceRecognizer.train();
		}
		
		
		String returnMessage;
		switch(result){
		case 0:
			returnMessage = "Succeed";
			break;
		case -1:
			returnMessage = "Exception";
			break;
		default:
			returnMessage = "Error";
			break;
		}
		return Response.status(200).entity(returnMessage).build();
	}

	@POST
	@Path("/postResource")
	//@Consumes("application/json")
	//information to register a resource: name,latitude,longitude,NSSID,type, send a generated SKEY back.
	public Response createResource(@FormParam("Username") String username, @FormParam("Password") String password,
			@FormParam("name") String name, @FormParam("latitude") String latitude,
			@FormParam("longitude") String longitude, @FormParam("NSSID") String NSSID,
			@FormParam("type") String type) {
		SqlConnection dao = new SqlConnection();
		System.out.println("postResource");
		int result;
		int userid = 0;
		try {
			if((userid=dao.authByUsernamePassword(username, password))>0){
				//auth pass
				result = 1;
			} else {
				//auth failed
				result = 0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = -1;
		}
		
		String returnMessage;
		switch(result){
		case 1:
			SecureRandom random = new SecureRandom();
			String SKEY = new BigInteger(130, random).toString(32);
			returnMessage = "Success,"+SKEY;
			try {
				dao.registerResource(name, latitude, longitude, NSSID, type, SKEY,userid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				returnMessage = "Exception";
			}
			break;
		case 0:
			returnMessage = "Failed";
			break;
		case -1:
			returnMessage = "Exception";
			break;
		default:
			returnMessage = "Error";
			break;
		}
		System.out.println(returnMessage);
		return Response.status(200).entity(returnMessage).build();
	}
	
	@POST
	@Path("/authResource")
	//information to authenticate a resource: NSSID,SKEY
	public Response authResource(@FormParam("NSSID") String NSSID, @FormParam("SKEY") String sKey) {
		SqlConnection dao = new SqlConnection();
		String result;
		try {
			if(dao.authByNssidSharedKey(NSSID, sKey)){
				//auth pass
				result = "Success";
			} else {
				//auth failed
				result = "Failed";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = "Exception";
		}
		
		return Response.status(200).entity(result).build();
	}
	
	/* 
	 * GEO-FENCING
	 * 
	 * After the user is successfully signed in the app will constantly be sending us
	 * location information. Every post from the android app will be handled here. 
	 * 
	 * Basically the post has values like the device information and the location of the
	 * device. We first check if the device is registered again, then we check if the device
	 * is already in contention for resources i.e it has been previously serviced.
	 * (Programmatically: If the user is already in the PASSIVE_USER table)
	 * 
	 * If not we check from his location if he is near any Resource using the above
	 * distance calculator. If he is within the threshold we add him to PASSIVE_USER along
	 * with the resource_id of the resource he is near to.
	 * 
	 */

    @POST
	@Path("/postLocation")
	@Consumes("application/json")
	public Response createPassiveUser(PassiveUser user) {
		
		ResultSet result = null;
		double dist;
		SqlConnection dao = new SqlConnection();
		System.out.println("Reached POST LOCATION");
		
		try {
			System.out.println("ABOUT TO READ :"+user.device_phy_id);
			result = dao.readPassiveUser(user.getDevice_Phy_ID());
			
			if(result != null){
				System.out.println("SHOULD NOT REACH HERE");
				dao.updatePassiveUser(user,result.getInt("USER_ID"));
			} else {
				System.out.println("Reached ELSE");
				result = dao.readResource();
				while(result.next()){
					double lat1 = Double.parseDouble(result.getString("LATITUDE"));
					double lat2 = Double.parseDouble(result.getString("LONGITUDE"));
					dist = distance(lat1,lat2,user.getLat(),user.getLon());
					System.out.println("Reached DISTANCE"+dist);
					
					if(dist < 1){
						dao.writePUT(user,result.getInt("ID"));
					}
					
					else {
						result.next();
						System.out.println("You are too far away. No resources. DISTANCE="+dist);
					}
						
				}
			} 
		
		} catch (SQLException e) {
				e.printStackTrace();
				return Response.status(304).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(304).build();
		}
		
		String http_result = "Passive User : " + user;
		return Response.status(201).entity(http_result).build();
	}
    
	@POST
	@Path("/postDevice")
	//@Consumes("application/json")	
	//information to register a resource: name,latitude,longitude,NSSID,type, send a generated SKEY back.
	public Response createDevice(@FormParam("Username") String username, @FormParam("Password") String password,
			@FormParam("name") String name, @FormParam("IMEI") String IMEI) {
		SqlConnection dao = new SqlConnection();
		System.out.println("postResource");
		int result;
		int userid = 0;
		try {
			if((userid=dao.authByUsernamePassword(username, password))>0){
				//auth pass
				result = 1;
			} else {
				//auth failed
				result = 0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = -1;
		}
		
		String returnMessage;
		switch(result){
		case 1:
			SecureRandom random = new SecureRandom();
			String credential = new BigInteger(80, random).toString(32);
			returnMessage = "Success,"+credential;
			try {
				dao.registerDevice(name, IMEI, credential, userid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				returnMessage = "Exception";
			}
			break;
		case 0:
			returnMessage = "Failed";
			break;
		case -1:
			returnMessage = "Exception";
			break;
		default:
			returnMessage = "Error";
			break;
		}
		System.out.println(returnMessage);
		return Response.status(200).entity(returnMessage).build();
	}
	
	public boolean active_user(int id, int auth) throws Exception{
		SqlConnection dao = new SqlConnection();
		return dao.writeToAUT(id,auth);
	}
	
	@POST
	@Path("/testImage")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response processTestImage(@FormParam("credential") String credential, 
			@FormParam("image1") String image1, @FormParam("image2") String image2) throws Exception {
		
		byte[] imageData = DatatypeConverter.parseBase64Binary(image1);
		String userName = "";
		int auth = 0;
		
		LBPHFaceRecognizer faceRecognizer = new LBPHFaceRecognizer(width, height);
		faceRecognizer.train();
		FaceTestResult result = faceRecognizer.test(imageData, "test1.jpg");
		userName = Util.getUserName(result.label);
		System.out.println("first user");
		System.out.println("UserName: " + userName);
		System.out.println("Prob: " + result.p);
		System.out.println("Credential: " + credential);
		System.out.println("Access: " +  Util.getResourceAccessStatus(result.label, credential));
		
		FaceTestResult targetResult = result;
		
		if(!image2.isEmpty()) {
			imageData = DatatypeConverter.parseBase64Binary(image2);
			result = faceRecognizer.test(imageData, "test2.jpg");
			userName = Util.getUserName(result.label);
			System.out.println("second user");
			System.out.println("UserName: " + userName);
			System.out.println("Prob: " + result.p);
			System.out.println("Credential: " + credential);
			System.out.println("Access: " +  Util.getResourceAccessStatus(result.label, credential));
			
			if(result.label != -1) {
				int resourceID = Util.getResourceID(result.label);
				if(Util.isUserResource(result.label, resourceID)) {
					targetResult = result;
				}
			}
		}
		
		
		if(active_user(targetResult.label, auth)) {
			return Response.status(200).entity(Util.getUserName(targetResult.label) + ":" + targetResult.p + ":" + Util.getResourceAccessStatus(targetResult.label, credential)).build();
		}
		else {
			System.out.println("the user is not an active user!");
			userName = "";
			return Response.status(200).entity(userName + ":" + 0 + ":public").build();
		}
	}
}
