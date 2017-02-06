package com.arms_asia.todo.rest.service;

import com.arms_asia.todo.rest.model.Projects;
import com.arms_asia.todo.rest.model.Tasks;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TodoService {
    @GET("projects/")
    Call<List<Projects>> getProjects();

    @POST("projects/")
    Call<Projects> createProject(@Body Projects project);

    @PUT("projects/{id}")
    Call<Projects> updateProject(@Path("id") int id, @Body Projects project);

    @DELETE("projects/{id}")
    Call<Void> deleteProject(@Path("id") int id);

    @GET("tasks/")
    Call<List<Tasks>> getTasksByProjectId(@Query("projectId") int projectId);

    @POST("tasks/")
    Call<Tasks> createTask(@Body Tasks task);

    @PUT("tasks/{id}")
    Call<Tasks> updateTask(@Path("id") int id, @Body Tasks task);

    @DELETE("tasks/{id}")
    Call<Void> deleteTask(@Path("id") int id);
}
