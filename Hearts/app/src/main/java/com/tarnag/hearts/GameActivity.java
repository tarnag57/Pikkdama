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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

/**
 * Created by viktor on 2017. 02. 28..
 */

public class GameActivity extends AppCompatActivity {

    GamePanel gamePanel;

    //stores players
    //Player[] players = new Player[4];

    String serverIp;
    boolean isServer = false;

    boolean isGiving = false;
    boolean isInGame = false;

    Communication communication;
    CommunicationServer communicationServer = null;
    ServerGameThread serverGameThread = null;

    ArrayList<Card> buffer = null;

    int roundNumber = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //CREATES UI
        //Turn title off
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //set to full screen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //custom view
        gamePanel = new GamePanel(this, this);
        setContentView(gamePanel);

        //LOGIC
        Intent intent = getIntent();
        serverIp = intent.getStringExtra("ServerIp");

        //waiting to close previous communication class
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        communication = new Communication(this);

        //if device is server
        if (serverIp.equals(communication.ownip)) {
            isServer = true;
            communicationServer = new CommunicationServer(this);
            serverGameThread = new ServerGameThread(this, communicationServer);

            //fills up players
            serverGameThread.players[0]=new Player(intent.getStringExtra("playerIp0"),intent.getStringExtra("NameIp0"));
            serverGameThread.players[0].position=0;
            serverGameThread.players[1]=new Player(intent.getStringExtra("playerIp1"),intent.getStringExtra("NameIp1"));
            serverGameThread.players[1].position=1;
            serverGameThread.players[2]=new Player(intent.getStringExtra("playerIp2"),intent.getStringExtra("NameIp2"));
            serverGameThread.players[2].position=2;
            serverGameThread.players[3]=new Player(intent.getStringExtra("playerIp3"),intent.getStringExtra("NameIp3"));
            serverGameThread.players[3].position=3;

            //starts game
            serverGameThread.start();
        }

    }

    //cards received from gamePanel
    public void cardsSelected(ArrayList<Card> cards) {

        //which mode
        if (isGiving) {
            if (cards.size() != 3) {
                return;
            }

            for (int i = 0; i < cards.size(); i++) {
                //giving to server
                communication.sendMessage(serverIp, "GIVING." + cards.get(i).type);

                //deleting from game panel's cards
                for (int j = 0; j < gamePanel.cards.size(); j++) {
                    if (gamePanel.cards.get(j).type.equals(cards.get(i).type)) {
                        gamePanel.cards.remove(j);
                        break;
                    }
                }
            }

            if (buffer != null) {
                for (int i = 0; i < buffer.size(); i++) {
                    gamePanel.cards.add(buffer.get(i));
                }
                buffer = null;
            }

            gamePanel.canPress = false;
            isGiving = false;
            gamePanel.draw();

        }

    }

    public void gotGiving(Card card) {

        //if player hasn't given cards
        if (isGiving) {
            buffer = new ArrayList<>();
            buffer.add(card);
        } else {
            gamePanel.cards.add(card);
            if (buffer != null) {
                if (buffer.size() != 0) {
                    for (int i = 0; i < buffer.size(); i++) {
                        gamePanel.cards.add(buffer.get(i));
                    }
                }
                buffer = null;
            }

            //if all cards have arrived
            if (gamePanel.cards.size() == 13) {

                //sorting cards
                Collections.sort(gamePanel.cards,new Comparator<Card>() {
                    @Override
                    public int compare(Card card, Card t1) {//sorting
                        if ((card.colour>t1.colour)||((card.colour==t1.colour) && (card.value<t1.value))) return -1;
                        return 1;
                    }});

                //redrawing
                gamePanel.draw();

                //starting game
                startRound();
            }
        }

    }

    //giving
    public void giving() {

        //if giving is necessary
        if (roundNumber % 4 != 0) {
            gamePanel.canPress = true;
            isGiving = true;
        } else {
            //skipping to start round
            startRound();
        }
    }

    //starts the actual round
    public void startRound() {
        isInGame = true;

        //checks for clubs2
        for (int i = 0; i < gamePanel.cards.size(); i++) {
            if (gamePanel.cards.get(i).type.equals("4_02")) {
                communication.sendMessage(serverIp, "CLUBS2");
            }
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
