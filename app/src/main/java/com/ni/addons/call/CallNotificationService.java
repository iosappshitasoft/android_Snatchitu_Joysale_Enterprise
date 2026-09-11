package com.ni.addons.call;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.ni.helper.SignallingClient;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;

public class CallNotificationService extends Service {

    private static final String TAG = CallNotificationService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service Started");
        setCallNotification(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CallNotificationService", "Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("CallNotificationService", "END");

        if (JoysaleApplication.getCurrentActivity() != null &&
                JoysaleApplication.getCurrentActivity() instanceof CallActivity) {
            CallActivity activity = (CallActivity) JoysaleApplication.getCurrentActivity();
            SignallingClient socketConnection = SignallingClient.getInstance();
            if (activity.roomId != null)
                socketConnection.close(activity.roomId);
        }

        stopSelf();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel("howzucall", 0);
        }
    }

    public void setCallNotification(Context context) {
        CallActivity.isCallPause = true;
        String appName = context.getString(R.string.app_name);
        long when = System.currentTimeMillis();

        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra("notification", "true");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, 0, intent, 0);

        Intent intent2 = new Intent(context, CallActivity.class);
        intent2.putExtra("_ACTION_", "endcall");
        intent2.setAction("End Call");
        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent endIntent = PendingIntent.getActivity(context, 0, intent2, 0);

        String channelId = context.getString(R.string.notification_channel_id);
        CharSequence channelName = context.getString(R.string.app_name);

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId);
        mBuilder.setContentTitle(appName)
                .setContentText("Ongoing call").setTicker(appName).setWhen(when)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Ongoing call"))
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.notifyicon)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .addAction(R.drawable.notifyicon, "End call", endIntent)
                .setOngoing(true)
                .setAutoCancel(true);
        Notification notification = mBuilder.build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(1000, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA | ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        } else {
            startForeground(1000, notification);
        }
    }
}