package com.example.ideo.tutorial_activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class sec_act extends AppCompatActivity implements View.OnClickListener {

    Button btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sec_act);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.btnBack ) {
            Intent intent = new Intent(this, MainActivity.class );
            startActivity(intent);
        }
    }
}
