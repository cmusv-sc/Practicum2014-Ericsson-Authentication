package edu.cmu.com.watchtv;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.EditText;

/**
 * Created by pratibha on 11/20/14.
 */
public class privateView extends Activity {

    EditText text1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privateview);
        //receiving the intent from the MyActivity
        Intent intent = getIntent();
        final String userName = intent.getStringExtra("Access");
        EditText text1 = (EditText) findViewById(R.id.text);
        text1.setText(userName);

        //View
        GridLayout gridview = (GridLayout) findViewById(R.id.gridlayout);
        final VideoView vidView1 = (VideoView) findViewById(R.id.myVideo1);
        final VideoView vidView2 = (VideoView) findViewById(R.id.myVideo2);
        final VideoView vidView3 = (VideoView) findViewById(R.id.myVideo3);
        final VideoView vidView4 = (VideoView) findViewById(R.id.myVideo4);
        final VideoView vidView5 = (VideoView) findViewById(R.id.myVideo5);
        final VideoView vidView6 = (VideoView) findViewById(R.id.myVideo6);
        final VideoView vidView7 = (VideoView) findViewById(R.id.myVideo7);
        final VideoView vidView8 = (VideoView) findViewById(R.id.myVideo8);
        String uriPath = "android.resource://edu.cmu.com.watchtv/" + R.raw.file;
        Uri uri = Uri.parse(uriPath);
        vidView1.setVideoURI(uri);
        MediaController vidControl1 = new MediaController(this);
        vidControl1.setAnchorView(vidView1);
        vidView1.setMediaController(vidControl1);
        final Button buttonStart = (Button) findViewById(R.id.button1);
        //setting onCLicklistner for the button to start playing the video
        buttonStart.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                buttonStart.setVisibility(View.GONE);
                vidView1.start();
            }

        });


        vidView2.setVideoURI(uri);
        MediaController vidControl2 = new MediaController(this);
        vidControl2.setAnchorView(vidView2);
        vidView2.setMediaController(vidControl2);
        final Button buttonStart2 = (Button) findViewById(R.id.button2);
        buttonStart2.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                buttonStart2.setVisibility(View.GONE);
                vidView2.start();
            }

        });


        vidView3.setVideoURI(uri);
        MediaController vidControl3 = new MediaController(this);
        vidControl3.setAnchorView(vidView3);
        vidView3.setMediaController(vidControl3);
        final Button buttonStart3 = (Button) findViewById(R.id.button3);
        buttonStart3.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                    buttonStart3.setVisibility(View.GONE);
                    vidView3.start();
                }

        });

        vidView4.setVideoURI(uri);
        MediaController vidControl4 = new MediaController(this);
        vidControl4.setAnchorView(vidView4);
        vidView4.setMediaController(vidControl4);
        final Button buttonStart4 = (Button) findViewById(R.id.button4);
        buttonStart4.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                    buttonStart4.setVisibility(View.GONE);
                    vidView4.start();
                }

        });

        vidView5.setVideoURI(uri);
        MediaController vidControl5 = new MediaController(this);
        vidControl5.setAnchorView(vidView5);
        vidView5.setMediaController(vidControl5);
        final Button buttonStart5 = (Button) findViewById(R.id.button5);
        buttonStart5.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                    buttonStart5.setVisibility(View.GONE);
                    vidView5.start();
                }

        });


        vidView6.setVideoURI(uri);
        MediaController vidControl6 = new MediaController(this);
        vidControl6.setAnchorView(vidView6);
        vidView6.setMediaController(vidControl6);
        final Button buttonStart6 = (Button) findViewById(R.id.button6);
        buttonStart6.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                buttonStart6.setVisibility(View.GONE);
                vidView6.start();
            }

        });

        vidView7.setVideoURI(uri);
        MediaController vidControl7 = new MediaController(this);
        vidControl5.setAnchorView(vidView7);
        vidView5.setMediaController(vidControl7);
        final Button buttonStart7 = (Button) findViewById(R.id.button7);
        buttonStart5.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                    buttonStart7.setVisibility(View.GONE);
                    vidView7.start();
                }


        });


        vidView8.setVideoURI(uri);
        MediaController vidControl8 = new MediaController(this);
        vidControl8.setAnchorView(vidView8);
        vidView6.setMediaController(vidControl8);
        final Button buttonStart8 = (Button) findViewById(R.id.button8);
        buttonStart8.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                    buttonStart8.setVisibility(View.GONE);
                    vidView8.start();
                }
        });

    }
}
