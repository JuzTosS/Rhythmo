package com.juztoss.rhythmo.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
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
    static Notification create(PlaybackService service)
    {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(service);
        notificationBuilder.setSmallIcon(R.drawable.logo_icon_transparent_48);


        Intent launchNowPlayingIntent = new Intent(service, PlayerActivity.class);
        PendingIntent launchNowPlayingPendingIntent = PendingIntent.getActivity(service, 0, launchNowPlayingIntent, 0);
        notificationBuilder.setContentIntent(launchNowPlayingPendingIntent);

        RemoteViews notificationView = new RemoteViews(service.getPackageName(), R.layout.notification_layout);
        if (service.isPlaying())
            notificationView.setImageViewResource(R.id.notification_pause, R.drawable.ic_pause_black_48dp);
        else
            notificationView.setImageViewResource(R.id.notification_pause, R.drawable.ic_play_arrow_black_48dp);

        Composition composition = ((RhythmoApp) service.getApplication()).getComposition(service.currentSongId());

        if (composition != null)
        {
            notificationView.setTextViewText(R.id.first_line, composition.name());
            notificationView.setTextViewText(R.id.second_line, composition.getFolder());
            notificationView.setTextViewText(R.id.bpm_label, String.format(Locale.US, "%.1f", service.getCurrentlyPlayingBPM()));
        }

        Intent switchPlaybackIntent = new Intent(service, PlaybackService.class);
        switchPlaybackIntent.setAction(PlaybackService.ACTION_COMMAND);
        switchPlaybackIntent.putExtra(PlaybackService.ACTION_NAME, PlaybackService.SWITCH_PLAYBACK_ACTION);
        PendingIntent switchPlaybackPendingIntent = PendingIntent.getService(service, 0, switchPlaybackIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.notification_pause, switchPlaybackPendingIntent);

        Intent playNextActionIntent = new Intent(service, PlaybackService.class);
        playNextActionIntent.setAction(PlaybackService.ACTION_COMMAND);
        playNextActionIntent.putExtra(PlaybackService.ACTION_NAME, PlaybackService.PLAY_NEXT_ACTION);
        PendingIntent playNextActionPendingIntent = PendingIntent.getService(service, 1, playNextActionIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.notification_next, playNextActionPendingIntent);

        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationBuilder.setContent(notificationView);
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);

        Notification notification = notificationBuilder.build();

        return notification;
    }
}
