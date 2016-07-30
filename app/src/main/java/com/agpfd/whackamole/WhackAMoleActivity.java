package com.agpfd.whackamole;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class WhackAMoleActivity extends Activity {

    private WhackAMoleView myWhackAMoleView;

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
    }
}
