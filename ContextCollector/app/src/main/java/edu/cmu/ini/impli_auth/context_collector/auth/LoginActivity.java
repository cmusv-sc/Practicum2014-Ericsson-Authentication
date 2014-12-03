package edu.cmu.ini.impli_auth.context_collector.auth;

import android.animation.*;
import android.annotation.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.telephony.*;
import android.text.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.widget.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.util.*;

import java.util.*;

import edu.cmu.ini.impli_auth.context_collector.*;
import edu.cmu.ini.impli_auth.context_collector.face.*;
import edu.cmu.ini.impli_auth.context_collector.geofence.*;
import edu.cmu.ini.impli_auth.context_collector.util.*;


/**
 * The LoginActivity registers the context collector to the authentication server.
 * User credentials is used to authenticate the user at initial connection. After
 * registering the context collector, a per device credential is generated for
 * authenticating future connection.
 */
public class LoginActivity extends Activity{
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // Views for UI references.
    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;
    private EditText mNameView;
    private View mProgressView;
    private View mLoginFormView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mConfirmPasswordView;
    private EditText mEmailView;

    String IMEINumber;

    // Load global variables.
	GlobalVariable gv = GlobalVariable.getInstance();
    private final String serverIP = gv.getSERVER_IP();

    //Request Code.
    static final int REGISTER_USER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUserNameView = (AutoCompleteTextView) findViewById(R.id.user_name);
        mPasswordView = (EditText) findViewById(R.id.password);

        final Button mSignInButton = (Button) findViewById(R.id.SIGN_IN_BUTTON);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mNameView = (EditText)findViewById(R.id.resource_name);
        mFirstNameView = (EditText)findViewById(R.id.first_name_reg);
        mLastNameView = (EditText)findViewById(R.id.last_name_reg);
        mConfirmPasswordView = (EditText)findViewById(R.id.password_confirm_reg);
        mEmailView = (EditText)findViewById(R.id.email_reg);

        final Button mRegisterButton = (Button) findViewById(R.id.register_button);
        final Button mSubmitRegisterButton = (Button) findViewById(R.id.submit_register_button);
        final Button mBackToLoginButton = (Button) findViewById(R.id.back_to_login_button);

        // Change the visibility of form for registration with button.
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mNameView.setVisibility(View.GONE);
                mConfirmPasswordView.setVisibility(View.VISIBLE);
                mFirstNameView.setVisibility(View.VISIBLE);
                mLastNameView.setVisibility(View.VISIBLE);
                mEmailView.setVisibility(View.VISIBLE);
                mSignInButton.setVisibility(View.GONE);
                mRegisterButton.setVisibility(View.GONE);
                mSubmitRegisterButton.setVisibility(View.VISIBLE);
                mBackToLoginButton.setVisibility(View.VISIBLE);
            }
        });

        // Submit registration request with button.
        mSubmitRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop sending the request if password and confirm password are different.
                if(!mPasswordView.getText().toString().equals(mConfirmPasswordView.getText().toString())){
                    //Password mismatched. Refuse register.
                    mPasswordView.setError("Password does not match.");
                    mPasswordView.requestFocus();
                    mPasswordView.setText("");
                    mConfirmPasswordView.setText("");
                    return;
                }

                // Getting necessary fields to send register request, pass control to FaceActivity.
                Intent registerUser = new Intent(LoginActivity.this, FaceActivity.class);
                registerUser.putExtra("UserName", mUserNameView.getText().toString());
                registerUser.putExtra("Password", mPasswordView.getText().toString());
                registerUser.putExtra("FirstName", mFirstNameView.getText().toString());
                registerUser.putExtra("LastName", mLastNameView.getText().toString());
                registerUser.putExtra("Email", mEmailView.getText().toString());
                startActivityForResult(registerUser, REGISTER_USER);
            }
        });

        // Set visibilities for login form.
        mBackToLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mNameView.setVisibility(View.VISIBLE);
                mConfirmPasswordView.setVisibility(View.GONE);
                mFirstNameView.setVisibility(View.GONE);
                mLastNameView.setVisibility(View.GONE);
                mEmailView.setVisibility(View.GONE);
                mSignInButton.setVisibility(View.VISIBLE);
                mSubmitRegisterButton.setVisibility(View.GONE);
                mBackToLoginButton.setVisibility(View.GONE);
                mRegisterButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REGISTER_USER) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Update the user if register is successful.
                Toast.makeText(LoginActivity.this, data.getBooleanExtra("Result", false)? "Registered":"Failed to Register",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     */
    public void attemptLogin() {
        // Try hiding soft input from window. Continue even if failed.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View focus = getCurrentFocus();
        if(focus != null) {
            IBinder windowToken = focus.getWindowToken();
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
        }

        // Start only one authentication task.
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String userName = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String deviceName = mNameView.getText().toString();

        // Get IMEI number to identify the context collector. May change other information in the future.
        TelephonyManager cellInfo = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        IMEINumber = cellInfo.getDeviceId();

        // Variables for stopping the login procedure if any checks failed and get focus.
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(userName)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(userName, password, deviceName, IMEINumber);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Override the back button to avoid user bypassing the login process.
     */
    @Override
    public void onBackPressed() {
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
     * Represents an asynchronous login task used to authenticate the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        // Information to send during user login.
        private final String mEmail;
        private final String mPassword;
        private final String mName;
        private final String mIMEI;

        UserLoginTask(String mEmail, String mPassword, String mName, String mIMEI) {
            this.mEmail = mEmail;
            this.mPassword = mPassword;
            this.mName = mName;
            this.mIMEI = mIMEI;
        }

        /**
         * The method to run login logic in the background.
         * @param params No used for now.
         * @return a boolean indicating the login status.
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair("Username", mEmail));
            nameValuePairs.add(new BasicNameValuePair("Password",mPassword));
            nameValuePairs.add(new BasicNameValuePair("name",mName));
            nameValuePairs.add(new BasicNameValuePair("IMEI",mIMEI));


            try{
                // Setup http client for connection.
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(String.format("http://%s:8080/CentralServer/json/postDevice/", serverIP));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();

                // Get the response.
                final String content = EntityUtils.toString(entity);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, content,
                                Toast.LENGTH_LONG).show();
                    }
                });

                // Get information from response.
                String[] elements = content.split(",");
                SharedPreferences sharedPref = getSharedPreferences("com.impl_auth.authenticationclient_preferences.xml",
                                               MODE_PRIVATE);

                // Handle different login status.
                if(elements[0].equalsIgnoreCase("Success")){
                    //store key in element[1]
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.share_perf_key), elements[1]);
                    editor.putString(getString(R.string.registered_flag), "True");
                    editor.apply();
                    return true;
                } else {
                    return false;
                }

            }catch(final Exception e){
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "ERROR " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
                System.out.println("Error in http connection "+e.toString());
            }
            return false;

        }

        /**
         * Handle the login result.
         * @param success a boolean indicates the login status.
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            // Remove the tracking login task and hide the progress bar.
            mAuthTask = null;
            showProgress(false);

            if (success) {
                // Redirect to main activity after login.
                Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(mainActivity);
            } else {
                mPasswordView.setError("Failed to authenticate with server");
                mPasswordView.requestFocus();
            }
        }

        /**
         * Reset view and remove task if canceled.
         */
        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}



