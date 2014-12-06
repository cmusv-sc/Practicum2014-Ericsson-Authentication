package edu.cmu.ini.impli_auth.auth_server.request;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.SecureRandom;
import java.math.BigInteger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import edu.cmu.ini.impli_auth.auth_server.database.SqlConnection;
import edu.cmu.ini.impli_auth.auth_server.datamodel.PassiveUser;
import edu.cmu.ini.impli_auth.auth_server.face.FaceTestResult;
import edu.cmu.ini.impli_auth.auth_server.face.LBPHFaceRecognizer;
import edu.cmu.ini.impli_auth.auth_server.util.Util;

/**
 * 
 * Contains RESTful interface for client application's registration and
 * authentication request
 */

@Path("/json")
public class CentralServer {

	private LBPHFaceRecognizer faceRecognizer = null;
	private static int WIDTH = 128, HEIGHT = 128;

	public CentralServer() {
		if (Util.canStartFaceReg()) {
			faceRecognizer = new LBPHFaceRecognizer(WIDTH, HEIGHT);
			faceRecognizer.train();
		} else {
			System.out
					.println("facial recognition training not start yet, we need at least two users to start it");
		}
	}

	@GET
	@Path("/getUserResources/{id}")
	public Response getUserResources(@PathParam("id") int id) {

		SqlConnection dao = new SqlConnection();
		ResultSet resultSet = null;
		try {
			resultSet = dao.getUserResources(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<table><tr><td>name</td><td>delete</td></tr>");
		String result = "Failed to get result";
		if (resultSet != null) {
			try {
				while (resultSet.next()) {
					sb.append("<tr><td>");
					sb.append(resultSet.getString("name"));
					sb.append("</td><td>");
					sb.append(String
							.format("<form action=\"/CentralServer/json/deleteResourceById\" method=\"POST\"><input type=\"hidden\" name=\"id\" value=\"%s\"><input type=\"submit\" value=\"Delete\"></form>",
									resultSet.getString("id")));
					sb.append("</td></tr>");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			sb.append("</table>");
			result = sb.toString();
		}
		return Response.status(200).entity(result).build();
	}

	@POST
	@Path("/deleteResourceById")
	// @Produces("application/json")
	public void deleteResourceById(@FormParam("id") int id) {
		SqlConnection dao = new SqlConnection();
		try {
			dao.deleteResourceById(id);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Function used to register the user
	 * 
	 * @param username
	 *            Username
	 * @param password
	 *            Password
	 * @param firstName
	 *            Entered first name
	 * @param lastName
	 *            Entered last name
	 * @param email
	 *            Email ID
	 * @param image1
	 *            First image
	 * @param image2
	 *            Second image
	 * @param image3
	 *            Third image
	 * @param image4
	 *            Fourth image
	 * @param image5
	 *            Fifth Image
	 * @return
	 */
	@POST
	@Path("/postUser")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createUser(@FormParam("Username") String username,
			@FormParam("Password") String password,
			@FormParam("FirstName") String firstName,
			@FormParam("LastName") String lastName,
			@FormParam("Email") String email,
			@FormParam("image1") String image1,
			@FormParam("image2") String image2,
			@FormParam("image3") String image3,
			@FormParam("image4") String image4,
			@FormParam("image5") String image5) {

		SqlConnection dao = new SqlConnection();
		int result = 0;
		try {
			dao.registerUser(username, password, firstName, lastName, email);
		} catch (Exception e) {
			e.printStackTrace();
			result = -1;
		}

		// save image into user's corresponding directory
		if (result != -1) {
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
			if (Util.canStartFaceReg()) {
				faceRecognizer.train();
			}
		}

		String returnMessage;
		switch (result) {
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

	/**
	 * Function used to register resources
	 * 
	 * @param username
	 *            Username of owner
	 * @param password
	 *            Password of owner
	 * @param name
	 *            Specific name of the resource
	 * @param latitude
	 *            Latitude of the resource location
	 * @param longitude
	 *            Longitude of the resource location
	 * @param NSSID
	 *            WiFi NSSID connected to
	 * @param type
	 *            Type of user can be public/private
	 * @return HTTP response with result code
	 */

	@POST
	@Path("/postResource")
	// @Consumes("application/json")
	// information to register a resource: name,latitude,longitude,NSSID,type,
	// send a generated SKEY back.
	public Response createResource(@FormParam("Username") String username,
			@FormParam("Password") String password,
			@FormParam("name") String name,
			@FormParam("latitude") String latitude,
			@FormParam("longitude") String longitude,
			@FormParam("NSSID") String NSSID, @FormParam("type") String type) {
		SqlConnection dao = new SqlConnection();
		System.out.println("postResource");
		int result;
		int userid = 0;
		try {
			if ((userid = dao.authByUsernamePassword(username, password)) > 0) {
				// auth pass
				result = 1;
			} else {
				// auth failed
				result = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = -1;
		}

		String returnMessage;
		switch (result) {
		case 1:
			SecureRandom random = new SecureRandom();
			String SKEY = new BigInteger(130, random).toString(32);
			returnMessage = "Success," + SKEY;
			try {
				dao.registerResource(name, latitude, longitude, NSSID, type,
						SKEY, userid);
			} catch (Exception e) {
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

	/**
	 * Method used to authenticate the resource everytime its switched back on
	 * 
	 * @param NSSID
	 *            Wifi NSSID connected to
	 * @param sKey
	 *            Shared key provided to the resource
	 * @return HTTP response with result code
	 */

	@POST
	@Path("/authResource")
	// information to authenticate a resource: NSSID,SKEY
	public Response authResource(@FormParam("NSSID") String NSSID,
			@FormParam("SKEY") String sKey) {
		SqlConnection dao = new SqlConnection();
		String result;
		try {
			if (dao.authByNssidSharedKey(NSSID, sKey)) {
				// auth pass
				result = "Success";
			} else {
				// auth failed
				result = "Failed";
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = "Exception";
		}

		return Response.status(200).entity(result).build();
	}

	/**
	 * 
	 * GEO-FENCING
	 * 
	 * After the user is successfully signed in the app will constantly be
	 * sending us location information. Every post from the android app will be
	 * handled here.
	 * 
	 * Basically the post has values like the device information and the
	 * location of the device. We first check if the device is registered again,
	 * then we check if the device is already in contention for resources i.e it
	 * has been previously serviced. (Programmatically: If the user is already
	 * in the PASSIVE_USER table)
	 * 
	 * If not we check from his location if he is near any Resource using the
	 * above distance calculator. If he is within the threshold we add him to
	 * PASSIVE_USER along with the resource_id of the resource he is near to.
	 * 
	 * @param user
	 *            On success we create a PassiveUser entry. The input we get
	 *            from the app is in PassiveUser object format.
	 * @return HTTP response with result code
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
			System.out.println("ABOUT TO READ :" + user.device_phy_id);
			result = dao.readPassiveUser(user.getDevice_Phy_ID());

			if (result != null) {
				System.out.println("SHOULD NOT REACH HERE");
				dao.updatePassiveUser(user, result.getInt("USER_ID"));
			} else {
				System.out.println("Reached ELSE");
				result = dao.readResource();
				while (result.next()) {
					double lat1 = Double.parseDouble(result
							.getString("LATITUDE"));
					double lat2 = Double.parseDouble(result
							.getString("LONGITUDE"));
					dist = Util.distance(lat1, lat2, user.getLat(),
							user.getLon());
					System.out.println("Reached DISTANCE" + dist);

					if (dist < 1) {
						dao.writePUT(user, result.getInt("ID"));
					}

					else {
						result.next();
						System.out
								.println("You are too far away. No resources. DISTANCE="
										+ dist);
					}

				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(304).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(304).build();
		}

		String http_result = "Passive User : " + user;
		return Response.status(201).entity(http_result).build();
	}

	/**
	 * Method used to create a device entry every time the user logs in
	 * 
	 * @param username
	 *            Username of the device owner
	 * @param password
	 *            Password of the device owner
	 * @param name
	 *            Device name
	 * @param IMEI
	 *            The IMEI no of the device. This can be replaced with any
	 *            device specific no.
	 * @return HTTP response with result code
	 */
	@POST
	@Path("/postDevice")
	// @Consumes("application/json")
	// information to register a resource: name,latitude,longitude,NSSID,type,
	// send a generated SKEY back.
	public Response createDevice(@FormParam("Username") String username,
			@FormParam("Password") String password,
			@FormParam("name") String name, @FormParam("IMEI") String IMEI) {
		SqlConnection dao = new SqlConnection();
		System.out.println("postResource");
		int result;
		int userid = 0;
		try {
			if ((userid = dao.authByUsernamePassword(username, password)) > 0) {
				// auth pass
				result = 1;
			} else {
				// auth failed
				result = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = -1;
		}

		String returnMessage;
		switch (result) {
		case 1:
			SecureRandom random = new SecureRandom();
			String credential = new BigInteger(80, random).toString(32);
			returnMessage = "Success," + credential;
			try {
				dao.registerDevice(name, IMEI, credential, userid);
			} catch (Exception e) {
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

	public boolean active_user(int id, int auth) throws Exception {
		SqlConnection dao = new SqlConnection();
		return dao.writeToAUT(id, auth);
	}

	/**
	 * run facial recognition test, first consider private resource owner in
	 * multiple user scenarios, and also only consider who is actually active
	 * user right now to refine the result (information come from Geo-fence)
	 * 
	 * The method is used to authenticate the user. The second image is for
	 * multiple user scenarios
	 * 
	 * @param credential
	 *            Credential provided to the resource (display) user is using
	 * @param image1
	 *            Image of the first user
	 * @param image2
	 *            Image of the second user
	 * @return HTTP response with result code
	 * @throws Exception
	 *             throws IOException
	 */
	@POST
	@Path("/testImage")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response processTestImage(
			@FormParam("credential") String credential,
			@FormParam("image1") String image1,
			@FormParam("image2") String image2) throws Exception {

		String userName = "";
		// facial recognition can't start yet, return response as unknown user
		if (!Util.canStartFaceReg()) {
			return Response.status(200).entity(userName + ":" + 0 + ":public")
					.build();
		}
		if (faceRecognizer == null) {
			faceRecognizer = new LBPHFaceRecognizer(WIDTH, HEIGHT);
			faceRecognizer.train();
		}

		byte[] imageData = DatatypeConverter.parseBase64Binary(image1);
		int auth = 0;

		FaceTestResult result = faceRecognizer.test(imageData, "test1.jpg");
		userName = Util.getUserName(result.label);
		System.out.println("first user");
		System.out.println("UserName: " + userName);
		System.out.println("Prob: " + result.p);
		System.out.println("Credential: " + credential);
		System.out.println("Access: "
				+ Util.getResourceAccessStatus(result.label, credential));

		FaceTestResult targetResult = result;

		if (!image2.isEmpty()) {
			imageData = DatatypeConverter.parseBase64Binary(image2);
			result = faceRecognizer.test(imageData, "test2.jpg");
			userName = Util.getUserName(result.label);
			System.out.println("second user");
			System.out.println("UserName: " + userName);
			System.out.println("Prob: " + result.p);
			System.out.println("Credential: " + credential);
			System.out.println("Access: "
					+ Util.getResourceAccessStatus(result.label, credential));

			if (result.label != -1) {
				int resourceID = Util.getResourceID(result.label);
				if (Util.isUserResource(result.label, resourceID)) {
					targetResult = result;
				}
			}
		}

		if (active_user(targetResult.label, auth)) {
			return Response
					.status(200)
					.entity(Util.getUserName(targetResult.label)
							+ ":"
							+ targetResult.p
							+ ":"
							+ Util.getResourceAccessStatus(targetResult.label,
									credential)).build();
		} else {
			System.out.println("the user is not an active user!");
			userName = "";
			return Response.status(200).entity(userName + ":" + 0 + ":public")
					.build();
		}
	}
}
