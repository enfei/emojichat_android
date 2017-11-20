package com.mema.muslimkeyboard.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

/**
 * Created by cloudstream on 6/4/17.
 */

public class SendPushTask extends AsyncTask<String, Void, String> {
    JSONObject pushJson;

    public SendPushTask(JSONObject pushInfo) {
        pushJson = pushInfo;
    }

    @Override
    protected String doInBackground(String... params) {

        String urlString = "https://fcm.googleapis.com/fcm/send";

        final OkHttpClient client = new OkHttpClient();
        final MediaType JSON = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(JSON, pushJson.toString());

        Request req = new Request.Builder()
                .url(urlString)
                .post(body)
                .addHeader("Authorization", "key=AAAAsO3s-LM:APA91bF2wCvdJIpw4ZfDVatBRBRF9yHVmMf5RT7wJtZ2_BmwJNPgTIViitaNfT6nGVYofjezo41MOQ9lBqsyCmD3SriVmeDs8sYG_msJ5YLx72KQ8cEN7cbw3a5-SrwV5KmhvarQ8RHe")
                .build();
        try {
            Response res = client.newCall(req).execute();
            if (!res.isSuccessful()) {
                throw new UnknownError("Error: " + res.code() + " " + res.body().string());
            }
            Log.d("MainActivity", res.body().toString());
        } catch (Exception e) {
//            send();
            Log.e("Exception", e.toString());
        }
        return null;
    }


}
