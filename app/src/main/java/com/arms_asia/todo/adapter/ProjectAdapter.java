package com.arms_asia.todo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arms_asia.todo.R;
import com.arms_asia.todo.helper.ItemClickListener;
import com.arms_asia.todo.rest.model.Projects;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    private Context mContext;
    private List<Projects> mProjectList;
    private ItemClickListener itemClickListener;
    private SparseBooleanArray mSelectedItemsIds;

    public void setOnClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {
        TextView tvProjectName;
        TextView tvUpdatedDate;
        TextView tvTasksCount;

        ProjectViewHolder(View itemView) {
            super(itemView);
            tvProjectName = (TextView) itemView.findViewById(R.id.tv_project_name);
            tvUpdatedDate = (TextView) itemView.findViewById(R.id.tv_updated_date);
            tvTasksCount = (TextView) itemView.findViewById(R.id.tv_tasks_count);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), false, null);
        }

        @Override
        public boolean onLongClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), true, null);
            return true;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemClickListener.onClick(view, getAdapterPosition(), false, motionEvent);
            return true;
        }
    }

    public ProjectAdapter(Context context, List<Projects> projectList) {
        mContext = context;
        mProjectList = projectList;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_list, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProjectViewHolder holder, int position) {

        String updatedDate = "Last updated : " +
                new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(
                        mProjectList.get(position).getUpdatedDate());

        holder.tvProjectName.setText(mProjectList.get(position).getName());
        holder.tvUpdatedDate.setText(updatedDate);
        holder.tvTasksCount.setText(String.valueOf(mProjectList.get(position).getTasksCount()));

        holder.itemView.setSelected(mSelectedItemsIds.get(position));
    }


    @Override
    public int getItemCount() {
        return mProjectList.size();
    }

    //Toggle selection methods
    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    //Remove selected selections
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    //Put or delete selected position into SparseBooleanArray
    private void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();
    }

    //Get total selected count
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    //Return all selected ids
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

}
