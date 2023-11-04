package adapter;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagement.R;

import java.util.List;
import java.util.Map;


public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder> {

    Context context;
    List<String> fileNames;
    Map<String,String> files;

    public AttachmentAdapter(List<String> fileNames, Map<String,String> files){
        this.fileNames = fileNames;
        this.files = files;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.attachment_items, parent, false);

        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = fileNames.get(position);
        holder.fileName.setText(name);
    }

    @Override
    public int getItemCount() {
        return fileNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView fileName;
        ImageView imgDownload;
        DownloadManager manager;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            fileName = itemView.findViewById(R.id.tvFileName_attachment_item);
            imgDownload = itemView.findViewById(R.id.imgDownload_attachment_item);

            manager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);

            imgDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fileName = fileNames.get(getAdapterPosition());
                    String url = files.get(fileName);
                    if(url != null){
                        Uri uri = Uri.parse(url);
                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                        long reference = manager.enqueue(request);
                    }
                    else{
                        Toast.makeText(context, "Can not find url", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
