package com.example.canbefluent;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

public class socket_service extends Service {
    private static final String TAG = "socket_service";
    String sender_index, receiver_index;
    public static Socket client_socket;     //클라이언트의 소켓
    public static ConnectionThread thread;

    boolean isConnect = false; //서버 접속여부를 판별하기 위한 변수
    boolean isRunning=false;    // 어플 종료시 스레드 중지를 위해...

    public socket_service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");

        thread = new ConnectionThread();
        thread.start();

        return super.onStartCommand(intent, flags, startId);


    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            client_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e(TAG, "onDestroy");
    }

    class ConnectionThread extends Thread {

        @Override
        public void run() {
            Log.e(TAG, "ConnectionThread 실행");
            try {
                // 접속한다.
                client_socket = new Socket("13.209.89.109", 3333);
                Log.e(TAG, "client socket 연결 성공");
                // 미리 입력했던 닉네임을 서버로 전달한다.
                // 스트림을 추출
                OutputStream os = client_socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);

                // 닉네임을 송신한다.
                dos.writeUTF("sender_index");
                dos.writeUTF("receiver_index");
//                dos.writeUTF(token);

                Log.e(TAG, "데이터 발신 완료");


                // 접속 상태를 true로 셋팅한다.
                isConnect=true;

                // 메세지 수신을 위한 스레드 가동
                isRunning=true;
//                MessageThread thread=new MessageThread(socket);
//                thread.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 메시지 수신을 위한 스레드
     */
    class MessageThread extends Thread {
        Socket socket;
        DataInputStream dis;

        public MessageThread(Socket socket) {
            Log.e(TAG, "MessageThread 실행");
            try {
                this.socket = socket;
                InputStream is = socket.getInputStream();
                dis = new DataInputStream(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try{
                while (isRunning){
                    // 서버로부터 데이터를 수신받는다.
                    final String msg=dis.readUTF();

                    /**
                     * 서버로부터 메시지 받으면 브로드캐스트로 msg, 발신자 id 날려줌
                     */
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 서버에 데이터를 전달하는 스레드
     */
    public void send_msg(String msg){
        SendToServerThread send_msg = new SendToServerThread(msg);
        send_msg.start();
    }

    class SendToServerThread extends Thread{

        String msg;
        DataOutputStream dos;

        public SendToServerThread(String msg){
            try{

                this.msg=msg;
                OutputStream os=client_socket.getOutputStream();
                dos=new DataOutputStream(os);
            }catch (Exception e){
                Log.e(TAG,  e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Log.e(TAG, "SendToServerThread 실행");
            try{
                // 서버로 데이터를 보낸다.
                dos.writeUTF(msg);

            }catch (Exception e){
                Log.e(TAG,  e.getMessage());
                e.printStackTrace();
            }
        }
    }



    /**
     * 현재 실행중인 activity 이름을 가져오는 메소드
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public String getTopActivityName(){
        String activity_name = "";
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();

        for (ActivityManager.AppTask task : tasks) {
            activity_name = task.getTaskInfo().topActivity.getClassName();
            Log.e(TAG, "stackId: " + task.getTaskInfo().topActivity.getClassName());
        }

        return activity_name;
    }
}