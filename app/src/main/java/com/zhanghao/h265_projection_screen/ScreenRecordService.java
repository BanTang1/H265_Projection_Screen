package com.zhanghao.h265_projection_screen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;

/**
 * 录屏服务
 */
public class ScreenRecordService extends Service {

    private static final String CHANNEL_ID = "MyForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private MediaProjectionManager mediaProjectionManager;
    private SocketPush socketPush;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // 创建前台服务通知
        Notification notification = buildNotification();

        // 将服务置于前台
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 在此执行前台服务的任务
        startScreenCapture(intent);

        // 如果服务被意外终止，系统会尝试重新创建服务
        return START_STICKY;
    }

    private void startScreenCapture(Intent intent) {
        ActivityResult activityResult = intent.getParcelableExtra("activityResult");
        MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(activityResult.getResultCode(), activityResult.getData());
        if (mediaProjection != null) {
            // 启动推流
            socketPush = new SocketPush();
            socketPush.start(mediaProjection);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "My Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification buildNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            // 在Android 8.0以下，不需要指定通知渠道
            builder = new Notification.Builder(this);
        }

        return builder
                .setContentTitle("My Foreground Service")
                .setContentText("Service is running in the foreground")
                .setSmallIcon(R.mipmap.ic_launcher) // 替换成你的应用图标
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socketPush.close();
        // 在服务销毁时，移除前台通知
        stopForeground(true);
    }

}
