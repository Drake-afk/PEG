package com.example.yerevanguideapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ItemAdapter extends BaseAdapter {

    LayoutInflater mInflater;
    public String[] places;
    public String[] descriptions;

    public ItemAdapter(Context c, String[] p, String [] d){
        places = p;
        descriptions = d;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return places.length;
    }

    @Override
    public Object getItem(int position) {
        return places[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = mInflater.inflate(R.layout.places_lisview, null);
        TextView placesTextView = (TextView) v.findViewById(R.id.placeTextView);
        TextView descriptionTextView = (TextView) v.findViewById(R.id.descriptionTextView);

        String name = places[position];
        String desc = descriptions[position];

        placesTextView.setText(name);
        descriptionTextView.setText(desc);


        return v;
    }
}
