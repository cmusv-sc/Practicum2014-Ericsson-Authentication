package edu.cmu.ini.impli_auth.server;

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

import edu.cmu.ini.impli_auth.face.AuthFaceRecognizer;

@Path("/json")
public class CentralServer {
	
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
	
	private double deg2rad(double deg) {
		  return (deg * Math.PI / 180.0);
	}

	
	private double rad2deg(double rad) {
		  return (rad * 180 / Math.PI);
	}


	@GET
	@Path("/db")
	public void getdata() {
		sqlConnection dao = new sqlConnection();
		try {
			dao.readDataBase(1);
			dao.writeDataBase(2, "picture2");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@POST
	@Path("/trainImage")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response postTrainImage(@FormParam("image") String image) {
		byte[] imageData = DatatypeConverter.parseBase64Binary(image);
		

		FileOutputStream outStream = null;
		/*
		 * // Write to local (for test) try {
		 * 
		 * String fileName = String.format("%d.jpg",
		 * System.currentTimeMillis()); File outFile = new File(fileName);
		 * outStream = new FileOutputStream(outFile);
		 * outStream.write(imageData); outStream.flush(); outStream.close();
		 * 
		 * sqlConnection dao = new sqlConnection(); try { ResultSet resultSet =
		 * dao.readDataBase(id); String picture = null; if(resultSet.next()) {
		 * AuthFaceRecognizer faceRecognizer = new AuthFaceRecognizer(outFile);
		 * faceRecognizer.run(); picture = resultSet.getString("PICTURE");
		 * picture += ";" + fileName; dao.updateDataBase(id, picture); } else {
		 * picture = fileName; dao.writeDataBase(id, picture); } return
		 * Response.status(200).entity("This user is " + id).build(); } catch
		 * (Exception e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 * 
		 * } catch (FileNotFoundException e) { e.printStackTrace(); } catch
		 * (IOException e) { e.printStackTrace(); } finally { }
		 */
		return Response.status(200).entity("post image succeed").build();
	}


	@GET
	@Path("/getUserResources/{id}")
	//@Produces("application/json")
	public Response getUserResources(@PathParam("id")int id) {

		sqlConnection dao = new sqlConnection();
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
		sqlConnection dao = new sqlConnection();
		try {
			dao.deleteResourceById(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

    @POST
	@Path("/postUser")
	@Consumes("application/json")
	public Response createUser(User user) {

		String result = "User created : " + user;
		return Response.status(201).entity(result).build();

	}

	@POST
	@Path("/postResource")
	//@Consumes("application/json")
	//information to register a resource: name,latitude,longitude,NSSID,type, send a generated SKEY back.
	public Response createResource(@FormParam("Username") String username, @FormParam("Password") String password,
			@FormParam("name") String name, @FormParam("latitude") String latitude,
			@FormParam("longitude") String longitude, @FormParam("NSSID") String NSSID,
			@FormParam("type") String type) {
		sqlConnection dao = new sqlConnection();
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
		return Response.status(200).entity(returnMessage).build();
	}
	
	@POST
	@Path("/authResource")
	//information to authenticate a resource: NSSID,SKEY
	public Response authResource(@FormParam("NSSID") String NSSID, @FormParam("SKEY") String sKey) {
		sqlConnection dao = new sqlConnection();
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

    @POST
	@Path("/postLocation")
	@Consumes("application/json")
	public Response createPassiveUser(PassiveUser user) {
		
		ResultSet result = null;
		double dist;
		sqlConnection dao = new sqlConnection();
		System.out.println("Reached POST LOCATION");
		
		try {
			//dao.writeTEST("hello");
			System.out.println("ABOUT TO READ :"+user.device_phy_id);
			result = dao.readPassiveUser(user.getDevice_Phy_ID());
			
			if(result != null){
				System.out.println("SHOULD NOT REACH HERE");
				dao.updatePassiveUser(user,result.findColumn("USER_ID"));
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
		sqlConnection dao = new sqlConnection();
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
		return Response.status(200).entity(returnMessage).build();
	}



	@POST
	@Path("/testImage")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response processTestImage(@FormParam("width") int width,
			@FormParam("height") int height, @FormParam("image") String image) {
		byte[] imageData = DatatypeConverter.parseBase64Binary(image);

		AuthFaceRecognizer faceRecognizer = new AuthFaceRecognizer(width,
				height);
		faceRecognizer.train();
		faceRecognizer.test(imageData);
		return Response.status(200).entity("the request is ok").build();
	}
}
