package com.tarnag.hearts;

import android.content.Intent;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

/**
 * Created by viktor on 2017. 02. 28..
 */

public class Communication {

    //activities
    ConnectActivity connectActivity = null;
    GameActivity gameActivity = null;

    //PORT USED BY APPS
    private final int clientReceivingPort = 2016;
    private final int clientSendingPort = 2015;

    boolean running = true;

    String ownip;

    //CONSTRUCTOR FOR MAIN ACTIVITY
    Communication (ConnectActivity connectActivity) {
        this.connectActivity=connectActivity;
        ownip = getIPAddress();
        Log.d("ClientCom", "CLIENTCOM CREATED from gameActivity");

        //creates listening thread and starts it
        SocketListeningThread socketListeningThread = new SocketListeningThread();
        socketListeningThread.start();
    }

    Communication (GameActivity activity) {
        this.gameActivity=activity;
        ownip = getIPAddress();
        Log.d("ClientCom", "CLIENTCOM CREATED from gameActivity");

        //creates listening thread and starts it
        SocketListeningThread socketListeningThread = new SocketListeningThread();
        socketListeningThread.start();
    }


    //PARESES THE RECEIVE MESSAGES AND TAKES ACTION
    public void parseReceivedMessage (String gotMsg, String clientIP) {
        if (connectActivity!=null){
            if (gotMsg.equals("OK")) {
                connectActivity.serverIP=clientIP;
                connectActivity.writeToUI(connectActivity.getResources().getString(R.string.waiting_for_server)+"\n");
                return;
            }
            if (gotMsg.equals("START")){
               connectActivity.startIntentFromClient();
            }
            return;
        }

        if (gameActivity!=null){
            if (gotMsg.contains("Player")){
                if (gotMsg.substring(0,11).equals("PlayerName.")){
                    String msg=gotMsg.substring(11);
                    int position=msg.charAt(0)-'0';
                    String name=msg.substring(2);
                    gameActivity.gamePanel.players[position]=new Player(null,name);
                    gameActivity.gamePanel.players[position].position=position;
                    return;
                }

                if (gotMsg.substring(0,12).equals("PlayerScore.")){
                    String msg=gotMsg.substring(12);
                    int position=msg.charAt(0)-'0';
                    gameActivity.gamePanel.players[position].score=Integer.parseInt(msg.substring(2));
                    return;
                }
            }
            if (gotMsg.contains("POSITION.")) {
                if (gotMsg.substring(0,9).equals("POSITION.")){
                    gameActivity.gamePanel.ownPosition=gotMsg.charAt(9)-'0';
                }
            }

            if (gotMsg.contains("ROUNDNUMBER.")){
                if (gotMsg.substring(0,12).equals("ROUNDNUMBER.")){
                    gameActivity.roundNumber=Integer.parseInt(gotMsg.substring(12));
                    return;
                }
            }

            if (gotMsg.contains("DEAL.")){
                if (gotMsg.substring(0,5).equals("DEAL.")){
                    gameActivity.gamePanel.cards.add(new Card(gotMsg.substring(5)));
                    Collections.sort(gameActivity.gamePanel.cards,new Comparator<Card>() {
                        @Override
                        public int compare(Card card, Card t1) {//sorting
                            if ((card.colour>t1.colour)||((card.colour==t1.colour) && (card.value<t1.value))) return 1;
                            return -1;
                        }});
                    if (gameActivity.gamePanel.cards.size()==13){
                        gameActivity.gamePanel.numOfCards[0]=13;
                        gameActivity.gamePanel.numOfCards[1]=13;
                        gameActivity.gamePanel.numOfCards[2]=13;
                        gameActivity.gamePanel.numOfCards[3]=13;
                        gameActivity.gamePanel.draw();
                    }
                    return;
                }
            }
        }


    }

    //MESSAGE SENDING FUNCTION
    public void sendMessage(String ip, String message) {
        if (connectActivity.isServer) {
            if (ip.equals(ownip)) {
                if (connectActivity != null) {
                    connectActivity.communicationServer.parseReceivedMessage(message, ip);
                } else if (gameActivity != null) {
                    gameActivity.communicationServer.parseReceivedMessage(message, ip);
                }
                return;
            }
        }


        int port = clientSendingPort;
        Log.d("sendMessage", "Sending message to " + ip + ":" + port + " says: " + message);
        SocketSendingThread socketSendingThread=new SocketSendingThread(ip,port,message);
        socketSendingThread.start();
    }

    //CLASS FOR SENDING SOCKETS
    public class SocketSendingThread extends Thread{
        private String destIP;
        private DataOutputStream dataOutputStream;
        private String msg;
        private int socketClientPORT;
        private Socket socket;

        SocketSendingThread(String destIP, int socketClientPort, String msg) {
            this.destIP = destIP;
            this.msg = msg;
            this.socketClientPORT = socketClientPort;
        }

        @Override
        public void run(){
            // Log.d("Android", "Sending thread started");

            // send request to the device
            try {
                socket = new Socket(destIP, socketClientPORT);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(msg);


                Log.d("SocketServerThread.run", msg +" sent to: "+destIP+"\n");

                //tons of exception handling
            } catch (UnknownHostException e) {
                //e.printStackTrace();
            } catch (IOException e) {
                //e.printStackTrace();

                //closes socket
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // e.printStackTrace();
                    }
                }
            }

        }
    }

    //THREADS THAT LISTENS TO OTHER'S CONNECTION
    private class SocketListeningThread extends Thread {  //BACZA FÜREDI KIRÁLY
        ServerSocket serverSocket;
        Socket socket;
        DataInputStream dataInputStream;
        String gotMsg; //msg from socket

        //actual thread
        public void run() {

            Log.d("SocketListeningThread", "SocketServerThread started");

            //creating socket
            try {
                serverSocket = new ServerSocket(clientReceivingPort);
                Log.d("SocketServerThread","serverSocket created");
            } catch (IOException e) {
                //e.printStackTrace();
            }


            while(running){
                try {

                    //waits until socket is accepted
                    socket = serverSocket.accept();

                    //gets the ip of sender
                    String clientIP = socket.getInetAddress().getHostAddress();

                    //writes it to ui thread
                    //writeOnUI("New socket accepted from: " + clientIP + "\n");

                    //gets data out of socket
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    gotMsg = dataInputStream.readUTF();

                    //prints out to ui
                    //writeOnUI("Message: " + gotMsg + "\n");

                    Log.d("SocketServerThread", "Message from " + clientIP + " says: " + gotMsg);

                    //parses the received message and takes action
                    parseReceivedMessage(gotMsg, clientIP);

                } catch (IOException e) {
                    //  e.printStackTrace();
                }

                //closing socket and IOStreams
                finally{
                    if (serverSocket!=null && !running){
                        try{
                            serverSocket.close();
                            Log.d("serverSocket","closed in finally");
                        } catch (IOException e){
                            //e.printStackTrace
                        }
                    }
                    if( socket!= null){
                        try {
                            socket.close();
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }
                    }

                    if( dataInputStream!= null) {
                        try {
                            dataInputStream.close();
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    //WRITES ON UI
    private void writeOnUI (String msg) {

        //if com is accessed from ConnectActivity
        if (connectActivity != null) {
            connectActivity.writeToUI(msg);
        }
    }

    //get own ip address
    public String getIPAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumerationNetworkInterface =
                    NetworkInterface.getNetworkInterfaces();
            while (enumerationNetworkInterface.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNetworkInterface.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            //e.printStackTrace();
        }
        return ip;
    }

    public String getSubIP(String ip)
    {

        //searches for subip (i.e. 192.168.1.)
        String subIP1 = ip.substring(0, ip.indexOf('.'));
        String subrem1 = ip.substring(ip.indexOf('.')+1);
        String subIP2 = subrem1.substring(0, subrem1.indexOf('.'));
        String subrem2 = subrem1.substring(subrem1.indexOf('.')+1);
        String subIP3 = subrem2.substring(0, subrem2.indexOf('.'));
        String subIP = subIP1 +"."+ subIP2 + "." + subIP3;

        return subIP;

    }
}
