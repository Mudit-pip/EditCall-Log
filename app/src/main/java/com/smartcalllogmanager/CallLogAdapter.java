package com.smartcalllogmanager;

import android.graphics.Color;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.ViewHolder> {

    public interface OnCallLogClickListener {
        void onItemClick(CallLogItem item);
        void onDeleteClick(CallLogItem item);
        void onCallClick(CallLogItem item);
    }

    private final List<CallLogItem> callLogItems;
    private final OnCallLogClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public CallLogAdapter(List<CallLogItem> callLogItems, OnCallLogClickListener listener) {
        this.callLogItems = callLogItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallLogItem item = callLogItems.get(position);

        String title = (item.getName() != null && !item.getName().isEmpty()) ? item.getName() : item.getNumber();
        holder.tvNumber.setText(title);

        String typeStr;
        int iconRes;
        int iconColor;
        int bgColor;

        switch (item.getType()) {
            case CallLog.Calls.MISSED_TYPE:
                typeStr = "Missed";
                iconRes = android.R.drawable.sym_call_missed;
                iconColor = Color.parseColor("#D32F2F"); // Red
                bgColor = Color.parseColor("#FFEBEE");
                break;
            case CallLog.Calls.OUTGOING_TYPE:
                typeStr = "Outgoing";
                iconRes = android.R.drawable.sym_call_outgoing;
                iconColor = Color.parseColor("#1976D2"); // Blue
                bgColor = Color.parseColor("#E3F2FD");
                break;
            case CallLog.Calls.INCOMING_TYPE:
                typeStr = "Incoming";
                iconRes = android.R.drawable.sym_call_incoming;
                iconColor = Color.parseColor("#388E3C"); // Green
                bgColor = Color.parseColor("#E8F5E9");
                break;
            default:
                typeStr = "Unknown";
                iconRes = android.R.drawable.sym_call_incoming;
                iconColor = Color.GRAY;
                bgColor = Color.parseColor("#F5F5F5");
                break;
        }

        holder.imgCallType.setImageResource(iconRes);
        holder.imgCallType.setColorFilter(iconColor);
        holder.avatarBackground.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bgColor));

        // Format duration
        int totalSecs = item.getDuration();
        int mins = totalSecs / 60;
        int secs = totalSecs % 60;
        String durationStr = (mins > 0) ? mins + "m " + secs + "s" : secs + "s";

        String dateStr = dateFormat.format(new Date(item.getDate()));

        String detailsStr = typeStr + " • " + dateStr + " • " + durationStr;
        holder.tvDetails.setText(detailsStr);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
        holder.btnCall.setOnClickListener(v -> listener.onCallClick(item));
    }

    @Override
    public int getItemCount() {
        return callLogItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View avatarBackground;
        ImageView imgCallType;
        TextView tvNumber;
        TextView tvDetails;
        ImageView btnDelete;
        ImageView btnCall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarBackground = itemView.findViewById(R.id.avatar_background);
            imgCallType = itemView.findViewById(R.id.img_call_type);
            tvNumber = itemView.findViewById(R.id.tv_number);
            tvDetails = itemView.findViewById(R.id.tv_details);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnCall = itemView.findViewById(R.id.btn_call);
        }
    }
}
