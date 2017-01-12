package com.tarnag.pikkdama;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marci on 2016. 12. 21..
 */

public class CardAdapter extends ArrayAdapter<Card> {
    private Context context;
    public List<CheckBox> checkBoxes = new ArrayList<>();

    CardAdapter (Context context, Card [] res) {
        super(context, R.layout.card_list, res);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.card_list, parent, false);

        Card singleCard = getItem(position);
        String cardType = singleCard.type;

        TextView textView = (TextView) customView.findViewById(R.id.textView);
        textView.setText(cardType);

        CheckBox checkBox = (CheckBox) customView.findViewById(R.id.checkBox);
        checkBoxes.add(checkBox);

        return customView;
    }
}
