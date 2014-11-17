package com.impl_auth.authenticationclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
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


/**
 * A login screen that offers login via email/password.

 */
public class LoginActivity extends Activity{

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;
    private EditText mNameView;
    private View mProgressView;
    private View mLoginFormView;
    private SharedPreferences sharedPref;
    private String registered;
    private TelephonyManager cellInfo;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mConfirmPasswordView;
    private EditText mEmailView;
    String IMEINumber;

    //server ip
    private static final String serverIP = "10.0.23.67";

    //Request Code
    static final int REGISTER_USER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUserNameView = (AutoCompleteTextView) findViewById(R.id.user_name);
        mPasswordView = (EditText) findViewById(R.id.password);

        final Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
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

        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mNameView.setVisibility(View.GONE);
                mConfirmPasswordView.setVisibility(View.VISIBLE);
                mFirstNameView.setVisibility(View.VISIBLE);
                mLastNameView.setVisibility(View.VISIBLE);
                mEmailView.setVisibility(View.VISIBLE);
                mEmailSignInButton.setVisibility(View.GONE);
                mRegisterButton.setVisibility(View.GONE);
                mSubmitRegisterButton.setVisibility(View.VISIBLE);
                mBackToLoginButton.setVisibility(View.VISIBLE);
            }
        });

        mSubmitRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: register a user by sending all info to camera view
                if(mPasswordView.getText() != mConfirmPasswordView.getText()){
                    //Password mismatched. Refuse register.
                    mPasswordView.setError("Password does not match.");
                    mPasswordView.requestFocus();
                    mPasswordView.setText("");
                    mConfirmPasswordView.setText("");
                    return;
                }

                Intent registerUser = new Intent(LoginActivity.this, FaceActivity.class);
                registerUser.putExtra("UserName", mUserNameView.getText());
                registerUser.putExtra("Password", mPasswordView.getText());
                registerUser.putExtra("FirstName", mFirstNameView.getText());
                registerUser.putExtra("LastName", mLastNameView.getText());
                registerUser.putExtra("Email", mEmailView.getText());
                startActivityForResult(registerUser, REGISTER_USER);
            }
        });

        mBackToLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mNameView.setVisibility(View.VISIBLE);
                mConfirmPasswordView.setVisibility(View.GONE);
                mFirstNameView.setVisibility(View.GONE);
                mLastNameView.setVisibility(View.GONE);
                mEmailView.setVisibility(View.GONE);
                mEmailSignInButton.setVisibility(View.VISIBLE);
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
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
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
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String deviceName = mNameView.getText().toString();

        cellInfo = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        IMEINumber = cellInfo.getDeviceId();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mUserNameView.setError(getString(R.string.error_invalid_email));
            focusView = mUserNameView;
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
            mAuthTask = new UserLoginTask(email, password, deviceName, IMEINumber);
            mAuthTask.execute((Void) null);
        }
    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        //return email.contains("@");
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        //return password.length() > 4;
        return true;
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
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

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

        @Override
        protected Boolean doInBackground(Void... params) {
            ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair("Username", mEmail));
            nameValuePairs.add(new BasicNameValuePair("Password",mPassword));
            nameValuePairs.add(new BasicNameValuePair("name",mName));
            nameValuePairs.add(new BasicNameValuePair("IMEI",mIMEI));


            try{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(String.format("http://%s:8080/CentralServer/json/postDevice/", serverIP));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();

                //
                // Read the contents of an entity and return it as a String.
                //
                final String content = EntityUtils.toString(entity);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, content,
                                Toast.LENGTH_LONG).show();
                    }
                });
                String[] elements = content.split(",");
                sharedPref = getSharedPreferences("com.impl_auth.authenticationclient_preferences.xml",
                                               MODE_PRIVATE);
                        //PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if(elements[0].equalsIgnoreCase("Success")){
                    //store key in element[1]
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.share_perf_key), elements[1]);
                    editor.putString(getString(R.string.registered_flag), "True");
                    editor.commit();
                    System.out.println("Committed");
                    return true;
                } else {
                    return false;
                }

				/*final String the_string_response = convertResponseToString(response);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(CamTestActivity.this, "Response " + the_string_response,
								Toast.LENGTH_LONG).show();
					}
				});*/

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

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                //finish();
                Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(mainActivity);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}



