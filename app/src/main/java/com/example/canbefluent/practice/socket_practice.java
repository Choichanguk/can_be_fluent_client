package com.example.canbefluent.practice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.canbefluent.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class socket_practice extends AppCompatActivity {
    private static final String TAG = "socket_practice";
    Socket socket = new Socket();
//    Scanner sc = new Scanner(System.in);

    InputStream is = null;
    InputStreamReader isr = null;
    BufferedReader br = null;

    OutputStream os = null;
    OutputStreamWriter osw = null;
    PrintWriter pw = null;

    Button btn;
    EditText textView;
    String message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_practice);
        textView = findViewById(R.id.text);
        btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = textView.getText().toString();

            }
        });
//        Client("3.34.44.183", 3333);
        connect connect = new connect();
        connect.start();

    }

    class connect extends Thread{
        public void run(){
            try {
//
                socket = new Socket("3.34.44.183", 3333);    // 소켓 서버에 접속
                Log.e(TAG, "socket 서버에 접속 성공");


                while (true) {

                    is = socket.getInputStream();
                    isr = new InputStreamReader(is, "UTF-8");
                    br = new BufferedReader(isr);

                    os = socket.getOutputStream();
                    osw = new OutputStreamWriter(os, "UTF-8");
                    pw = new PrintWriter(osw, true);

                    // 읽는거
//                    System.out.print(">>");
                    String data = message;

                    if ("exit".equals(data))
                        break;

                    pw.println(data);

                    data = br.readLine();
//                    System.out.println("<< " + data);

                }

//                // OutputStream - 클라이언트에서 Server로 메세지 발송
//                OutputStream out = socket.getOutputStream();
//                // socket의 OutputStream 정보를 OutputStream out에 넣은 뒤
//                PrintWriter writer = new PrintWriter(out, true);
//                // PrintWriter에 위 OutputStream을 담아 사용
//
//                writer.println("CLIENT TO SERVER");
//                // 클라이언트에서 서버로 메세지 보내기
//
//                // InputStream - Server에서 보낸 메세지 클라이언트로 가져옴
//                InputStream input = socket.getInputStream();
//                // socket의 InputStream 정보를 InputStream in에 넣은 뒤
//                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//                // BufferedReader에 위 InputStream을 담아 사용
//
//                Log.e(TAG, reader.readLine());   // 서버에서 온 메세지 확인
//                Log.e(TAG, "CLIENT SOCKET CLOSE");



//                socket.close(); // 소켓 종료

            } catch (IOException e) {
                // 소켓 생성 과정에서 I/O 에러 발생.
                Log.e(TAG, String.valueOf(e));
                e.printStackTrace();
            } catch (SecurityException se) {
                se.printStackTrace();
                Log.e(TAG, "SecurityException 에러");
                // security manager에서 허용되지 않은 기능 수행.
            }
            finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

//                sc.close();
            }
        }
    }

    public void Client(String address, int port) {
        try {
            Socket socket = new Socket(address, port);
            Log.e("Client", port + "번 포트로 클라이언트 시작됨");



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}