package com.example.bus3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {


    //  General
    String token = null;

    private TextView mTextViewResult;
    private TextView viewLate;

    // Morning
    String n = null;
    String n2 = null;
    JSONArray arr = null;
    String n3 = null;

    // Afternoon
    String response_lvl0 = null;
    JSONArray response_lvl1 = null;
    String bus_0 = null;
    String bus_0_time = null;
    String bus_1 = null;
    String bus_1_time = null;
    String bus_2 = null;
    String bus_2_time = null;
    String bus_3 = null;
    String bus_3_time = null;
    private TextView view0;
    private TextView view1;
    private TextView view2;
    private TextView view3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.starting_layout);

        // Weekend control
        String day = LocalDate.now().getDayOfWeek().name();
        if (day.equals("Saturday")) {
            setContentView(R.layout.weekend_layout);
        } else  if (day.equals("Sunday")) {
            setContentView(R.layout.weekend_layout);
        }

        // Weekday continue
        createNotificationChannel();

        //  ------ Generate Access Token  - -- -- - ------
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.vasttrafik.se:443/token";

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("Authorization", "Basic TjJQWkRCQnBfUzNXZEdLQlVBYTY0dVlqaGRrYTpxX05DQUd2RGJ5dkVhaGgwTjQwV2hKTXd4QU1h")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("CREATION", "Token onFailure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    final String myResponse = response.body().string();
                    Log.d("CREATION", "Token onResponse (200)");


                    // Extract token
                    try {
                        JSONObject obj = new JSONObject(myResponse);
                        token = obj.getString("access_token");
                        Log.d("CREATION", "Token generated: "+token);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        token = "Something wrong";
                        Log.d("CREATION", "Something Wrong");
                    }


                    MainActivity.this.runOnUiThread(() -> {


                        //  ------ Morning  - -- -- - ------
                        Timer timer = new Timer();
                        int INTERVAL_MSEC = 1000 * 60 * 60 * 24;

                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(System.currentTimeMillis());
                        cal.set(Calendar.HOUR_OF_DAY, 5);
                        cal.set(Calendar.MINUTE, 55);
                        if(Calendar.getInstance().after(cal)){
                            // Move to tomorrow
                            cal.add(Calendar.DATE, 1);
                        }
                        Date alarmTime = cal.getTime();


                        Log.d("CREATION", "Alarm time: " + alarmTime.toString());

                        // Scheduling task. Each day at 05.55
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        morning(token);
                                    }
                                });
                            }
                        };
                        timer.scheduleAtFixedRate(task, alarmTime, INTERVAL_MSEC);


                        //  ------ Afternoon  - -- -- - ------
                        Integer hour = Integer.parseInt(LocalTime.now().toString().substring(0, 2));
                        if (hour < 19) {
                            if (hour > 13) {
                                afternoon(token);
                            } else {
                                setContentView(R.layout.work_layout);
                            }
                        } else {
                            setContentView(R.layout.home_layout);
                        }

                    });


                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_refresh) {

            Integer hour = Integer.parseInt(LocalTime.now().toString().substring(0, 2));
            if (hour < 7) {
                morning(token);
            } else {
                if (hour < 19) {
                    if (hour > 13) {
                        afternoon(token);
                    } else {
                        setContentView(R.layout.work_layout);
                    }
                } else {
                    setContentView(R.layout.home_layout);
                }

                view0 = findViewById(R.id.uppd);
                view0.setText("Uppd kl.: " + LocalTime.now().toString().substring(0, 8));


                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }




    public void morning(String token) {



        Log.d("CREATION", "START MORNING");
        setContentView(R.layout.activity_main);


        // Request
        String today = LocalDate.now().toString();
        Log.d("CREATION", "Today's date: "+today);

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.vasttrafik.se/bin/rest.exe/v2/departureBoard?" +
                "id=9021014005440000&" +
                "date=" + today + "&" +
                "time=06%3A15&" +
                "timeSpan=60&" +
                "direction=9021014007488000&" +
                "format=json";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        Log.d("CREATION", url);

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("CREATION", "onFailure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    final String myResponse = response.body().string();
                    Log.d("CREATION", "onResponse");

                    // Getting departure time
                    try {
                        JSONObject obj = new JSONObject(myResponse);
                        n = obj.getString("DepartureBoard");
                        JSONObject obj2 = new JSONObject(n);
                        arr = obj2.getJSONArray("Departure");
                        n2 = arr.getString(0);
                        JSONObject obj3 = new JSONObject(n2);
                        n3 = obj3.getString("rtTime");
                        Log.d("CREATION", "Korrekt");

                    } catch (JSONException e) {
                        e.printStackTrace();
                        n3 = "Something wrong";
                        Log.d("CREATION", "Something Wrong");
                    }
                    MainActivity.this.runOnUiThread(() -> {
                        mTextViewResult = findViewById(R.id.time);
                        mTextViewResult.setText(n3);
                        Log.d("CREATION", "Avgår: " + n3);

                        String minutes = n3.substring(Math.max(n3.length() - 2, 0));
                        Integer actual_time = Integer.parseInt(minutes);
                        Integer right_time = 16;
                        Integer min_late = actual_time - right_time;

                        viewLate = findViewById(R.id.late);
                        viewLate.setText(min_late.toString());
                        //push_notification(n3, min_late.toString());

                    });
                }
            }
        });

    }

    public void afternoon(String token) {
        Log.d("CREATION", "START Afternon");
        setContentView(R.layout.afternoon_layout);

        // Request
        String now_date = LocalDate.now().toString();
        String now_time = LocalTime.now().toString().substring(0, 5);
        String proc_now_time = now_time.substring(0, 2) + "%3A" + now_time.substring(3);

        Log.d("CREATION", "Today's date " + now_date);
        Log.d("CREATION", "Time right now " + proc_now_time);

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.vasttrafik.se/bin/rest.exe/v2/departureBoard?" +
                "id=9021014007486000&" +
                "date=" + now_date + "&" +
                "time=" + proc_now_time + "&" +
                "timeSpan=60&" +
                "direction=9021014005440000&" +
                "format=json";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();


        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("CREATION", "onFailure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    final String myResponse = response.body().string();
                    Log.d("CREATION", "onResponse (200)");
                    Log.d("CREATION", myResponse);


                    // Getting departure time
                    try {
                        JSONObject obj = new JSONObject(myResponse);
                        response_lvl0 = obj.getString("DepartureBoard");
                        JSONObject obj2 = new JSONObject(response_lvl0);
                        response_lvl1 = obj2.getJSONArray("Departure");
                        bus_0 = response_lvl1.getString(0);
                        JSONObject bus_0_obj = new JSONObject(bus_0);
                        bus_0_time = bus_0_obj.getString("rtTime");
                        Log.d("CREATION", "Bus 0: " + bus_0_time);
                        bus_1 = response_lvl1.getString(1);
                        JSONObject bus_1_obj = new JSONObject(bus_1);
                        bus_1_time = bus_1_obj.getString("rtTime");
                        Log.d("CREATION", "Bus 1: " + bus_1_time);
                        bus_2 = response_lvl1.getString(2);
                        JSONObject bus_2_obj = new JSONObject(bus_2);
                        bus_2_time = bus_2_obj.getString("rtTime");
                        Log.d("CREATION", "Bus 2: " + bus_2_time);
                        bus_3 = response_lvl1.getString(3);
                        JSONObject bus_3_obj = new JSONObject(bus_3);
                        bus_3_time = bus_3_obj.getString("rtTime");
                        Log.d("CREATION", "Bus 3: " + bus_3_time);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        n3 = "Something wrong";
                        Log.d("CREATION", "Something Wrong");
                    }


                    MainActivity.this.runOnUiThread(() -> {
                        view0 = findViewById(R.id.bus_0);
                        view0.setText(bus_0_time);
                        view1 = findViewById(R.id.bus_1);
                        view1.setText(bus_1_time);
                        view2 = findViewById(R.id.bus_2);
                        view2.setText(bus_2_time);
                        view3 = findViewById(R.id.bus_3);
                        view3.setText(bus_3_time);


                    });


                }
            }
        });
    }

    public void push_notification(String time, String late) {
        // builder constructor (innehåll)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "id_1")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Bus 242")
                .setContentText(late + " minutes late, leaves at " + time)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // manager constructor
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
        Log.d("CREATION", "Notification");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel_name";
            String description = "channel_description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("id_1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Log.d("CREATION", "Notification channel set up");
        }
    }



}



/*
Kvar att göra:
Testa:
    - token generation
    - afternoon
    - morning, timer och notification (kan köras mellan 8-9)
 */

