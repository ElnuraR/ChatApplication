package com.example.chatapplication.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.intefaces.OnItemClickListener;
import com.example.chatapplication.models.Message;
import com.example.chatapplication.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.ESTIMATE;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT= 1;

    private List<Message> list;
    private LayoutInflater inflater;

    public MessageAdapter(Context context, List<Message> list) {
        inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = inflater.inflate(R.layout.chat_item_right, parent, false);
        } else {
            view = inflater.inflate(R.layout.chat_item_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = list.get(position);
        holder.show_message.setText(message.getText());

        Date date =  message.getTimestamp().toDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm" );
        String time = simpleDateFormat.format(date);
        holder.message_time.setText(time);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView show_message;
        private final TextView message_time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            message_time = itemView.findViewById(R.id.message_time);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getSender().equals(FirebaseAuth.getInstance().getUid())) {
            return  MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
