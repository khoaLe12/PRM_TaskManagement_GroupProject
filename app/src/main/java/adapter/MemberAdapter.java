package adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.taskmanagement.R;
import com.example.taskmanagement.project.MemberProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.User;
import models.UserProjectCrossRef;
import utils.CircleTransform;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    boolean isManager = false;
    Context context;
    List<User> members;
    TaskStoreDatabase db;
    int projectId = 0;
    boolean createTask = false;
    int selectedMember = -1;
    private OnSelectedMemberListener mListener;

    public interface OnSelectedMemberListener{
        void onSelectedMember(int userId);
    }

    public MemberAdapter(List<User> members, boolean isManager){
        this.members = members;
        this.isManager = isManager;
    }

    public void setMembers(List<User> members){
        this.members = members;
        notifyDataSetChanged();
    }

    public void setProjectId(int projectId){
        this.projectId = projectId;
    }

    public void setCreateTask(boolean createTask){
        this.createTask = createTask;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        if(context instanceof OnSelectedMemberListener){
            mListener = (OnSelectedMemberListener) context;
        }

        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.member_items, parent, false);

        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User member = members.get(position);
        holder.tvName.setText(member.getName());
        Picasso.get()
                .load(member.getImageUrl())
                .transform(new CircleTransform())
                .into(holder.imgAvatar);

        if(createTask){
            if(member.getUserId() == selectedMember){
                holder.imgOut.setImageResource(R.drawable.baseline_check_24);
            }
            else{
                holder.imgOut.setImageDrawable(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout llMember;
        ImageView imgAvatar, imgOut;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            db = Room.databaseBuilder(context, TaskStoreDatabase.class, Constants.DB_NAME).build();

            imgAvatar = itemView.findViewById(R.id.ivAvatar_member_item);
            tvName = itemView.findViewById(R.id.tvName_member_item);
            imgOut = itemView.findViewById(R.id.imgOut_member_item);
            llMember = itemView.findViewById(R.id.llMemberProfile_member_item);

            if(isManager){
                imgOut.setVisibility(View.VISIBLE);
                imgOut.setImageResource(R.drawable.baseline_close_24);
            }
            else if(createTask){
                imgOut.setVisibility(View.VISIBLE);
                imgOut.setBackground(ActivityCompat.getDrawable(context, R.drawable.custom_layout_border));
            }

            imgOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isManager){
                        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                User member = members.get(getAdapterPosition());
                                if(member != null){
                                    members.remove(member);
                                    UserProjectCrossRef entity = new UserProjectCrossRef(member.getUserId(), projectId);
                                    db.userProjectDAO().delete(entity);
                                    ((Activity)context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        });
                    }
                    else if(createTask){
                        User member = members.get(getAdapterPosition());
                        if(member.getUserId() != selectedMember){
                            selectedMember = member.getUserId();
                            mListener.onSelectedMember(selectedMember);
                            notifyDataSetChanged();
                        }
                    }
                }
            });

            llMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isManager){
                        User member = members.get(getAdapterPosition());
                        if(member != null){
                            Intent intent = new Intent(context, MemberProfileActivity.class);
                            intent.putExtra(Constants.PROJECT_ID, projectId);
                            intent.putExtra(Constants.USER_ID, member.getUserId());
                            ((Activity) context).startActivity(intent);
                        }
                        else{
                            Toast.makeText(context, "Load user information fail", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }
}
