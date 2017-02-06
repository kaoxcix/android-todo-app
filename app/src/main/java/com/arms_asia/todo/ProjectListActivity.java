package com.arms_asia.todo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.arms_asia.todo.adapter.ProjectAdapter;
import com.arms_asia.todo.helper.ItemClickListener;
import com.arms_asia.todo.rest.RestClient;
import com.arms_asia.todo.rest.model.Projects;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectListActivity extends AppCompatActivity implements ItemClickListener {
    List<Projects> projectList;
    RecyclerView recyclerViewProject;
    ProjectAdapter projectAdapter;
    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProject();
            }
        });

        initRecyclerView();
        loadProject();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_project_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reload_project_list) {
            loadProject();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView() {
        projectList = new ArrayList<>();
        recyclerViewProject = (RecyclerView) findViewById(R.id.rv_project);
        projectAdapter = new ProjectAdapter(ProjectListActivity.this, projectList);
        projectAdapter.setOnClickListener(this);
        recyclerViewProject.addItemDecoration(new DividerItemDecoration(ProjectListActivity.this, DividerItemDecoration.VERTICAL));
        recyclerViewProject.setLayoutManager(new LinearLayoutManager(ProjectListActivity.this));
        recyclerViewProject.setAdapter(projectAdapter);
    }

    private void loadProject() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching projects");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Call<List<Projects>> call = RestClient.getTodoService().getProjects();
        call.enqueue(new Callback<List<Projects>>() {
            @Override
            public void onResponse(Call<List<Projects>> call, Response<List<Projects>> response) {
                //Get our list of project
            if(response.body().size() > 0) {
                projectList.clear();
                projectList.addAll(response.body());
                projectAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            } else {
                progressDialog.dismiss();
                Toast.makeText(ProjectListActivity.this, "No any project", Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void onFailure(Call<List<Projects>> call, Throwable t){
            //Handle on Failure here
            progressDialog.dismiss();
            Toast.makeText(ProjectListActivity.this, "Failed to retrieve project", Toast.LENGTH_LONG).show();
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
                Intent intent = new Intent(ProjectListActivity.this, TaskListActivity.class);
                intent.putExtra(TaskListActivity.PROJECT_ID, projectList.get(position).getId());
                intent.putExtra(TaskListActivity.PROJECT_NAME, projectList.get(position).getName());
                startActivity(intent);
            }
        }
    }

    private void addProject() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ProjectListActivity.this);
        final View view = layoutInflaterAndroid.inflate(R.layout.dialog_project, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectListActivity.this);
        builder.setView(view);
        final EditText editTextProjectName = (EditText) view.findViewById(R.id.edt_project_name);
        builder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_positive_button),
                        new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialogBox, int id) {
                        dialogBox.dismiss();

                        final ProgressDialog progressDialog = new ProgressDialog(ProjectListActivity.this);
                        progressDialog.setMessage("Adding a new projects");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        String projectName = editTextProjectName.getText().toString().trim();
                        if(projectName.length() > 0) {
                            Projects project = new Projects();
                            project.setName(projectName);
                            Call<Projects> call = RestClient.getTodoService().createProject(project);
                            call.enqueue(new Callback<Projects>() {
                                @Override
                                public void onResponse(Call<Projects> call, Response<Projects> response) {
                                    if (response.isSuccessful()) {
                                        loadProject();
                                        progressDialog.dismiss();
                                    }
                                }


                                @Override
                                public void onFailure(Call<Projects> call, Throwable t) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ProjectListActivity.this, "Failed to add a new project", Toast.LENGTH_LONG).show();
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
        projectAdapter.toggleSelection(position);//Toggle the selection

        boolean hasCheckedItems = projectAdapter.getSelectedCount() > 0;//Check if any items are already selected or not

        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = startSupportActionMode(new ToolbarActionModeCallback(ProjectListActivity.this, projectAdapter));


        } else if (!hasCheckedItems && mActionMode != null) {
            // there no selected items, finish the actionMode
            mActionMode.finish();
        }

        if (mActionMode != null) {
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(projectAdapter.getSelectedCount()) + " selected");

            Menu menu = mActionMode.getMenu();
            if(projectAdapter.getSelectedCount() > 1) {
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
        private ProjectAdapter projectAdapter;

        public ToolbarActionModeCallback(Context context, ProjectAdapter projectAdapter) {
            this.context = context;
            this.projectAdapter = projectAdapter;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_menu_project, menu);//Inflate the menu over action mode
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
                    final View view = layoutInflaterAndroid.inflate(R.layout.dialog_project, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(view);

                    final SparseBooleanArray selectedE = projectAdapter.getSelectedIds();

                    final EditText editTextProjectName = (EditText) view.findViewById(R.id.edt_project_name);
                    editTextProjectName.setText(String.valueOf(projectList.get(selectedE.keyAt(0)).getName()));
                    builder
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    int projectId = projectList.get(selectedE.keyAt(0)).getId();
                                    String projectName = editTextProjectName.getText().toString().trim();
                                    if(projectName.length() > 0) {
                                        Projects project = new Projects();
                                        project.setName(projectName);

                                        Call<Projects> call = RestClient.getTodoService().updateProject(projectId, project);
                                        call.enqueue(new Callback<Projects>() {
                                            @Override
                                            public void onResponse(Call<Projects> call, Response<Projects> response) {
                                                if (response.isSuccessful()) {
                                                    loadProject();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<Projects> call, Throwable t) {

                                            }
                                        });
                                    }
                                }
                            })

                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogBox, int id) {
                                            dialogBox.cancel();
                                        }
                                    });

                    AlertDialog alertDialogAndroid = builder.create();
                    alertDialogAndroid.show();
                    break;
                case R.id.action_delete:
                    final SparseBooleanArray selectedD = projectAdapter.getSelectedIds();

                    final ProgressDialog progressDialog = new ProgressDialog(ProjectListActivity.this);
                    progressDialog.setMessage("Adding a new projects");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                        for (int i = 0; i < selectedD.size(); i++) {
                            if (selectedD.valueAt(i)) {
                                final int key = selectedD.keyAt(i);

                                Call<Void> callT = RestClient.getTodoService().deleteProject(projectList.get(key).getId());
                                final int finalI = i;
                                callT.enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {

                                        if(finalI == (selectedD.size() -1)) {
                                            progressDialog.dismiss();
                                            mode.finish();
                                            loadProject();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        if(finalI == (selectedD.size() -1)) {
                                            progressDialog.dismiss();
                                            mode.finish();
                                            loadProject();
                                        }
                                    }
                                });
                            }
                        }


                    break;
            }
            return false;
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {
            projectAdapter.removeSelection();
            if (mActionMode != null)
                mActionMode = null;
        }
    }
}
