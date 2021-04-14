package com.example.chatapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.chatapplication.chat.ChatActivity;
import com.example.chatapplication.chat.ChatAdapter;
import com.example.chatapplication.contacts.ContactsActivity;
import com.example.chatapplication.contacts.ContactsAdapter;
import com.example.chatapplication.intefaces.OnItemClickListener;
import com.example.chatapplication.models.Chat;
import com.example.chatapplication.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Chat> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, PhoneActivity.class));
            return;
        }
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        setTitle("All chats");

        recyclerView = findViewById(R.id.recyclerView);
        initList();
        getChats();
    }

    public void onClickContacts(View view) {
        startActivity(new Intent(this, ContactsActivity.class));
    }

    private void initList() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new ChatAdapter(this, list);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("chat", list.get(position));
                startActivity(intent);
            }
        });
    }

    private void getChats() {
        String myUserId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection("chats")
                .whereArrayContains("userIds", myUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            switch (change.getType()) {
                                case ADDED:
                                    Chat chat = change.getDocument().toObject(Chat.class);
                                    chat.setId(change.getDocument().getId());
                                    list.add(chat);
                                    break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}