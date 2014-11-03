package edu.cmu.ini.impli_auth.server;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;

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
/*
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
		*/
		AuthFaceRecognizer faceRecognizer = new AuthFaceRecognizer(new File(""));
		faceRecognizer.run();
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
	public Response createActiveUser(ActiveUser user) {

		String result = "Active User created : " + user;
		sqlConnection dao = new sqlConnection();
		try {
			dao.writeToAUT(user);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.status(201).entity(result).build();
		
	}
	
	
}