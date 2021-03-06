package com.tarnag.hearts;

import java.util.Random;

/**
 * Created by viktor on 2017. 02. 28..
 */

public class Card {
    String type;  //X_YY
    int colour;
    int value;
    String name;
    String bmName = "x";
    boolean selected = false;
    float random;
    int point=0;



    Card (String s){  //s=X_YY
        type=s;
        String value;
        String colour;
        value=s.substring(2);
        this.value=Integer.parseInt(value);
        colour=s.substring(0,1);
        this.colour=Integer.parseInt(colour);
        addName();
        addPoint();
    }

    Card (int Colour,int Value){
        colour=Colour;
        value=Value;
        type="";
        type+=Integer.toString(Colour);
        type+="_";
        if (Value<10) type+="0";
        type+=Integer.toString(Value);
        Random rand = new Random();
        random = rand.nextFloat();
        addName();
        addPoint();
    }

    //Adds name string to display for user
    private void addName(){

        String suit = "";
        switch (colour) {
            case 1: suit = "hearts"; break;
            case 2: suit = "diamonds"; break;
            case 3: suit = "spades"; break;
            case 4: suit = "clubs"; break;
        }

        String symbol = "";
        if (value < 11) {
            symbol = Integer.toString(value);
            bmName += symbol;
        }
        switch (value) {
            case 11: symbol = "J"; bmName += "jack"; break;
            case 12: symbol = "Q"; bmName += "queen"; break;
            case 13: symbol = "K"; bmName += "king"; break;
            case 14: symbol = "A"; bmName += "ace"; break;
        }

        name = suit + " " + symbol;

        bmName += "_of_" + suit;

        if ((value > 10) && (value < 14)) {
            bmName += "2";
        }
    }

    void addPoint(){
        if (colour==1) point=1;
        if (colour==3 && value==12) point=13;
    }

}
