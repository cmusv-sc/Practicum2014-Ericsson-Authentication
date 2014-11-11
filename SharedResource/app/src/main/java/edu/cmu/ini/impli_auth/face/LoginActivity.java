package edu.cmu.ini.impli_auth.face;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import java.util.List;


/**
 * A login screen that offers login via email/password.

 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor>{

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
    private SharedPreferences sharedPref;
    private String registered;

    //server ip
    private static final String serverIP = "192.168.0.100";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mNameView = (EditText)findViewById(R.id.resource_name);
        mLatitudeView= (EditText)findViewById(R.id.latitude);
        mLongitudeView= (EditText)findViewById(R.id.longitude);
        mTypeView= (EditText)findViewById(R.id.type);
    }

    @Override
    protected void onResume(){
        super.onResume();
        //sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
        //registered = sharedPref.getString(getString(R.string.share_perf_key),null);



        sharedPref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        registered = sharedPref.getString(getString(R.string.registered_flag),null);
        Toast.makeText(LoginActivity.this, registered,
                Toast.LENGTH_LONG).show();
        if (registered != null){
            //sign in with share secret
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            String nssid =  wifi.getConnectionInfo().getMacAddress();
            String sharedKey = sharedPref.getString(getString(R.string.share_perf_key),null);
            showProgress(true);
            mShareSecretLoginTask = new ShareSecretLoginTask(nssid, sharedKey);
            mShareSecretLoginTask.execute((Void) null);
        }

    }

    private void populateAutoComplete() {
        if (VERSION.SDK_INT >= 14) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, this);
        } else if (VERSION.SDK_INT >= 8) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
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
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String resource_name = mNameView.getText().toString();
        String latitude = mLatitudeView.getText().toString();
        String longitude = mLongitudeView.getText().toString();

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String nssid =  wifi.getConnectionInfo().getMacAddress();

        String type = mTypeView.getText().toString();

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
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                                                                     .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<String>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

	    @Override
	    protected void onPostExecute(List<String> emailAddressCollection) {
	       addEmailsToAutoComplete(emailAddressCollection);
	    }
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
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
            ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair("Username", mEmail));
            nameValuePairs.add(new BasicNameValuePair("Password",mPassword));
            nameValuePairs.add(new BasicNameValuePair("name",mName));
            nameValuePairs.add(new BasicNameValuePair("latitude",mLatitude));
            nameValuePairs.add(new BasicNameValuePair("longitude",mLongtitude));
            nameValuePairs.add(new BasicNameValuePair("NSSID",mNSSID));
            nameValuePairs.add(new BasicNameValuePair("type",mType));


            try{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(String.format("http://%s:8080/CentralServer/json/postResource/", serverIP));
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
                if(elements[0].equalsIgnoreCase("Success")){
                    //store key in element[1]
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.share_perf_key), elements[1]);
                    editor.putString(getString(R.string.registered_flag), "True");
                    editor.commit();
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
                Intent camTestActivity = new Intent(LoginActivity.this, CamActivity.class);
                startActivity(camTestActivity);
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
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
            ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair("NSSID",mNSSID));
            nameValuePairs.add(new BasicNameValuePair("SKEY",mSKey));

            try{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(String.format("http://%s:8080/CentralServer/json/authResource/", serverIP));
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
                return content.equalsIgnoreCase("Success");

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
            mShareSecretLoginTask = null;
            showProgress(false);

            if (success) {
                //finish();
                Intent camTestActivity = new Intent(LoginActivity.this, CamActivity.class);
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



