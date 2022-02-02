package com.example.translator;

public class MyTranslation {
    private String date;
    private String original_text;
    private String translated_text;
    private int idTranslation;

    public MyTranslation(String date, String original_text, String translated_text, int idTranslation){
        this.date = date;
        this.original_text = original_text;
        this.translated_text = translated_text;
        this.idTranslation = idTranslation;
    }

    public String getDate() {return date;}

    public void setDate(String date) {this.date = date;}

    public String getOriginal_text() {return original_text;}

    public void setOriginal_text(String original_text) {this.original_text = original_text;}

    public String getTranslated_text() {return translated_text;}

    public void setTranslated_text(String translated_text) {this.translated_text = translated_text;}

    public int getIdTranslation() {return idTranslation;}

    public void setIdTranslation(int idTranslation) {this.idTranslation = idTranslation;}

    @Override
    public String toString() {
        return "Translation [id=" + idTranslation + ", date=" + date + ",translatedtext" +translated_text + ",originaltext" + original_text + "]";
    }
}
