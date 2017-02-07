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

                // Call method addProject() to Start project add dialog
                addProject();
            }
        });

        // Prepare progress dialog for each Retrofit Request
        mProgressDialog = new ProgressDialog(ProjectListActivity.this);
        mProgressDialog.setMessage("Loading");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        // Prepare Empty RecyclerView
        initRecyclerView();

        // When open Project Activity, Display progress dialog and Fetch project list by Retrofit
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

            // When click item on ActionBar (ToolBar) Fetch project list by Retrofit request
            initProjectList();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView() {
        // Prepare mProjectList as empty ArrayList
        mProjectList = new ArrayList<>();

        // Passing Context and ProjectList(empty) to Adapter and Call Adapter.Onclick
        mProjectAdapter = new ProjectAdapter(ProjectListActivity.this, mProjectList);
        mProjectAdapter.setOnClickListener(this);

        // Prepare RecyclerView add divider , set Layout look like a standard ListView and set adapter
        mRecyclerViewProject = (RecyclerView) findViewById(R.id.rv_project);
        mRecyclerViewProject.addItemDecoration(new DividerItemDecoration(ProjectListActivity.this, DividerItemDecoration.VERTICAL));
        mRecyclerViewProject.setLayoutManager(new LinearLayoutManager(ProjectListActivity.this));
        mRecyclerViewProject.setAdapter(mProjectAdapter);
    }

    private void initProjectList() {
        mProgressDialog.show();
        fetchProject();
    }

    private void fetchProject() {

        // Clear project list when fetch project every time
        mProjectList.clear();

        // Make a request by Retrofit for retrieving project list as JSON data and convert to <List<Projects> object
        Call<List<Projects>> call = RestClient.getTodoService().getProjects();
        call.enqueue(new Callback<List<Projects>>() {
            @Override
            public void onResponse(Call<List<Projects>> call, Response<List<Projects>> response) {

                // If retrieve respond data, Add to project list and dismiss progress dialog
                if (response.body() != null) {
                    mProjectList.addAll(response.body());
                    mProgressDialog.dismiss();

                // If not retrieve respond data, just dismiss progress dialog and show Toast message
                } else {
                    mProgressDialog.dismiss();
                    Toast.makeText(ProjectListActivity.this, "Nothing project", Toast.LENGTH_SHORT).show();
                }

                // Notify to adapter that data has changed
                mProjectAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Projects>> call, Throwable t) {

                // If fail to make a request
                // - Notify to adapter that data has changed()
                // - Dismiss progress dialog
                // - Show Toast message with throwable message
                mProjectAdapter.notifyDataSetChanged();
                mProgressDialog.dismiss();
                Toast.makeText(ProjectListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view, int position, boolean isLongClick, MotionEvent motionEvent) {
        if (isLongClick) {

            // If LongClick Always start Action Mode
            onListItemSelect(position);
        } else {

            //If Click in Action Mode
            if (mActionMode != null) {
                onListItemSelect(position);

            //If Click not in Action Mode, Go to Task List Activity
            } else {
                Intent intent = new Intent(ProjectListActivity.this, TaskListActivity.class);
                intent.putExtra(TaskListActivity.PROJECT_ID, mProjectList.get(position).getId());
                intent.putExtra(TaskListActivity.PROJECT_NAME, mProjectList.get(position).getName());
                startActivity(intent);
            }
        }
    }

    private void addProject() {

        // Get LayoutInflater from this Acitivity
        LayoutInflater layoutInflater = LayoutInflater.from(ProjectListActivity.this);

        // Inflate dialog_project.xml to view object
        final View view = layoutInflater.inflate(R.layout.dialog_project, null);

        // Make builder for AlertDialog with the default alert dialog theme
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectListActivity.this);

        // Set a custom view resource
        builder.setView(view);

        // Set title
        TextView textViewDialogTitle = (TextView) view.findViewById(R.id.tv_dialog_title);
        textViewDialogTitle.setText(getString(R.string.dialog_project_add_title));

        final EditText editTextProjectName = (EditText) view.findViewById(R.id.edt_project_name);
        builder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_positive_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialogBox, int id) {

                                // If get String from EditText, Make a Request by Retrofit
                                String projectName = editTextProjectName.getText().toString().trim();
                                if (projectName.length() > 0) {
                                    mProgressDialog.show();

                                    Projects project = new Projects();
                                    project.setName(projectName);
                                    Call<Projects> call = RestClient.getTodoService().createProject(project);
                                    call.enqueue(new Callback<Projects>() {
                                        @Override
                                        public void onResponse(Call<Projects> call, Response<Projects> response) {
                                            // Fetch project list after new project has been added
                                            fetchProject();
                                        }

                                        @Override
                                        public void onFailure(Call<Projects> call, Throwable t) {

                                            // If fail to make a request
                                            // - Dismiss progress dialog
                                            // - Show Toast message with throwable message
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

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void onListItemSelect(int position) {
        // Toggle the selection
        mProjectAdapter.toggleSelection(position);

        //Check if any items are already selected or not
        boolean hasCheckedItems = mProjectAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            // If there are some selected items, start the mActionMode
            mActionMode = startSupportActionMode(new ToolbarActionModeCallback(ProjectListActivity.this, mProjectAdapter));


        } else if (!hasCheckedItems && mActionMode != null) {
            // If there no selected items, finish the mActionMode
            mActionMode.finish();
        }

        if (mActionMode != null) {
            // Set action mode title on item selection
            mActionMode.setTitle(String.valueOf(mProjectAdapter.getSelectedCount()) + " selected");

            // If selected item more than 1, display only delete action
            // else display edit and del action
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
            //Inflate the menu over action mode
            mode.getMenuInflater().inflate(R.menu.action_menu, menu);
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
                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    final View view = layoutInflater.inflate(R.layout.dialog_project, null);
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

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
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
