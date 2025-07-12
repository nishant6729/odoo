
package com.example.skillswaps.Screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillswaps.R
import androidx.navigation.NavController
import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsPage(navController: NavController) {
    val scope      = rememberCoroutineScope()
    val fire       = remember { FirestoreHelper() }
    val context    = LocalContext.current
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var requests by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load friend request senders
    LaunchedEffect(Unit) {
        val ids   = fire.getFriendRequestsList()
        val users = fire.getUsersByIds(ids)
        requests  = users
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friend Requests") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB89653))
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                requests.isEmpty() -> {
                    Text(
                        text = "No Friend Requests",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFFF8E1))
                    ) {
                        items(requests) { user ->
                            RequestCard(
                                user = user,
                                onClick = { /* optionally navigate to profile */ },
                                onAccept = {
                                    fire.acceptFriendRequest(user.uid, context) {
                                        requests = requests.filterNot { it.uid == user.uid }
                                    }
                                },
                                onDecline = {
                                    // remove request only
                                    val remaining = requests
                                        .filterNot { it.uid == user.uid }
                                        .map { it.uid }
                                    fire.updateField(
                                        collection = "data",
                                        docId      = currentUid,
                                        field      = "friendRequest",
                                        value      = remaining,
                                        onComplete = {
                                            Toast.makeText(context, "Request declined", Toast.LENGTH_SHORT).show()
                                            requests = requests.filterNot { it.uid == user.uid }
                                        },
                                        onError = { e ->
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    user: User,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onClick: () -> Unit = {}
) {
    val font1 = FontFamily(Font(R.font.font1, FontWeight.SemiBold))
    val font2 = FontFamily(Font(R.font.font2, FontWeight.Medium))
    val font3 = FontFamily(Font(R.font.font3, FontWeight.Normal))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable{onClick()},
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD95A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // User image and location omitted for brevity
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = font1,
                    color = Color.Black
                )
                Text(
                    text = "(${user.occupation})",
                    fontSize = 13.sp,
                    fontFamily = font2,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = user.description,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    fontFamily = font3,
                    color = Color.Black.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(112, 177, 0))
                    ) {
                        Text("ACCEPT", fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                    Button(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(200, 0, 0))
                    ) {
                        Text("DECLINE", fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }
    }
}


