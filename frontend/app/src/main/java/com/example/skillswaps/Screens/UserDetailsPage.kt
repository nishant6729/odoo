package com.example.skillswaps.Screens

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.snapshots.SnapshotStateList


import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.skillswaps.R
import com.google.firebase.auth.FirebaseAuth
import com.maxkeppeker.sheets.core.icons.sharp.ContentCopy
import kotlinx.coroutines.coroutineScope

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun UserListScreen(
    navController: NavController,
    pageSize: Long = 3L,             // how many users per page
    onUserClick: (User) -> Unit = {}  // optional: handle click on a user
) {
    val firestoreHelper = remember { FirestoreHelper() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    //Drawer ke liye
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var searchQuery by remember { mutableStateOf("") }


    // State: list of loaded users
    val users = remember { mutableStateListOf<User>() }
    // State: loading flag
    var isLoading by remember { mutableStateOf(false) }
    // State: last DocumentSnapshot for pagination
    var lastSnapshot by remember { mutableStateOf<DocumentSnapshot?>(null) }
    // State: whether we've reached the end (no more pages)
    var endReached by remember { mutableStateOf(false) }

    // LazyListState to detect scroll
    val listState = rememberLazyListState()
    var UserID by remember { mutableStateOf<String>("") }

    // Function to load next page
    fun loadNextPage() {
        if (isLoading || endReached) return
        isLoading = true
        coroutineScope.launch {
            val (fetchedUsers, last) = firestoreHelper.getUsersPage(pageSize, lastSnapshot)
            if (fetchedUsers.isNotEmpty()) {
                users.addAll(fetchedUsers)
                lastSnapshot = last
                // If fewer items than pageSize, we've likely reached the end
                if (fetchedUsers.size < pageSize) {
                    endReached = true
                }
            } else {
                // No results: either error or no more data
                endReached = true
                if (lastSnapshot == null) {
                    // First load returned empty: no users in database
                    Toast.makeText(context, "No users found", Toast.LENGTH_SHORT).show()
                }
            }
            isLoading = false


        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        loadNextPage()


    }

    // Pagination: when scrolled to the last item, load more
    LaunchedEffect(listState) {
        snapshotFlow {
            // index of last visible item
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }
            .distinctUntilChanged()
            .collect { index ->
                // If visible index == last loaded user index, attempt load next
                if (index != null && index >= users.size - 1) {
                    loadNextPage()
                }
            }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // Main Content
        HomeContent (onMenuClick = {
            coroutineScope.launch {
                drawerState.open()
            }
        },listState,isLoading,users,UserID,navController)

        // Custom Drawer
        if (drawerState.isOpen) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Drawer
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.6f)
                        .clip(
                            RoundedCornerShape(
                                topEnd = 24.dp,
                                bottomEnd = 24.dp
                            )
                        )
                        .background(Color.White),
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp
                ) {
                    DrawerContent(drawerState, navController = navController) {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                }

                // Blurred transparent overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }
                        .background(Color.Black.copy(alpha = 0.2f))
                        .graphicsLayer {
                            alpha = 0.99f // Needed for blur
                            renderEffect = RenderEffect
                                .createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
                                .asComposeRenderEffect()
                        }
                )
            }
        }
    }

    // UI: LazyColumn showing users, plus a loading indicator at bottom

}

@Composable
fun DrawerContent(drawerState: DrawerState, navController: NavController, onClick: () -> Unit) {
    val context=LocalContext.current
    var coroutineScope=rememberCoroutineScope()
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 32.dp)
        .background(Color.White)) {
        Spacer(modifier=Modifier.height(10.dp))
        Row (modifier=Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.Left){
            IconButton(onClick={
                coroutineScope.launch {
                    drawerState.close()
                }
            }) {
                Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = null
                    ,tint=Color.Black)
            }
            Spacer(modifier=Modifier.width(50.dp))
            Text("Menu",
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp)
        }
        Spacer(modifier=Modifier.height(10.dp))
        Divider(
            color = Color.Black,
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier=Modifier.height(60.dp))
        DrawerItems("Personal Details",R.drawable.portfolio,onClick={
            navController.navigate("PersonalDetails")
        })
        DrawerItems("Community",R.drawable.community,onClick={
            navController.navigate("communityHome")
        })
        DrawerItems("Premium",R.drawable.crown1,onClick={
            navController.navigate("Premium")
        })
        DrawerItems("Time Table",R.drawable.table,onClick={
            navController.navigate("TimeTable")
        })
        DrawerItems("History",R.drawable.history,onClick={
            navController.navigate("History")
        })
        DrawerItems("Portfolio",R.drawable.notepad,onClick={
            navController.navigate("Portfolio")
        })
        DrawerItems("Personal Coaching",R.drawable.coach,onClick={
            navController.navigate("PersonalCoaching")
        })
        DrawerItems("Friend Requests",R.drawable.friendrequest,onClick={
            navController.navigate("FriendRequest")
        })
        Divider(
            color = Color.Gray,
            thickness = 1.5.dp,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier= Modifier.height(180.dp))
        Column(verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End) {
            Divider(
                color = Color.Black,
                thickness = 2.5.dp,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier=Modifier.height(6.dp))
            Row (modifier=Modifier
                .fillMaxWidth()
                .clickable(onClick = {
                    FirestoreHelper().logoutUser(context,navController)
                }),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Left){
                Spacer(modifier=Modifier.width(16.dp))
                IconButton(onClick={

                }) {
                    Icon(painter = painterResource(R.drawable.baseline_logout_24), contentDescription = null
                        ,tint=Color.Black,
                        modifier=Modifier.size(30.dp))
                }
                Spacer(modifier=Modifier.width(18.dp))
                Text("Log out",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp)
            }

        }


    }
}
@Composable
fun DrawerItems(text:String,icon:Int,onClick: () -> Unit){
    Divider(
        color = Color.Gray,
        thickness = 1.5.dp,
        modifier = Modifier
            .fillMaxWidth()
    )
    Spacer(modifier=Modifier.height(10.dp))
    Row (modifier=Modifier
        .fillMaxWidth()
        .clickable(onClick = {
            onClick()
        }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Left){
        Spacer(modifier=Modifier.width(8.dp))
        IconButton(onClick={

        },modifier=Modifier.size(35.dp)) {
            Icon(painter = painterResource(icon), contentDescription = null
                ,tint=Color.Black,modifier=Modifier.size(28.dp))
        }
        Spacer(modifier=Modifier.width(10.dp))
        Text(text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp)
    }
    Spacer(modifier=Modifier.height(10.dp))
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(onMenuClick:()->Unit,
                listState: LazyListState,isLoading: Boolean,
                users: SnapshotStateList<User>,Uid:String,navController: NavController){
    var isSelected by remember{
        mutableStateOf("home")
    }
    // Inside your HomeContent composable:

    var searchQuery by remember { mutableStateOf("") }

    val filteredUsers by remember {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                // No query → show all users
                users.toList()
            } else {
                // Query present → filter by name or skill
                users.filter { user ->
                    user.firstName.contains(searchQuery, ignoreCase = true) ||
                            user.lastName.contains(searchQuery, ignoreCase = true) ||
                            user.skills.any { it.contains(searchQuery, ignoreCase = true) }
                }
            }
        }
    }

// Then use `filteredUsers` in your LazyColumn:
// items(filteredUsers) { … }


    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val coroutineScope=rememberCoroutineScope()
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Row(modifier=Modifier
                    .wrapContentSize()
                    .padding(8.dp)){
                    IconButton(onClick = onMenuClick,modifier=Modifier.size(36.dp)) {
                        Icon(painter = painterResource(R.drawable.menu), contentDescription = null,tint=Color.White)
                    }
                    Spacer(modifier=Modifier.width(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search users or skills", fontSize = 12.sp) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp),

                    )

                }
            }
            , actions = {
                Row(modifier=Modifier
                    .wrapContentSize()
                    .padding(16.dp)){


                    IconButton(onClick = {

                    },modifier=Modifier.size(36.dp)) {
                        Icon(painter = painterResource(R.drawable.search), contentDescription = null,tint=Color.White)
                    }
                    Spacer(modifier=Modifier.width(36.dp))
                    IconButton(onClick = {

                    }) {
                        Icon(painter = painterResource(R.drawable.crown),
                            contentDescription = null,
                            tint=Color(255,183,67,255))
                    }
                }
            }, scrollBehavior = scrollBehavior,
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

                        isSelected="star"
                        navController.navigate("likedUser")
                    }, colors = IconButtonDefaults.iconButtonColors(
                        if(isSelected=="star"){
                            Color(234,197,88,255)
                        }else{
                            Color.Transparent
                        }
                    ), modifier=Modifier
                        .clip(RoundedCornerShape(0.5f))
                        .size(45.dp)
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
            items(filteredUsers) { user ->
                if(user.uid!= FirebaseAuth.getInstance().currentUser?.uid){
                    UserCard(user = user, onClick = { navController.navigate("detailsPage/${user.uid}") })
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
@Composable


fun UserCard(user: User, onClick: () -> Unit = {}) {
    // Example state used in vote logic
    val helper = remember { FirestoreHelper() }
    val context=LocalContext.current
    val scope = rememberCoroutineScope()


    var upCount by remember { mutableStateOf(user.upvotes) }
    var downCount by remember { mutableStateOf(user.downvotes) }

    val likedUsers = remember { mutableStateListOf<String>() }
    var vote by remember { mutableStateOf("") }
    var firestoreHelper= FirestoreHelper()
    // Load likedUsers once when this Composable enters composition
    LaunchedEffect(Unit) {
        try {
            val fetched: List<String> = firestoreHelper.getCurrentUserLikedIds()
            likedUsers.clear()
            likedUsers.addAll(fetched)
            // Now set initial vote state for this 'user'
            if (likedUsers.contains(user.uid)) {
                vote = "Up"
            } else {
                vote = ""
            }
        } catch (e: Exception) {
            // handle error, e.g., show Toast
            Toast.makeText(context, "Failed to load liked users: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    // Example fonts (replace with your own)
    val font1 = FontFamily(Font(R.font.font1, FontWeight.SemiBold))
    val font2 = FontFamily(Font(R.font.font2, FontWeight.Medium))
    val font3 = FontFamily(Font(R.font.font3, FontWeight.Normal))
    val font4 = FontFamily(Font(R.font.font4, FontWeight.Normal))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(Color(0xFFFFD95A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Image + location
            Box(
                modifier = Modifier
                    .size(width = 130.dp, height = 200.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = user.imageUrl),
                    contentDescription = "User Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Color(0xAA000000), shape = RoundedCornerShape(topEnd = 6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_location_pin_24),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.location,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } // end Box

            Spacer(modifier = Modifier.width(12.dp))

            // Right section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()  // or IntrinsicSize.Min if desired
            ) {
                // Name & occupation
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${user.firstName} ${user.lastName}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = font1,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(${user.occupation})",
                    fontSize = 13.sp,
                    fontFamily = font2,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = user.description,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    fontFamily = font3,
                    color = Color.Black.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Upvote / Downvote row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(
                            if (vote == "Up") R.drawable.baseline_thumb_up_24
                            else R.drawable.outline_thumb_up_24
                        ),
                        contentDescription = "Upvote",
                        tint = if (vote == "Up") Color.Blue else Color.Black,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                val newVote = if (vote == "Up") "" else "Up"
                                // optimistic UI update
                                if (newVote == "Up") {
                                    upCount += 1
                                    if (vote == "Down") downCount -= 1
                                } else {
                                    upCount -= 1
                                }
                                vote = newVote
                                scope.launch {
                                    helper.setUpvote(targetUid = user.uid, shouldUpvote = (newVote == "Up"))
                                }
                            }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$upCount", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upvote", fontSize = 13.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        painter = painterResource(
                            if (vote == "Down") R.drawable.baseline_thumb_down_24
                            else R.drawable.outline_thumb_down_off_alt_24
                        ),
                        contentDescription = "Downvote",
                        tint = if (vote == "Down") Color.Red else Color.Black,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                val newVote = if (vote == "Down") "" else "Down"
                                if (newVote == "Down") {
                                    downCount += 1
                                    if (vote == "Up") upCount -= 1
                                } else {
                                    downCount -= 1
                                }
                                vote = newVote
                                scope.launch {
                                    helper.setDownvote(targetUid = user.uid, shouldDownvote = (newVote == "Down"))
                                }
                            }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$downCount", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Downvote", fontSize = 13.sp)
                } // end Upvote/Downvote Row

                Spacer(modifier = Modifier.height(12.dp))

                // Contact Info aligned to end
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = user.phone,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = font4,
                        color = Color.DarkGray
                    )
                    Text(
                        text = user.email,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = font4,
                        color = Color.DarkGray
                    )
                }
            } // end Right Column
        } // end Row
    } // end Card
} // end UserCard
