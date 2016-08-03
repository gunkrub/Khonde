/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tny.khonde;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.ArrayList;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        //String type = data.getString("type");
        //int job_id = Integer.parseInt(data.getString("job_id"));
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        //Log.e(TAG,"Type: "+type);
        //Log.e(TAG,"JOB ID: "+job_id);

        if (data.getString("type").equals("NEW_BID")) {
            Intent broadcast = new Intent();
            broadcast.putExtra("new_bid", data.getString("message"));
            broadcast.putExtra("job_code", data.getString("job_code"));
            broadcast.setAction("JOBBID");
            sendBroadcast(broadcast);
        } else if (data.getString("type").equals("NEW_STATUS")) {
            DBHelper mydb = new DBHelper(getApplicationContext());
            ArrayList<String> job_codes = new ArrayList<>();
            job_codes.add(data.getString("job_code"));
            mydb.updateStatus(job_codes, Integer.parseInt(data.getString("status")));

            Intent broadcast = new Intent();
            broadcast.setAction("JOBDONE");
            sendBroadcast(broadcast);
            MainActivity.sendNotification("สินค้าของท่านถึงที่หมายอย่างปลอดภัย [" + data.getString("job_code") + "]",this);
        } else {
            MainActivity.sendNotification(message,this);
        }

        //sendNotification(type,message,job_id);

    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);


        stackBuilder.addNextIntent(intent);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.khonde_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.khonde_logo))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentTitle("ขนดี")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = notificationBuilder.build();
        int smallIconId = getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());
        if (smallIconId != 0) {
            notification.contentView.setViewVisibility(smallIconId, View.INVISIBLE);
        }
        notificationManager.notify(0 /* ID of notification */, notification);
    }
}
