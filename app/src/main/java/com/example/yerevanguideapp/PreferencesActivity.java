package com.example.yerevanguideapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class PreferencesActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "userPreferences";
    public static final String PREFS_MUSEUMS = "museums";
    public static final String PREFS_GALLERIES = "galleries";
    public static final String PREFS_PARKS = "parks";
    public static final String PREFS_MONUMENTS = "monuments";
    public static final String PREFS_OTHERS = "others";

    private CheckBox chBxMuseums;
    private CheckBox chBxMonuments;
    private CheckBox chBxGalleries;
    private CheckBox chBxParks;
    private CheckBox chBxOthers;

    static private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        chBxGalleries = (CheckBox) findViewById(R.id.chBxGalleries);
        chBxMonuments = (CheckBox) findViewById(R.id.chBxMonuments);
        chBxMuseums = (CheckBox) findViewById(R.id.chBxMuseums);
        chBxParks = (CheckBox) findViewById(R.id.chBxParks);
        chBxOthers = (CheckBox) findViewById(R.id.chBxOthers);


        loadPrefs();

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Old way of preferences (1)
                /*UserPreferences newUserPrefs = new UserPreferences();
                newUserPrefs.setGalleriesChecked(chBxGalleries.isChecked());
                newUserPrefs.setMonumentsChecked(chBxMonuments.isChecked());
                newUserPrefs.setParksChecked(chBxParks.isChecked());
                newUserPrefs.setMuseumsChecked(chBxMuseums.isChecked());
                newUserPrefs.setOthersChecked(chBxOthers.isChecked());
                MainActivity.userPreferences = newUserPrefs;*/
                //end(1)
                setPrefs();
                Toast.makeText(PreferencesActivity.this, "Preferences saved", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadPrefs()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        chBxGalleries.setChecked(sharedPreferences.getBoolean(PREFS_GALLERIES, false));
        chBxMonuments.setChecked(sharedPreferences.getBoolean(PREFS_MONUMENTS, false));
        chBxMuseums.setChecked(sharedPreferences.getBoolean(PREFS_MUSEUMS, false));
        chBxParks.setChecked(sharedPreferences.getBoolean(PREFS_PARKS, false));
        chBxOthers.setChecked(sharedPreferences.getBoolean(PREFS_OTHERS, false));

    }
    private void setPrefs()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putBoolean(PREFS_GALLERIES, chBxGalleries.isChecked());
        sharedPreferencesEditor.putBoolean(PREFS_MUSEUMS, chBxMuseums.isChecked());
        sharedPreferencesEditor.putBoolean(PREFS_MONUMENTS, chBxMonuments.isChecked());
        sharedPreferencesEditor.putBoolean(PREFS_PARKS, chBxParks.isChecked());
        sharedPreferencesEditor.putBoolean(PREFS_OTHERS, chBxOthers.isChecked());
        sharedPreferencesEditor.apply();
    }
}
