package com.tarnag.pikkdama;

/**
 * Created by Marci on 2016. 12. 21..
 */

public class Card {
    String type;  //X_YY
    int colour;
    int value;

    Card (String s){  //s=X_YY
        type=s;
        String value;
        String colour;
        value=s.substring(2);
        this.value=Integer.parseInt(value);
        colour=s.substring(0,1);
        this.colour=Integer.parseInt(colour);
    }

    Card (int Colour,int Value){
        colour=Colour;
        value=Value;
        type="";
        type+=Integer.toString(Colour);
        type+="_";
        if (Value<10) type+="0";
        type+=Integer.toString(Value);

    }
}
