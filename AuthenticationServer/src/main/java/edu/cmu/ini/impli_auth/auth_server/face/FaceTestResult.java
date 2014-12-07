package edu.cmu.ini.impli_auth.auth_server.face;

/**
 * 
 * Type for facial recognition testing result. 
 * label -> user id 
 * p -> authentication probability
 *
 */
public class FaceTestResult {
	public int label;
	public double p;

	public FaceTestResult(int label, double p) {
		this.label = label;
		this.p = p;
	}
}
