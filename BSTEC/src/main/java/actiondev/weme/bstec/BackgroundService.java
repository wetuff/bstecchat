package actiondev.weme.bstec;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.WebView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class BackgroundService extends IntentService {

    private WebView mWebView;
    private MainActivity mySurfaceView;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";

    public int CHAT_ID = 0;
    public String CHAT_LAST_MESSAGE = null;
    public String LINK = "https://bstecbosch.com.br/validateChat.asmx/ConsultarLista?alpha="+CHAT_ID+"&beta="+CHAT_LAST_MESSAGE;

    static final String UPDATE_INFO = "actiondev.weme.bstec.BackgroundService.updateLastChatUser";

    public BackgroundService() {
        super("test-service");
        //Log.i("BackgroundService","Running service BackgroundService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered
        String action = intent.getAction();
        Log.i("BackgroundService","Running service BackgroundService 4");

        int id = intent.getIntExtra("CHAT_ID", 0);
        String name = intent.getStringExtra("CHAT_LAST_MESSAGE");
        if(name != null){
            updateLastChatUser(id, name);
        }

        checkNewMessages();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //do something you want before bstec closes.

        Intent intent = new Intent("actiondev.weme.bstec.BackgroundService");
        sendBroadcast(intent);

        finishNotification();

        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);
        super.onTaskRemoved(rootIntent);

        //stop service
        //this.stopSelf();
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

    public void updateLastChatUser(int chatID, String lastMessage) {
        CHAT_ID = chatID;
        CHAT_LAST_MESSAGE = lastMessage;
        Log.i("BackgroundService", lastMessage);
    }

    public void checkNewMessages() {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        Log.i("BackgroundService","checkNewMessages");

        if(CHAT_ID != 0 && CHAT_LAST_MESSAGE != null) {

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = "";

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(LINK);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                }
                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e("BackgroundService", "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("BackgroundService", "Error closing stream", e);
                    }
                }
            }

            Log.i("BackgroundService", forecastJsonStr);
            finishNotification();

        }
    }
}