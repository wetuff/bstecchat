package actiondev.weme.bstec;

import android.app.AlarmManager;
import android.os.Handler;
import android.util.Log;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationChannel;
import android.support.v4.app.NotificationCompat;
import android.webkit.JavascriptInterface;
import android.app.Service;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends Activity {


    private WebView mWebView;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";

    public int CHAT_ID = 0;
    public String CHAT_LAST_MESSAGE = null;

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        System.out.println("onStop called");

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new WebViewJavaScriptInterface(this), "AndroidInterface");
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                //finishNotification();
                scheduleAlarm();
            }
        }, 10000);
    }

    // Setup a recurring alarm every half hour
    public void scheduleAlarm() {

        long firstMillis = System.currentTimeMillis(); // alarm is set right away

        Intent intent = new Intent(getApplicationContext(), BackgroundService.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 10000, pintent);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();  // Always call the superclass method first
        System.out.println("onDestroy called");
        finishNotification();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //startService(new Intent(this, StartChatBstec.class));

        mWebView = (WebView) findViewById(R.id.activity_main_webview);

        // Force links and redirects to open in the WebView instead of in a browser
        mWebView.setWebViewClient(new WebViewClient());

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new WebViewJavaScriptInterface(this), "AndroidInterface");
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        // Use remote resource
        mWebView.loadUrl("https://chat.bstecbosch.com.br/");

        // Stop local links and redirects from opening in browser instead of WebView
        mWebView.setWebViewClient(new MyAppWebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());

        // Use local resource
        // mWebView.loadUrl("file:///android_asset/www/index.html");

        // Construct our Intent specifying the Service
        Intent i = new Intent(this, BackgroundService.class);
        startService(i);
        scheduleAlarm();

        // active JobSchedulerReceiver
        Intent intent = new Intent();
        intent.setAction("actiondev.weme.bstec.MyAlarmReceiver");

        //IntentService iserv = new IntentService();

        sendBroadcast(intent);

    }

    // Prevent the back-button from closing the bstec
    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            //mWebView.goBack();
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * JavaScript Interface. Web code can access methods in here
     * (as long as they have the @JavascriptInterface annotation)
     */
    public class WebViewJavaScriptInterface{

        private Context context;

        /*
         * Need a reference to the context in order to sent a post message
         */
        public WebViewJavaScriptInterface(Context context){
            this.context = context;
        }

        /*
         * This method can be called from Android. @JavascriptInterface
         * required after SDK version 17.
         */
        @JavascriptInterface
        public void notificationChat() {
            //addNotification();
        }

        @JavascriptInterface
        public void lastChatUser(int chatID, String lastMessage) {
            setLastChatUser(chatID, lastMessage);
            CHAT_ID = chatID;
        }

        @JavascriptInterface
        public void getLastToken(int chatID) {

            CHAT_ID = chatID;
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    //mWebView.loadUrl("javascript:updateToken('"+FirebaseInstanceId.getInstance().getToken()+"')");
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        mWebView.evaluateJavascript("updateToken('"+CHAT_ID+"', '"+FirebaseInstanceId.getInstance().getToken()+"')", null);
                    } else {
                        mWebView.loadUrl("javascript:updateToken('"+CHAT_ID+"', '"+FirebaseInstanceId.getInstance().getToken()+"')");
                    }
                    //Log.d("getLastToken","Token: "+FirebaseInstanceId.getInstance().getToken());
                }
            });
        }
    }

    //private void addNotification()
    //{
    //    NotificationCompat.Builder builder =
    //            new NotificationCompat.Builder(this)
    //                    .setSmallIcon(R.mipmap.ic_launcher)
    //                    .setContentTitle("BSTEC")   //this is the title of notification
    //                    .setColor(101)
    //                    .setAutoCancel(true)
    //                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    //                    .setPriority(2)
    //                    .setContentText("Você tem mensagens não lidas no Chat BSTEC.");   //this is the message showed in notification
    //    Intent intent = new Intent(this, MainActivity.class);
    //    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    //    builder.setContentIntent(contentIntent);

    //    //Vibration
    //    builder.setVibrate(new long[] { 500, 1000 });

    //    // Add as notification
    //    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    //    manager.notify(0, builder.build());
    //}


    public void setLastChatUser(int chatID, String lastMessage) {
        CHAT_ID = chatID;
        CHAT_LAST_MESSAGE = lastMessage;

        if(CHAT_LAST_MESSAGE != null) {
            Intent myIntent = new Intent(BackgroundService.UPDATE_INFO);
            myIntent.setPackage("actiondev.weme.bstec");

            myIntent.putExtra("CHAT_ID", chatID);
            myIntent.putExtra("CHAT_LAST_MESSAGE", lastMessage);
            startService(myIntent);

            System.out.println(lastMessage);
        }
    }


    private void addNotification()
    {

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("BSTEC")   //this is the title of notification
                .setColor(101)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(2)
                .setContentText("Você tem mensagens não lidas no Chat BSTEC.");   //this is the message showed in notification
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        //Vibration
        builder.setVibrate(new long[] { 500, 1000 });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "BSTEC Chat", importance);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[] { 500, 1000 });
            assert manager != null;
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            manager.createNotificationChannel(notificationChannel);
        }

        // Add as notification
        manager.notify(0, builder.build());

    }

    public void finishNotification()
    {

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("BSTEC")   //this is the title of notification
                .setColor(101)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(2)
                .setContentText("Lembre-se de voltar ao Chat BSTEC para conferir novas mensagens.");   //this is the message showed in notification
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        //Vibration
        builder.setVibrate(new long[] { 500, 1000 });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[] { 500, 1000 });
            assert manager != null;
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            manager.createNotificationChannel(notificationChannel);
        }

        // Add as notification
        manager.notify(0, builder.build());

    }

    public class StartChatBstec extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            // do your jobs here
            System.out.println("Running StartChatBstec");
            Log.i("StartChatBstec","Running service Chat");

            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public void onTaskRemoved(Intent rootIntent) {
            System.out.println("onTaskRemoved called");
            super.onTaskRemoved(rootIntent);
            //do something you want before bstec closes.
            finishNotification();

            //stop service
            //this.stopSelf();
        }


    }

}