package com.example.translator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SavedWords extends AppCompatActivity {

    ArrayList<MyTranslation> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_words);

        getSupportActionBar().hide();

        DatabaseManager helper = new DatabaseManager(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cursor_question = db.query("WORDS", new String[]{"DATE","TRANSLATED_WORD","TRANSLATED_LANG","ID"},null,null,null,null,null);

        while(cursor_question.moveToNext()){
            String date = cursor_question.getString(0);
            String translated_word = cursor_question.getString(1);
            String translated_lang = cursor_question.getString(2);
            int id_word = cursor_question.getInt(3);

            //NAPRAVI POREDJENJE DATUMA DA NE BUDU STARIJI OD 10 DANA

            MyTranslation myTranslation = new MyTranslation(date,translated_word,translated_lang,id_word);
            list.add(myTranslation);
            Collections.sort(list, new Comparator<MyTranslation>(){
                public int compare(MyTranslation obj1, MyTranslation obj2) {
                    // ## Ascending order
                    //return Integer.valueOf(obj1.getIdTranslation()).compareTo(obj2.getIdTranslation());

                    // ## Descending order
                    return Integer.valueOf(obj2.getIdTranslation()).compareTo(obj1.getIdTranslation());
                }
            });

            ArrayList<MyTranslation> lista = new ArrayList<MyTranslation>();
            MyTranslationAdapter adapter = new MyTranslationAdapter(this,list);
            adapter.notifyDataSetChanged();
            ListView listViewClan = findViewById(R.id.myTranslationList);
            listViewClan.setAdapter(adapter);
            // Closing cursor and database usage.
            //cursor_question.close();
            db.close();

        }

        TextView textView = findViewById(R.id.optionalText);
        if(list.isEmpty()){
            textView.setVisibility(View.VISIBLE);
        }else{
            textView.setVisibility(View.INVISIBLE);
        }
    }

    private void deleteMyTranslation(SQLiteDatabase sqLiteDatabase, int id){
        ContentValues cv = new ContentValues();
        String id_string = String.valueOf(id);
        sqLiteDatabase.delete("WORDS","id=?",new String[]{id_string});
    }

    public void deleteTranslation(View view) {
        DatabaseManager helper = new DatabaseManager(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        View parentRow = (View) view.getParent();
        ListView listView = (ListView) parentRow.getParent();
        final int position = listView.getPositionForView(parentRow);

        MyTranslation mt = list.get(position);
        int translation_id = mt.getIdTranslation();

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pop_up_confirm, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        Button confirm = (Button) popupView.findViewById(R.id.confirm_popup);
        confirm.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteMyTranslation(db, translation_id);
                popupWindow.dismiss();
                db.close();
                recreate();
            }
        });

        Button cancel = (Button) popupView.findViewById(R.id.cancel_popup);
        cancel.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                db.close();
            }
        });
    }

    public void backArrow(View view) {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }
}