package com.tarnag.hearts;

import android.support.annotation.IntegerRes;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by BA on 2017. 03. 03..
 */

public class Round {
    int startposotion;
    int currentposition;
    int point=0;
    GameActivity gameActivity;
    CommunicationServer communicationServer;
    //ArrayList<Card> cards=new ArrayList<>();
    int colourofRound;
    int highestcardvalue;
    int placeofhighestcardvalue;
    ServerGameThread serverGameThread;


    Round(int pos, String card, GameActivity gameActivity, CommunicationServer communicationServer,ServerGameThread serverGameThread){
        Log.d("pos",Integer.toString(pos));
        startposotion=pos+100;
        Log.d("startposition",Integer.toString(startposotion));
        this.gameActivity=gameActivity;
        this.communicationServer=communicationServer;
        this.serverGameThread=serverGameThread;
        //addCard(card);
        currentposition=pos+15;
    }

    void addCard(String card){
        currentposition++;
        Log.d("currentposition",Integer.toString(currentposition));
        Log.d("startpositition", "a"+Integer.toString(startposotion));
        for (int i=0;i<4;i++){
            communicationServer.sendMessage(i,"PLAYED."+Integer.toString(currentposition%4)+"."+card);
        }
        Card car= new Card(card);
        point+=car.point;
        if ((startposotion-currentposition)%4==0){
            colourofRound=car.colour;
            highestcardvalue=car.value;
            placeofhighestcardvalue=currentposition;
        }
        else {
            if (car.colour==colourofRound)
                if (car.value>highestcardvalue){
                    highestcardvalue=car.value;
                    placeofhighestcardvalue=currentposition;
                }
        }

        //Log.d("Place of highest card",Integer.toString(placeofhighestcardvalue));

        //Log.d("currentposition",Integer.toString(currentposition));
        //gdsa
        //Log.d("startpositition", Integer.toString(startposotion));

        Log.d("Position in a round",Integer.toString((startposotion-currentposition)%4));
        if ((startposotion-currentposition)%4==1){
            Log.d("Finishround","called");
            finishround();
        }
    }

    void finishround(){
        //points
        serverGameThread.callNumber++;
        serverGameThread.pointsInRound[placeofhighestcardvalue%4]+=point;
        Log.d("callNumber", Integer.toString(serverGameThread.callNumber));
        if (serverGameThread.callNumber==13){
            //checking if someone got all points
            boolean isAll=false;
            for (int i=0;i<4;i++){
                if (serverGameThread.pointsInRound[i]==26) isAll=true;
            }

            for (int i=0;i<4;i++){
                int pff;
                if (!isAll) pff=serverGameThread.pointsInRound[i]; else pff=26-serverGameThread.pointsInRound[i];
                serverGameThread.points[i]+=pff;
                serverGameThread.pointsInRound[i]=0;
                Log.d("Point of "+ Integer.toString(i),Integer.toString(pff));
                Log.d("All point of "+ Integer.toString(i),Integer.toString(serverGameThread.points[i]));
                serverGameThread.players[i].score=serverGameThread.points[i];
            }
            serverGameThread.callNumber=0;
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }

            serverGameThread.beforeDealing();

            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }


            serverGameThread.dealing();
            serverGameThread.round = null;

        }
        else {

            //calling next round
            for (int i = 0; i < 4; i++) {
                if (gameActivity.serverGameThread.canCallHearts)
                    communicationServer.sendMessage(i, "CALL." + Integer.toString(placeofhighestcardvalue % 4) + ".HEARTS");
                else
                    communicationServer.sendMessage(i, "CALL." + Integer.toString(placeofhighestcardvalue % 4));
            }
            serverGameThread.round = null;
        }
    }
}
