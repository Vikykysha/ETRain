package com.example.ideo.tutorial_intent;

import android.os.Bundle;
import java.sql.Date;
import java.text.SimpleDateFormat;
import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

public class Info extends Activity {

    TextView txtInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super .onCreate(savedInstanceState);
        setContentView(R.layout.info);
// получаем Intent, который вызывал это Activity
        Intent intent = getIntent();
// читаем из него action
        String action = intent.getAction();        String format = "", textInfo = "";
// в зависимости от action заполняем переменные
        if (action.equals("com.example.intent.action.showtime")) {
            format = "HH:mm:ss";
            textInfo = "Time: ";
        }
        else if (action.equals("com.example.intent.action.showdate")) {
            format = "dd.MM.yyyy";
            textInfo = "Date: ";
        }
// в зависимости от содержимого переменной format
// получаем дату или время в переменную datetime
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String datetime = sdf.format( new Date(System.currentTimeMillis()));
        txtInfo = (TextView) findViewById(R.id.tvInfo);
        txtInfo.setText(textInfo + datetime);
    }
}