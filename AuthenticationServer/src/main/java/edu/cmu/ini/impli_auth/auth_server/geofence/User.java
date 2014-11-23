package edu.cmu.ini.impli_auth.auth_server.geofence;

public class User {

	String first_name;
	String last_name;
	String email;
	String username;
	String password;
	
	
	public String getFirstName() {
		return first_name;
	}

	public void setFirstName(String name) {
		this.first_name = name;
	}
	
	public String getLastName() {
		return last_name;
	}

	public void setLastName(String name) {
		this.last_name = name;
	}

	public String getUserName() {
		return username;
	}

	public void setUserName(String name) {
		this.username = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String name) {
		this.password = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String name) {
		this.email = name;
	}

	@Override
	public String toString() {
		return "User [name="+first_name+" "+last_name+ ", Email=" + email + "]";
	}

}