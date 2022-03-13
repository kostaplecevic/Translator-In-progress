package com.example.translator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import java.util.Calendar;
import java.util.Date;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        getSupportActionBar().hide();

        ImageView imageView = findViewById(R.id.homeImage);
        imageView.setImageResource(R.drawable.home_pic);

        myAlarm();

        //createNotification();
    }

    public void myAlarm() {
        // Namesta vreme kad ce se svaki dan pokrenuti notifikacija
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 50);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTime().compareTo(new Date()) < 0)
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        }

    }

    public void goToInstructions(View view) {
        Intent intent = new Intent(this, InstructionsPage.class);
        startActivity(intent);
    }

    public void goToSavedWords(View view) {
        Intent intent = new Intent(this, SavedWords.class);
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