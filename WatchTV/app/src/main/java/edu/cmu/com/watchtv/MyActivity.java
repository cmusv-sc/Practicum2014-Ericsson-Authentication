package edu.cmu.com.watchtv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import java.util.*;
import java.lang.*;

public class MyActivity extends Activity {
    boolean pressed = false;
    Button myButton;
    EditText myText;
    EditText myText2;
	private String TAG = "MyActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        EditText myText = (EditText) findViewById(R.id.editText);
        final EditText myText2 = (EditText) findViewById(R.id.editText2);
        final Button myButton = (Button) findViewById(R.id.my_button);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent();
                i.setComponent(new ComponentName("edu.cmu.ini.impli_auth.face",
		                "edu.cmu.ini.impli_auth.face.FaceActivity"));
                startActivityForResult(i, 1);
            }
        });
    }
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            String location = data.getStringExtra("Access");
            if (requestCode == 1 ) {
                if(resultCode == RESULT_OK && location.equals("public")){
                    Intent intent = new Intent(MyActivity.this, PlayVideo.class);
	                Log.d(TAG, String.valueOf(data.getDoubleExtra("Prob", -1.0)));
	                Log.d(TAG, data.getStringExtra("UserName"));
	                Log.d(TAG, location);
                    intent.putExtra("probability", data.getDoubleExtra("Prob", -1.0));
	                intent.putExtra("username", data.getStringExtra("UserName"));
                    startActivity(intent);

                }
                if(resultCode == RESULT_OK&& location.equals("private")){
                    Intent intent = new Intent(MyActivity.this, privateView.class);
                    intent.putExtra("username", data.getStringExtra("UserName"));
	                Log.d(TAG, String.valueOf(data.getDoubleExtra("Prob", -1.0)));
	                Log.d(TAG, data.getStringExtra("UserName"));
	                Log.d(TAG, location);
                    startActivity(intent);

                }
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "To use this service please register with Pyxis", Toast.LENGTH_LONG).show();
                }
            }
        }

    }
