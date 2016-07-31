package com.agpfd.whackamole;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class WhackAMoleActivity extends Activity {

    private static final int TOGGLE_SOUND = 1;

    private WhackAMoleView myWhackAMoleView;
    private boolean soundEnabled = true;

    /**
     * Called when the activity is first created
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.whackamole_layout);
        myWhackAMoleView = (WhackAMoleView) findViewById(R.id.mole);
        myWhackAMoleView.setKeepScreenOn(true);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem toggleSound = menu.add(0, TOGGLE_SOUND, 0, getString(R.string.toggle_sound));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case TOGGLE_SOUND:
                String soundEnabledText = getString(R.string.sound_on);
                if (soundEnabled) {
                    soundEnabled = false;
                    myWhackAMoleView.soundOn = false;
                    soundEnabledText = getString(R.string.sound_off);
                } else {
                    soundEnabled = true;
                    myWhackAMoleView.soundOn = true;
                }
                Toast.makeText(this, soundEnabledText, Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }
}
