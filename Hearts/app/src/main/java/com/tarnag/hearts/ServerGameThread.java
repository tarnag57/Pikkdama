package com.tarnag.hearts;

import android.util.Log;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Marci on 2017. 02. 28..
 */

public class ServerGameThread extends Thread {

    public Player[] players=new Player[4];
    private GameActivity gameActivity;
    private CommunicationServer communicationServer;
    int roundNumber = 0;
    int callNumber;

    int givenCards = 0;
    boolean isInGame = false;
    int[] points={0,0,0,0};
    int[] pointsInRound={0,0,0,0};
    Round round=null;

    boolean canCallHearts=false;


    ServerGameThread(GameActivity gameActivity, CommunicationServer communicationServer){
        this.gameActivity = gameActivity;
        this.communicationServer = communicationServer;
    }

    private void resetBeforeRound() {
        isInGame = false;
        givenCards = 0;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

        sendingFirstInfos();

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

        beforeDealing();

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }


        dealing();

        //waiting for response from clients

    }

    public void cardGiven(String ip, String msg) {

        //determine player index
        int index = -1;
        for (int i = 0; i < 4; i++) {
            if (ip.equals(players[i].ip)) {
                index = i;
            }
        }

        //determine dest player
        int destIndex=index ;
        switch(roundNumber%4){
            case 1: destIndex++;
                break;
            case 2: destIndex--;
                break;
            case 3: destIndex+=2;
                break;
        }
        send(destIndex%4, msg);

        //counting given cards
        givenCards++;
        if (givenCards == 12) {
            isInGame = true;
        }

    }

    private void sendingFirstInfos() {
        for (int i=0;i<4;i++) {
            for (int j = 0; j < 4; j++) {
                send(i, "PlayerName." + Integer.toString(j) + "." + players[j].playerName);
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

        for (int i=0;i<4;i++){
            send(i,"Position."+Integer.toString(i));
        }

    }

    public void beforeDealing(){
        callNumber=0;
        //sending scores
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                send(i, "PlayerScore."+Integer.toString(j)+"."+Integer.toString(players[j].score));
            }
        }

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }


        //sending round number
        roundNumber++;
        for (int i=0;i<4;i++){
            send(i, "ROUNDNUMBER."+Integer.toString(roundNumber));
        }

        resetBeforeRound();
    }


    public void dealing(){

        //creating deck
        Card[] deck = new Card[52];
        int index = 0;
        for (int i = 1; i < 5; i++) {
            for (int j = 2; j < 15; j++) {
                deck[index] = new Card(i, j);
                index++;
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                   // e.printStackTrace();
                }
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

        //sending cards to players
        index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 13; j++) {
                Player currPlayer = players[i];
                send(i,"DEAL." + deck[index].type);
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
                index++;
            }
        }
    }

    public void addCard(int pos, String card){
        //Log.d("callNumber",Integer.toString(callNumber));
        if (round==null) round=new Round(pos,card,gameActivity,communicationServer,this);
        round.addCard(card);
    }


    private void send(int position, String msg){
        communicationServer.sendMessage(position,msg);
    }
}
