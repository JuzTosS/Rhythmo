package com.juztoss.rhythmo.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.widget.RemoteViews;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.views.activities.PlayerActivity;

import java.util.Locale;

/**
 * Created by JuzTosS on 5/7/2016.
 */
public class PlaybackNotification
{

    public static final String NOTIFICATION_CHANNEL_ID = "com.juztoss.rhythmo.playback";

    @Nullable
    public static Notification create(PlaybackService service, String notificationChannelId)
    {
        createNotificationChannel(service);

        Composition composition = ((RhythmoApp) service.getApplication()).getComposition(service.getCurrentSongId());
        if(composition == null)
            return null;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(service, notificationChannelId);
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);


        Intent launchNowPlayingIntent = new Intent(service, PlayerActivity.class);
        PendingIntent launchNowPlayingPendingIntent = PendingIntent.getActivity(service, 0, launchNowPlayingIntent, PendingIntent.FLAG_MUTABLE);
        notificationBuilder.setContentIntent(launchNowPlayingPendingIntent);

        RemoteViews notificationView = new RemoteViews(service.getPackageName(), R.layout.notification_layout);
        if (service.isPlaying())
            notificationView.setImageViewResource(R.id.notification_pause, R.drawable.ic_pause_black_48dp);
        else
            notificationView.setImageViewResource(R.id.notification_pause, R.drawable.ic_play_arrow_black_48dp);

        notificationView.setTextViewText(R.id.first_line, composition.name());
        notificationView.setTextViewText(R.id.second_line, composition.getFolder());
        notificationView.setTextViewText(R.id.bpm_label, String.format(Locale.US, "%.1f", service.getCurrentlyPlayingBPM()));

        Intent switchPlaybackIntent = new Intent(service, PlaybackService.class);
        switchPlaybackIntent.setAction(PlaybackService.ACTION_COMMAND);
        switchPlaybackIntent.putExtra(PlaybackService.ACTION_NAME, PlaybackService.SWITCH_PLAYBACK_ACTION);
        PendingIntent switchPlaybackPendingIntent = PendingIntent.getService(service, 0, switchPlaybackIntent, PendingIntent.FLAG_MUTABLE);
        notificationView.setOnClickPendingIntent(R.id.notification_pause, switchPlaybackPendingIntent);

        Intent playNextActionIntent = new Intent(service, PlaybackService.class);
        playNextActionIntent.setAction(PlaybackService.ACTION_COMMAND);
        playNextActionIntent.putExtra(PlaybackService.ACTION_NAME, PlaybackService.PLAY_NEXT_ACTION);
        PendingIntent playNextActionPendingIntent = PendingIntent.getService(service, 1, playNextActionIntent, PendingIntent.FLAG_MUTABLE);
        notificationView.setOnClickPendingIntent(R.id.notification_next, playNextActionPendingIntent);

        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationBuilder.setContent(notificationView);
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);

        Notification notification = notificationBuilder.build();

        return notification;
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Playback";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        }
    }

}
