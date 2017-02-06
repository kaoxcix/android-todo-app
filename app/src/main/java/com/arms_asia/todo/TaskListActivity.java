package com.arms_asia.todo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.arms_asia.todo.adapter.TaskAdapter;
import com.arms_asia.todo.helper.ItemClickListener;
import com.arms_asia.todo.rest.RestClient;
import com.arms_asia.todo.rest.model.Tasks;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskListActivity extends AppCompatActivity implements ItemClickListener {

    public static final String PROJECT_ID = "projectId";
    public static final String PROJECT_NAME = "projectName";

    List<Tasks> taskList;
    RecyclerView recyclerViewTask;
    TaskAdapter taskAdapter;
    int projectId;
    String projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        projectId = getIntent().getExtras().getInt(PROJECT_ID);
        projectName = getIntent().getExtras().getString(PROJECT_NAME);

        getSupportActionBar().setTitle(projectName);

        initRecyclerView();
        loadTask();
    }

    private void initRecyclerView() {
        taskList = new ArrayList<>();
        recyclerViewTask = (RecyclerView) findViewById(R.id.rv_task);
        taskAdapter = new TaskAdapter(TaskListActivity.this, taskList);
        taskAdapter.setOnClickListener(this);
        recyclerViewTask.addItemDecoration(new DividerItemDecoration(TaskListActivity.this, DividerItemDecoration.VERTICAL));
        recyclerViewTask.setLayoutManager(new LinearLayoutManager(TaskListActivity.this));
        recyclerViewTask.setAdapter(taskAdapter);
    }

    private void loadTask() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching task");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Call<List<Tasks>> call = RestClient.getTodoService().getTasksByProjectId(projectId);
        call.enqueue(new Callback<List<Tasks>>() {
            @Override
            public void onResponse(Call<List<Tasks>> call, Response<List<Tasks>> response) {
                //Get our list of task
                if(response.body() != null) {
                    taskList.clear();
                    taskList.addAll(response.body());
                    taskAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(TaskListActivity.this, "No any task", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<List<Tasks>> call, Throwable t){
                //Handle on Failure here
                progressDialog.dismiss();
                Toast.makeText(TaskListActivity.this, "Failed to retrieve task", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View view, int position, boolean isLongClick, MotionEvent motionEvent) {
        if (isLongClick) {
            Toast.makeText(view.getContext(), "POS : " + position + " is LONG click", Toast.LENGTH_SHORT).show();
        } else {

        }
    }

}
