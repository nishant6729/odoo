package com.example.skillswaps.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import com.example.skillswaps.R

import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedUsersScreen(
    pageSize: Int = 5,

    navController: NavController,onUserClick: (User) -> Unit = {}
) {
    val firestoreHelper = remember { FirestoreHelper() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // The full list of liked UIDs:
    var likedIds by remember { mutableStateOf<List<String>>(emptyList()) }
    // Pagination index into likedIds
    var currentIndex by remember { mutableStateOf(0) }
    // Loaded User objects so far
    val users = remember { mutableStateListOf<User>() }
    // Loading & endReached flags
    var isLoading by remember { mutableStateOf(false) }
    var endReached by remember { mutableStateOf(false) }

    // LazyListState to detect scroll
    val listState = rememberLazyListState()

    // Load the likedIds once on first composition
    LaunchedEffect(Unit) {
        isLoading = true
        val fetchedLiked = firestoreHelper.getCurrentUserLikedIds()
        likedIds = fetchedLiked
        // If empty, show a toast or simply endReached=true so no pages load
        if (fetchedLiked.isEmpty()) {
            endReached = true
            Toast.makeText(context, "You have not liked any users yet", Toast.LENGTH_SHORT).show()
        } else {
            // load first page
            val nextEnd = (0 + pageSize).coerceAtMost(fetchedLiked.size)
            val pageIds = fetchedLiked.subList(0, nextEnd)
            val fetchedUsers = firestoreHelper.getUsersByIds(pageIds)
            // Optionally reorder fetchedUsers to match pageIds order:
            val ordered = pageIds.mapNotNull { id ->
                fetchedUsers.find { it.uid == id }
            }
            users.addAll(ordered)
            currentIndex = nextEnd
            if (currentIndex >= fetchedLiked.size) {
                endReached = true
            }
        }
        isLoading = false
    }

    // Function to load next page
    fun loadNextPage() {
        if (isLoading || endReached) return
        isLoading = true
        coroutineScope.launch {
            val total = likedIds.size
            val start = currentIndex
            val nextEnd = (start + pageSize).coerceAtMost(total)
            if (start >= nextEnd) {
                endReached = true
                isLoading = false
                return@launch
            }
            val pageIds = likedIds.subList(start, nextEnd)
            val fetchedUsers = firestoreHelper.getUsersByIds(pageIds)
            // reorder to match pageIds:
            val ordered = pageIds.mapNotNull { id ->
                fetchedUsers.find { it.uid == id }
            }
            users.addAll(ordered)
            currentIndex = nextEnd
            if (currentIndex >= total) {
                endReached = true
            }
            isLoading = false
        }
    }

    // Pagination: when scrolled to last visible item, load more
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }
            .distinctUntilChanged()
            .collect { index ->
                if (index != null && index >= users.size - 1 && !endReached) {
                    loadNextPage()
                }
            }
    }

    // UI
    var isSelected by remember{
        mutableStateOf("star")
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {

            },
            actions = {
                Row(modifier=Modifier
                    .wrapContentSize()
                    .padding(16.dp)){
                    IconButton(onClick = {

                    },modifier=Modifier.size(36.dp)) {
                        Icon(painter = painterResource(R.drawable.crown), contentDescription = null,tint=Color.White)
                    }
                    Spacer(modifier=Modifier.width(36.dp))
                    IconButton(onClick = {

                    }) {
                        Icon(painter = painterResource(R.drawable.crown),
                            contentDescription = null,
                            tint=Color(255,183,67,255))
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                Color(184,150,83,255)
            )

        )
    }
        , bottomBar = {
            BottomAppBar(modifier = Modifier.background(Color(184,150,83,255)),
                containerColor =Color(184,150,83,255)) {

                Row (modifier=Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(184, 150, 83, 255)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly){
                    IconButton(onClick = {
                        isSelected="home"
                        navController.navigate("home")
                    }, colors = IconButtonDefaults.iconButtonColors(
                        if(isSelected=="home"){
                            Color(234,197,88,255)
                        }else{
                            Color.Transparent
                        }
                    ), modifier=Modifier
                        .clip(RoundedCornerShape(0.5f))
                        .size(42.dp),

                        ) {
                        Icon(painter = painterResource(R.drawable.home), contentDescription = null,tint=Color.White,
                            modifier=Modifier.padding(6.dp))
                    }
                    IconButton(onClick = {
                        if(!(isSelected=="star")){
                            isSelected="star"
                            navController.navigate("likedUser")
                        }

                    }, colors = IconButtonDefaults.iconButtonColors(
                        if(isSelected=="star"){
                            Color(234,197,88,255)
                        }else{
                            Color.Transparent
                        }
                    ), modifier=Modifier
                        .clip(RoundedCornerShape(0.5f))
                        .size(45.dp),
                    ) {
                        Icon(painter = painterResource(R.drawable.star), contentDescription = null,tint=Color.White,
                            modifier=Modifier.padding(6.dp))
                    }
                    IconButton(onClick = {
                        isSelected="swap"
                        navController.navigate("SwapDonePage")
                    }, colors = IconButtonDefaults.iconButtonColors(
                        if(isSelected=="swap"){
                            Color(234,197,88,255)
                        }else{
                            Color.Transparent
                        }
                    ), modifier=Modifier
                        .clip(RoundedCornerShape(0.5f))
                        .size(45.dp)
                    ) {
                        Icon(painter = painterResource(R.drawable.swap), contentDescription = null,tint=Color.White,
                            modifier=Modifier.padding(6.dp))
                    }
                    IconButton(onClick = {
                        isSelected="account"
                        navController.navigate("requestPage")
                    }, colors = IconButtonDefaults.iconButtonColors(
                        if(isSelected=="account"){
                            Color(234,197,88,255)
                        }else{
                            Color.Transparent
                        }
                    ), modifier=Modifier
                        .clip(RoundedCornerShape(0.5f))
                        .size(45.dp)
                    ) {
                        Icon(painter = painterResource(R.drawable.user), contentDescription = null,tint=Color.White,
                            modifier=Modifier.padding(6.dp))
                    }
                }
            }

        }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(Color(255, 247, 212, 255))
        ) {
            item{
                Text("Swap,learn,grow", fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp))

            }
            items(users) { user ->
                if(user.uid!= FirebaseAuth.getInstance().currentUser?.uid){
                    UserCard(user = user, onClick = {  })
                }



            }
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

