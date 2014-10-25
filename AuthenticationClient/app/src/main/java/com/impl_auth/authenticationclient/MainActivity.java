package com.impl_auth.authenticationclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;


public class MainActivity extends Activity {

    ToggleButton getLocationService;
    EditText ipAddressField;
    EditText updateFrequencyField;
    Button send_location;

    GPSTracker tracker;
    private boolean serviceNotRunning = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLocationService = (ToggleButton) findViewById(R.id.toggleButton1);
        ipAddressField = (EditText)findViewById(R.id.editText1);
        updateFrequencyField = (EditText)findViewById(R.id.editTextUpdateInterval);

        send_location = (Button)findViewById(R.id.button);

        getLocationService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    Intent i = new Intent(MainActivity.this, GPSTracker.class);
                    i.putExtra("IP", ipAddressField.getText().toString());
                    i.putExtra("updateInterval",Integer.valueOf(updateFrequencyField.getText().toString()));
                    startService(i);
                } else {
                    stopService(new Intent(MainActivity.this, GPSTracker.class));
                }
            }
        });

//        send_location.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                InfoJsonSend json = new InfoJsonSend(this,)
//            }
//        });
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
//        int errorcode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getBaseContext());
//        if(errorcode != 0){
//            int requestCode = 0;
//            Dialog errorDiag = GooglePlayServicesUtil.getErrorDialog(errorcode, this, requestCode);
//            errorDiag.show();
//        }
        super.onResume();

    }

}
