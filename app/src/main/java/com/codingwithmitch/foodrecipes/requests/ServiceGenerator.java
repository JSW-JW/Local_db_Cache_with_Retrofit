package com.codingwithmitch.foodrecipes.requests;

import com.codingwithmitch.foodrecipes.util.Constants;
import com.codingwithmitch.foodrecipes.util.LiveDataCallAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    private static OkHttpClient client = new OkHttpClient().newBuilder()
            // time to establish connection to server
            .connectTimeout(Constants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            // time between each byte read from server
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            // time between each byte sent to server
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build();

    private final static Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    private static Retrofit.Builder retrofitBuilder =
            new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create(gson));

    // when searchApi is executed, is the ResponseType given automatically to Converter ?
    // Or is there separate part that notifies the Converter which type of Response it is?

    private static Retrofit retrofit = retrofitBuilder.build();

    private static RecipeApi recipeApi = retrofit.create(RecipeApi.class);

    public static RecipeApi getRecipeApi(){
        return recipeApi;
    }
}
