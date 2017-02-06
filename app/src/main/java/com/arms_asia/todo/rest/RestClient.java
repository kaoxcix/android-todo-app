package com.arms_asia.todo.rest;

import com.arms_asia.todo.rest.service.TodoService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {
    private static final String BASE_URL = "http://192.168.57.1:50002";
    private static TodoService todoService;

    public static TodoService getTodoService() {
        if (todoService == null) {

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                    .create();

            Retrofit client = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            todoService = client.create(TodoService.class);
        }

        return todoService;
    }
}
