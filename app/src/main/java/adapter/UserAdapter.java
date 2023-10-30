package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import utils.CircleTransform;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    Context context;

    List<User> users;

    public UserAdapter(List<User> users){
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.user_items, parent, false);

        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvUsername.setText(user.getUsername());
        holder.tvInviteCode.setText(user.getInviteCode());
        holder.tvRole.setText(user.getRole());
        Picasso.get()
                .load(user.getImageUrl())
                .transform(new CircleTransform())
                .into(holder.avatar);
        if(user.getBlocked()){
            holder.blockIcon.setImageResource(R.drawable.baseline_lock_24);
        }
        else{
            holder.blockIcon.setImageResource(R.drawable.baseline_lock_open_24);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView avatar, blockIcon;
        TextView tvUsername, tvInviteCode, tvRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.ivAvatar);
            blockIcon = itemView.findViewById(R.id.ivBlockToggle);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvInviteCode = itemView.findViewById(R.id.tvInviteCode);
            tvRole = itemView.findViewById(R.id.tvRole);

            blockIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = users.get(getAdapterPosition());
                    if(user != null){
                        user.setBlocked(!user.getBlocked());
                        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                TaskStoreDatabase db = Room.databaseBuilder(context, TaskStoreDatabase.class, Constants.DB_NAME).build();
                                db.userDAO().update(user);
                            }
                        });
                        notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
