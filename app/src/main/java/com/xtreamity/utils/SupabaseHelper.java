package com.xtreamity.utils;

import androidx.annotation.NonNull;

import okhttp3.*;

import java.io.IOException;

public class SupabaseHelper {
    private static final String SUPABASE_URL = "https://pnktnulxxfoipfbgwmjm.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBua3RudWx4eGZvaXBmYmd3bWptIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkxODk3NzIsImV4cCI6MjA1NDc2NTc3Mn0.HjkhxFV45gITRsmROt5ZuRnJL8Jm8ITIUDNwTcQ6V_Q";
    private static final String STORAGE_BUCKET = "SERVERS";

    private static final OkHttpClient client = new OkHttpClient();

    public interface Callback {
        void onSuccess(byte[] data);

        void onFailure(String errorMessage);
    }

    public static void downloadFile(String fileName, Callback callback) {
        String url = SUPABASE_URL + "/storage/v1/object/public/" + STORAGE_BUCKET + "/" + fileName;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {  // ✅ Use OkHttp Callback
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e.getMessage());  // Pass error to our custom Callback
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().bytes());  // Pass data to our custom Callback
                } else {
                    callback.onFailure("Failed to fetch file");
                }
            }
        });
    }

}
