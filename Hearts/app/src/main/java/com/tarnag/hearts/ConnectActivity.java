package com.tarnag.hearts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ConnectActivity extends AppCompatActivity {

    Communication communication = null;
    CommunicationServer communicationServer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
    }

    public void searchServerClicked(View view) {

    }

    public void hostServerClicked(View view) {

    }
}
