package edu.cmu.ini.impli_auth.server;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
 
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
	@Path("/pic")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response postImage(@FormParam("id") String id_str, @FormParam("image") String image) {
		System.out.println(id_str);
		//System.out.println(image);
		int id = Integer.parseInt(id_str);
		byte[] imageData = DatatypeConverter.parseBase64Binary(image);

		FileOutputStream outStream = null;

		// Write to local (for test)
		try {		
			String fileName = String.format("%d.jpg", System.currentTimeMillis());
			File outFile = new File(fileName);
			outStream = new FileOutputStream(outFile);
			outStream.write(imageData);
			outStream.flush();
			outStream.close();
			
			sqlConnection dao = new sqlConnection();
			try {
				ResultSet resultSet = dao.readDataBase(id);
				String picture = null;
				if(resultSet.next()) {
					AuthFaceRecognizer faceRecognizer = new AuthFaceRecognizer(outFile);
					faceRecognizer.run();
					picture = resultSet.getString("PICTURE");
					picture += ";" + fileName;
					dao.updateDataBase(id, picture);
				}
				else {
					picture = fileName;
					dao.writeDataBase(id, picture);
				}
				return Response.status(200).entity("This user is " + id).build();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
		
		return Response.status(200).entity("the request is failed").build();
	}
	
	
	@GET
	@Path("/getUser")
	@Produces("application/json")
	public User getUser() {

		User user = new User();
		user.setFirstName("Alok");
		user.setLastName("Nerurkar");
		user.setEmail("alok@email.com");
		user.setUserName("alok");
		user.setPassword("password");
		
		return user; 

	}
	
	@GET
	@Path("/getDevice")
	@Produces("application/json")
	public Device getDevice() {

		Device device = new Device();
		device.setName("Phone");
		device.setStrength(10);
		device.setPhyAttr("IMEI");
		
		return device; 

	}
	
	@GET
	@Path("/getTEST")
	@Produces(MediaType.APPLICATION_SVG_XML)
	public String getTEST(){
		String print = "HELL";
		sqlConnection dao = new sqlConnection();
		try {
			print = dao.readTEST();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return print; 

	}

	
	@GET
	@Path("/getResource")
	@Produces("application/json")
	public Resource getResource() {

		Resource resource = new Resource();
		resource.setLat("100");
		resource.setLon("100");
		resource.setName("TV");
		resource.setNSSID("CMU");
		resource.setType("display");
		
		return resource; 

	}
	
	@GET
	@Path("/getActiveUser")
	@Produces("application/json")
	public ActiveUser getActiveUser() {

		ActiveUser user = new ActiveUser();
		user.setLat("100");
		user.setLon("100");
		user.setNSSID("CMU");
		user.setID(1);
		user.setUser_ID(1);
		
		return user; 

	}
	
	@GET
	@Path("/getPassiveUser")
	@Produces("application/json")
	public PassiveUser getPassiveUser() {

		PassiveUser user = new PassiveUser();
		user.setLat(0);
		user.setLon(0);
		user.setNSSID("CMU");
		user.setSteps(0);
		user.setDevice_Phy_ID("Hello");
		return user; 

	}
	
	@POST
	@Path("/postUser")
	@Consumes("application/json")
	public Response createUser(User user) {

		String result = "User created : " + user;
		return Response.status(201).entity(result).build();
		
	}
	
	@POST
	@Path("/postDevice")
	@Consumes("application/json")
	public Response createDevice(Device device) {

		String result = "Device created : " + device;
		return Response.status(201).entity(result).build();
		
	}
	
	@POST
	@Path("/postResource")
	@Consumes("application/json")
	public Response createResource(Resource resource) {

		String result = "Resource created : " + resource;
		return Response.status(201).entity(result).build();
		
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
					
					else
						result.next();
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
	@Path("/AuthResquest")
	@Consumes("application/json")
	public Response auth_request() {
		//TODO Complete this after building application
		String result = "Authenticated";
		return Response.status(201).entity(result).build();
		
	}
}