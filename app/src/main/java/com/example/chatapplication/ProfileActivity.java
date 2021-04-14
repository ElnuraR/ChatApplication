package com.example.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText editName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        editName = findViewById(R.id.editName);
    }

    public void onClickSave(View view) {
        String name = editName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            editName.setError("Введите имя");
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        String userId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Ошибка ввода", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}