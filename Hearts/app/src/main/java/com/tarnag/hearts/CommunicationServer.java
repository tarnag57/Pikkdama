package com.tarnag.hearts;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by viktor on 2017. 02. 28..
 */

public class CommunicationServer {

    //Activities
    ConnectActivity connectActivity = null;
    GameActivity gameActivity = null;

    //PORT USED BY APPS
    private final int serverReceivingPort = 2015;
    private final int serverSendingPort = 2016;

    //Communication
    Communication communication;
    String ownip;

    boolean running = true;
    CommunicationServer (ConnectActivity activity) {
        connectActivity = activity;
        ownip = getIpAddress();

        //creates listening thread and starts it
        SocketListeningThread socketListeningThread = new SocketListeningThread();
        socketListeningThread.start();

    }

    private void parseReceivedMessage(String msg, String ip) {
        //if used from connectionActivity
        if (connectActivity != null) {

            //if it was a name
            if (msg.length() > 4) {
                if (msg.substring(0, 4).equals("NAME")) {

                    //determine received name
                    String newPlayerName = msg.substring(5);
                    Player newPlayer = new Player(ip, newPlayerName);

                    //responds to server
                    sendMessage(ip, "OK");

                    //puts it to listView in connectionActivity
                    connectActivity.putToList(newPlayer);

                }
            }
            return;
        }
    }

    //MESSAGE SENDING FUNCTION
    public void sendMessage(String ip, String message) {
        if (ip.equals(ownip)) {
            if (connectActivity != null) {
                connectActivity.communication.parseReceivedMessage(message, ip);
            } else if(gameActivity != null) {
                //TODO GAMEACTIVITY
            }
            return;
        }
        int port = serverSendingPort;
        Log.d("sendMessage", "Sending message to " + ip + ":" + port + " says: " + message);
        SocketSendingThread socketSendingThread=new SocketSendingThread(ip,port,message);
        socketSendingThread.start();
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

    //setting running to false, to stop current serverSocket
    void closingServerSocket() {
        running = false;
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

}
