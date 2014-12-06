package edu.cmu.com.watchtv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class ViewVideo extends Activity {
    private static final int INSERT_ID = Menu.FIRST;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        String filename = extras.getString("videofilename");
        VideoView vv = new VideoView(getApplicationContext());
        setContentView(vv);
        vv.setVideoPath(filename);
        vv.setMediaController(new MediaController(this));
        vv.requestFocus();
        vv.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0,"FullScreen");

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                createNote();
        }
        return true;
    }

    @SuppressLint("AppCompatMethod")
    private void createNote() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

}