package com.juztoss.bpmplayer.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 5/7/2016.
 */
public class PlaybackNotification
{
    @SuppressLint("DefaultLocale")
    static Notification create(PlaybackService service)
    {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(service);
        notificationBuilder.setSmallIcon(R.drawable.ic_play_arrow_black_24dp);


        Intent launchNowPlayingIntent = new Intent(PlaybackService.LAUNCH_NOW_PLAYING_ACTION);
        PendingIntent launchNowPlayingPendingIntent = PendingIntent.getBroadcast(service, 0, launchNowPlayingIntent, 0);
        notificationBuilder.setContentIntent(launchNowPlayingPendingIntent);

        RemoteViews notificationView = new RemoteViews(service.getPackageName(), R.layout.notification_layout);
        if (service.isPlaying())
            notificationView.setImageViewResource(R.id.notification_pause, R.drawable.ic_pause_black_48dp);
        else
            notificationView.setImageViewResource(R.id.notification_pause, R.drawable.ic_play_arrow_black_48dp);

        Composition composition = ((BPMPlayerApp) service.getApplication()).getComposition(service.currentSongId());

        if (composition != null)
        {
            notificationView.setTextViewText(R.id.first_line, composition.name());
            notificationView.setTextViewText(R.id.second_line, composition.getFolder());
            notificationView.setTextViewText(R.id.bpm_label, String.format("%.1f", service.getCurrentlyPlayingBPM()));
        }

        Intent switchPlaybackIntent = new Intent(PlaybackService.SWITCH_PLAYBACK_ACTION);
        PendingIntent switchPlaybackPendingIntent = PendingIntent.getBroadcast(service, 0, switchPlaybackIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.notification_pause, switchPlaybackPendingIntent);

        notificationBuilder.setContent(notificationView);
        Notification notification = notificationBuilder.build();

        return notification;
    }
}
