package com.tarnag.pikkdama_server;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class ConnectionActivity extends AppCompatActivity {

    //contains connections
    ListView listView;

    //contains players
    List<Player> players = new ArrayList<>();

    PlayerAdapter playerAdapter;
    ServerCom serverCom;

    String message = "";
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_activity);

        listView = (ListView) findViewById(R.id.listView);
        textView = (TextView) findViewById(R.id.textView);

        //starts listening
        serverCom = new ServerCom(this);
    }

    public void putToList (Player player) {

        Log.d("putToList", player.playerName);

        //add player to list
        players.add(player);

        //convert it to array
        Player[] playersArray = new Player[players.size()];
        players.toArray(playersArray);

        //puts it through listAdapter and displays it
        playerAdapter = new PlayerAdapter(this, playersArray);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(playerAdapter);
            }
        });

    }

    protected void startGameOnClick (View view) {

        //when button was pushed
        int size = players.size();
        for (int i = 0; i < size; i++) {

            //gets spinner
            Spinner spinner = playerAdapter.spinners.get(i);
            String selected = spinner.getSelectedItem().toString();

            //gets selected
            int num = 0;
            switch (selected) {
                case "None": num = 0; break;
                case "Player 1": num = 1; break;
                case "Player 2": num = 2; break;
                case "Player 3": num = 3; break;
                case "Player 4": num = 4; break;
            }

            Player player = players.get(i);
            player.position = num;

            Log.d("startGameOnClick", "New position for " + player.playerName + ": " + num);
        }

        //check for validity of selection
        boolean validity = true;
        int[] selectedPlayers = new int[4];
        for (int i = 0; i < 4; i++) {
            selectedPlayers[i] = -1;
        }

        //filling up selectedPlayers array
        for (int i = 0; i < size; i++) {
            int position = players.get(i).position;
            if (position != 0) {
                if (selectedPlayers[position - 1] == -1) {
                    selectedPlayers[position - 1] = i;
                } else {
                    validity = false;
                }
            }
        }

        //checking selectedPlayers array for missing positions
        for (int i = 0; i < 4; i++) {
            if (selectedPlayers[i] == -1) {
                validity = false;
            }
        }

        //if selection was valid
        if (validity) {
            message += "Valid selection";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(message);
                }
            });

            //pings selected devices
            for (int i = 0; i < 4; i++) {
                serverCom.sendMessage(players.get(selectedPlayers[i]).ip,
                        serverCom.serverSendingPort, "START");
            }

            //closing serverSocket
            serverCom.closingServerSocket();

            //puts extras into intent and starts new activity
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("playerName0", players.get(selectedPlayers[0]).playerName);
            intent.putExtra("playerIp0", players.get(selectedPlayers[0]).ip);
            intent.putExtra("playerName1", players.get(selectedPlayers[1]).playerName);
            intent.putExtra("playerIp1", players.get(selectedPlayers[1]).ip);
            intent.putExtra("playerName2", players.get(selectedPlayers[2]).playerName);
            intent.putExtra("playerIp2", players.get(selectedPlayers[2]).ip);
            intent.putExtra("playerName3", players.get(selectedPlayers[3]).playerName);
            intent.putExtra("playerIp3", players.get(selectedPlayers[3]).ip);

            //starts new activity, closes this one
            this.startActivity(intent);
            this.finish();


        } else {
            message += "Invalid selection";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(message);
                }
            });
        }

    }


}
