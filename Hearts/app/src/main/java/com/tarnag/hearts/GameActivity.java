package com.tarnag.hearts;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by viktor on 2017. 02. 28..
 */

public class GameActivity extends AppCompatActivity {

    GamePanel gamePanel;

    //stores players
    Player[] players = new Player[4];

    String serverIp;
    boolean isServer=false;

    Communication communication;
    CommunicationServer communicationServer=null;
    ServerGameThread serverGameThread=null;

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
        Intent intent=getIntent();
        serverIp=intent.getStringExtra("ServerIp");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        communication=new Communication(this);
        if (serverIp.equals(communication.ownip)) {
            isServer=true;
            communicationServer=new CommunicationServer(this);
            serverGameThread=new ServerGameThread(this,communicationServer);
            players[0]=new Player(intent.getStringExtra("playerIp0"),intent.getStringExtra("NameIp0"));
            players[0].position=0;
            players[1]=new Player(intent.getStringExtra("playerIp0"),intent.getStringExtra("NameIp1"));
            players[1].position=1;
            players[2]=new Player(intent.getStringExtra("playerIp0"),intent.getStringExtra("NameIp2"));
            players[2].position=2;
            players[3]=new Player(intent.getStringExtra("playerIp0"),intent.getStringExtra("NameIp3"));
            players[3].position=3;
            serverGameThread.start();
        }

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
