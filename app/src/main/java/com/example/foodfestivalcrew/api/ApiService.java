package com.example.foodfestivalcrew.api;

import com.example.foodfestivalcrew.model.FoodItem;
import com.example.foodfestivalcrew.model.FoodListResponse;
import com.example.foodfestivalcrew.model.IngredientResponse;
import com.example.foodfestivalcrew.model.LoginRequest;
import com.example.foodfestivalcrew.model.LoginResponse;
import com.example.foodfestivalcrew.model.PackagingResponse;
import com.example.foodfestivalcrew.model.StatusResponse;
import com.example.foodfestivalcrew.model.TokenResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("food")
    Call<FoodListResponse> getFood(
            @Header("x-token") String token,
            @Query("month") int month,
            @Query("year") int year
    );

    @GET("ingredients")
    Call<IngredientResponse> getIngredients(
            @Header("x-token") String token,
            @Query("food_id") int foodId
    );

    @GET("status")
    Call<StatusResponse> getStatus(@Header("x-token") String token);

    @GET("packaging")
    Call<PackagingResponse> getPackaging(
            @Header("x-token") String token,
            @Query("option") String option
    );

    @GET("token")
    Call<TokenResponse> getToken();
}