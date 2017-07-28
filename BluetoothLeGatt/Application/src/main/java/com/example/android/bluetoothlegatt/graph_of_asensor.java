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
        import static java.lang.Math.abs;

public class graph_of_asensor extends Activity  {
    private GraphView graph;
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> series2;
    LineGraphSeries<DataPoint> seriesControl;
    ArrayList<Float> get_squat = new ArrayList<Float>();
    ArrayList<Float> accel_x = new ArrayList<Float>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_of_asensor);
        graph = (GraphView) findViewById(R.id.graph_of_asensor);
        get_squat = ( ArrayList<Float>) getIntent().getSerializableExtra("graph_axis");
        accel_x = ( ArrayList<Float>) getIntent().getSerializableExtra("graph_accel");
        draw_graph(get_squat, accel_x);

    }


    private void draw_graph(ArrayList<Float> data, ArrayList<Float> data2) {
        graph.removeAllSeries();
        series = new LineGraphSeries<DataPoint>();
         series2 = new LineGraphSeries<DataPoint>();
        seriesControl = new LineGraphSeries<DataPoint>();
        float y1 , y2;
        float x = 0;
        int controlPitch = 45;

        for(int i = 0; i < data.size(); i++) {
           x = i;
            /*
            if (i == 0) {
                x = 0;
            } else {
                x += 0.05;
            }
            */
           // x = data2.indexOf(data2.get(i)) / 2;

            y1 = abs(data.get(i));
            y2 = abs(data2.get(i));
            /*
            //если подсчитываем приседаний через массив
    y2 = abs(data2.get(i*2))  ;
    */
    series2.appendData(new DataPoint(x, y2), true, data2.size());



            series.appendData(new DataPoint(x, y1), true, data.size());
            seriesControl.appendData(new DataPoint(x, controlPitch), true, data.size());

        }
        /*
        for (int i = 0; i < data2.size(); i++) {
            if (i == 0) {
                x = 0;
            } else {
                x += 0.05;
            }
            y2 = data2.get(i);
            series2.appendData(new DataPoint(x, y2), true, data.size());
        }
        */

        //НАСТРОЙКА ГРАФИКОВ


        //seriesControl.setTitle("Control Pitch");
        // series.setColor(Color.GREEN);
        // series.setDrawDataPoints(true);
        //series.setDataPointsRadius(6);
        series.setThickness(5);
        series.setThickness(3);
        series.setColor(Color.GREEN);
        seriesControl.setThickness(2);
        seriesControl.setColor(Color.RED);

        graph.addSeries(series);
        // set second scale
        graph.getSecondScale().addSeries(series2);
        graph.getSecondScale().setMinY(0);
        graph.getSecondScale().setMaxY(25);
        // the y bounds are always manual for second scale
        graph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.RED);
        graph.addSeries(seriesControl);

        graph.getGridLabelRenderer().setVerticalAxisTitle("Угол наклона спины");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Время, сек");
        graph.getViewport().setMaxY(100);
        graph.getViewport().setMinY(0);
        //необходимо, для того чтобы учитывал заданные самостоятельно диапазон
        graph.getViewport().setYAxisBoundsManual(true);
        //graph.getViewport().setXAxisBoundsManual(true);
        /*
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
        */
    }

}
