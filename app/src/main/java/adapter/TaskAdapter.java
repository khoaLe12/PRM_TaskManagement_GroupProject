package adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.taskmanagement.R;
import com.example.taskmanagement.task.TaskDetailActivity;

import java.util.List;

import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    Context context;
    List<Task> tasks;
    boolean isManager = false;
    int projectId = -1;
    TaskStoreDatabase db;

    public TaskAdapter(List<Task> tasks, boolean isManager){
        this.tasks = tasks;
        this.isManager = isManager;
    }

    public void setList(List<Task> tasks){
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public void setProjectId(int projectId){
        this.projectId = projectId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.task_items, parent, false);

        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvDeadline.setText(task.getDeadline().toString());

        if(task.getConfirmStatus() == 0){
            holder.tvConfirm.setText(Constants.WAITING);
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.baseline_hourglass_empty_24);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            holder.tvConfirm.setCompoundDrawables(drawable, null, null, null);
            holder.tvConfirm.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_blue_status));
        }
        else if(task.getConfirmStatus() == 1){
            holder.tvConfirm.setText(Constants.CONFIRM);
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.baseline_check_24);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            holder.tvConfirm.setCompoundDrawables(drawable, null, null, null);
            holder.tvConfirm.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_green_status));
        }
        else if(task.getConfirmStatus() == 2){
            holder.tvConfirm.setText(Constants.REJECT);
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.baseline_not_interested_24);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            holder.tvConfirm.setCompoundDrawables(drawable, null, null, null);
            holder.tvConfirm.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_red_status));
        }
        else{
            holder.tvConfirm.setText("Error");
        }

        if(task.getStatus() == 0){
            holder.tvTaskStatus.setText(Constants.IN_PROGRESS);
            holder.tvTaskStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_blue_status));
        }
        else if(task.getStatus() == 1){
            holder.tvTaskStatus.setText(Constants.DONE);
            holder.tvTaskStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_green_status));
        }
        else if(task.getStatus() == 2){
            holder.tvTaskStatus.setText(Constants.OVERDUE);
            holder.tvTaskStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_red_status));
        }
        else{
            holder.tvTaskStatus.setText("Error");
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle, tvDeadline, tvConfirm, tvTaskStatus;
        ImageView imgClear;
        LinearLayout llTaskItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            db = Room.databaseBuilder(context, TaskStoreDatabase.class, Constants.DB_NAME).build();

            tvTitle = itemView.findViewById(R.id.tvTitle_task_item);
            tvDeadline = itemView.findViewById(R.id.tvDeadline_task_item);
            tvConfirm = itemView.findViewById(R.id.tvConfirmStatus_task_item);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus_task_item);
            imgClear = itemView.findViewById(R.id.imgClear_task_item);
            llTaskItem = itemView.findViewById(R.id.llTaskItem_task_item);

            if(isManager){
                imgClear.setVisibility(View.VISIBLE);
            }

            imgClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            Task task = tasks.get(getAdapterPosition());
                            if(task != null){
                                tasks.remove(task);
                                db.taskDAO().delete(task);
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
                }
            });

            llTaskItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, TaskDetailActivity.class);
                    intent.putExtra(Constants.TASK_ID, tasks.get(getAdapterPosition()).getTaskId());
                    intent.putExtra(Constants.IS_MANAGER, isManager);
                    if(projectId != -1){
                        intent.putExtra(Constants.PROJECT_ID, projectId);
                    }
                    Activity activity = (Activity) context;
                    activity.startActivity(intent);
                    activity.finish();
                }
            });
        }
    }
}
