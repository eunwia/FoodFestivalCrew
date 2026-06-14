package com.example.foodfestivalcrew.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // ⚠️ CHANGE THIS to your PC's IP address when testing on a real phone
    // If using emulator keep 10.0.2.2, if real device use your PC's IP e.g. 192.168.1.5
    public static final String BASE_URL = "http://172.20.81.239:8080/api/";

    private static Retrofit instance;

    public static Retrofit getInstance() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    public static ApiService getService() {
        return getInstance().create(ApiService.class);
    }
}