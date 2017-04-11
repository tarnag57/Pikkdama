package com.tarnag.hearts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

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

    boolean hearts = false;
    boolean isYouCall = false;

    boolean isYouPlay = false;

    Communication communication;
    CommunicationServer communicationServer = null;
    ServerGameThread serverGameThread = null;

    ArrayList<Card> buffer = null;

    int roundNumber = 0;

    ArrayList<Card> playedCards = null;

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
            serverGameThread.players[0]=new Player(intent.getStringExtra("playerIp0"),intent.getStringExtra("playerName0"));
            serverGameThread.players[0].position=0;
            serverGameThread.players[1]=new Player(intent.getStringExtra("playerIp1"),intent.getStringExtra("playerName1"));
            serverGameThread.players[1].position=1;
            serverGameThread.players[2]=new Player(intent.getStringExtra("playerIp2"),intent.getStringExtra("playerName2"));
            serverGameThread.players[2].position=2;
            serverGameThread.players[3]=new Player(intent.getStringExtra("playerIp3"),intent.getStringExtra("playerName3"));
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
                Context context = getApplicationContext();
                CharSequence text = getResources().getString(R.string.onlythree) ;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
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
                    Log.d(Integer.toString(i) + ". element of buffer: ", buffer.get(i).type);
                    gamePanel.cards.add(buffer.get(i));
                }
                buffer = null;
                //sorting cards
                Collections.sort(gamePanel.cards,new Comparator<Card>() {
                    @Override
                    public int compare(Card card, Card t1) {//sorting
                        if ((card.colour>t1.colour)||((card.colour==t1.colour) && (card.value<t1.value))) return -1;
                        return 1;
                    }});
            }

            gamePanel.canPress = false;
            isGiving = false;
            if (gamePanel.cards.size() == 13) {
                gamePanel.draw();
                startRound();
            }

        }

        if (isYouCall) {
            if (cards.size() != 1) {
                Context context = getApplicationContext();
                CharSequence text = getResources().getString(R.string.onlyone) ;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            if (!canCallThisCard(cards.get(0))) {
                Context context = getApplicationContext();
                CharSequence text = getResources().getString(R.string.cantcall) ;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }

            //remove card form own cards
            for (int j = 0; j < gamePanel.cards.size(); j++) {
                if (gamePanel.cards.get(j).type.equals(cards.get(0).type)) {
                    gamePanel.cards.remove(j);
                    break;
                }
            }

            gamePanel.canPress = false;
            isYouCall = false;
            //send back
            communication.sendMessage(serverIp, "PLAYED." + cards.get(0).type);

            gamePanel.draw();
        }

        if (isYouPlay) {
            if (cards.size() != 1) {
                Context context = getApplicationContext();
                CharSequence text = getResources().getString(R.string.onlyone) ;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            if (!canPlayThisCard(cards.get(0))) {
                Context context = getApplicationContext();
                CharSequence text = getResources().getString(R.string.cantplay) ;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }

            //remove card form own cards
            for (int j = 0; j < gamePanel.cards.size(); j++) {
                if (gamePanel.cards.get(j).type.equals(cards.get(0).type)) {
                    gamePanel.cards.remove(j);
                    break;
                }
            }

            gamePanel.canPress = false;
            isYouPlay = false;
            //send back
            communication.sendMessage(serverIp, "PLAYED." + cards.get(0).type);

            gamePanel.draw();
        }

    }

    public void gotGiving(Card card) {

        //if player hasn't given cards
        if (isGiving) {
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

    //someone played a card
    public void cardPlayed(int player, Card card) {

        if (playedCards == null) {
            playedCards = new ArrayList<>();
        }

        //add played cards
        playedCards.add(card);

        //remove one card
        gamePanel.numOfCards[player]--;
        Log.d("Card removed from", Integer.toString(player) + " with name " + gamePanel.players[player].playerName);

        //move token
        gamePanel.isToken = true;
        gamePanel.token = (player + 1) % 4;


        gamePanel.draw();

        if (playedCards.size() == 4) {
            endCall();
            return;
        }

        if ((player + 1) % 4 == gamePanel.ownPosition) {
            youPlay();
        }

    }

    public void youPlay() {
        isYouPlay = true;
        gamePanel.canPress = true;
    }

    public void endCall() {
        //TODO ending round
        playedCards = null;
    }

    //giving -> this is before startRound()
    public void giving() {

        roundNumber++;

        //if giving is necessary
        if (roundNumber % 4 != 0) {
            gamePanel.canPress = true;
            isGiving = true;
            buffer = new ArrayList<>();
        } else {
            //skipping to start round
            startRound();
        }
    }

    //starts the actual round
    public void startRound() {
        isInGame = true;

        //creates 13 cards
        for (int i = 0; i < 4; i++) {
            gamePanel.numOfCards[i] = 13;
        }

        //checks for clubs2
        for (int i = 0; i < gamePanel.cards.size(); i++) {
            if (gamePanel.cards.get(i).type.equals("4_02")) {
                communication.sendMessage(serverIp, "CLUBS2");
            }
        }
    }

    public void call(int player, boolean hearts) {

        Log.d("Player to call", Integer.toString(player) + " hearts: " + Boolean.toString(hearts));

        //determine token
        gamePanel.token = player;
        gamePanel.isToken = true;

        //remove played cards from the center
        playedCards = new ArrayList<>();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        gamePanel.draw();

        //if you call
        if (player == gamePanel.ownPosition) {
            youCall(hearts);
        }

    }

    public boolean canCallThisCard(Card card) {

        //if this is the first round -> clubs2
        if (gamePanel.cards.size() == 13) {
            return card.type.equals("4_02");
        }

        //if hearts has been played -> you can call anything
        if (hearts) {
            return true;
        }

        if (card.colour != 1) {
            return true;
        }

        //if you only have hearts
        boolean onlyHearts = true;
        for (int i = 0; i < gamePanel.cards.size(); i++) {
            if (gamePanel.cards.get(i).colour != 1) {
                onlyHearts = false;
            }
        }

        return onlyHearts;

    }

    public boolean canPlayThisCard(Card card) {

        //if you have same colour
        if (card.colour == playedCards.get(0).colour) {
            return true;
        }

        //if you don't have same colour
        boolean hasSame = false;
        for (int i = 0; i < gamePanel.cards.size(); i++) {
            if (gamePanel.cards.get(i).colour == playedCards.get(0).colour) {
                hasSame = true;
            }
        }

        //if you have same
        if (hasSame) {
            return false;
        }

        //if this is the first round
        if (gamePanel.cards.size() == 13) {
            if (card.point > 0) {
                return false;
            }
        }

        return true;
    }

    public void youCall(boolean hearts) {
        this.hearts = hearts;
        isYouCall = true;
        gamePanel.canPress = true;
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
