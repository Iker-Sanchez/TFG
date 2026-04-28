package com.example.tfg;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // EMULADOR:   http://10.0.2.2:3000/
    // MÓVIL REAL: http://192.168.1.12:3000/
    private static final String BASE_URL = "http://10.0.2.2:3000/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}