package com.example.chatapplication.contacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.chatapplication.R;
import com.example.chatapplication.chat.ChatActivity;
import com.example.chatapplication.intefaces.OnItemClickListener;
import com.example.chatapplication.models.Chat;
import com.example.chatapplication.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactsAdapter adapter;
    private List<User> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Contacts");

        recyclerView = findViewById(R.id.recyclerView);

        initList();
        getContacts();
    }

    private void initList() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new ContactsAdapter(this, list);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
                intent.putExtra("user", list.get(position));
                startActivity(intent);
            }
        });
    }

    private void getContacts() {
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        for (DocumentSnapshot snapshot : snapshots) {
                            User user = snapshot.toObject(User.class);
                            if (user != null) {
                                user.setId(snapshot.getId());
                            }
                            if (!(user.getId()).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                list.add(user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

}