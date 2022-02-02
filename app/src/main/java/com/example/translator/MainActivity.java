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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
public class MainActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_main);

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
        translatedEditText = findViewById(R.id.translatedText);

        c1 = findViewById(R.id.translateButton);

        spinner = (Spinner)findViewById(R.id.spinner);
        mAdapter = new LanguageAdapter(this,mCountryList);
        mAdapter.setDropDownViewResource(R.layout.language_spinner_row);
        spinner.setAdapter(mAdapter);
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
                translatedEditText.setText(getResources().getString(R.string.no_connection));
            }

            String toSpeak = translatedEditText.getText().toString();
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

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }
        catch (Exception e) {
            Toast
                    .makeText(MainActivity.this, " " + e.getMessage(),
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
        translatedEditText.setText(translatedText);
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
        String text = translatedEditText.getText().toString();
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
        String toSpeak = translatedEditText.getText().toString();
        Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
        t1.setLanguage(new Locale(translateLanguage));
        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void saveToFile(View view) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pop_up, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        Button confirm = (Button)popupView.findViewById(R.id.confirm_popup);
        confirm.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                CheckBox check1 = (CheckBox) popupView.findViewById(R.id.checkBox1);
                CheckBox check2 = (CheckBox) popupView.findViewById(R.id.checkBox2);
                try{
                    String speech =  originalEditText.getText().toString();
                    String trans =  translatedEditText.getText().toString();

                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("config.txt", Context.MODE_PRIVATE));
                    if(check1.isChecked()){
                        outputStreamWriter.write(speech);
                    }
                    if(check2.isChecked()){
                        outputStreamWriter.write(trans);
                    }
                    outputStreamWriter.close();

                }catch (IOException e) {
                    e.printStackTrace(); }

                popupWindow.dismiss();
                //recreate();
            }});

        Button cancel = (Button)popupView.findViewById(R.id.cancel_popup);
        cancel.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }});
    }

    public void readFromFile(View view) {

        String ret = "";

        try {
            InputStream inputStream = openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        Toast.makeText(this, ret, Toast.LENGTH_SHORT).show();

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