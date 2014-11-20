package com.impl_auth.authenticationclient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import java.util.Calendar;

import static com.impl_auth.authenticationclient.R.string.*;


public class MainActivity extends Activity {

    ToggleButton getLocationService;
    EditText ipAddressField;
    EditText updateFrequencyField;
    Button send_location;
    SharedPreferences sharedPref;
    boolean serviceRunning;
    Intent i;

    private boolean serviceNotRunning = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getLocationService = (ToggleButton) findViewById(R.id.toggleButton1);
        ipAddressField = (EditText)findViewById(R.id.editText1);
        updateFrequencyField = (EditText)findViewById(R.id.editTextUpdateInterval);

        send_location = (Button)findViewById(R.id.button);

        /*
        Getting the last recorded day to reset the step counter value everyday.
        Can be used for context like tired, bored etc.
         */

        sharedPref = getSharedPreferences("com.impl_auth.authenticationclient_preferences.xml",
                                            MODE_PRIVATE);
        String registered = sharedPref.getString(getString(registered_flag),null);
        int current_day = sharedPref.getInt(getString(R.string.lastRecordedDay),0);

        if(current_day == 0){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(lastRecordedDay), Calendar.DAY_OF_YEAR);
            editor.commit();
            current_day = sharedPref.getInt(getString(R.string.lastRecordedDay),0);
        }

        if (current_day != Calendar.DAY_OF_YEAR){
            if(serviceRunning){
                //reset
                stopService(i);
                //restart
                i.putExtra("IP", ipAddressField.getText().toString());
                i.putExtra("updateInterval",Integer.valueOf(updateFrequencyField.getText().toString()));
                startService(i);
            }
        }
        if(registered != null){
            // start service if registered.
            i = new Intent(MainActivity.this, GPSTracker.class);
            i.putExtra("IP", ipAddressField.getText().toString());
            i.putExtra("updateInterval",Integer.valueOf(updateFrequencyField.getText().toString()));
            startService(i);
            serviceRunning = true;
            super.onStop();
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();

    }

}
