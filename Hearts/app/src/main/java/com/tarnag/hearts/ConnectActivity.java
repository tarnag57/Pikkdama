package com.tarnag.hearts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ConnectActivity extends AppCompatActivity {

    Communication communication = null;
    CommunicationServer communicationServer = null;

    boolean isConnected = false;

    //UI ELEMENTS

    EditText editName;
    TextView textViewStatus;

    //chosen name of the user
    String ownName = "";

    //message on textViewStatus
    String status = "";

    String ip;
    String subIp;

    String serverIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
    }

    public void searchServerClicked(View view) {
        ownName = editName.getText().toString();

        //check if name is empty
        if (ownName.equals("")) {
            writeToUI(getResources().getString(R.string.must_enter_name)+"\n");
            return;
        }
    }
        Communication communication=new  Communication(this);
        //searching for server

        ip = communication.getIPAddress();
        subIp = communication.getSubIP(ip);

        for (int i = 0; i < 256; i++){
            communication.sendMessage(subIp + "." + Integer.toString(i), "NAME." + ownName);
            //slows down traffic
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }

    public void hostServerClicked(View view) {

    }

    public void writeToUI(String msg) {

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
