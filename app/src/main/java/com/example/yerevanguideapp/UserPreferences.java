package com.example.yerevanguideapp;

public class UserPreferences{
    public boolean isMuseumsChecked;
    public boolean isGalleriesChecked;
    public boolean isMonumentsChecked;
    public boolean isParksChecked;
    public boolean isOthersChecked;

    public UserPreferences()
    {
        isMuseumsChecked = true;
        isGalleriesChecked = true;
        isMonumentsChecked = true;
        isParksChecked = true;
        isOthersChecked = true;
    }

    public void setGalleriesChecked(boolean galleriesChecked) {
        isGalleriesChecked = galleriesChecked;
    }

    public void setMonumentsChecked(boolean monumentsChecked) {
        isMonumentsChecked = monumentsChecked;
    }

    public void setMuseumsChecked(boolean museumsChecked) {
        isMuseumsChecked = museumsChecked;
    }

    public void setOthersChecked(boolean othersChecked) {
        isOthersChecked = othersChecked;
    }

    public void setParksChecked(boolean parksChecked) {
        isParksChecked = parksChecked;
    }
}