package com.tarnag.pikkdama_server;

/**
 * Created by viktor on 2016. 12. 19..
 */

public class Player {

    public int position = 0;
    public final String ip;
    public final String playerName;

    Player (String ip, String playerName) {
        this.ip = ip;
        this.playerName = playerName;
    }

}
