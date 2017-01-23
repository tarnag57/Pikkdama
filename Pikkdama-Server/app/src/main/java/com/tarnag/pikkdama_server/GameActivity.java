package com.tarnag.pikkdama_server;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;

import static java.lang.Thread.sleep;

public class GameActivity extends AppCompatActivity {

    TextView textView;
    Player[] players = new Player[4];
    ServerCom serverCom;
    Card[] deck = new Card[52];
    int gameNumber=0;
    boolean isgiving=false;
    boolean isplaying=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        textView = (TextView) findViewById(R.id.textView);

        //adds players from extras
        Intent intent = getIntent();
        for (int i = 0; i < 4; i++) {
            String playerName = intent.getStringExtra("playerName" + String.valueOf(i));
            String playerIp = intent.getStringExtra("playerIp" + String.valueOf(i));
            players[i] = new Player(playerIp, playerName);
        }

        dealer();

    }

    protected void dealer() {
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (gameNumber==0)serverCom = new ServerCom(this);
        gameNumber++;

        for (int i = 0; i < 4; i++) {
            Player currPlayer = players[i];
            serverCom.sendMessage(currPlayer.ip, serverCom.serverSendingPort, "NUMBER." + Integer.toString(gameNumber));
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //TODO állás kiiratása és változtatása

        int index = 0;

        for (int i = 1; i < 5; i++) {
            for (int j = 2; j < 15; j++) {
                deck[index] = new Card(i, j);
                index++;
            }
        }

        //sorting deck by random
        Arrays.sort(deck, new Comparator<Card>() {
            @Override
            public int compare(Card card, Card t1) {
                if (card.random > t1.random) return 1;
                if (card.random < t1.random) return -1;
                return 0;
            }
        });

        //send cards to players
        index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 13; j++) {
                Player currPlayer = players[i];
                serverCom.sendMessage(currPlayer.ip, serverCom.serverSendingPort, "DEAL." + deck[index].type);
                index++;
            }
        }

        giving();
    }

    protected void giving(){
        if (gameNumber % 4 != 0){
            serverCom.receivedGivingCard = 0;
            isgiving = true;
        }

    }

    protected void game(){
        isplaying = true;

    }
}
