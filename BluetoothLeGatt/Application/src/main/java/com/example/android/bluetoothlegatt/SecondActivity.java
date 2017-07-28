package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static android.os.SystemClock.sleep;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;


import org.w3c.dom.Text;

import static android.os.SystemClock.sleep;


public class SecondActivity extends Activity implements View.OnClickListener{

    private TextView txt_squats, txt_errors, txt_weight;
    private Button btnPrev, btnLast;
    private GraphView graph;
    final String LOG_TAG = "myLogs";

    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> seriesControl;
    ArrayList<Integer> get_squat = new ArrayList<Integer>();
    Gson gson = new Gson();
    DBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);

        btnLast = (Button) findViewById(R.id.btnLast);
        btnLast.setOnClickListener(this);

        btnPrev = (Button) findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(this);

        txt_squats = (TextView) findViewById(R.id.squat_num);

        txt_errors = (TextView) findViewById(R.id.error_num);

        txt_weight = (TextView) findViewById(R.id.weight);

        graph = (GraphView) findViewById(R.id.graph);

        get_squat = getIntent().getIntegerArrayListExtra("graph_intent");

    }

    @Override
    public void onClick(View v) {
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("mytable", null, null, null, null, null, null);

        ArrayList<Integer> output_data = new ArrayList<>();
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        int indPitch;
        int indReps;
        int indReps_bad;
        int indWeight;
        String reps = new String();
        String reps_bad = new String();
        String weight = new String();
        String gson_data;

        if (c.moveToLast()) {
            switch (v.getId()) {
                case R.id.btnLast:
                    indPitch = c.getColumnIndex("pitch");
                    indReps = c.getColumnIndex("reps");
                    indReps_bad = c.getColumnIndex("reps_bad");
                    indWeight = c.getColumnIndex("weight");

                    reps = c.getString(indReps).toString();
                    reps_bad = c.getString(indReps_bad).toString();
                    weight = c.getString(indWeight).toString();

                    gson_data = c.getString(indPitch);

                    output_data = gson.fromJson(gson_data, type);
                    Log.i(LOG_TAG, "reps = " + reps + ", weight = " + weight + ", pitch = " + output_data);
                    break;
                case R.id.btnPrev:
                    c.moveToPrevious();
                    c.moveToPrevious();

                    indPitch = c.getColumnIndex("pitch");
                    indReps = c.getColumnIndex("reps");
                    indReps_bad = c.getColumnIndex("reps_bad");
                    indWeight = c.getColumnIndex("weight");

                    reps = c.getString(indReps).toString();
                    reps_bad = c.getString(indReps_bad).toString();
                    weight = c.getString(indWeight).toString();

                    gson_data = c.getString(indPitch);

                    output_data = gson.fromJson(gson_data, type);
                    Log.i(LOG_TAG, "reps = " + reps + ", weight = " + weight + ", pitch = " + output_data);
                default:
                    break;
            }
            txt_squats.setText(reps);
            txt_errors.setText(reps_bad);
            txt_weight.setText(weight);
            draw_graph(output_data);
        }
        c.close();
        dbHelper.close();
    }

    private void draw_graph(ArrayList<Integer> data) {
        graph.removeAllSeries();
        series = new LineGraphSeries<DataPoint>();
        seriesControl = new LineGraphSeries<DataPoint>();
        int y;
        double x = 0;
        int controlPitch = 45;

        for(int i = 0; i < data.size(); i++) {
            //из расчета частоты снимаемых измерений: 1 измерение раз в 50 мс; тогда за 1 секунду снимается 20 измерений; тогда 1 измерение снимается раз в 0.05 секунд
            //по оси x предполагается откладывать время в секундах, прошедшее с начала приседаний
            if (i == 0) {
                x = 0;
            } else {
                x += 0.05;
            }

            y = data.get(i);

            series.appendData(new DataPoint(x, y), true, data.size());
            seriesControl.appendData(new DataPoint(x, controlPitch), true, data.size());
        }

        //НАСТРОЙКА ГРАФИКОВ


        //seriesControl.setTitle("Control Pitch");
        // series.setColor(Color.GREEN);
        // series.setDrawDataPoints(true);
        //series.setDataPointsRadius(6);
        series.setThickness(10);
        seriesControl.setThickness(2);
        seriesControl.setColor(Color.RED);

        graph.addSeries(series);
        graph.addSeries(seriesControl);

        graph.getGridLabelRenderer().setVerticalAxisTitle("Угол наклона спины");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Время, сек");
        graph.getGridLabelRenderer().setVerticalAxisTitleTextSize(20);
        graph.getGridLabelRenderer().setLabelVerticalWidth(12);
        //graph.getGridLabelRenderer().setPadding(12);
        graph.getViewport().setMinX(0.05);

        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(90);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);

        //graph.getGridLabelRenderer().setHorizontalAxisTitle("Number of squats");
        series.setDrawBackground(true); // activate the background feature<br />
        series.setBackgroundColor(Color.rgb(186, 194, 255)); // below the line fill with color<br />
    }

    static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, "newdb1", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table mytable" + "("
                    + "id integer primary key autoincrement,"
                    + "pitch text,"
                    + "reps integer,"
                    + "reps_bad integer,"
                    + "weight integer"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}