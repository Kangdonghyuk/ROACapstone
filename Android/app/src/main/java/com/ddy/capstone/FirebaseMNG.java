package com.ddy.capstone;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMNG extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        Log.d("MainActivity", "TEST_DO_RUN");
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Intent intent = new Intent(this, MainActivity.class);

        Toast.makeText(this, "HELO", Toast.LENGTH_LONG).show();

        Log.d("MainActivity", "TEST_DO_RECEIVE");
        //MainActivity.I.SendNotification("hello noti");
        //Toast.makeText(MainActivity, "receive", Toast.LENGTH_LONG).show();
        //MainActivity.I.SendNotification("push noti");
    }


    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }
}
