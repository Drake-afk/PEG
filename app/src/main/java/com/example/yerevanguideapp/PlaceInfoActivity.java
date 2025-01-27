package com.example.yerevanguideapp;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;

public class PlaceInfoActivity extends AppCompatActivity {

    private TextView name;
    private TextView info;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_info);

        Resources res = getResources();
        String[] placeInfoArray = res.getStringArray(R.array.long_info);
        String[] placeNameArray = res.getStringArray(R.array.places);


        name = findViewById(R.id.tvName);
        info = findViewById(R.id.tvInfo);
        image = findViewById(R.id.imgPlace);
        Intent intent =  getIntent();
        //int index = intent.getIntExtra("ITEM_PLACE_INDEX", -1);

        String clickedPlaceName = intent.getStringExtra("ITEM_PLACE_NAME");
        String clickedPlaceDescription;

        if (clickedPlaceName.equals("Opera Theatre")){
            clickedPlaceDescription = placeInfoArray[0];
            fitImage(image, R.drawable.opera_house_theatre);
        } else if(clickedPlaceName.equals("Arno Babajanyan Statue"))
        {
            clickedPlaceDescription = placeInfoArray[1];
            fitImage(image, R.drawable.babajanayn_monument);
        } else if(clickedPlaceName.equals("Aram Khachaturian Statue"))
        {
            clickedPlaceDescription = placeInfoArray[2];
            fitImage(image, R.drawable.aram_khachaturian_monument);
        }else if(clickedPlaceName.equals("Komitas Park"))
        {
            clickedPlaceDescription = placeInfoArray[3];
            fitImage(image, R.drawable.komitas_park);
        }else
        {
            clickedPlaceDescription = "No description yet";
        }

        name.setText(clickedPlaceName);
        info.setText(clickedPlaceDescription);
    }

    private int getImgIndex(int index)
    {
        switch(index) {
            case 0: return R.drawable.babajanayn_monument;
            case 1: return R.drawable.opera_house_theatre;
            default: return -1;
        }
    }

    private void fitImage(ImageView img, int pic)
    {
        Display screen = getWindowManager().getDefaultDisplay();
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), pic, options);

        int imgWidth = options.outWidth;
        int screenWidth = screen.getWidth();

        if(imgWidth > screenWidth)
        {
            int ratio = Math.round((float)imgWidth / (float)screenWidth);
            options.inSampleSize = ratio;
        }

        options.inJustDecodeBounds = false;
        Bitmap scaledImg = BitmapFactory.decodeResource(getResources(), pic, options);
        img.setImageBitmap(scaledImg);
    }
}
