package adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.taskmanagement.R;
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


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflatter = LayoutInflater.from(context);

        View view = layoutInflatter.inflate(R.layout.member_items, parent, false);

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
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imgAvatar, imgOut;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            db = Room.databaseBuilder(context, TaskStoreDatabase.class, Constants.DB_NAME).build();

            imgAvatar = itemView.findViewById(R.id.ivAvatar_member_item);
            tvName = itemView.findViewById(R.id.tvName_member_item);
            imgOut = itemView.findViewById(R.id.imgOut_member_item);

            if(isManager){
                imgOut.setVisibility(View.VISIBLE);
            }

            imgOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
            });
        }
    }
}
