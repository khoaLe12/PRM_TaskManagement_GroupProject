package adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagement.BillActivity;
import com.example.taskmanagement.R;

import java.text.SimpleDateFormat;
import java.util.List;

import constants.Constants;
import models.Bill;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {

    Context context;
    List<Bill> bills;

    public BillAdapter(List<Bill> bills) {
        this.bills = bills;
    }

    public void setList(List<Bill> bills) {
        this.bills = bills;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.bill_items, parent, false);

        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bill bill = bills.get(position);
        holder.tvTitle.setText(bill.getTitle());
        holder.tvContent.setText(bill.getContent());
        holder.tvMoney.setText(String.valueOf(bill.getMoney()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        holder.tvDate.setText(sdf.format(bill.getCreatedDate()));
        holder.tvSender.setText(String.valueOf(bill.getSenderId()));
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvContent, tvDate, tvMoney, tvRecipientId, tvSender;
        LinearLayout billItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle_bill_item);
            tvContent = itemView.findViewById(R.id.tvContent_bill_item);
            tvDate = itemView.findViewById(R.id.tvDate_bill_item);
            tvMoney = itemView.findViewById(R.id.tvMoney_bill_item);
            tvSender = itemView.findViewById(R.id.tvSender_bill_item);
            billItem = itemView.findViewById(R.id.billItem_layout);

            billItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bill project = bills.get(getAdapterPosition());
                    Intent intent = new Intent(context, BillActivity.class);
                    intent.putExtra(Constants.PROJECT_ID, project.getId());
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }

            });
        }
    }
}
