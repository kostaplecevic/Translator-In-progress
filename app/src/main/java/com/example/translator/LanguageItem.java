package com.example.translator;

public class LanguageItem {
    private String mCountryName;
    private int mFlagImage;
    public LanguageItem(String countryName, int flagImage) {
        mCountryName = countryName;
        mFlagImage = flagImage;
    }
    public String getCountryName() {
        return mCountryName;
    }
    public int getFlagImage() {
        return mFlagImage;
    }
}