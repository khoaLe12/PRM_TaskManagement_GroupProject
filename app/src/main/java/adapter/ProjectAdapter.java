package adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.taskmanagement.ProjectDetailActivity;
import com.example.taskmanagement.R;

import java.util.List;

import constants.Constants;
import models.Project;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    Context context;
    List<Project> projects;

    public ProjectAdapter(List<Project> projects){
        this.projects = projects;
    }

    public void setList(List<Project> projects){
        this.projects = projects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.project_items, parent, false);

        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.tvTitle.setText(project.getTitle());
        holder.tvDate.setText(project.getCreatedDate().toString());

        if(project.getStatus() == 1){
            holder.tvStatus.setText(Constants.IN_PROGRESS);
            holder.tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_in_progress_status));
        }
        else if(project.getStatus() == 0){
            holder.tvStatus.setText(Constants.END);
            holder.tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_end_status));
        }
        else{
            holder.tvStatus.setText("Error");
        }

        SharedPreferences shared = context.getSharedPreferences(Constants.LOGIN, context.MODE_PRIVATE);
        int userId = shared.getInt(Constants.USER_ID, 0);

        if(userId != 0){
            if(project.getManagerId() == userId){
                holder.imgIcon.setImageResource(R.drawable.baseline_manager_24);
            }
            else{
                holder.imgIcon.setImageResource(R.drawable.baseline_member_24);
            }
        }

    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imgIcon;
        TextView tvStatus, tvTitle, tvDate;
        LinearLayout projectItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvStatus = itemView.findViewById(R.id.tvStatus_project_item);
            tvTitle = itemView.findViewById(R.id.tvTitle_project_item);
            tvDate = itemView.findViewById(R.id.tvDate_project_item);
            imgIcon = itemView.findViewById(R.id.imgRoleIcon_project_item);
            projectItem = itemView.findViewById(R.id.projectItem_layout);

            projectItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Project project = projects.get(getAdapterPosition());
                    Intent intent = new Intent(context, ProjectDetailActivity.class);
                    intent.putExtra(Constants.PROJECT_ID, project.getProjectId());
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            });
        }
    }
}
