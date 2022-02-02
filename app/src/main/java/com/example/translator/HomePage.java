package com.example.translator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        getSupportActionBar().hide();

        ImageView imageView = findViewById(R.id.homeImage);
        imageView.setImageResource(R.drawable.home_pic);


    }

    public void goToInstructions(View view) {
        Intent intent = new Intent(this, InstructionsPage.class);
        startActivity(intent);
    }

    public void goToTranslatePage(View view) {
        Intent intent = new Intent(this, TranslationPage.class);
        startActivity(intent);
    }

    public void goToMyTranslationsPage(View view) {
        Intent intent = new Intent(this, MyTranslations.class);
        startActivity(intent);
    }
}