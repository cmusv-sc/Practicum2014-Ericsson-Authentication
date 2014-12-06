package edu.cmu.ini.impli_auth.context_collector.geofence;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import edu.cmu.ini.impli_auth.context_collector.*;
import edu.cmu.ini.impli_auth.context_collector.auth.*;


public class MainActivity extends Activity {

    Button logout;
    SharedPreferences sharedPref;
    //Boolean to check if GPS Tracker service is running
    boolean serviceRunning;
    Intent i;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		logout = (Button) findViewById(R.id.button);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPref = getSharedPreferences("com.impl_auth.authenticationclient_preferences.xml",
                        MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.apply();
                //Stop services
                stopService(i);

				// Send user back to login page.
				Intent loginActivity = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(loginActivity);
			}
		});

        /*
        Getting the last recorded day to reset the step counter value everyday.
        Can be used for context like tired, bored etc.
         */

        sharedPref = getSharedPreferences("com.impl_auth.authenticationclient_preferences.xml",
                                            MODE_PRIVATE);
        String registered = sharedPref.getString(getString(R.string.registered_flag),null);
        int current_day = sharedPref.getInt(getString(R.string.lastRecordedDay),0);

        if(current_day == 0){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.lastRecordedDay), Calendar.DAY_OF_YEAR);
            editor.apply();
            current_day = sharedPref.getInt(getString(R.string.lastRecordedDay),0);
        }

        if (current_day != Calendar.DAY_OF_YEAR){
            if(serviceRunning){
                //reset
                stopService(i);
                //restart
                startService(i);
            }
        }
        if(registered != null){
            // start service if registered.
            i = new Intent(MainActivity.this, GPSTracker.class);
            startService(i);
            serviceRunning = true;
        } else {
            // redirect to login activity if not registered
            Intent loginActivity = new Intent(this, LoginActivity.class);
            startActivity(loginActivity);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return R.id.action_settings == id || super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();

    }
}
