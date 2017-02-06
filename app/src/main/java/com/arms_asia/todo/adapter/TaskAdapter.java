package com.arms_asia.todo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arms_asia.todo.R;
import com.arms_asia.todo.helper.ItemClickListener;
import com.arms_asia.todo.rest.model.Tasks;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TodoViewHolder> {
    private List<Tasks> mTaskList;
    private ItemClickListener itemClickListener;

    public void setOnClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }



    class TodoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {
        TextView tvTaskName;

        TodoViewHolder(View itemView) {
            super(itemView);
            tvTaskName = (TextView) itemView.findViewById(R.id.tv_task_name);
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

    public TaskAdapter(Context context, List<Tasks> taskList) {
        this.mTaskList = taskList;

    }

    @Override
    public TodoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_list, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TodoViewHolder holder, int position) {
        holder.tvTaskName.setText(mTaskList.get(position).getName());
    }


    @Override
    public int getItemCount() {
        return mTaskList.size();
    }

}
