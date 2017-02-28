package com.tarnag.hearts;

/**
 * Created by viktor on 2017. 02. 28..
 */

public class CommunicationServer {

    //Activities
    ConnectActivity connectActivity;

    //PORT USED BY APPS
    public final int clientReceivingPort = 2016;
    public final int clientSendingPort = 2015;

    //Communication
    Communication communication;

    boolean running = true;


}
