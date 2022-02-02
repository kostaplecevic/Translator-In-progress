package com.example.translator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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

        final RelativeLayout rLayout1 = findViewById(R.id.relLay);
        int screenWidth = rLayout1.getWidth();

        RelativeLayout relativeLayout2 = findViewById(R.id.relLay2);

        int numberOfTextViews = 1;
        TextView[] textViewArray = new TextView[brojLoopova + 2];

        rLayout1.removeAllViews();
        int currWidth = 0;
        boolean isNewLine = false;
        boolean firstLine = true;
        boolean lineCounter = true;
        int o = 0;
        int width = 0;

        for (int i = 0; i < brojLoopova; i++) {
            RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView text = new TextView(this);
            text.setPadding(0,4,7,0);
            text.setText(arr[i]);
            System.out.println(arr[i] + " OVDE PISE TEKST");
            text.setId(numberOfTextViews);
            text.setOnClickListener(myListener);

            if(numberOfTextViews>=2){
                textViewArray[numberOfTextViews].measure(0,0);
                width = textViewArray[numberOfTextViews].getMeasuredWidth();
            }

            if((currWidth)<=screenWidth){
                currWidth += width + 10;
                isNewLine = false;
            }
            else{
                currWidth = currWidth + width;
                firstLine = false;
                isNewLine = true;
            }

            if (o==0){
                lparams.addRule(RelativeLayout.ALIGN_START);
                text.setLayoutParams(lparams);
                text.setId(numberOfTextViews);
                rLayout1.addView(text);
                numberOfTextViews = numberOfTextViews + 1;
                textViewArray[numberOfTextViews] = text;
            }
            else if(isNewLine){
                if(lineCounter){
                    lparams.addRule(RelativeLayout.BELOW, 1);
                    lineCounter = false;
                }else{
                    lparams.addRule(RelativeLayout.RIGHT_OF, numberOfTextViews-1);
                    lparams.addRule(RelativeLayout.BELOW, 2000);
                }
                text.setLayoutParams(lparams);
                text.setId(numberOfTextViews);
                relativeLayout2.addView(text);
                numberOfTextViews = numberOfTextViews + 1;
                textViewArray[numberOfTextViews] = text;
            }
            else if(firstLine){
                lparams.addRule(RelativeLayout.RIGHT_OF, numberOfTextViews-1);
                text.setLayoutParams(lparams);
                text.setId(numberOfTextViews);
                rLayout1.addView(text);
                numberOfTextViews = numberOfTextViews + 1;
                textViewArray[numberOfTextViews] = text;
            }
//            else{
//                lparams.addRule(RelativeLayout.RIGHT_OF, 2000 - 1 + o);
//                lparams.addRule(RelativeLayout.BELOW, 2000 - currCounter + o);
//                text.setLayoutParams(lparams);
//                text.setId(numberOfTextViews);
//                rLayout1.addView(text);
//            }

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
            Toast.makeText(TranslationPage.this,
                    message, Toast.LENGTH_LONG).show();
        }
    };


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
        textViewSetup("Bio sam tamo i onda sam video nju upoznao sam je u skoli i mrzeo sam je najvise na svetu" +
                " jer je bila uzasna osoba i niko nije gori");
        translatedText = "bio sam tamo";
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
        EditText editText2 = findViewById(R.id.translatedText);
        String string1 = editText.getText().toString();
        String string2 = editText2.getText().toString();

        System.out.println(date);

        DatabaseManager.insertTranslation(db,date,string1,string2);
        db.close();
    }

    public void backArrow(View view) {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }

}