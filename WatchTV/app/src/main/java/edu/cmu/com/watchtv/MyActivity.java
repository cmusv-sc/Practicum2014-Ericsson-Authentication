package edu.cmu.com.watchtv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.os.Bundle;
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
                i.setComponent(new ComponentName("edu.cmu.com.watchtv", "edu.cmu.com.watchtv.CamTestActivity"));
                startActivityForResult(i, 1);
            }
        });
    }
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            if (requestCode == 1) {
                if(resultCode == RESULT_OK){
                    /*
                    String sTextFromET = myText2.getText().toString(); //converting the number textview to int
                    int nIntFromET = new Integer(sTextFromET).intValue();
                    //sending the intent to PlayVideo.class
                    Intent intent = new Intent(MyActivity.this, PlayVideo.class);
                    intent.putExtra("probability", nIntFromET);
                    startActivity(intent);
                        */
                    Intent intent = new Intent(MyActivity.this, PlayVideo.class);
                    intent.putExtra("probability", data);
                    startActivity(intent);

                }
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "To use this service please register with Pyxis", Toast.LENGTH_LONG).show();
                    //Write your code if there's no result
                }
            }
        }

    }
