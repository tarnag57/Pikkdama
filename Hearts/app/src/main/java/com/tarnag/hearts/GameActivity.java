package com.tarnag.hearts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by viktor on 2017. 02. 28..
 */

public class GameActivity extends AppCompatActivity {

    GamePanel gamePanel;

    //stores players
    Player[] players = new Player[4];

    String serverIp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //CREATES UI
        //Turn title off
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //set to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //custom view
        gamePanel = new GamePanel(this);
        setContentView(gamePanel);

        //LOGIC


    }

    @Override
    protected void onPause() {
        super.onPause();
        gamePanel.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gamePanel.resume();
    }
}
