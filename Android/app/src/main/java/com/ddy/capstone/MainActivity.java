package com.ddy.capstone;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.os.Build.ID;

public class MainActivity extends AppCompatActivity {
    public static MainActivity I;

    DatabaseReference mPostReference;

    boolean isConnected;

    EditText editText_serialNumber;
    TextView textView_isConnected;
    Button button_connect;
    Button button_createToken;
    ImageView imageView_live;

    Toast toastMsg;

    String channelId;
    String channelName;
    NotificationManager notifManager;
    NotificationCompat.Builder builder;

    String deviceToken = "";

    StorageReference gsReference;

    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        I = this;

        toastMsg = Toast.makeText(this, "msg", Toast.LENGTH_LONG);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        ConnectView();
        ConnectEvent();

        SetNotification();

        isConnected = false;

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new Instance ID token
                        deviceToken = task.getResult().getToken();;
                        Log.d("MainActivity", "Refreshed token: " + deviceToken);
                    }
                });

        mPostReference = FirebaseDatabase.getInstance().getReference();
        mPostReference.child(deviceToken).addValueEventListener(postListener);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        gsReference = storage.getReferenceFromUrl("gs://capstone-liunx0.appspot.com/testVideo.mp4");

        //VideoView videoView = (VideoView)findViewById(R.id.videoView_blockBox);
        //
        //videoView.setVideoURI(Uri.parse("https://firebasestorage.googleapis.com/v0/b/capstone-liunx0.appspot.com/o/testVideo.mp4?alt=media&token=c016d508-2107-4ded-92fb-04d8bf78810d"));
                // videoView.setVideoURL(url);
                //
        //videoView.draw();
        //videoView.setVideoURI(Uri.parse("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov"));
        //videoView.setVideoURI(Uri.parse("192.168.0.5:5000/calc"));
        //final MediaController mediaController =
        //        new MediaController(this);
        //videoView.setMediaController(mediaController);

        new Thread(new Runnable() {
            public void run() {
                try {
                    bitmap = getBitmap("http://cnlab.hanyang.ac.kr/static/images/CNLAB_basic_compressed.png");
                }
                catch(Exception e) { }
                finally {
                    if(bitmap!=null) {
                        runOnUiThread(new Runnable() {
                            @SuppressLint("NewApi")
                            public void run() {
                                toastMsg.setText("run thread");
                                toastMsg.show();
                                imageView_live.setImageBitmap(bitmap);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private Bitmap getBitmap(String url) {
        URL imgUrl = null;
        HttpURLConnection connection = null;
        InputStream is = null;

        Bitmap retBitmap = null;

        try{
            imgUrl = new URL(url);
            connection = (HttpURLConnection) imgUrl.openConnection();
            connection.setDoInput(true); //url로 input받는 flag 허용
            connection.connect(); //연결
            is = connection.getInputStream(); // get inputstream
            retBitmap = BitmapFactory.decodeStream(is);
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            if(connection!=null) {
                connection.disconnect();
            }
            return retBitmap;
        }
    }

    ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                String getSerial = snapshot.getValue().toString();

                if(getSerial.startsWith("{Serial")) {
                    getSerial = getSerial.substring(8, getSerial.length() - 1);

                    textView_isConnected.setText("connected");
                    textView_isConnected.setTextColor(Color.GREEN);
                    editText_serialNumber.setText(getSerial);
                    editText_serialNumber.setEnabled(false);
                    isConnected = true;
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    void ConnectView() {
        textView_isConnected = (TextView)findViewById(R.id.textView_isConnected);
        editText_serialNumber = (EditText)findViewById(R.id.editText_serialNumber);
        button_connect = (Button)findViewById(R.id.button_connect);
        button_createToken = (Button)findViewById(R.id.button_createToken);
        imageView_live = (ImageView)findViewById(R.id.imageView_live);
    }

    void ConnectEvent() {
        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Connection Not-Connection Check
                Map<String, Object> deviceList = new HashMap<>();
                Map<String, Object> deviceListValue = new HashMap<>();
                //Notification Check
                Map<String, Object> serialList = new HashMap<>();
                Map<String, Object> serialListValue = new HashMap<>();

                isConnected = !isConnected;

                if(isConnected) {
                    textView_isConnected.setText("connected");
                    textView_isConnected.setTextColor(Color.GREEN);
                    editText_serialNumber.setEnabled(false);

                    deviceListValue.put("Serial", editText_serialNumber.getText().toString());
                    serialListValue.put("Device", deviceToken);

                    builder.setContentText("Success Connect");
                }
                else if(!isConnected) {
                    textView_isConnected.setText("not connected");
                    textView_isConnected.setTextColor(Color.RED);
                    editText_serialNumber.setEnabled(true);

                    builder.setContentText("Disconnect");
                }

                deviceList.put(deviceToken , deviceListValue);
                serialList.put(editText_serialNumber.getText().toString(), serialListValue);

                mPostReference.updateChildren(deviceList);
                mPostReference.updateChildren(serialList);

                notifManager.notify(0, builder.build());
            }
        });

        button_createToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public void SendNotification(String str) {
        toastMsg.setText(str);
        toastMsg.show();

        //Toast.makeText(MainActivity.thi)
        builder.setContentText("firebase pushed");
        notifManager.notify(0, builder.build());
    }

    void SetNotification() {
        channelId = "channel";
        channelName = "Channel Name";

        notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notifManager.createNotificationChannel(mChannel);
        }

        builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

        Intent notificationIntent = new Intent(getApplicationContext() , MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //int requestID = (int) System.currentTimeMillis();

        //PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
        //        requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentTitle("Notification Car InnerBlackBox") // required
                .setContentText("People In Car emergency life of he")  // required
                .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                .setAutoCancel(true) // 알림 터치시 반응 후 삭제
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground))
                .setBadgeIconType(R.drawable.ic_launcher_foreground)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(),
                        (int)System.currentTimeMillis(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
