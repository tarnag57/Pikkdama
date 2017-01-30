package com.tarnag.pikkdama;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ConnectActivity extends AppCompatActivity {

    boolean isConnected=false;

    //UI ELEMENTS

    EditText editName;
    TextView textViewStatus;

    //clientCom for com purposes
    ClientCom clientCom;

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

        //get ui el
        editName = (EditText) findViewById(R.id.editName);
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);

        //creates ClientCom for com purposes
        clientCom = new ClientCom(this);


    }

    protected void buttonConnectClicked(View view) {

        //checks if button was already pressed
        if (isConnected) return;

        //gets own name
        ownName = editName.getText().toString();

        //check if name is empty
        if (ownName.equals("")) {
            writeToUI(getResources().getString(R.string.must_enter_name)+"\n");
            return;
        }

        ip = clientCom.getIPAddress();
        subIp = clientCom.getSubIP(ip);

        for (int i = 0; i < 256; i++){
            clientCom.sendMessage(subIp + "." + Integer.toString(i), clientCom.clientSendingPort, "NAME." + ownName);
            //slows down traffic
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }




    }

    //writes on connectActivity thread
    public void writeToUI (final String msg) {
        status += msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewStatus.setText(status);
            }
        });
    }
}
