package com.example.chatapplication.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.chatapplication.R;
import com.example.chatapplication.models.Chat;
import com.example.chatapplication.models.Message;
import com.example.chatapplication.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior.ESTIMATE;

public class ChatActivity extends AppCompatActivity {

    private EditText editText;
    private User user;
    private Chat chat;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> list = new ArrayList<>();

    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editText = findViewById(R.id.editText);
        recyclerView = findViewById(R.id.recyclerView);

        user = (User) getIntent().getSerializableExtra("user");
        chat = (Chat) getIntent().getSerializableExtra("chat");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (chat == null) {
            chat = new Chat();
            ArrayList<String> userIds = new ArrayList<>();
            userIds.add(user.getId());
            userIds.add(currentUser.getUid());
            chat.setUserIds(userIds);
            chat.setSender(currentUser.getUid());
            chat.setReceiver(user.getId());
        } else {
            initList();
            getMessages();
        }

//        FirebaseFirestore.getInstance()
//                .collection("chats")
//                .whereArrayContains("userIds", currentUser.getUid())
//                .get()
//                .addOnSuccessListener(new EventListener<>())
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot snapshots) {
//                        for (DocumentSnapshot snapshot : snapshots) {
//                            chat = snapshot.toObject(Chat.class);
//
//                            assert chat != null;
//                            if (chat.getSender().equals(currentUser.getUid()) && chat.getReceiver().equals(chat.getUserIds().get(0))) {
//                                initList();
//                                getMessages();
//                            } else {
//                                chat = new Chat();
//                                ArrayList<String> userIds = new ArrayList<>();
//                                userIds.add(user.getId());
//                                userIds.add(currentUser.getUid());
//                                chat.setUserIds(userIds);
//                                chat.setSender(currentUser.getUid());
//                                chat.setReceiver(user.getId());
//                            }
//                        }
//                    }
//                });

        FirebaseFirestore.getInstance().collection("users")
                .document(chat.getUserIds().get(0))
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        user = snapshot.toObject(User.class);
                        assert user != null;
                        setTitle(user.getName());
                    }
                });
    }

    private void initList() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new MessageAdapter(this, list);
        recyclerView.setAdapter(adapter);
    }

    private void getMessages() {
        FirebaseFirestore.getInstance().collection("chats")
                .document(chat.getId())
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                assert snapshots != null;
                for (DocumentChange change : snapshots.getDocumentChanges()) {
                    Message message = change.getDocument().toObject(Message.class);
                    switch (change.getType()) {
                        case ADDED:
                            message.setTimestamp(change.getDocument().getTimestamp("timestamp"));
                            list.add(message);
                            break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void onClickSend(View view) {
        String text = editText.getText().toString().trim();
        if (chat.getId() != null) {
            sendMessage(text);
        } else {
            createChat(text);
        }
    }

    private void sendMessage(String text) {
        Map<String, Object> map = new HashMap<>();
        map.put("text", text);
        map.put("receiver", chat.getReceiver());
        map.put("sender", chat.getSender());
        map.put("timestamp", new Timestamp(new Date()));
        FirebaseFirestore.getInstance().collection("chats")
                .document(chat.getId())
                .collection("messages")
                .add(map);

        editText.setText("");
    }

    private void createChat(final String text) {
        FirebaseFirestore.getInstance()
                .collection("chats")
                .add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        chat.setId(documentReference.getId());
                        initList();
                        getMessages();
                        sendMessage(text);
                    }
                });
    }
}