package com.tarnag.hearts;

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
        startposotion=pos;
        this.gameActivity=gameActivity;
        this.communicationServer=communicationServer;
        this.serverGameThread=serverGameThread;
        //addCard(card);
        currentposition=pos-1;
    }

    void addCard(String card){
        currentposition++;
        for (int i=0;i<4;i++){
            communicationServer.sendMessage(i,"PLAYED."+Integer.toString(currentposition%4)+"."+card);
        }
        Card car= new Card(card);
        point+=car.point;
        if (startposotion-currentposition==1){
            colourofRound=car.colour;
            highestcardvalue=car.value;
            placeofhighestcardvalue=currentposition%4;
        }
        else {
            if (car.colour==colourofRound)
                if (car.value>highestcardvalue){
                    highestcardvalue=car.value;
                    placeofhighestcardvalue=currentposition%4;
                }
        }

        if ((startposotion-currentposition)%4==3){
            Log.d("Finishround","called");
            finishround();
        }
    }

    void finishround(){
        //points
        serverGameThread.callNumber++;
        serverGameThread.pointsInRound[placeofhighestcardvalue]+=point;

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
            }
            serverGameThread.callNumber=0;
        }

        //calling next round
        for (int i=0;i<4;i++) {
        if (gameActivity.serverGameThread.canCallHearts)
            communicationServer.sendMessage(i, "CALL." + Integer.toString(placeofhighestcardvalue) + ".HEARTS");
            else communicationServer.sendMessage(i, "CALL." + Integer.toString(placeofhighestcardvalue));
        }
    }
}
