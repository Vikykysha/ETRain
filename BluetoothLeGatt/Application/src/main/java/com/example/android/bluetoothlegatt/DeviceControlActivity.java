
package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.lang.*;
import com.example.android.bluetoothlegatt.SecondActivity.DBHelper;
import com.google.gson.Gson;

import java.util.ArrayList;

import static android.os.SystemClock.sleep;
import static com.example.android.bluetoothlegatt.BluetoothLeService.EXTRA_DATA;

public class DeviceControlActivity extends Activity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final String LOG_TAG = "control_logs";

    private int squat_number = 0;
    private int error_number = 0;
    private TextView mDataField;
    private TextView isright;
    private TextView sqt_num;
    private EditText inputWeight;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBLEService;
    private boolean mConnected = false;
    private final int STATE_START = 1;
    private final int STATE_STOP = 0;
    private int state = 0;
    private int front_state = 0;
    private boolean error_detected = false;
    private ArrayList<Integer> squats = new ArrayList();
    static  DBHelper dbHelper;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBLEService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBLEService.initialize()) {
                finish();
            }
            mBLEService.connect(mDeviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBLEService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDataField = (TextView) findViewById(R.id.data_value);
        isright = (TextView) findViewById(R.id.isright);
        sqt_num = (TextView) findViewById(R.id.sqtView);
        inputWeight = (EditText) findViewById(R.id.inputWeight);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBLEService != null) {
            final boolean result = mBLEService.connect(mDeviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBLEService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBLEService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBLEService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read

    /*
      Третья стадия:
    * В activity создали сервис-> Сервис отправляет данные, обрабатывает их-> Отсылает данные в broadcast receiver
    * mGattUpdateReceiver принимает на себя intent
    * при направлении команды sendbroadcast()
    * метода broadcastUpdate класса BluetoothLeService
    * */

    private final BroadcastReceiver
            mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            if (action.equals(BluetoothLeService.ACTION_GATT_CONNECTED)) {
                mConnected = true;
                invalidateOptionsMenu();
            }
            else if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
                mConnected = false;
                invalidateOptionsMenu();
                mDataField.setText(R.string.no_data);
            }
            else if (action.equals(BluetoothLeService.ACTION_DATA_AVAILABLE)) {
                displayData(intent.getStringExtra(EXTRA_DATA));
            }
        }
    };

    private void displayData(String data) {
        if (data != null) {

            StringBuffer data_value = new StringBuffer(data);
            StringBuffer data_uuid = new StringBuffer(data);
            data_value.delete(0, 36);
            data_uuid.delete(36, data_uuid.length());

            String uuid_str = new String(data_uuid);

            if (uuid_str.equals(BluetoothLeService.ANGLE_CHAR)) {
                processingData(data_value);
            }

            if ( (state == STATE_START) && (mBLEService != null)) {
                mBLEService.readANGLE();
            }
        }
    }

    private void processingData(StringBuffer data) {

        int data_int = Integer.parseInt(data.toString());
        squats.add(data_int);

        if (data_int > 45) {
            mDataField.setText(data);
            isright.setText("Правильно");
        } else {
            mDataField.setText(data);
            isright.setText("Неправильно");
        }

        if ( (data_int < 65) && (data_int > 60)) {
            front_state = 1;
            error_detected = false;
        } else if ( (data_int < 55) && (front_state == 1) ) {
            front_state = 2;
            squat_number++;
            sqt_num.setText(Integer.toString(squat_number));
        }
        if ( (data_int < 45) && (!error_detected) ) {
            error_detected = true;
            error_number++;
        }
    }


    public void onClickStart(View v) {
        if (mBLEService != null) {

            /*
            Надо было всего-то поставить задержку на writeCharacteristic()
            потому что BLE не успеевал обработать запрос
            sleep for 7s for autocalibrate at initial point
             */

            sleep(1000);
            mBLEService.writeCustomCharacteristic(1);
            sleep(200);

            state = STATE_START;
            front_state = 0;
            squat_number = 0;
            error_number = 0;
            squats = new ArrayList();

            mBLEService.readANGLE();
        }
    }

    public void onClickStop(View v) {
        sleep(200);
        mBLEService.writeCustomCharacteristic(0);
        sleep(200);
        mBLEService.writeCustomCharacteristic(0);
        if (state==STATE_START) {
            store_data();
        }
        state = STATE_STOP;
    }

    //Метод для перехода на новый экран
    public void newScreen(View v) {

        if (state == STATE_STOP) {
            Intent intObj = new Intent(this, SecondActivity.class);
            intObj.putIntegerArrayListExtra("graph_intent", squats);
            intObj.putExtra("squats", squat_number);
            intObj.putExtra("errors", error_number);
            startActivity(intObj);
        } else {
            isright.setText("Press Stop");
        }

    }

    private void store_data() {
        Gson gson = new Gson();
        String inputString = gson.toJson(squats);

        dbHelper = new DBHelper(this);
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Log.i(LOG_TAG, "--- Insert in mytable: ---");
        cv.put("pitch", inputString);
        cv.put("reps", squat_number);
        cv.put("reps_bad", error_number);
        cv.put("weight", Integer.parseInt(inputWeight.getText().toString()));
        db.insert("mytable", null, cv);

        long rowID = db.insert("mytable", null, cv);
        Log.i(LOG_TAG, "row inserted, ID = " + rowID + ", reps = " + squat_number +
                ", pitch = " + inputString);
        dbHelper.close();
    }
}
