package cellscanner.wowtor.github.com.cellscanner.recorder;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cellscanner.wowtor.github.com.cellscanner.App;
import cellscanner.wowtor.github.com.cellscanner.Database;
import cellscanner.wowtor.github.com.cellscanner.MainActivity;
import cellscanner.wowtor.github.com.cellscanner.R;

public class LocationRecordingService extends Service {

    private static final int NOTIF_ID = 123;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final String SERVICE_TAG = "FOREGROUND_SERVICE_TAG";
    private Timer mTimer;
    private TelephonyManager mTelephonyManager;
    private Database mDB;

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new Timer();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mDB = App.getDatabase();
        // TODO: FIND OUT WHY HERE
        mDB.storePhoneID(getApplicationContext());
        mDB.storeVersionCode(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(SERVICE_TAG, "on start command");
        startForeground(NOTIF_ID, getActivityNotification("started"));

        // start the times, schedule for every second
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // TODO: Move to own method
                List<CellInfo> cellinfo = getCellInfo();
                String[] cellstr = storeCellInfo(cellinfo);
                NotificationManager mngr =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mngr.notify(NOTIF_ID, getActivityNotification(String.format("%d cells registered (%d visible)", cellstr.length, cellinfo.size())));
            }
        }, 0, App.UPDATE_DELAY_MILLIS);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        Log.i(SERVICE_TAG, "on destroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification getActivityNotification(String text) {
        createNotificationChannel();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Cellscanner")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_symbol24)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @SuppressLint("MissingPermission")
    private List<CellInfo> getCellInfo() {
        // TODO: surround wit version aware permission settings and requests
        /*
          - This code should not run if the permissions are not there
          - Code should check and ask for permissions when the 'start recording switch' in the main activity
            is switched to start running when the permissions are not there
         */
        return mTelephonyManager.getAllCellInfo();
    }

    private String[] storeCellInfo(List<CellInfo> cellinfo) {
        /*
        // TODO: Be more clear around this

        This code does not store the records, this code;
        - creates new records
        - updates already stored records
        - turns modified records in a string and reports them back

         */
        String[] cellstr;
        try {
            cellstr = mDB.storeCellInfo(cellinfo);
            if (cellstr.length == 0)
                cellstr = new String[]{"no data"};
        } catch(Throwable e) {
            cellstr = new String[]{"error"};
        }
        return cellstr;
    }

}
