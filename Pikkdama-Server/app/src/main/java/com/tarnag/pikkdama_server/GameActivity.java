package com.tarnag.pikkdama_server;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
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
    boolean hasBeenHearts = false;
    int startingPlayer = 0;

    //stores points for current round
    int[] currPoints = new int[4];

    //stores the cards during this turn
    ArrayList<Card> currCards = new ArrayList<>();

    //how many turns have been
    private int turnCounter = 0;
    Card firstCard = null;
    int curPlayer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
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


        if (gameNumber == 0)serverCom = new ServerCom(this);
        gameNumber++;

        //sends the number of current game
        for (int i = 0; i < 4; i++) {
            Player currPlayer = players[i];
            serverCom.sendMessage(currPlayer.ip, serverCom.serverSendingPort, "NUMBER." + Integer.toString(gameNumber));
            try {
                sleep(100);
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
        }

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
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
                index++;
            }
        }

        giving();
    }

    //registers giving
    protected void giving(){
        if (gameNumber % 4 != 0){
            serverCom.receivedGivingCard = 0;
            isgiving = true;
        }

    }

    //starts round
    protected void game(){
        isplaying = true;
        hasBeenHearts = false;
        //13 turns
        startTurn(true);
    }

    //starts the actual round
    protected void startTurn(boolean clubs2) {

        curPlayer = startingPlayer;
        firstCard = null;

        //sends message to starting playaer
        String msg = "CALL.";
        if (clubs2) {
            msg += "CLUBS2";
        }
        if (hasBeenHearts) {
            msg += "HEARTS";
        }
        serverCom.sendMessageToNthPlayer(startingPlayer, msg);
    }

    //alerts the player that it is your turn
    public void alertPlayer() {
        int nextPlayerIndex = curPlayer % 4;
        Player nextPlayer = players[nextPlayerIndex];
        String msg = "PLAY." + firstCard.colour;
        serverCom.sendMessage(nextPlayer.ip, serverCom.serverSendingPort, msg);
    }

    //someone played a card
    public void playedACard(Card card) {
        currCards.add(card);
        if (card.colour == 1) hasBeenHearts = true;
        curPlayer++;

        //checks if its the first
        if (currCards.size() == 1) {
            firstCard = card;
        }

        //updates ui
        showsUI();

        //moves to next player
        if (currCards.size() == 4) {
            endTurn();
        } else {
            curPlayer++;
            alertPlayer();
        }
    }

    //the end of every turn, resets everything, add points to scores
    public void endTurn() {

        //determines the score of cards
        int score = 0;
        for (int i = 0; i < 4; i++) {
            if (currCards.get(i).colour == 1) score++;
            if (currCards.get(i).type.equals("3_12")) score += 13;
        }

        //determine who gets them
        int max = 0;
        Card maxCard = firstCard;
        for (int i = 1; i < 4; i++) {
            Card card = currCards.get(i);
            if ((card.colour == maxCard.colour) && (card.value > maxCard.value)) max = i;
        }
        int loserPlayerIndex = (curPlayer - (4 - max)) % 4; //TODO this might be incorrect, must check
        currPoints[loserPlayerIndex] += score;
        startingPlayer = loserPlayerIndex;

        //resets everything
        currCards = new ArrayList<>();
        turnCounter++;
        resetsUI();

        //checks if there has benn 13 turns
        if (turnCounter == 13) {
            endRound();
        } else {
            startTurn(false);
        }
    }

    private void endRound() {

        //handling scores
        //checks for 26
        int score26 = -1;
        for (int i = 0; i < 4; i++) {
            if (currPoints[i] == 26) score26 = i;
        }
        if (score26 > -1) {
            for (int i = 0; i < 4; i++) {
                if (score26 != i) players[i].score += 26;
            }
        } else {
            for (int i = 0; i < 4; i++) {
                players[i].score += currPoints[i];
            }
        }

        //resets scores and everything else
        for (int i = 0; i < 4; i++) {
            currPoints[i] = 0;
        }
        isplaying = false;
        showStats();
        gameNumber++;

        //new game, new dealing
        dealer();
    }

    private void showsUI() {
        //TODO someone played a card, lets show it! (currCards ArrayList<Card>)
    }

    private void resetsUI() {
        //TODO its the ned of the turn, someone gets the cards, resets the screen
    }

    private void showStats() {
        //TODO ui: shows current points and stats
    }
}
