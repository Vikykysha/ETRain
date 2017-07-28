package com.ideo.broadcast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {
    
    final String LOG_TAG = "myLogs";
    
    ExecutorService es;
    Object someRes;
    
    public void onCreate() {
        super .onCreate();
        Log.i(LOG_TAG, "MyService onCreate");
        es = Executors.newFixedThreadPool(3);
        someRes = new Object();
    }

    public void onDestroy() {
        super .onDestroy();
        Log.i(LOG_TAG, "MyService onDestroy");
        someRes = null ;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "MyService onStartCommand");
        int time = intent.getIntExtra("time", 1);
        MyRun mr = new MyRun(time, startId);
        es.execute(mr);
        return super .onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent arg0) {
        return null ;
    }

    class MyRun implements Runnable {
        int time;
        int startId;
        public MyRun( int time, int startId) {
            this .time = time;
            this .startId = startId;
            Log.i(LOG_TAG, "MyRun#" + startId + " create");
        }
        public void run() {
            Log.i(LOG_TAG, "MyRun#" + startId + " start, time = " + time);
            try {
                TimeUnit.SECONDS.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Log.i(LOG_TAG, "MyRun#" + startId + " someRes = " + someRes.getClass() );
            } catch (NullPointerException e) {
                Log.i(LOG_TAG, "MyRun#" + startId + " error, null pointer");
            }
            stop();
        }
        void stop() {
            Log.i(LOG_TAG, "MyRun#" + startId + " end, stopSelf(" + startId + ")");
            stopSelf(startId);
        }
    }
}