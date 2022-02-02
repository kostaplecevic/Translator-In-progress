package com.example.translator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class InstructionsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions_page);

        getSupportActionBar().hide();
    }

    public void backArrow(View view) {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }
}