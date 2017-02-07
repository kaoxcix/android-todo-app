package com.arms_asia.todo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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

    private List<Tasks> mTaskList;
    private RecyclerView mRecyclerViewTask;
    private TaskAdapter mTaskAdapter;
    private int mProjectId;
    private String mProjectName;
    private ActionMode mActionMode;
    private ProgressDialog mProgressDialog;


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
                addTask();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProjectId = getIntent().getExtras().getInt(PROJECT_ID);
        mProjectName = getIntent().getExtras().getString(PROJECT_NAME);

        getSupportActionBar().setTitle(mProjectName);

        mProgressDialog = new ProgressDialog(TaskListActivity.this);
        mProgressDialog.setMessage("Loading");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        initRecyclerView();
        initTaskList();
    }

    private void initRecyclerView() {
        mTaskList = new ArrayList<>();
        mRecyclerViewTask = (RecyclerView) findViewById(R.id.rv_task);
        mTaskAdapter = new TaskAdapter(TaskListActivity.this, mTaskList);
        mTaskAdapter.setOnClickListener(this);
        mRecyclerViewTask.addItemDecoration(new DividerItemDecoration(TaskListActivity.this, DividerItemDecoration.VERTICAL));
        mRecyclerViewTask.setLayoutManager(new LinearLayoutManager(TaskListActivity.this));
        mRecyclerViewTask.setAdapter(mTaskAdapter);
    }

    private void initTaskList() {
        mProgressDialog.show();
        fetchTask();
    }

    private void fetchTask() {
        mTaskList.clear();

        Call<List<Tasks>> call = RestClient.getTodoService().getTasksByProjectId(mProjectId);
        call.enqueue(new Callback<List<Tasks>>() {
            @Override
            public void onResponse(Call<List<Tasks>> call, Response<List<Tasks>> response) {
                //Get our list of task

                if (response.body() != null) {
                    mTaskList.addAll(response.body());
                    mProgressDialog.dismiss();

                } else {
                    mProgressDialog.dismiss();
                    Toast.makeText(TaskListActivity.this, "Nothing task", Toast.LENGTH_SHORT).show();
                }

                mTaskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Tasks>> call, Throwable t) {
                //Handle on Failure here
                mTaskAdapter.notifyDataSetChanged();
                mProgressDialog.dismiss();
                Toast.makeText(TaskListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view, int position, boolean isLongClick, MotionEvent motionEvent) {
        if (isLongClick) {
            onListItemSelect(position);
        } else {
            //If ActionMode not null select item
            if (mActionMode != null) {
                onListItemSelect(position);
            } else {
            }
        }
    }

    private void addTask() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(TaskListActivity.this);
        final View view = layoutInflaterAndroid.inflate(R.layout.dialog_task, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(TaskListActivity.this);
        builder.setView(view);
        TextView textViewDialogTitle = (TextView) view.findViewById(R.id.tv_dialog_title);
        textViewDialogTitle.setText(getString(R.string.dialog_task_add_title));
        final EditText editTextTaskName = (EditText) view.findViewById(R.id.edt_task_name);
        builder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_positive_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialogBox, int id) {

                                String taskName = editTextTaskName.getText().toString().trim();
                                if (taskName.length() > 0) {
                                    mProgressDialog.show();

                                    Tasks task = new Tasks();
                                    task.setName(taskName);
                                    task.setProjectId(mProjectId);
                                    task.setStatus(false);
                                    Call<Tasks> call = RestClient.getTodoService().createTask(task);
                                    call.enqueue(new Callback<Tasks>() {
                                        @Override
                                        public void onResponse(Call<Tasks> call, Response<Tasks> response) {
                                            fetchTask();
                                        }

                                        @Override
                                        public void onFailure(Call<Tasks> call, Throwable t) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(TaskListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        })

                .setNegativeButton(getString(R.string.dialog_negative_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.dismiss();
                            }
                        });

        AlertDialog alertDialogAndroid = builder.create();
        alertDialogAndroid.show();
    }

    //List item select method
    private void onListItemSelect(int position) {
        mTaskAdapter.toggleSelection(position);//Toggle the selection

        boolean hasCheckedItems = mTaskAdapter.getSelectedCount() > 0;//Check if any items are already selected or not

        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the mActionMode
            mActionMode = startSupportActionMode(new TaskListActivity.ToolbarActionModeCallback(TaskListActivity.this, mTaskAdapter));


        } else if (!hasCheckedItems && mActionMode != null) {
            // there no selected items, finish the mActionMode
            mActionMode.finish();
        }

        if (mActionMode != null) {
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(mTaskAdapter.getSelectedCount()) + " selected");

            Menu menu = mActionMode.getMenu();
            if (mTaskAdapter.getSelectedCount() > 1) {
                menu.findItem(R.id.action_edit).setVisible(false);
                menu.findItem(R.id.action_delete).setVisible(true);
            } else {
                menu.findItem(R.id.action_edit).setVisible(true);
                menu.findItem(R.id.action_delete).setVisible(true);
            }
        }

    }

    public class ToolbarActionModeCallback implements ActionMode.Callback {

        private Context context;
        private TaskAdapter mTaskAdapter;

        ToolbarActionModeCallback(Context context, TaskAdapter mTaskAdapter) {
            this.context = context;
            this.mTaskAdapter = mTaskAdapter;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_menu, menu);//Inflate the menu over action mode
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.findItem(R.id.action_edit).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            return true;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
                    final View view = layoutInflaterAndroid.inflate(R.layout.dialog_task, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(view);

                    final SparseBooleanArray selectedE = mTaskAdapter.getSelectedIds();

                    TextView textViewDialogTitle = (TextView) view.findViewById(R.id.tv_dialog_title);
                    textViewDialogTitle.setText(getString(R.string.dialog_task_edit_title));

                    final EditText editTextTaskName = (EditText) view.findViewById(R.id.edt_task_name);
                    editTextTaskName.setText(String.valueOf(mTaskList.get(selectedE.keyAt(0)).getName()));
                    builder
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {

                                    int taskId = mTaskList.get(selectedE.keyAt(0)).getId();
                                    String taskName = editTextTaskName.getText().toString().trim();
                                    if (taskName.length() > 0) {
                                        mProgressDialog.show();

                                        Tasks task = new Tasks();
                                        task.setName(taskName);

                                        Call<Tasks> call = RestClient.getTodoService().updateTask(taskId, task);
                                        call.enqueue(new Callback<Tasks>() {
                                            @Override
                                            public void onResponse(Call<Tasks> call, Response<Tasks> response) {
                                                fetchTask();
                                            }

                                            @Override
                                            public void onFailure(Call<Tasks> call, Throwable t) {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(TaskListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            })

                            .setNegativeButton(getString(R.string.dialog_negative_button),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogBox, int id) {
                                            dialogBox.dismiss();
                                        }
                                    });

                    AlertDialog alertDialogAndroid = builder.create();
                    alertDialogAndroid.show();
                    break;

                case R.id.action_delete:
                    mProgressDialog.show();

                    final SparseBooleanArray selectedD = mTaskAdapter.getSelectedIds();
                    for (int i = 0; i < selectedD.size(); i++) {
                        if (selectedD.valueAt(i)) {
                            final int key = selectedD.keyAt(i);

                            Call<Void> callT = RestClient.getTodoService().deleteTask(mTaskList.get(key).getId());
                            final int current = i;
                            callT.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (current == (selectedD.size() - 1)) {
                                        fetchTask();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    if (current == (selectedD.size() - 1)) {
                                        fetchTask();
                                    }
                                }
                            });
                        }
                    }

                    break;
            }

            mode.finish();

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mTaskAdapter.removeSelection();
            if (mActionMode != null)
                mActionMode = null;
        }
    }

}
