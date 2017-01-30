package com.tarnag.pikkdama;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.lang.Object;

public class GameActivity extends AppCompatActivity {

    String serverIp;
    public int clientReceivingPort;
    public int clientSendingPort;
    private ListView listView;
    CardAdapter cardAdapter;

    public int roundNumber = 0;
    private boolean isInGame = false;
    private boolean isInGiving = true;
    private boolean canPlayCard = false;
    private boolean hasBeenHearts = false;
    private boolean clubs2 = false;
    private boolean youCall = false;
    private int colourOfCall = 0;

    List<Card> ownCards = new ArrayList<>();


   ClientCom clientCom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        listView = (ListView) findViewById(R.id.listView);

        //gets info out of intent
        Intent intent=getIntent();
        serverIp=intent.getStringExtra("ServerIp");
        clientReceivingPort=intent.getIntExtra("ReceivingPort",2016);
        clientSendingPort=intent.getIntExtra("SendingPort",2015);

        //waits until previous ClientCom destroys
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

        clientCom=new ClientCom(this);

    }

    //starts the next round
    public void startRound() {
        roundNumber++;
        isInGame = false;
        if (roundNumber % 4 == 0) {
            startGame();
        } else {
            isInGiving = true;
            createListView();
        }
    }

    public void createListView() {

        Log.d("createListView", "sorting cards");
        //sorting cards
        Collections.sort(ownCards, new Comparator<Card>() {
            @Override
            public int compare(Card card, Card t1) {
                if (card.colour > t1.colour) return 1;
                if (card.colour < t1.colour) return -1;
                if (card.value > t1.value) return 1;
                if (card.value < t1.value) return -1;
                return 0;
            }
        });
        Card[] ownCardsArray = new Card[ownCards.size()];
        ownCards.toArray(ownCardsArray);


        //creating adapter
        Log.d("createListView", "Creating listView");
        cardAdapter = new CardAdapter(this, ownCardsArray);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(cardAdapter);
            }
        });

    }

    // Using an AsyncTask to load the slow images in a background thread
    /*new AsyncTask<ViewHolder, Void, Bitmap>() {
        private ViewHolder v;

        @Override
        protected Bitmap doInBackground(ViewHolder... params) {
            v = params[0];
            return mFakeImageLoader.getImage();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (v.position == position) {
                // If this item hasn't been recycled already, hide the
                // progress and set and show the image
                v.progress.setVisibility(View.GONE);
                v.icon.setVisibility(View.VISIBLE);
                v.icon.setImageBitmap(result);
            }
        }
    }.execute(holder);

    static class ViewHolder {
        TextView text;
        TextView timestamp;
        ImageView icon;
        ProgressBar progress;
        int position;
    }*/

    //ALL USER INTERACTION
    public void onOkButtonClick(View view) {

        Log.d("onOkButtonClick", "Clicked and isInGame: " + isInGame);
        //if during giving
        if (isInGiving) {

            //get checked cards
            List<String> giving = new ArrayList<>();
            int size = cardAdapter.checkBoxes.size();
            Log.d("giving", "Length of checkBoxes: " + size);
            for (int i = 0; i < size; i++) {

                //get checkbox
                CheckBox checkBox = cardAdapter.checkBoxes.get(i);
                if (checkBox.isChecked()) {
                    Card card = ownCards.get(i);
                    giving.add(card.type);
                    Log.d("giving", "Added: " + card.type);
                }
            }

            //check if there are 3 of them
            if (giving.size() == 3) {
                for (int i = 0; i < 3; i++) {

                    String givenCard = giving.get(i);
                    //deleting from ownCards
                    int len = ownCards.size();
                    for (int j = 0; j < len; j++) {
                        if (ownCards.get(j).type.equals(givenCard)) {
                            ownCards.remove(j);
                            len--;
                        }
                    }

                    String msg = "GIVING." + givenCard;
                    clientCom.sendMessage(serverIp, clientCom.clientSendingPort, msg);
                }
                isInGiving = false;
            }
            else {
                //alerting user
                Context context = getApplicationContext();
                CharSequence msg = "Invalid selection";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, msg, duration).show();
            }

        }

        //if during game
        if (isInGame) {

            //if you can play a card
            if (canPlayCard) {

                //gets selected card
                List<Card> selected = new ArrayList<>();
                int size = cardAdapter.checkBoxes.size();
                int selectedCard = 0;

                for (int i = 0; i < size; i++) {
                    if (cardAdapter.checkBoxes.get(i).isChecked()) {
                        selected.add(ownCards.get(i));
                    }
                }

                //checks if it is one card
                if (selected.size() == 1) {
                    //checks if you can play it
                    if (canPlayThisCard(selected.get(0))) {
                        ownCards.remove(selectedCard);
                        playCard(selected.get(0));
                    }

                } else {
                    //TODO notifies player that he selected to many cards
                }
            }
        }

    }

    private boolean canPlayThisCard(Card card) {

        //if you call
        if (youCall) {
            if (clubs2) {
                return card.type.equals("4_02");
            }
            if (!hasBeenHearts) {
                if (card.colour != 1) return true;

                //checks if you have only hearts
                boolean hasNonHearts = false;
                int size = ownCards.size();
                for (int i = 0; i < size; i++) {
                    if (ownCards.get(i).colour != 1) hasNonHearts = true;
                }
                return !hasNonHearts;
            }
            return true;
        }
        //middle of a turn
        else {

            //TODO cannot play valuable card in the first turn

            if (card.colour == colourOfCall) return true;

            //checks if you have no colour of call
            boolean hasColourOfCall = false;
            int size = ownCards.size();
            for (int i = 0; i < size; i++) {
                if (ownCards.get(i).colour != colourOfCall) hasColourOfCall = true;
            }
            return !hasColourOfCall;
        }
    }

    private void playCard(Card card) {
        canPlayCard = false;
        youCall = false;
        String msg = "PLAY." + card.type;
        clientCom.sendMessage(serverIp, clientSendingPort, msg);
        createListView();
    }

    //starts the actual game
    public void startGame() {

        //creates UI
        createListView();

        //sets inGame mode
        isInGame = true;

        //checks for clubs 2
        boolean clubs2 = false;
        for (int i = 0; i < 13; i++) {
            if (ownCards.get(i).type.equals("4_02")) {
                clubs2 = true;
            }
        }
        if (clubs2) {
            clientCom.sendMessage(serverIp, clientSendingPort, "CLUBS2");
        }

    }

    //your call, notifies the system that you can play a card
    public void yourCall(boolean clubs2, boolean hasBeenHearts) {
        this.youCall = true;
        this.clubs2 = clubs2;
        this.hasBeenHearts = hasBeenHearts;
        canPlayCard = true;
        //TODO notifies player to play a card
    }

    //your turn to play a card
    public void yourPlay(int colourOfCall) {
        this.colourOfCall = colourOfCall;
        canPlayCard = true;
        //TODO notifies player to play a card
    }

}
