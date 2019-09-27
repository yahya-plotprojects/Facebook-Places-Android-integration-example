/*
 * Copyright (c) 2019 by Floating Market BV. Alle rechten voorbehouden.
 */

package com.plotprojects.facebookplaceswrapperlib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class BackgroundProcessingService extends Service {
    private static final String STICKY_CHANNEL_NAME = "Facebook Places Refresh";
    private static final int SERVICE_ID = 505;
    private static boolean channelCreated;
    private BackgroundServiceBinder binder = new BackgroundServiceBinder(this);

    public BackgroundProcessingService() {
        //no-op
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    static class BackgroundServiceBinder extends Binder {
        private final Service service;

        public BackgroundServiceBinder(Service service) {
            this.service = service;
        }

        void registerService(Notification notification) {
            service.startForeground(SERVICE_ID, notification);
        }

        public void unregisterService(Context context, ServiceConnection serviceConnection) {
            context.unbindService(serviceConnection);
            service.stopForeground(true);
            service.stopSelf();
        }
    }

    static class BackgroundServiceHandler {
        private final Context context;
        private ServiceConnection serviceConnection;
        private BackgroundServiceBinder binder;

        public BackgroundServiceHandler(Context context) {
            this.context = context;
        }

        void setBinder(BackgroundServiceBinder binder) {
            this.binder = binder;
        }

        void setServiceConnection(ServiceConnection serviceConnection) {
            this.serviceConnection = serviceConnection;
        }

        void hideNotification() {
            binder.unregisterService(context, serviceConnection);
        }
    }

    static BackgroundServiceHandler showNotification(final Context context) {
        final BackgroundServiceHandler result = new BackgroundServiceHandler(context);

        Intent intent = new Intent(context, BackgroundProcessingService.class);

        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                BackgroundServiceBinder backgroundServiceBinder = ((BackgroundServiceBinder) service);
                result.setBinder(backgroundServiceBinder);
                result.setServiceConnection(this);

                Intent hideIntent = new Intent();
                hideIntent.setPackage(context.getPackageName());

                Notification notification = createNotification(context);
                backgroundServiceBinder.registerService(notification);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                //no-op
            }
        };

        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);

        return result;
    }

    private static Notification createNotification(Context context) {
        createNotificationChannelIfNotExists(context);

        int resolvedSmallIcon = determineIcon(context);
        PendingIntent contentIntent = getPendingIntent(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, STICKY_CHANNEL_NAME)
                .setSmallIcon(resolvedSmallIcon)
                .setAutoCancel(false)
                .setGroup("plot-sticky")
                .setContentText("Looking what is nearby")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setLocalOnly(true)
                .setContentIntent(contentIntent)
                .setShowWhen(false);

        final Notification stickyTestNotification = builder.build();
        stickyTestNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        return stickyTestNotification;
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static int determineIcon(Context context) {
        return context.getApplicationInfo().icon == 0 ? android.R.drawable.star_on : context.getApplicationInfo().icon;
    }

    private static void createNotificationChannelIfNotExists(Context context) {
        if (channelCreated || Build.VERSION.SDK_INT < 26) {
            return;
        }
        channelCreated = true;

        NotificationChannel channel = new NotificationChannel(STICKY_CHANNEL_NAME,
                "Background updates",
                NotificationManager.IMPORTANCE_LOW);

        channel.setShowBadge(false);
        channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        channel.enableLights(false);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

}
