package com.arms_asia.todo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
    private List<Projects> mProjectList;
    private RecyclerView mRecyclerViewProject;
    private ProjectAdapter mProjectAdapter;
    private ActionMode mActionMode;
    private ProgressDialog mProgressDialog;

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

        mProgressDialog = new ProgressDialog(ProjectListActivity.this);
        mProgressDialog.setMessage("Loading");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        initRecyclerView();
        initProjectList();
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
            initProjectList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView() {
        mProjectList = new ArrayList<>();
        mRecyclerViewProject = (RecyclerView) findViewById(R.id.rv_project);
        mProjectAdapter = new ProjectAdapter(ProjectListActivity.this, mProjectList);
        mProjectAdapter.setOnClickListener(this);
        mRecyclerViewProject.addItemDecoration(new DividerItemDecoration(ProjectListActivity.this, DividerItemDecoration.VERTICAL));
        mRecyclerViewProject.setLayoutManager(new LinearLayoutManager(ProjectListActivity.this));
        mRecyclerViewProject.setAdapter(mProjectAdapter);
    }

    private void initProjectList() {
        mProgressDialog.show();
        fetchProject();
    }

    private void fetchProject() {
        mProjectList.clear();

        Call<List<Projects>> call = RestClient.getTodoService().getProjects();
        call.enqueue(new Callback<List<Projects>>() {
            @Override
            public void onResponse(Call<List<Projects>> call, Response<List<Projects>> response) {

                //Get our list of project
                if (response.body() != null) {
                    mProjectList.addAll(response.body());
                    mProgressDialog.dismiss();
                } else {
                    mProgressDialog.dismiss();
                    Toast.makeText(ProjectListActivity.this, "Nothing project", Toast.LENGTH_SHORT).show();
                }

                mProjectAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Projects>> call, Throwable t) {
                //Handle on Failure here
                mProgressDialog.dismiss();
                Toast.makeText(ProjectListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
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
                intent.putExtra(TaskListActivity.PROJECT_ID, mProjectList.get(position).getId());
                intent.putExtra(TaskListActivity.PROJECT_NAME, mProjectList.get(position).getName());
                startActivity(intent);
            }
        }
    }

    private void addProject() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ProjectListActivity.this);
        final View view = layoutInflaterAndroid.inflate(R.layout.dialog_project, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectListActivity.this);
        builder.setView(view);
        TextView textViewDialogTitle = (TextView) view.findViewById(R.id.tv_dialog_title);
        textViewDialogTitle.setText(getString(R.string.dialog_project_add_title));
        final EditText editTextProjectName = (EditText) view.findViewById(R.id.edt_project_name);
        builder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_positive_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialogBox, int id) {

                                String projectName = editTextProjectName.getText().toString().trim();
                                if (projectName.length() > 0) {
                                    mProgressDialog.show();

                                    Projects project = new Projects();
                                    project.setName(projectName);
                                    Call<Projects> call = RestClient.getTodoService().createProject(project);
                                    call.enqueue(new Callback<Projects>() {
                                        @Override
                                        public void onResponse(Call<Projects> call, Response<Projects> response) {
                                            fetchProject();
                                        }

                                        @Override
                                        public void onFailure(Call<Projects> call, Throwable t) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(ProjectListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
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
        mProjectAdapter.toggleSelection(position);//Toggle the selection

        boolean hasCheckedItems = mProjectAdapter.getSelectedCount() > 0;//Check if any items are already selected or not

        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the mActionMode
            mActionMode = startSupportActionMode(new ToolbarActionModeCallback(ProjectListActivity.this, mProjectAdapter));


        } else if (!hasCheckedItems && mActionMode != null) {
            // there no selected items, finish the mActionMode
            mActionMode.finish();
        }

        if (mActionMode != null) {
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(mProjectAdapter.getSelectedCount()) + " selected");

            Menu menu = mActionMode.getMenu();
            if (mProjectAdapter.getSelectedCount() > 1) {
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
        private ProjectAdapter mProjectAdapter;

        ToolbarActionModeCallback(Context context, ProjectAdapter mProjectAdapter) {
            this.context = context;
            this.mProjectAdapter = mProjectAdapter;
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
                    final View view = layoutInflaterAndroid.inflate(R.layout.dialog_project, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(view);

                    TextView textViewDialogTitle = (TextView) view.findViewById(R.id.tv_dialog_title);
                    textViewDialogTitle.setText(getString(R.string.dialog_project_edit_title));

                    final SparseBooleanArray selectedE = mProjectAdapter.getSelectedIds();

                    final EditText editTextProjectName = (EditText) view.findViewById(R.id.edt_project_name);
                    editTextProjectName.setText(String.valueOf(mProjectList.get(selectedE.keyAt(0)).getName()));
                    builder
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {

                                    int projectId = mProjectList.get(selectedE.keyAt(0)).getId();
                                    String projectName = editTextProjectName.getText().toString().trim();
                                    if (projectName.length() > 0) {
                                        mProgressDialog.show();

                                        Projects project = new Projects();
                                        project.setName(projectName);

                                        Call<Projects> call = RestClient.getTodoService().updateProject(projectId, project);
                                        call.enqueue(new Callback<Projects>() {
                                            @Override
                                            public void onResponse(Call<Projects> call, Response<Projects> response) {
                                                fetchProject();
                                            }

                                            @Override
                                            public void onFailure(Call<Projects> call, Throwable t) {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(ProjectListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
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

                    final SparseBooleanArray selectedD = mProjectAdapter.getSelectedIds();
                    for (int i = 0; i < selectedD.size(); i++) {
                        if (selectedD.valueAt(i)) {
                            final int key = selectedD.keyAt(i);

                            Call<Void> callT = RestClient.getTodoService().deleteProject(mProjectList.get(key).getId());
                            final int current = i;
                            callT.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (current == (selectedD.size() - 1)) {
                                        fetchProject();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    if (current == (selectedD.size() - 1)) {
                                        fetchProject();
                                        Toast.makeText(ProjectListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
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
            mProjectAdapter.removeSelection();
            if (mActionMode != null)
                mActionMode = null;
        }
    }
}
