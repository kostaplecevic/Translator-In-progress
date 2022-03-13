package com.example.translator;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;


@RequiresApi(api = Build.VERSION_CODES.R)
public class TranslationPage extends AppCompatActivity {

    private EditText originalEditText;
    private EditText translatedEditText;

    private String originalText;
    private String translatedText;

    TextToSpeech t1;
    ConstraintLayout c1;

    Translate translate;
    private boolean connected;

    private String originalLanguage = "en";
    private String translateLanguage = "sr";

    private Spinner spinner;
    private Spinner spinner2;
    private static final ArrayList<String> items = new ArrayList<>();
    private static final ArrayList<String> items2 = new ArrayList<>();
    private static final ArrayList<Integer> items3 = new ArrayList<>();
    private String filepath = null;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;

    private ArrayList<LanguageItem> mCountryList = new ArrayList<>();
    private LanguageAdapter mAdapter;

    private int line_starter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_page);

        getSupportActionBar().hide();

        DatabaseManager helper = new DatabaseManager(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("LANGUAGES", new String[]{"LANG_TAG","LANG_NAME","IMAGE_RESOURCE_ID","ID"}, null, null, null, null, null);

        while (cursor.moveToNext()){
            String language_tag = cursor.getString(0);
            String language_name = cursor.getString(1);
            int image_id = cursor.getInt(2);
            int language_id = cursor.getInt(3);

            items.add(language_name);
            items2.add(language_tag);
            items3.add(image_id);

            mCountryList.add(new LanguageItem(language_name,image_id));
        }

        originalEditText = findViewById(R.id.originalText);
        //translatedEditText = findViewById(R.id.translatedText);

        c1 = findViewById(R.id.translateButton);

        spinner = (Spinner)findViewById(R.id.spinner);

        mAdapter = new LanguageAdapter(this,mCountryList);
        mAdapter.setDropDownViewResource(R.layout.language_spinner_row);
        spinner.setAdapter(mAdapter);
        spinner.setDropDownVerticalOffset(250);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String name = spinner.getSelectedItem().toString();
                int position = spinner.getSelectedItemPosition();
                String id = items2.get(position);

                originalLanguage = id;

                System.out.println(name + "SPINER1");
                System.out.println(id + "SPINER1");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinner2 = (Spinner)findViewById(R.id.spinner2);
        mAdapter = new LanguageAdapter(this,mCountryList);
        mAdapter.setDropDownViewResource(R.layout.language_spinner_row);
        spinner2.setAdapter(mAdapter);
        spinner2.setDropDownVerticalOffset(250);
        spinner2.setSelection(1);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String name = spinner2.getSelectedItem().toString();
                int position = spinner2.getSelectedItemPosition();
                String id = items2.get(position);

                translateLanguage = id;

                System.out.println(name + "SPINER2");
                System.out.println(id + "SPINER2");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR){
                    t1.setLanguage(new Locale(translateLanguage));
                }
            }
        });

        c1.setOnClickListener(v -> {
            if (checkInternetConnection()) {

                //If there is internet connection, get translate service and start translation:
                getTranslateService();
                translate();

            } else {

                //If not, display "no connection" warning:
                //translatedEditText.setText(getResources().getString(R.string.no_connection));
            }

            //String toSpeak = translatedEditText.getText().toString();
            String toSpeak = translatedText;
            Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
            t1.setLanguage(new Locale(translateLanguage));
            t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        });

    }

    public void getSpeechInput(View view) {

        Intent intent
                = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                originalLanguage);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,15 );

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }
        catch (Exception e) {
            Toast
                    .makeText(TranslationPage.this, " " + e.getMessage(),
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void getTranslateService() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try (InputStream is = getResources().openRawResource(R.raw.credentials)) {

            //Get credentials:
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);

            //Set credentials and get translate service:
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
            translate = translateOptions.getService();

        } catch (IOException ioe) {
            ioe.printStackTrace();

        }
    }

    public void translate() {
        //Get input text to be translated:
        originalText = originalEditText.getText().toString();
        Translation translation = translate.translate(originalText, Translate.TranslateOption.targetLanguage(translateLanguage),
                Translate.TranslateOption.model("base"));
        translatedText = translation.getTranslatedText();

        //Translated text and original text are set to TextViews:
        //translatedEditText.setText(translatedText);
        textViewSetup(translatedText);
    }

    @SuppressLint("ResourceType")
    private void textViewSetup(String translatedText) {
        String[] arr = translatedText.split("\\W+");
        int brojLoopova = arr.length;

        ConstraintLayout rLayout1 = findViewById(R.id.relLay);
        rLayout1.removeAllViews();
        int screenWidth = rLayout1.getWidth();
        ConstraintSet constraintSet = new ConstraintSet();

        DatabaseManager helper = new DatabaseManager(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        //RelativeLayout relativeLayout2 = findViewById(R.id.relLay2);

        int numberOfTextViews = 1;
        TextView[] textViewArray = new TextView[brojLoopova + 2];
        textViewArray[0] = new TextView(this);

        int currWidth = 0;
        boolean isNewLine = false;
        boolean firstLine = true;
        boolean lineCounter = true;
        int o = 0;
        int width = 0;
        int line_start_id = 1;
        boolean matching = false;

        for (int i = 0; i < brojLoopova; i++) {
            TextView text = new TextView(this);
            text.setPadding(0,4,7,0);

            Cursor cursor_question = db.query("WORDS", new String[]{"TRANSLATED_WORD", "TRANSLATED_LANG"},null,null,null,null,null);

            while(cursor_question.moveToNext()) {
                String database_word = cursor_question.getString(0);
                String database_lang = cursor_question.getString(1);
                if(database_lang.equals(translateLanguage) && database_word.equals(arr[i])){
                    matching = true;
                    System.out.println("OVDE JE IZVLACENJE RECI IZ BAZE I SETOVANJE TRUE AKO JESTE MATCHING");
                }
            }

            text.setText(arr[i]);
            text.setId(numberOfTextViews);
            if(matching){
                // ALSO MAKE IT BOLD
                text.setTextColor(Color.parseColor("#00cc00"));
                System.out.println("SETOVANJE BOJE");
                matching = false;
            }
            ConstraintLayout.LayoutParams textParams = new ConstraintLayout.LayoutParams
                    (ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            text.setLayoutParams(textParams);
            text.setOnClickListener(myListener);
            rLayout1.addView(text,0);
            constraintSet.clone(rLayout1);

            if(numberOfTextViews>=2){
                textViewArray[numberOfTextViews-1].measure(0,0);
                width = textViewArray[numberOfTextViews-1].getMeasuredWidth();
            }

            if(currWidth + 10<=screenWidth){
                currWidth += width + 15;
            }
            else{
                firstLine = false;
                isNewLine = true;
                line_starter++;
                lineCounter = true;
                currWidth = 0;
            }

            if (o==0){
                line_start_id = numberOfTextViews;
                constraintSet.connect(text.getId(), ConstraintSet.START, rLayout1.getId(), ConstraintSet.START,0);
                constraintSet.applyTo(rLayout1);

                textViewArray[numberOfTextViews] = text;
                numberOfTextViews = numberOfTextViews + 1;
            }
            else if(isNewLine){
                if(lineCounter){
                    constraintSet.connect(text.getId(), ConstraintSet.TOP, textViewArray[line_start_id].getId(), ConstraintSet.BOTTOM,0);
                    constraintSet.connect(text.getId(), ConstraintSet.START, rLayout1.getId(), ConstraintSet.START,0);
                    constraintSet.applyTo(rLayout1);
                    lineCounter = false;
                    line_start_id = numberOfTextViews;
                }else{
                    constraintSet.connect(text.getId(), ConstraintSet.START, textViewArray[numberOfTextViews-1].getId(), ConstraintSet.END,0);
                    constraintSet.connect(text.getId(), ConstraintSet.TOP, textViewArray[numberOfTextViews-1].getId(), ConstraintSet.TOP,0);
                    constraintSet.applyTo(rLayout1);
                }

                textViewArray[numberOfTextViews] = text;
                numberOfTextViews = numberOfTextViews + 1;
            }
            else if(firstLine){
                constraintSet.connect(text.getId(), ConstraintSet.START, textViewArray[numberOfTextViews-1].getId(), ConstraintSet.END,0);
                constraintSet.applyTo(rLayout1);

                textViewArray[numberOfTextViews] = text;
                numberOfTextViews = numberOfTextViews + 1;
            }
            o++;
        }
    }

    public View.OnClickListener myListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int idTexta = view.getId();
            System.out.println("ID TEXTAAAA" + idTexta);
            TextView tv = findViewById(idTexta);
            String message = tv.getText().toString();
            addNewWord(view);
            Toast.makeText(TranslationPage.this,
                    message, Toast.LENGTH_LONG).show();
        }
    };

    private void addNewWordDataBase(SQLiteDatabase sqLiteDatabase, int id){
        ContentValues cv = new ContentValues();
        String id_string = String.valueOf(id);
        sqLiteDatabase.delete("TRANSLATION","id=?",new String[]{id_string});
    }

    public void addNewWord(View view) {
        DatabaseManager helper = new DatabaseManager(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        System.out.println("PISI AKO UDJES");

        int idTexta = view.getId();
        TextView tv = findViewById(idTexta);
        String message = tv.getText().toString();

        System.out.println(message);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pop_up_add_word, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        Button confirm = (Button) popupView.findViewById(R.id.confirm_popup_word);
        confirm.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd-mm-yyyy").format(new Date());
                DatabaseManager.insertWord(db,message,translateLanguage,date);

                Cursor cursor_word = db.query("WORDS", new String[]{"TRANSLATED_WORD","TRANSLATED_LANG"},null,null,null,null,null);

                while(cursor_word.moveToNext()) {
                    String trans_word = cursor_word.getString(0);
                    String trans_lang = cursor_word.getString(1);

                    System.out.println(trans_word + " i " + trans_lang);
                }

                    //addNewWordDataBase(db, translation_id);
                popupWindow.dismiss();
                db.close();
            }
        });

        Button cancel = (Button) popupView.findViewById(R.id.cancel_popup_word);
        cancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                db.close();
            }
        });
    }


    public boolean checkInternetConnection() {

        //Check internet connection:
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //Means that we are connected to a network (mobile or wi-fi)
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

        return connected;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                originalEditText.setText(
                        Objects.requireNonNull(result).get(0));
            }
        }
    }

    public void setClipboard(View view) {
        //String text = translatedEditText.getText().toString();
        String text = "BAL";
        Context context = this;
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            clipboard.setPrimaryClip(clip);
        }
    }

    public void readTranslatedText(View view) {
        textViewSetup("Bio sam kuci pa sam otisao u skolu");
        //String toSpeak = translatedEditText.getText().toString();
        String toSpeak = translatedText;
        Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
        t1.setLanguage(new Locale(translateLanguage));
        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void saveTranslation(View view) {
        DatabaseManager helper = new DatabaseManager(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        EditText editText = findViewById(R.id.originalText);
        //EditText editText2 = findViewById(R.id.translatedText);
        String string1 = editText.getText().toString();
        String string2 = translatedText;

        System.out.println(date);

        DatabaseManager.insertTranslation(db,date,string1,string2);
        db.close();
    }

    public void backArrow(View view) {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }

}