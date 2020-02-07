package com.example.hearts;

import android.content.Context;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PlayerReadyAdapter extends ArrayAdapter<PlayerReadyClass> {

    private Context context;
    private List<PlayerReadyClass>  status;

    static class PlayerViewHolder {

        TextView PlayerNameTextView;
        TextView PlayerReadyTextView;
    }

    PlayerReadyAdapter(Context context, List<PlayerReadyClass> status){
        super(context,R.layout.listview_item_player_ready,  status);
        this.context=context;
        this.status=status;
    }
    private int lastPosition = -1;

    @Override
    public int getCount() {
        return this.status.size();
    }

    @Nullable
    @Override
    public PlayerReadyClass getItem(int position) {
        return this.status.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PlayerViewHolder viewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.listview_item_player_ready, parent, false);
            viewHolder = new PlayerViewHolder();
          //  viewHolder.fruitImg = (ImageView) row.findViewById(R.id.fruitImg);
            viewHolder.PlayerNameTextView = (TextView) row.findViewById(R.id.playerNameTextView);
            viewHolder.PlayerReadyTextView = (TextView) row.findViewById(R.id.readyTextView);
            row.setTag(viewHolder);
        } else {
            viewHolder = (PlayerViewHolder) row.getTag();
        }
        PlayerReadyClass fruit = getItem(position);
    //    viewHolder.fruitImg.setImageResource(fruit.getFruitImg());
        viewHolder.PlayerNameTextView.setText(fruit.getName());
        if(fruit.getReady()){
            viewHolder.PlayerReadyTextView.setText("Ready");
        }else {
            viewHolder.PlayerReadyTextView.setText("Not Ready");
        }
       // viewHolder.calories.setText(fruit.getCalories());
        return row;
    }




}
