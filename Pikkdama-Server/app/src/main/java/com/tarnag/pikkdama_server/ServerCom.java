package com.tarnag.pikkdama_server;

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
import java.util.HashMap;

import static java.lang.Thread.sleep;

/**
 * Created by viktor on 2016. 12. 15..
 */

public class ServerCom {

    //activities
    private ConnectionActivity connectionActivity = null;
    private GameActivity gameActivity = null;


    boolean running = true;

    int receivedGivingCard=0;

    String[][] givingCards=new String[4][3];
    int[] receivedGivingCards=new int[4];

    int roundNumber=1;

    //PORT USED BY THE APP
    public final int serverReceivingPort = 2015;
    public final int serverSendingPort = 2016;

    ServerCom (ConnectionActivity activity) {
        connectionActivity = activity;

        //creates listening thread and starts it
        SocketListeningThread socketListeningThread = new SocketListeningThread();
        socketListeningThread.start();

        for (int i=0;i<4;i++){
            receivedGivingCards[i]=0;
        }
    }

    ServerCom (GameActivity activity) {
        gameActivity = activity;

        //creates listening thread and starts it
        SocketListeningThread socketListeningThread = new SocketListeningThread();
        socketListeningThread.start();
    }

    //setting running to false, to stop current serverSocket
    public void closingServerSocket() {
        running = false;
    }

    //CLASS FOR SENDING SOCKETS
    private class SocketSendingThread extends Thread{
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
            } catch (Exception e) {
                //e.printStackTrace();
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
    private class SocketListeningThread extends Thread {
        ServerSocket serverSocket;
        Socket socket;
        DataInputStream dataInputStream;
        String gotMsg; //msg from socket

        //actual thread
        public void run() {

            Log.d("SocketListeningThread", "SocketServerThread started");

            //creating socket
            try {
                serverSocket = new ServerSocket(serverReceivingPort);
                Log.d("SocketServerThread","serverSocket created");
            } catch (IOException e) {
                e.printStackTrace();
            }


            while(running){
                try {

                    //waits until socket is accepted
                    socket = serverSocket.accept();

                    //gets the ip of sender
                    String clientIP = socket.getInetAddress().getHostAddress();

                    //gets data out of socket
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    gotMsg = dataInputStream.readUTF();

                    Log.d("SocketServerThread", "Message from " + clientIP + " says: " + gotMsg);

                    //parses the received message and takes action
                    parseReceivedMessage(gotMsg, clientIP);

                } catch (IOException e) {
                    //  e.printStackTrace();
                }

                //closing socket and IOStreams
                finally{
                    if (serverSocket != null && !running){
                        Log.d("ServerListeningThread", "closing serverSocket");
                        try{
                            serverSocket.close();
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

    private void parseReceivedMessage(String msg, String ip) {

        //if used from connectionActivity
        if (connectionActivity != null) {

            //if it was a name
            if (msg.length() > 4) {
                if (msg.substring(0,4).equals("NAME")) {

                    //determine received name
                    String newPlayerName = msg.substring(5);
                    Player newPlayer = new Player(ip, newPlayerName);

                    //responds to server
                    sendMessage(ip, serverSendingPort, "OK");

                    //puts it to listView in connectionActivity
                    connectionActivity.putToList(newPlayer);

                }
            }
            return;
        }

        //if used from gameActivity
        if (gameActivity != null) {

            //used in giving mode
            if (gameActivity.isgiving){

                //giving
                if (msg.substring(0,7).equals("GIVING.")){
                    receivedGivingCard++;


                    int sender = -1;
                    for (int i = 0; i < 4; i++){
                        if (gameActivity.players[i].ip.equals(ip)) sender = i;
                    }

                    givingCards[sender][receivedGivingCards[sender]] = msg;
                    receivedGivingCards[sender]++;

                    //if all the cards were received resets everything and sends them back to players
                    if (receivedGivingCard == 12){
                        receivedGivingCard = 0;
                        for (int i = 0; i < 4; i++){
                            receivedGivingCards[i] = 0;
                        }

                        gameActivity.isgiving = false;
                        sendGivingCards();
                    }

                }
            }
            //if not giving (e.g. actual game)
            else {

                //checking for clubs2
                if (msg.equals("CLUBS2")) {
                    gameActivity.startingPlayer = playerFromIp(ip);
                    gameActivity.game();
                }

                //if card was played
                if (msg.length() > 4) {
                    if (msg.substring(0,5).equals("PLAY.")) {
                        Card playedCard = new Card(msg.substring(5));
                        gameActivity.playedACard(playedCard);
                    }
                }
            }
        }
    }


    public void sendGivingCards(){

        //determining receiver mod 4
        int givingWhere = 0;
        switch (roundNumber%4){
            case 1:  {givingWhere = 1; break;}
            case 2: {givingWhere = 3; break;}
            case 3: {givingWhere = 2; break;}
        }

        //sending cards to receivers
        for (int i = 0; i < 4; i++){
            int receiver = (i + givingWhere) % 4;
            for (int j = 0; j < 3; j++){
                sendMessage(gameActivity.players[receiver].ip, serverSendingPort, givingCards[i][j]);
            }
            try {
                sleep(100);
            } catch (InterruptedException e) {
               // e.printStackTrace();
            }
        }
    }

    private int playerFromIp(String ip) {
        for (int i = 0; i < 4; i++) {
            if (gameActivity.players[i].ip.equals(ip)) {
                return i;
            }
        }

        return -1;
    }

    public void sendMessageToNthPlayer(int i, String msg) {
        if (i > 3) return;
        sendMessage(gameActivity.players[i].ip, serverSendingPort, msg);
    }

    //sends message (msg) to dstIP:socketClientPort
    public void sendMessage (String destIP, int socketClientPort, String msg) {
        //starts SocketSendingThread
        SocketSendingThread socketSendingThread = new SocketSendingThread(destIP, socketClientPort, msg);
        socketSendingThread.start();
        Log.d("sendMessage", "Sending message to " + destIP + ":" + socketClientPort + " message: " + msg);
    }

    //Returns ip of current device
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
            e.printStackTrace();
        }
        return ip;
    }

    //Returns subIP of ip address (i.e. "192.168.1.78" -> "192.168.1.")
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

    //Returns last bit of IP (i.e. "192.168.1.78" -> "78")
    public String getLastBitIP(String ip)
    {

        //searches for subip (i.e. 192.168.1.)
        String subIP1 = ip.substring(0, ip.indexOf('.'));
        String subrem1 = ip.substring(ip.indexOf('.')+1);
        String subIP2 = subrem1.substring(0, subrem1.indexOf('.'));
        String subrem2 = subrem1.substring(subrem1.indexOf('.')+1);
        String subIP3 = subrem2.substring(0, subrem2.indexOf('.'));
        String subIP = subIP1 +"."+ subIP2 + "." + subIP3;
        String subrem3 = subrem2.substring(subrem2.indexOf('.')+1);

        return subrem3;

    }

}
