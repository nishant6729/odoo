package com.example.skillswaps.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.skillswaps.R
import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestPage(navController: NavController){
    var isSelected by remember{
        mutableStateOf("account")
    }
    val font1 = FontFamily(Font(R.font.font1, FontWeight.ExtraBold))
    val viewModelScope= rememberCoroutineScope()
    var firestoreHelper= FirestoreHelper()
    var userList by remember { mutableStateOf<List<User>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        try {
            val ids   = firestoreHelper.getRequestReceivedList()
            val users = firestoreHelper.getUsersFromIds(ids)
            // reassign the entire list
            userList = users
            Log.d("RequestPage", "Loaded ${users.size} users")
            isLoading=false
        } catch (e: Exception) {
            Log.e("RequestPage", "Error loading users", e)
        }

    }
    Scaffold (topBar = {
        TopAppBar(title = {
            Row(modifier=Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center)
            {
                Text("Request Received",
                    color=Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    fontFamily = font1)


            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            Color(184,150,83,255)
        ))
    },bottomBar = {
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
        ){
        LazyColumn (modifier=Modifier.fillMaxSize().
        background(Color(255, 247, 212, 255)).
        padding(it)){

            items(userList) {user->
                RequestCard(user, onClick = {
                    navController.navigate("requestMemberDetails/${user.uid}")
                }, onClick2 = {
                    firestoreHelper.acceptSwapRequest(user.uid)
                })
            }
            if(userList.isEmpty()){
                item(){
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally, // ← here!
                            verticalArrangement = Arrangement.Center
                        ) {
                            Spacer(modifier=Modifier.height(250.dp))
                            Text(
                                "No Request Received",
                                color = Color.Black,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }


                }

            }
            if(isLoading){
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
fun RequestCard(user:User,onClick:()->Unit={},onClick2:()->Unit={}){
    val font1 = FontFamily(Font(R.font.font1, FontWeight.SemiBold))
    val font2 = FontFamily(Font(R.font.font2, FontWeight.Medium))
    val font3 = FontFamily(Font(R.font.font3, FontWeight.Normal))
    val font4 = FontFamily(Font(R.font.font4, FontWeight.Normal))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {onClick()},
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
                    Button(modifier=Modifier.fillMaxWidth(), onClick = {
                        onClick2()
                    },
                        colors = ButtonDefaults.buttonColors(Color(112,177,0,255))) {
                        Text("ACCEPT", fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color=Color.White)
                    }
                } // end Upvote/Downvote Row



                // Contact Info aligned to end

            } // end Right Column
        } // end Row
    } // end Card
}