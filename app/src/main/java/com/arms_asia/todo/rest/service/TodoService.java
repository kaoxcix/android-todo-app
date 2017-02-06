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

//    // Project API
//    @GET("project/getAllProject")
//    Call<List<Project>> getAllProject();
//
//    @POST("project/create")
//    Call<Project> createProject(@Body Project project);
//
//    @POST("project/update")
//    Call<Project> updateProject(@Body Project project);
//
//    @DELETE("project/delete/{id}")
//    Call<Void> deleteProject(@Path("id") int id);
//
//    // Task API
//    @GET("getTask/{id}")
//    Call<List<Task>> getTask(@Path("id") int id);
//
//    @POST("create")
//    Call<Task> createTask(@Body Task task);
//
//    @POST("update/{id}/{status}")
//    Call<Task> updateTaskStatus(@Path("id") int id, @Path("status") boolean status);
//
//    @DELETE("delete/{id}")
//    Call<Void> deleteTask(@Path("id") int id);
}
