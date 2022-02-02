package com.example.translator;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final String  DB_NAME = "databaseTranslationApp";
    private static final int  DB_VERSION = 1;

    public DatabaseManager(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);}

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {  updateDatabase(sqLiteDatabase, 0, DB_VERSION);}

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) { updateDatabase(sqLiteDatabase, 0, DB_VERSION); }

    private void updateDatabase(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion){
        if(oldVersion<1){
            sqLiteDatabase.execSQL("CREATE TABLE TRANSLATION(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + "DATE TEXT NOT NULL, " + "ORIGINAL_TEXT TEXT NOT NULL, " + "TRANSLATED_TEXT TEXT NOT NULL);");
            sqLiteDatabase.execSQL("CREATE TABLE LANGUAGES(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + "LANG_TAG TEXT NOT NULL, " + "LANG_NAME TEXT NOT NULL, " + "IMAGE_RESOURCE_ID INT);");
            insertLanguage(sqLiteDatabase,"en", "English", R.drawable.flag_uk);
            insertLanguage(sqLiteDatabase,"sr", "Serbian", R.drawable.flag_serbia);
            insertLanguage(sqLiteDatabase,"es", "Spanish", R.drawable.flag_spain);
            insertLanguage(sqLiteDatabase,"it", "Italian", R.drawable.flag_italy);
            insertLanguage(sqLiteDatabase,"fr", "French", R.drawable.flag_fance);
            insertLanguage(sqLiteDatabase,"zh-CN", "Chinese", R.drawable.flag_china);
            insertLanguage(sqLiteDatabase,"ru", "Russian", R.drawable.flag_russia);
            insertLanguage(sqLiteDatabase,"de", "German", R.drawable.flag_germany);
            insertLanguage(sqLiteDatabase,"hi", "Hindi", R.drawable.flag_india);
            insertLanguage(sqLiteDatabase,"ar", "Arabic", R.drawable.flag_arabic);
            insertLanguage(sqLiteDatabase,"pt-PT", "Portuguese", R.drawable.flag_portugal);
            insertLanguage(sqLiteDatabase,"bn", "Bengali", R.drawable.flag_bengali);
            insertLanguage(sqLiteDatabase,"ja", "Japanese", R.drawable.flag_fjapan);
        }
        if(oldVersion <2){ }

    }
    public static void insertLanguage(SQLiteDatabase sqLiteDatabase, String language_tag, String language_name, int resourceId){
        ContentValues langValues = new ContentValues();
        langValues.put("LANG_TAG", language_tag);
        langValues.put("LANG_NAME", language_name);
        langValues.put("IMAGE_RESOURCE_ID", resourceId);
        sqLiteDatabase.insert("LANGUAGES", null, langValues);
    }
    public static void insertTranslation(SQLiteDatabase sqLiteDatabase, String date,String original_text, String translated_text){
        ContentValues translationValues = new ContentValues();
        translationValues.put("DATE", date);
        translationValues.put("ORIGINAL_TEXT", original_text);
        translationValues.put("TRANSLATED_TEXT", translated_text);
        sqLiteDatabase.insert("TRANSLATION", null, translationValues);
    }

}