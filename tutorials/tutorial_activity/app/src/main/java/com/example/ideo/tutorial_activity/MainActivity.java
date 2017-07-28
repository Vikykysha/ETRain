package com.example.ideo.tutorial_activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAct = (Button) findViewById(R.id.btnAct);
        btnAct.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.btnAct ) {
            Intent intent = new Intent(this, sec_act.class );
            startActivity(intent);
        }
    }
}
