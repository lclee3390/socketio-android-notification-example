package com.example.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MainActivity extends AppCompatActivity {
    String CHAT_SERVER_URL = "http://10.0.2.2:3000";
    Socket socket = null;
    Emitter.Listener connectListener = null;
    int mId = 1234;
    String username = "aaa";
    WebView webview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);

        setContentView(webview);
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl("http://google.com.tw");



        IO.Options options = new IO.Options();
        options.reconnection = true;

        if(socket==null){
            try {
//                socket = IO.socket(CHAT_SERVER_URL,options);
                socket = IO.socket(CHAT_SERVER_URL);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

//        connectListener = new Emitter.Listener() {
//            @Override
//            public void call(Object... args) {
//            }
//        };
//        socket.on(Socket.EVENT_CONNECT, connectListener);


            socket.connect();
            JSONObject data = new JSONObject();

            try {
                data.put("username",username);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            socket.emit("login",data);
            socket.on("notification", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("data=>"+args[0]);
                    try {
                        JSONObject obj = (JSONObject) args[0];
                        showNotification(obj.getString("title"), obj.getString("text"));
                        try {
                            socket.emit("ok",new JSONObject().put("notificationId",obj.getString("notificationId")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(socket!=null){
            socket.close();
        }
    }

    private void showNotification(String title, String text){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(title)
                        .setContentText(text);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
    }
}
