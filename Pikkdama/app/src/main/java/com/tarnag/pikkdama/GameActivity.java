package com.tarnag.pikkdama;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    String serverIp;
    public int clientReceivingPort;
    public int clientSendingPort;
    private ListView listView;
    CardAdapter cardAdapter;

    public int roundNumber = 0;
    private boolean isInGame = false;
    private boolean isInGiving = true;

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

        }

    }

    //starts the actual game
    public void startGame() {
        isInGame = true;
    }

}
