package com.tarnag.hearts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ConnectActivity extends AppCompatActivity {

    Communication communication = null;
    CommunicationServer communicationServer = null;

    boolean isConnected = false;
    boolean isServer = false;

    //UI ELEMENTS
    EditText editName;
    TextView textViewStatus;
    ListView listView;
    Button startButton;

    //chosen name of the user
    String ownName = "";

    //message on textViewStatus
    String status = "";

    String ip;
    String subIp;

    String serverIP;

    //contains players
    List<Player> players = new ArrayList<>();
    PlayerAdapter playerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        listView = (ListView) findViewById(R.id.listView);
        startButton = (Button) findViewById(R.id.startButton);
        textViewStatus = (TextView) findViewById(R.id.status);
        editName = (EditText) findViewById(R.id.editText);
    }

    public void searchServerClicked(View view) {
        if (isConnected) return;
        if (isServer) return;

        writeToUI(getResources().getString(R.string.search_for_server));
        ownName = editName.getText().toString();

        //check if name is empty
        if (ownName.equals("")) {
            writeToUI(getResources().getString(R.string.must_enter_name) + "\n");
            return;
        }
        communication = new Communication(this);

        //searching for server

        ip = communication.getIPAddress();
        subIp = communication.getSubIP(ip);

        for (int i = 0; i < 256; i++) {
            communication.sendMessage(subIp + "." + Integer.toString(i), "NAME." + ownName);
            //slows down traffic
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    public void hostServerClicked(View view) {
        Log.d("hostServerClicked", "started");

        ownName = editName.getText().toString();

        if (ownName.equals("")) {
            writeToUI(getResources().getString(R.string.must_enter_name) + "\n");
            return;
        }

        isServer = true;
        writeToUI(getResources().getString(R.string.hosting_server));
        Log.d("hostServerClicked", "Written to ui");
        communicationServer = new CommunicationServer(this);
        listView.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.INVISIBLE);
        Player player = new Player(communication.getIPAddress(), ownName);
        putToList(player);
    }

    public void writeToUI(final String msg) {
        status = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewStatus.setText(status);
            }
        });
    }

    public void putToList(Player player) {
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

    public void startGameOnClick (View view) {

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

            //pings selected devices
            for (int i = 0; i < 4; i++) {
                communicationServer.sendMessage(players.get(selectedPlayers[i]).ip, "START");
            }

            //closing serverSocket
            communicationServer.closingServerSocket();

            //puts extras into intent and starts new activity
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("isServer", isServer);
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
            final String message = "Invalid selection";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewStatus.setText(message);
                }
            });
        }

    }

    //STARTS GameActivity IF DEVICES FUNCTIONS AS CLIENT
    public void startIntentFromClient() {
        communication.sendMessage(serverIP, "duvgfvefhbj");
        //new activity for the game
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("ServerIp", serverIP);
        intent.putExtra("OwnName", ownName);
        this.startActivity(intent);
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
