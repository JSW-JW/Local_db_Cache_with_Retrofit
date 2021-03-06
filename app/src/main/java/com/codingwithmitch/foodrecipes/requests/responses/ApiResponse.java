package com.codingwithmitch.foodrecipes.requests.responses;

import android.util.Log;

import java.io.IOException;

import retrofit2.Response;

public class ApiResponse<T> {

    private static final String TAG = "ApiResponse";

    public ApiResponse<T> create(Throwable error) {
        return new ApiErrorResponse<>(!error.getMessage().equals("") ? error.getMessage() : "Unknown error\nCheck network connection");
    }

    public ApiResponse<T> create(Response<T> response) {
        if(response.isSuccessful()) {
            T body = response.body();

            if( body == null || response.code() == 204) { // 204 is empty response code.
                Log.d(TAG, "create: ApiEmptyResponse");
                return new ApiEmptyResponse<>();
            }
            else {
                return new ApiSuccessResponse<>(response.body());
            }
        }
        else {
            String errorMessage = "";
            try {
                errorMessage = response.errorBody().string();
            } catch (IOException e) {
                e.printStackTrace();
                errorMessage = response.message();
            }
            return new ApiErrorResponse<>(errorMessage);
        }
    }

    public class ApiSuccessResponse<T> extends ApiResponse<T> {

        private T body;

        ApiSuccessResponse(T body) {
            this.body = body;
        }

        public T getBody() {
            return body;
        }
    }

    public class ApiErrorResponse<T> extends ApiResponse<T> {

        private String errorMessage;

        ApiErrorResponse(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage(){
            return errorMessage;
        }

    }

    public class ApiEmptyResponse<T> extends ApiResponse<T> {};

}
