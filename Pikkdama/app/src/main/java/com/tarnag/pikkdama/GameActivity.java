package com.tarnag.pikkdama;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    String serverIp;
    public int clientReceivingPort;
    public int clientSendingPort;
    private ListView listView;
    CardAdapter cardAdapter;

    List<Card> ownCards=new ArrayList<>();


   ClientCom clientCom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        listView = (ListView) findViewById(R.id.listView);

        Intent intent=getIntent();
        serverIp=intent.getStringExtra("ServerIp");
        clientReceivingPort=intent.getIntExtra("ReceivingPort",2016);
        clientSendingPort=intent.getIntExtra("SendingPort",2015);

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

        clientCom=new ClientCom(this);

    }

    public void createListView() {

        //copying card types into array
        String[] cardNames = new String[13];
        for (int i = 0; i < 13; i++) {
            cardNames[i] = ownCards.get(i).type;
        }

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

}
