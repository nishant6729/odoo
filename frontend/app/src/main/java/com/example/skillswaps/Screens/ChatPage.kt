package com.example.skillswaps.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.skillswaps.ChatLogic.ChatMessage
import com.example.skillswaps.ChatLogic.ChatRepository
import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ChatPage(
    receiver: String,
    navController: NavController
) {
    val senderUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var currentUser by remember { mutableStateOf<User?>(null) }
    var otherUser   by remember { mutableStateOf<User?>(null) }
    val firestoreHelper = FirestoreHelper()

    // Load both users once
    LaunchedEffect(receiver) {
        currentUser = firestoreHelper.getUserByUid(senderUid)
        otherUser   = firestoreHelper.getUserByUid(receiver)
    }

    // Show loading until both are non-null
    if (currentUser != null && otherUser != null) {
        ChatScreen(
            currentUser = currentUser!!,
            otherUser   = otherUser!!,
            onBack      = { navController.popBackStack() }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading chat…", fontSize = 18.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUser: User,
    otherUser: User,
    onBack: () -> Unit
) {
    var messages    by remember { mutableStateOf(listOf<ChatMessage>()) }
    var currentText by remember { mutableStateOf("") }

    // 1) Listen for incoming messages
    DisposableEffect(otherUser.uid) {
        val registration = ChatRepository.observeChat(
            currentUser.uid,
            otherUser.uid
        ) { msgs -> messages = msgs }
        onDispose { registration.remove() }
    }

    // 2) Wrap the entire Column in imePadding() so Compose
    //    applies bottom padding equal to keyboard height
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding() // status & nav bars
            .imePadding()        // keyboard inset
    ) {
        TopAppBar(
            title = { Text("${otherUser.firstName} ${otherUser.lastName}") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // 3) Make the LazyColumn also respect imePadding/pooling
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .imePadding(),            // extra safety
            reverseLayout = false
        ) {
            items(messages) { msg ->
                val isMine = msg.senderId == currentUser.uid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (isMine) Color(0xFFDCF8C6) else Color.White,
                        tonalElevation = 1.dp,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(msg.text, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }

        // 4) Input row stays visible above the keyboard
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = currentText,
                onValueChange = { currentText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") }
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                val text = currentText.trim()
                if (text.isNotEmpty()) {
                    // optimistic UI
                    messages = messages + ChatMessage(currentUser.uid, text, System.currentTimeMillis())
                    // send
                    ChatRepository.sendMessage(currentUser.uid, otherUser.uid, text)
                        .addOnFailureListener { /* handle error */ }
                    currentText = ""
                }
            }) {
                Text("Send")
            }
        }
    }
}

