package com.tarnag.pikkdama;

/**
 * Created by Marci on 2016. 12. 21..
 */

public class Card {
    String type;  //X_YY
    int colour;
    int value;
    String name;

    Card (String s){  //s=X_YY
        type=s;
        String value;
        String colour;
        value=s.substring(2);
        this.value=Integer.parseInt(value);
        colour=s.substring(0,1);
        this.colour=Integer.parseInt(colour);
        addName();
    }

    Card (int Colour,int Value){
        colour=Colour;
        value=Value;
        type="";
        type+=Integer.toString(Colour);
        type+="_";
        if (Value<10) type+="0";
        type+=Integer.toString(Value);
        addName();
    }

    //Adds name string to display for user
    private void addName(){

        String suit = "";
        switch (colour) {
            case 1: suit = "Hearts"; break;
            case 2: suit = "Diamonds"; break;
            case 3: suit = "Spades"; break;
            case 4: suit = "Clubs"; break;
        }

        String symbol = "";
        if (value < 11) {
            symbol = Integer.toString(value);
        }
        switch (value) {
            case 11: symbol = "J"; break;
            case 12: symbol = "Q"; break;
            case 13: symbol = "K"; break;
            case 14: symbol = "A"; break;
        }

        name = suit + " " + symbol;
    }
}
