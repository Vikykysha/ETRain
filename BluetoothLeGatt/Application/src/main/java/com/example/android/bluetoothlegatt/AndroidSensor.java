package com.example.android.bluetoothlegatt;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import java.util.ArrayList;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.content.Context;
import android.view.View;
import android.content.Intent;
import android.content.IntentFilter;

import static java.lang.Math.abs;


//Мысли: 1. Для определения правильного/неправильного угла необходимо ввести режим обучения, где спишуться идеальные данные для конкретного пользования. В дальнейшем любой отклонение свыше нормы будет сигнализировать об возможных ошибках в технике.
//2.выбор режима занятий: простейшая реализация - три режима с тремя количествами приседаний. телефон просигнализирует, когда необходимое колво приседаний в пределах погрешности выполнено
//3. зафиксировать максимальное ускорение по оси при приседании вниз и при приседании вверх ( для этого сначала надо определить что человек находиться внизу, это делаем с помощью того же условия < 45, тоже самое для состояния вверху)
public class AndroidSensor extends Activity {
    ArrayList<Float> get_squat = new ArrayList<Float>(); //Для записи количества приседаний
    ArrayList<Float> accel_x = new ArrayList<Float>();//Для записи ускорения по x
    boolean topFront = false;
    TextView tvText;
    TextView numSquats;
    TextView varning;
    SensorManager sensorManager;
    Sensor sensorAccel;
    Sensor sensorLinAccel;
    Sensor sensorGravity;
    Sensor sensorMagnet;
    private ArrayList<Float> acceleration_x = new ArrayList();
    Integer num_ = 0;
    Integer num_squats = 0;
    int rotation;

    StringBuilder sb = new StringBuilder();
    StringBuilder squat_string = new StringBuilder();

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_android);
        tvText = (TextView) findViewById(R.id.sensorText);
        numSquats = (TextView) findViewById(R.id.numOfSquats);
        varning = (TextView) findViewById(R.id.varning);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorLinAccel = sensorManager
                .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensorAccel,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorLinAccel,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorGravity,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorMagnet, SensorManager.SENSOR_DELAY_NORMAL);

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getDeviceOrientation();
                        getActualDeviceOrientation();
                        showInfo();
                    }
                });
            }
        };
        timer.schedule(task, 0, 200);
        WindowManager windowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
        Display display = windowManager.getDefaultDisplay();
        rotation = display.getRotation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listener);
        timer.cancel();
    }

    String format(float values[]) {
        return String.format("%1$.1f\t\t%2$.1f\t\t%3$.1f", values[0], values[1],
                values[2]);
    }

    void showInfo() {
        sb.setLength(0);
        sb.append("Orientation : " + format(valuesResult)).append("\nOrientation 2: " + format(valuesResult2))
        ;
        int size = get_squat.size();
        if (size > 1) {
            if (!topFront && abs(get_squat.get(size - 1)) < 55) {
                topFront = true;
            } else if (topFront && abs(get_squat.get(size - 1)) > 55) {
                topFront = false;
                num_squats++;
            }
            if (topFront && abs(get_squat.get(size - 1)) < 45) {
                varning();
            } else {
                varning.setText("good!!");
            }
        }
        //Другой способ подсчета угла
        /*
        if (get_squat.size() >= 7) {
            int size = get_squat.size();
            if (abs(get_squat.get(size - 5)) < 50 && abs(get_squat.get(size - 4)) < 50 && abs(get_squat.get(size - 3)) < 50 && abs(get_squat.get(size - 2)) > 50 &&abs(get_squat.get(size - 1)) > 50) {
                num_squats++;
            }

        }
*/

        tvText.setText(sb);
        squat_string.setLength(0);
        squat_string.append(num_squats / 2);
        numSquats.setText(squat_string);
        //предупреждает резмерное заполнение squats
        /*if (get_squat.size() > 107) {
            get_squat.clear();
        }
        */

    }
    void varning() {
       varning.setText("Wrong angle!!!");

    }
    //Метод для перехода на новый экран
    public void newScreen2(View v) {


            Intent i = new Intent(this, graph_of_asensor.class);
           i.putExtra("graph_axis", get_squat);
            i.putExtra("graph_accel", accel_x);

            startActivity(i);
    }


    float[] r = new float[9];

    void getDeviceOrientation() {
        SensorManager.getRotationMatrix(r, null, valuesAccel, valuesMagnet);
        SensorManager.getOrientation(r, valuesResult);

        valuesResult[0] = (float) Math.toDegrees(valuesResult[0]);
        valuesResult[1] = (float) Math.toDegrees(valuesResult[1]);
        valuesResult[2] = (float) Math.toDegrees(valuesResult[2]);
        return;
    }

    float[] inR = new float[9];
    float[] outR = new float[9];

    void getActualDeviceOrientation() {
        num_++;
        SensorManager.getRotationMatrix(inR, null, valuesAccel, valuesMagnet);
        int x_axis = SensorManager.AXIS_X;
        int y_axis = SensorManager.AXIS_Y;
        switch (rotation) {
            case (Surface.ROTATION_0): break;
            case (Surface.ROTATION_90):
                x_axis = SensorManager.AXIS_Y;
                y_axis = SensorManager.AXIS_MINUS_X;
                break;
            case (Surface.ROTATION_180):
                y_axis = SensorManager.AXIS_MINUS_Y;
                break;
            case (Surface.ROTATION_270):
                x_axis = SensorManager.AXIS_MINUS_Y;
                y_axis = SensorManager.AXIS_X;
                break;
            default: break;
        }
        SensorManager.remapCoordinateSystem(inR, x_axis, y_axis, outR);
        SensorManager.getOrientation(outR, valuesResult2);
        valuesResult2[0] = (float) Math.toDegrees(valuesResult2[0]);
        valuesResult2[1] = (float) Math.toDegrees(valuesResult2[1]);
        //Мое
        get_squat.add(valuesResult2[1]);
        //Суть заведем массив куда будем добавлять каждый пятый к примеру данные с магнет. В саммом массиве будем брать среднне за несколько приседаний и дальше с условиями

        /*
       //Для случая, когда подсчитывае количество приседаний через массив
        if (num_ % 2 == 0) {
            get_squat.add(valuesResult2[1]);
        }
        */
        valuesResult2[2] = (float) Math.toDegrees(valuesResult2[2]);
        //предупреждает переполнение num_
        if (num_ > 103) {
            num_ = 0;
        }
        return;
    }

    float[] valuesAccel = new float[3];
    float[] valuesMagnet = new float[3];
    float[] valuesResult = new float[3];
    float[] valuesResult2 = new float[3];


    SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    for (int i=0; i < 3; i++){
                        valuesAccel[i] = event.values[i];
                    }
                    accel_x.add(valuesAccel[1] + valuesAccel[0]);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    for (int i=0; i < 3; i++){
                        valuesMagnet[i] = event.values[i];
                    }

                    break;
            }
        }
    };

}

