package com.example.androidmessengerulstu.bottomnav.chats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.androidmessengerulstu.chats.Chat;
import com.example.androidmessengerulstu.chats.ChatsAdapter;
import com.example.androidmessengerulstu.databinding.FragmentChatsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import javax.annotation.Nonnull;

public class ChatsFragment extends Fragment {
    private FragmentChatsBinding binding;

    @androidx.annotation.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        loadChats();

        return binding.getRoot();
    }

    private void loadChats() {
        ArrayList<Chat> chats = new ArrayList<>();
        HashSet<String> uniqueChatIds = new HashSet<>(); // Для проверки уникальности

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot userChatsSnapshot = snapshot.child("Users").child(uid).child("chats");
                if (!userChatsSnapshot.exists()) {
                    Toast.makeText(getContext(), "No chats found", Toast.LENGTH_SHORT).show();
                    return;
                }

                String chatsStr = userChatsSnapshot.getValue(String.class);
                if (chatsStr == null || chatsStr.isEmpty()) {
                    Toast.makeText(getContext(), "No chats available", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Убираем возможные пробелы и пустые строки
                String[] chatsIds = chatsStr.split(",");
                for (String chatId : chatsIds) {
                    chatId = chatId.trim(); // Убираем пробелы
                    if (chatId.isEmpty() || uniqueChatIds.contains(chatId)) {
                        continue; // Пропустить пустые или дублирующиеся chatId
                    }
                    uniqueChatIds.add(chatId);

                    DataSnapshot chatSnapshot = snapshot.child("Chats").child(chatId);
                    if (!chatSnapshot.exists()) {
                        continue; // Пропустить отсутствующий чат
                    }

                    String userId1 = chatSnapshot.child("user1").getValue(String.class);
                    String userId2 = chatSnapshot.child("user2").getValue(String.class);

                    if (userId1 == null || userId2 == null) {
                        continue; // Пропустить чат с некорректными данными
                    }

                    String chatUserId = (uid.equals(userId1)) ? userId2 : userId1;
                    DataSnapshot chatUserSnapshot = snapshot.child("Users").child(chatUserId);
                    if (!chatUserSnapshot.exists() || !chatUserSnapshot.hasChild("username")) {
                        continue; // Пропустить, если пользователь не найден
                    }

                    String chatName = chatUserSnapshot.child("username").getValue(String.class);
                    if (chatName == null) {
                        continue; // Пропустить, если имя пользователя пустое
                    }

                    Chat chat = new Chat(chatId, chatName, userId1, userId2);
                    chats.add(chat);
                }

                // Устанавливаем адаптер для RecyclerView
                binding.chatsRv.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.chatsRv.setAdapter(new ChatsAdapter(chats));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to get user chats", Toast.LENGTH_SHORT).show();
            }
        });
    }


}