package com.tarnag.hearts;

/**
 * Created by viktor on 2017. 02. 28..
 */

public class Player {
    public int position = 0;
    public final String ip;
    public final String playerName;
    public int score=0;

    Player (String ip, String playerName) {
        this.ip = ip;
        this.playerName = playerName;
    }
}
