package com.example.androidmessengerulstu.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.androidmessengerulstu.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class ChatUtil {
    public static void createChat(Context context, User user) {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String chatId = generateChatId(uid, user.uid);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Чат не существует, создаем новый
                    HashMap<String, String> chatInfo = new HashMap<>();
                    chatInfo.put("user1", uid);
                    chatInfo.put("user2", user.uid);

                    reference.setValue(chatInfo)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    addChatIdToUser(uid, chatId);
                                    addChatIdToUser(user.uid, chatId);
                                    Toast.makeText(context, "Chat created successfully.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Failed to create chat: " + task.getException(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    // Чат уже существует
                    Toast.makeText(context, "Chat already exists.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(context, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



    private static String generateChatId(String userId1, String userId2){
        String sumUser1User2 = userId1+userId2;
        char[] charArray = sumUser1User2.toCharArray();
        Arrays.sort(charArray);

        return new String(charArray);
    }

    private static void addChatIdToUser(String uid, String chatId){
        FirebaseDatabase.getInstance().getReference().child("Users").child(uid)
                .child("chats").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        String chats = task.getResult().getValue().toString();
                        String chatsUpd = addIdToStr(chats, chatId);

                        FirebaseDatabase.getInstance().getReference().child("Users").child(uid)
                                .child("chats").setValue(chatsUpd);
                    }
                });
    }

    private static String addIdToStr(String str, String chatId){
        str += (str.isEmpty()) ? chatId : (","+chatId);
        return str;
    }
}
