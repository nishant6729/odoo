package com.example.skillswaps.Screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.CommunityPost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    postType: String,    // e.g. "exchange", must exactly match Firestore field values
    pageSize: Int = 10
) {
    val helper = remember { FirestoreHelper() }
    val scope  = rememberCoroutineScope()
    val ctx    = LocalContext.current

    // Null means “All categories”
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var catMenuOpen by remember { mutableStateOf(false) }
    val categories = listOf("Programming", "Design", "Music", "Public Speaking", "Career", "Other")

    // Paging state
    val posts = remember { mutableStateListOf<CommunityPost>() }
    var isLoading by remember { mutableStateOf(false) }
    var lastSnap by remember { mutableStateOf<DocumentSnapshot?>(null) }
    var endReached by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Fetch a page of results. If reset=true, clear existing first.
    fun fetchPage(reset: Boolean) {
        scope.launch {
            if (reset) {
                posts.clear()
                lastSnap = null
                endReached = false
            }
            if (endReached || isLoading) return@launch
            isLoading = true
            try {
                Log.d("FeedScreen", "Querying type='$postType' category='${selectedCategory ?: "ALL"}' lastSnap=$lastSnap")
                val (fetched, last) = if (selectedCategory == null) {
                    // All categories: only filter by type
                    helper.getCommunityPostsByTypePage(
                        type = postType,
                        pageSize = pageSize,
                        lastDocument = lastSnap
                    )
                } else {
                    // Specific category
                    helper.getCommunityPostsByTypeAndCategoryPage(
                        type = postType,
                        category = selectedCategory!!,
                        pageSize = pageSize,
                        lastDocument = lastSnap
                    )
                }
                Log.d("FeedScreen", "Fetched size=${fetched.size}")
                posts.addAll(fetched)
                lastSnap = last
                endReached = (fetched.size < pageSize) || (last == null)

                if (reset) {
                    if (fetched.isEmpty()) {
                        Toast.makeText(
                            ctx,
                            "No posts for type='$postType' category='${selectedCategory ?: "All"}'",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                    Toast.makeText(ctx, "Index building; try again shortly", Toast.LENGTH_LONG).show()
                    Log.w("FeedScreen", "Index not ready", e)
                } else {
                    Toast.makeText(ctx, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    Log.e("FeedScreen", "Firestore query error", e)
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Trigger reload when postType or selectedCategory changes
    LaunchedEffect(postType, selectedCategory) {
        fetchPage(reset = true)
    }
    // Pagination: when scrolled to bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { idx ->
                if (idx != null && idx >= posts.size - 1) {
                    fetchPage(reset = false)
                }
            }
    }

    Column(Modifier.fillMaxSize()) {
        // Category dropdown
        ExposedDropdownMenuBox(
            expanded = catMenuOpen,
            onExpandedChange = { catMenuOpen = !catMenuOpen },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = selectedCategory ?: "All categories",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                label = { Text("Category") },
                modifier = Modifier.menuAnchor().fillMaxWidth()



            )
            ExposedDropdownMenu(
                expanded = catMenuOpen,
                onDismissRequest = { catMenuOpen = false },
            ) {
                // “All categories” → null filter
                DropdownMenuItem(text = { Text("All categories") }, onClick = {
                    selectedCategory = null
                    catMenuOpen = false

                })
                categories.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat) }, onClick = {
                        selectedCategory = cat
                        catMenuOpen = false
                    })
                }
            }
        }
        Divider()

        // Content
        if (!isLoading && posts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No posts yet in this category/type", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(posts) { post ->
                    PostCard(post) {
                        navController.navigate("postDetail/${post.id}")
                    }
                }
                if (isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
