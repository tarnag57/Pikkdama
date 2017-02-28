package com.tarnag.hearts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viktor on 2017. 02. 28..
 */

public class PlayerAdapter extends ArrayAdapter<Player> {
    private Context context;
    List<Spinner> spinners = new ArrayList<>();

    PlayerAdapter (Context context, Player[] res) {
        super(context, R.layout.list_item_player, res);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.list_item_player, parent, false);

        //get the player and its name
        Player singlePlayer = getItem(position);
        String playerName = singlePlayer.playerName;

        TextView textView = (TextView) customView.findViewById(R.id.textView);
        Spinner spinner = (Spinner) customView.findViewById(R.id.spinner);

        spinners.add(position, spinner);

        textView.setText(playerName);

        //spinner types
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.Player, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //aligning spinner
        spinner.setAdapter(adapter);

        return customView;
    }
}
