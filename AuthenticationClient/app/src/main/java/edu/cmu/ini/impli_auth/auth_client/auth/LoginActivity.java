package edu.cmu.ini.impli_auth.auth_client.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;

import edu.cmu.ini.impli_auth.auth_client.*;
import edu.cmu.ini.impli_auth.auth_client.face.*;
import edu.cmu.ini.impli_auth.auth_client.util.*;

/**
 * Login the authentication client on share resource. Obtain the share secret for
 * future communication.
 */
public class LoginActivity extends Activity {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;
	private ShareSecretLoginTask mShareSecretLoginTask = null;

	// UI references.
	private AutoCompleteTextView mEmailView;
	private EditText mPasswordView;
	private EditText mNameView;
	private EditText mLatitudeView;
	private EditText mLongitudeView;
	private EditText mTypeView;
	private View mProgressView;
	private View mLoginFormView;

	// Shared preference.
	private SharedPreferences sharedPref;

	//Authentication server ip.
	private GlobalVariable gv = GlobalVariable.getInstance();
	private final String serverIP = gv.getSERVER_IP();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Set up the login form.
		mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
		mPasswordView = (EditText) findViewById(R.id.password);

		Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
		mEmailSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mProgressView = findViewById(R.id.login_progress);
		mNameView = (EditText) findViewById(R.id.resource_name);
		mLatitudeView = (EditText) findViewById(R.id.latitude);
		mLongitudeView = (EditText) findViewById(R.id.longitude);
		mTypeView = (EditText) findViewById(R.id.type);
	}

	@Override
	protected void onResume() {
		super.onResume();
		sharedPref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
		String registered = sharedPref.getString(getString(R.string.registered_flag), null);
		Toast.makeText(LoginActivity.this, registered,
				Toast.LENGTH_LONG).show();
		if (registered != null) {
			//sign in with share secret
			WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			String nssid = wifi.getConnectionInfo().getMacAddress();
			String sharedKey = sharedPref.getString(getString(R.string.share_perf_key), null);
			showProgress(true);
			mShareSecretLoginTask = new ShareSecretLoginTask(nssid, sharedKey);
			mShareSecretLoginTask.execute((Void) null);
		}

	}

	/**
	 * Attempts to sign in.
	 */
	public void attemptLogin() {
		// Try hiding soft input from window. Continue even if failed.
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		View focus = getCurrentFocus();
		if (focus != null) {
			IBinder windowToken = focus.getWindowToken();
			inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String email = mEmailView.getText().toString();
		String password = mPasswordView.getText().toString();
		String resource_name = mNameView.getText().toString();
		String latitude = mLatitudeView.getText().toString();
		String longitude = mLongitudeView.getText().toString();

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		String nssid = wifi.getConnectionInfo().getMacAddress();

		String type = mTypeView.getText().toString();

		boolean cancel = false;
		View focusView = null;


		// Check for a valid password, if the user entered one.
		if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(email)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true);
			mAuthTask = new UserLoginTask(email, password, resource_name, latitude, longitude, nssid, type);
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime).alpha(
					show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login task used to authenticate the client on share resource.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

		private final String mEmail;
		private final String mPassword;
		private final String mName;
		private final String mLatitude;
		private final String mLongtitude;
		private final String mNSSID;
		private final String mType;

		UserLoginTask(String mEmail, String mPassword, String mName, String mLatitude, String mLongtitude, String mNSSID, String mType) {
			this.mEmail = mEmail;
			this.mPassword = mPassword;
			this.mName = mName;
			this.mLatitude = mLatitude;
			this.mLongtitude = mLongtitude;
			this.mNSSID = mNSSID;
			this.mType = mType;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("Username", mEmail));
			nameValuePairs.add(new BasicNameValuePair("Password", mPassword));
			nameValuePairs.add(new BasicNameValuePair("name", mName));
			nameValuePairs.add(new BasicNameValuePair("latitude", mLatitude));
			nameValuePairs.add(new BasicNameValuePair("longitude", mLongtitude));
			nameValuePairs.add(new BasicNameValuePair("NSSID", mNSSID));
			nameValuePairs.add(new BasicNameValuePair("type", mType));


			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(String.format("http://%s:8080/CentralServer/json/postResource/", serverIP));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();

				// Handle the response.
				final String content = EntityUtils.toString(entity);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(LoginActivity.this, content,
								Toast.LENGTH_LONG).show();
					}
				});
				String[] elements = content.split(",");
				if (elements[0].equalsIgnoreCase("Success")) {
					//store key in element[1]
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString(getString(R.string.share_perf_key), elements[1]);
					editor.putString(getString(R.string.registered_flag), "True");
					editor.apply();
					return true;
				} else {
					return false;
				}
			} catch (final Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(LoginActivity.this, "ERROR " + e.getMessage(),
								Toast.LENGTH_LONG).show();
					}
				});
				System.out.println("Error in http connection " + e.toString());
			}
			return false;

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				Intent camTestActivity = new Intent(LoginActivity.this, FaceActivity.class);
				startActivity(camTestActivity);
			} else {
				mPasswordView.setError("Failed to authenticate with server");
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

	/**
	 * Represents an asynchronous login task using share secret.
	 */
	public class ShareSecretLoginTask extends AsyncTask<Void, Void, Boolean> {

		private final String mNSSID;
		private final String mSKey;

		ShareSecretLoginTask(String mNSSID, String mSKey) {
			this.mNSSID = mNSSID;
			this.mSKey = mSKey;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("NSSID", mNSSID));
			nameValuePairs.add(new BasicNameValuePair("SKEY", mSKey));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(String.format("http://%s:8080/CentralServer/json/authResource/", serverIP));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();

				// Handle response.
				final String content = EntityUtils.toString(entity);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(LoginActivity.this, content,
								Toast.LENGTH_LONG).show();
					}
				});
				return content.equalsIgnoreCase("Success");

			} catch (final Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(LoginActivity.this, "ERROR " + e.getMessage(),
								Toast.LENGTH_LONG).show();
					}
				});
				System.out.println("Error in http connection " + e.toString());
			}
			return false;

		}

		/**
		 * Method to handle login result.
		 *
		 * @param success A boolean value indicates login status.
		 */
		@Override
		protected void onPostExecute(final Boolean success) {
			mShareSecretLoginTask = null;
			showProgress(false);

			if (success) {
				// Redirect to face activity after login.
				Intent camTestActivity = new Intent(LoginActivity.this, FaceActivity.class);
				startActivity(camTestActivity);
			}
		}

		@Override
		protected void onCancelled() {
			mShareSecretLoginTask = null;
			showProgress(false);
		}
	}
}



