package com.example.canbefluent;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.canbefluent.pojoClass.getResult;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFireBaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "onMessageReceived");

        String type = remoteMessage.getData().get(Constants.REMOTE_MSG_TYPE);
        if(type != null){
            Log.e("FCM", "type not null");
            // 영상통화 알림일 경우
            if(type.equals(Constants.REMOTE_MSG_INVITATION)){
                Log.e("FCM", "type invitation");
                Intent intent = new Intent(getApplicationContext(), IncomingInvitationActivity.class);
                intent.putExtra(
                        Constants.REMOTE_MSG_MEETING_TYPE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_TYPE)
                );
                intent.putExtra("user name", remoteMessage.getData().get("user name"));
                intent.putExtra("user profile", remoteMessage.getData().get("user profile"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN)
                );
                intent.putExtra(
                        Constants.REMOTE_MSG_MEETING_ROOM,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_ROOM)
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else if(type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                Intent intent = new Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE);
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITATION_RESPONSE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE)
                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
            else if(type.equals("follow")){
                Log.e("FCM", "type follow");
                sendFollowNotification(remoteMessage.getData().get("user name"));
            }
        }
        else{
            Log.e("FCM", "type null");
            Map<String, String> messageData = remoteMessage.getData();

            Log.e(TAG, "data: " + messageData);

            sendNotification(messageData.get("Nick"), messageData.get("body"), messageData.get("room_index"));

            Log.e(TAG, "nick: " + messageData.get("Nick"));
            Log.e(TAG, "body: " + messageData.get("body"));
            Log.e(TAG, "room_index: " + messageData.get("room_index"));
        }
    }
    // [END receive_message]


    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     * 새토큰이 발행될 때나 토큰이 변경될 때 실행.
     */
    @Override
    public void onNewToken(String token) {
        Log.e(TAG, "Refreshed token: " + token);
        // 앱을 켤때마다 실행된다.

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        // 서버에 토큰을 넘기는 메서드
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
//    private void scheduleJob() {
//        // [START dispatch_job]
//        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
//                .build();
//        WorkManager.getInstance().beginWith(work).enqueue();
//        // [END dispatch_job]
//    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     * 새 토큰이 발행되면 서버에 토큰을 저장시키는 메서드.
     */
    private void sendRegistrationToServer(String token) {
        Log.e(TAG, "sendRegistrationToServer");
        // TODO: Implement this method to send token to your app server.
        sharedPreference sharedPreference = new sharedPreference();
        String user_id = sharedPreference.loadUserId(MyFireBaseMessagingService.this);



        // 새로 발행된 토큰을 서버로 보낸다.
        RetrofitClient retrofitClient = new RetrofitClient();
        Call<getResult> call =  retrofitClient.service.update_token(user_id, token);
        call.enqueue(new Callback<getResult>() {
            @Override
            public void onResponse(Call<getResult> call, Response<getResult> response) {
                Log.e(TAG, "onResponse");
            }

            @Override
            public void onFailure(Call<getResult> call, Throwable t) {
                Log.e(TAG, "onFailure");
            }
        });
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
//     * @param messageBody FCM message body received.
     */
    private void sendNotification(String nick, String message, String room_index) {
        Log.e(TAG, "sendNotification");
        Intent intent = new Intent(this, message_room.class);
        intent.putExtra("type", "from notification");
        intent.putExtra("room_index", room_index);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "choi";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.exchange_img)
                        .setContentTitle(nick)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendFollowNotification(String first_name) {
        Log.e(TAG, "sendNotification");
        Intent intent = new Intent(this, show_follow_activity.class);
//        intent.putExtra("type", "from notification");
//        intent.putExtra("room_index", room_index);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "choi";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.exchange_img)
                        .setContentTitle("팔로우 메세지")
                        .setContentText(first_name + "님이 당신을 팔로우합니다.")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
