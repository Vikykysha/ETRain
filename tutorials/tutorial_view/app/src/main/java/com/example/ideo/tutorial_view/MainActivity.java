package com.example.ideo.tutorial_view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView myText;
    Button btnOK;
    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myscreen);

        myText = (TextView) findViewById(R.id.myText);
        btnOK = (Button) findViewById(R.id.btnOK);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        btnOK.setOnClickListener( this );
        btnCancel.setOnClickListener( this );

/*
        Обработчик кнопки по функции

        View.OnClickListener oclBtn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btnOK)
                    myText.setText("Нажата кнопка ОК");
                if (v.getId() == R.id.btnCancel)
                    myText.setText("Нажата кнопка Cancel");
            }
        };

        btnOK.setOnClickListener(oclBtn);
        btnCancel.setOnClickListener(oclBtn);
*/    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnOK)
            myText.setText("Нажата кнопка ОК");
        if (v.getId() == R.id.btnCancel)
            myText.setText("Нажата кнопка Cancel");
    }

    public void onClickOK(View view) {
    }

    public void onClickCancel(View view) {
    }
}
