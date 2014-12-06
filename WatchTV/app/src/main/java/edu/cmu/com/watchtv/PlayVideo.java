package edu.cmu.com.watchtv;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The activity to play video based on auth result. Result includes probability and resource type.
 * Explanation can be find in project document.
 */
public class PlayVideo extends Activity {
    //double High = 70;
    double Medium = 70;
    double Min = 15;
    String uriPath = "";
    String username,access;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.playvideo);

        //receiving the intent from the MyActivity
        Intent intent = getIntent();
        final double probability = intent.getDoubleExtra("probability", 0);
        username = intent.getStringExtra("username");
        access = intent.getStringExtra("access");

        Toast.makeText(this, String.format("Username: %s, Access Type: %s, Probability: %f", username, access, probability), Toast.LENGTH_LONG).show();

        TextView welcome = (TextView)findViewById(R.id.welcome_header);
        welcome.setText("Welcome back "+username+"...");

        System.out.println(probability);

        //View
        final ImageView imgView1 = (ImageView) findViewById(R.id.imageView);
        final ImageView imgView2 = (ImageView) findViewById(R.id.imageView2);
        final ImageView imgView3 = (ImageView) findViewById(R.id.imageView3);
        final ImageView imgView4 = (ImageView) findViewById(R.id.imageView4);
        final ImageView imgView5 = (ImageView) findViewById(R.id.imageView5);
        final ImageView imgView6 = (ImageView) findViewById(R.id.imageView6);
        final ImageView imgView7 = (ImageView) findViewById(R.id.imageView7);
        final ImageView imgView8 = (ImageView) findViewById(R.id.imageView8);
        final ImageView imgView9 = (ImageView) findViewById(R.id.imageView9);
        final ImageView imgView10 = (ImageView) findViewById(R.id.imageView10);
        final ImageView imgView11 = (ImageView) findViewById(R.id.imageView11);
        final ImageView imgView12 = (ImageView) findViewById(R.id.imageView12);

        uriPath = "android.resource://edu.cmu.com.watchtv/" + R.raw.file;

        Uri  videoURI = Uri.parse(uriPath);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, videoURI);
        Bitmap bitmap = retriever
                .getFrameAtTime(100000,MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);


        imgView1.setImageBitmap(bitmap);
        imgView2.setImageBitmap(bitmap);
        imgView3.setImageBitmap(bitmap);
        imgView4.setImageBitmap(bitmap);
        imgView5.setImageBitmap(bitmap);
        imgView6.setImageBitmap(bitmap);
        imgView7.setImageBitmap(bitmap);
        imgView8.setImageBitmap(bitmap);
        imgView9.setImageBitmap(bitmap);
        imgView10.setImageBitmap(bitmap);
        imgView11.setImageBitmap(bitmap);
        imgView12.setImageBitmap(bitmap);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.private_message)
                .setTitle(R.string.app_name).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        imgView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(access.equals("public")){
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                if(access.equals("private")){
                    Intent play_video = new Intent(PlayVideo.this,ViewVideo.class);
                    play_video.putExtra("videofilename",uriPath);
                    startActivity(play_video);
                }
            }
        });

        imgView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(access.equals("public")){
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                if(access.equals("private")){
                    Intent play_video = new Intent(PlayVideo.this,ViewVideo.class);
                    play_video.putExtra("videofilename",uriPath);
                    startActivity(play_video);
                }
            }
        });
        imgView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(access.equals("public")){
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                if(access.equals("private")){
                    Intent play_video = new Intent(PlayVideo.this,ViewVideo.class);
                    play_video.putExtra("videofilename",uriPath);
                    startActivity(play_video);
                }
            }
        });

        imgView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(access.equals("public")){
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                if(access.equals("private")){
                    Intent play_video = new Intent(PlayVideo.this,ViewVideo.class);
                    play_video.putExtra("videofilename",uriPath);
                    startActivity(play_video);
                }
            }
        });

        imgView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(access.equals("public")){
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                if(access.equals("private")){
                    Intent play_video = new Intent(PlayVideo.this,ViewVideo.class);
                    play_video.putExtra("videofilename",uriPath);
                    startActivity(play_video);
                }
            }
        });

        imgView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(access.equals("public")){
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                if(access.equals("private")){
                    Intent play_video = new Intent(PlayVideo.this,ViewVideo.class);
                    play_video.putExtra("videofilename",uriPath);
                    startActivity(play_video);
                }
            }
        });

        imgView7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(probability > Min){
                    Intent play_video = new Intent(PlayVideo.this, ViewVideo.class);
                    play_video.putExtra("videofilename", uriPath);
                    startActivity(play_video);
                }
                else{
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });

        imgView8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(probability > Min){
                    Intent play_video = new Intent(PlayVideo.this, ViewVideo.class);
                    play_video.putExtra("videofilename", uriPath);
                    startActivity(play_video);
                }
                else{
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });

        imgView9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(probability > Min){
                    Intent play_video = new Intent(PlayVideo.this, ViewVideo.class);
                    play_video.putExtra("videofilename", uriPath);
                    startActivity(play_video);
                }
                else{
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });

        imgView10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(probability > Medium){
                    Intent play_video = new Intent(PlayVideo.this, ViewVideo.class);
                    play_video.putExtra("videofilename", uriPath);
                    startActivity(play_video);
                }
                else{
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });

        imgView11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(probability > Medium){
                    Intent play_video = new Intent(PlayVideo.this, ViewVideo.class);
                    play_video.putExtra("videofilename", uriPath);
                    startActivity(play_video);
                }
                else{
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });

        imgView12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(probability > Medium){
                    Intent play_video = new Intent(PlayVideo.this, ViewVideo.class);
                    play_video.putExtra("videofilename", uriPath);
                    startActivity(play_video);
                }
                else{
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });
    }

}
