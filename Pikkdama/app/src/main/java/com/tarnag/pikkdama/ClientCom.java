package com.tarnag.pikkdama;

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
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Created by viktor on 2016. 12. 14..
 */

public class ClientCom {

    //activities
    ConnectActivity connectActivity = null;
    GameActivity gameActivity=null;

    //PORT USED BY THE APP
    public final int clientReceivingPort = 2016;
    public final int clientSendingPort = 2015;


    boolean running = true;

    //CONSTRUCTOR FOR MAIN ACTIVITY
    ClientCom (ConnectActivity connectActivity) {
        this.connectActivity=connectActivity;

        Log.d("ClientCom", "CLIENTCOM CREATED from gameActivity");

        //creates listening thread and starts it
        SocketListeningThread socketListeningThread = new SocketListeningThread();
        socketListeningThread.start();


    }

    //CONSTRUCTOR FOR GAME ACTIVITY
    ClientCom(GameActivity gameActivity){
        this.gameActivity=gameActivity;

        Log.d("ClientCom", "CLIENTCOM CREATED from gameActivity");

        //creates listening thread and starts it
        SocketListeningThread socketListeningThread = new SocketListeningThread();
        socketListeningThread.start();
    }

    //MESSAGE SENDING FUNCTION
    public void sendMessage(String ip,int port, String message){
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

    //PARESES THE RECEIVE MESSAGES AND TAKES ACTION
    private void parseReceivedMessage (String gotMsg, String clientIP) {

        if (gotMsg.equals("OK") && connectActivity!=null){
            connectActivity.isConnected=true;
            connectActivity.serverIP=clientIP;
            writeOnUI(connectActivity.getResources().getString(R.string.waiting_for_server)+"\n");}

        if (gotMsg.equals("START")&& connectActivity!=null){
            running=false;
            sendMessage(connectActivity.serverIP,clientSendingPort,"duvgfvefhbj");
           // try {wait(500);} catch (InterruptedException e) {
                // e.printStackTrace();}
            //new activity for the game
            Intent intent=new Intent(connectActivity,GameActivity.class);
            intent.putExtra("ServerIp",connectActivity.serverIP);
            intent.putExtra("SendingPort",clientSendingPort);
            intent.putExtra("ReceivingPort",clientReceivingPort);
            intent.putExtra("OwnName",connectActivity.ownName);
            connectActivity.startActivity(intent);
            connectActivity.finish();
        }

        if (gameActivity == null) return;

        //THESE ARE RUNNING ONLY IF STARTED FROM GAME ACTIVITY
        if (gotMsg.substring(0,5).equals("DEAL.")){
            Card card = new Card(gotMsg.substring(5));
            gameActivity.ownCards.add(card);

            //if 13 cards have been received
            if (gameActivity.ownCards.size() == 13) {
                Log.d("parseReceivedMessage", "GOT 13 CARDS");
                gameActivity.startRound();
            }
        }

        //giving 3 cards
        if (gotMsg.substring(0,7).equals("GIVING.")) {
            Log.d("parse_GIVING", gotMsg.substring(7));
            Card card = new Card(gotMsg.substring(7));
            Log.d("parse_GIVING", "added card");
            gameActivity.ownCards.add(card);
            if (gameActivity.ownCards.size() == 13) {
                gameActivity.createListView();
                gameActivity.startGame();
            }

        }

        //number of player
        if (gotMsg.substring(0,7).equals("NUMBER.")) {
            gameActivity.roundNumber = Integer.parseInt(gotMsg.substring(7));
        }


    }

    //WRITES ON UI
    private void writeOnUI (String msg) {

        //if com is accessed from ConnectActivity
        if (connectActivity != null) {
            connectActivity.writeToUI(msg);
        }
    }

    public String getIpAddress() {
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
