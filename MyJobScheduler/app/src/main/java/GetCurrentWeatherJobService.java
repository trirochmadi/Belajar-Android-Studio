package com.example.admin.myjobscheduler;

import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.dicoding.myjobscheduler.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class GetCurrentWeatherJobService extends JobService{
    public static final String TAG = GetCurrentWeatherJobService.class.getSimpleName();
    final String APP_ID ="37089e8d8d4957742660723459b67e90";
    final String CITY = "Sleman";
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob() Executed");
        getCurrentWeather(params);
        return true;
    }

    private void getCurrentWeather(final JobParameters job) {
        Log.d(TAG,"Running");
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://api.openweathermap.org/data/2.5/weather?q="+CITY+"&appid="+APP_ID;
        Log.e(TAG, "getCurrentWeather: "+url );
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG, result);
                try {
                    JSONObject responObject = new JSONObject(result);
                    String currentWeather = responObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = responObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin = responObject.getJSONObject("main").getDouble("temp");
                    double temInCelcius = tempInKelvin - 273;
                    String temperatur = new DecimalFormat("##.##").format(temInCelcius);
                    String title = "Current Weather";
                    String message = currentWeather +", "+description+" with "+temperatur+" celcius";
                    int notifID = 100;
                    showNotification(getApplication(), title, message, notifID);
                    jobFinished(job, false);
                } catch (Exception e){
                    jobFinished(job, true);
                    e.printStackTrace();
                }
            }

            private void showNotification(Context context, String title, String message, int notifId){
                NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setContentTitle(title)
                        .setSmallIcon(R.drawable.baseline_replay_30_black_24dp)
                        .setContentText(message)
                        .setColor(ContextCompat.getColor(context, android.R.color.black))
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                        .setSound(alarmSound);
                notificationManagerCompat.notify(notifId, builder.build());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                jobFinished(job, true);
            }
        });
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "OnStopJob() Executed");
        return true;
    }
}
