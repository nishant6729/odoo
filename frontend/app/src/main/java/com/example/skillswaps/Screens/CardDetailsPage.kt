package com.example.skillswaps.Screens

import android.widget.Space
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.skillswaps.R
import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailsPage(navController: NavController,uid: String,flag:Boolean=true){
    var firestoreHelper= FirestoreHelper()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,

    )
    val availabilityList = listOf("Morning", "Afternoon", "Evening", "Weekend")
    val selectedAvailabilities = remember { mutableStateListOf<String>() }
    var context= LocalContext.current
    var wanted by remember{mutableStateOf("")}
    var offering by remember{mutableStateOf("")}
    var req by remember { mutableStateOf("") }
    var send by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<User>(User()) }
    var curruser by remember { mutableStateOf<User>(User()) }
    val font1 = FontFamily(Font(R.font.font1, FontWeight.ExtraBold))
    var curruid by remember{mutableStateOf("")}
    curruid= FirebaseAuth.getInstance().currentUser?.uid?:""
    LaunchedEffect(Unit) {
        user= firestoreHelper.getUserByUid(uid)?:User()
        curruser=firestoreHelper.getUserByUid(curruid)?:User()
    }
    val currUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isPrivateProfile = !user.publicId
    val isFriend = currUid in user.friends
    val alreadyRequested = currUid in user.friendRequest
    Scaffold(topBar = {
        TopAppBar(title = {
            Row(modifier=Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center)
            {
                Text(user.firstName+" "+user.lastName,
                    color=Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    fontFamily = font1)


            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            Color(184,150,83,255)
        ))
    },  bottomBar = {
        BottomAppBar(containerColor = Color(184,150,83,255)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                if (!flag) {
                    // your ACCEPT button as before…
                    Button(
                        onClick = { /* … */ },
                        colors = ButtonDefaults.buttonColors(Color(112,177,0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                    ) {
                        Text("ACCEPT", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }

                } else {
                    when {
                        // 1) Private profile & not yet friends → show “Friend Request”
                        isPrivateProfile && !isFriend && !alreadyRequested -> {
                            Button(
                                onClick = {
                                    // append currUid into user.friendRequest
                                    val updated = user.friendRequest + currUid
                                    firestoreHelper.updateField(
                                        collection = "data",
                                        docId = user.uid,
                                        field = "friendRequest",
                                        value = updated
                                    ) {
                                        Toast.makeText(context, "Request sent!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C8B82)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(65.dp)
                            ) {
                                Text("Send Friend Request", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                            }
                        }
                        // 2) Private but already requested OR not private & not flagged → show disabled Swap
                        (isPrivateProfile && !isFriend && alreadyRequested) -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(65.dp)
                            ) {
                                Text("Request Pending", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
                        }
                        // 3) Public profile OR they are already friends → normal Swap
                        else -> {
                            Button(
                                onClick = { scope.launch { sheetState.show() } },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(76,61,61,255)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(65.dp)
                            ) {
                                Text("Swap", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }) {
        Column(modifier=Modifier
            .padding(it)
            .fillMaxSize()
            .background(Color(255, 247, 212, 255)))
        {
            Spacer(modifier=Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically)
            {
                Image(painter = rememberAsyncImagePainter(model = user.imageUrl),
                    contentDescription = null,modifier=Modifier
                        .height(200.dp)
                        .width(400.dp)


                )

            }
            Spacer(modifier=Modifier.height(20.dp))
            LazyRow (modifier=Modifier.padding(8.dp)){
                items (user.skills){
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color.Gray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(
                                Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color =Color.Black
                        )
                    }
                    Spacer(modifier=Modifier.width(8.dp))
                }
            }
            Spacer(modifier=Modifier.height(16.dp))
            Text(
                user.description,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium, modifier = Modifier.padding(10.dp),
                letterSpacing = 0.6.sp,
                lineHeight = 21.sp,
                maxLines = 12,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier=Modifier.height(16.dp))
            Column (modifier=Modifier.wrapContentSize().padding(16.dp)){
                Row(modifier=Modifier.wrapContentSize()){
                    Text("Achievements:", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(modifier=Modifier.width(4.dp))
                    Text(user.achievements, fontWeight = FontWeight.Medium, fontSize = 14.sp,
                        modifier=Modifier.offset(0.dp,2.dp), maxLines = 2,
                        overflow = TextOverflow.Clip)
                }

                Spacer(modifier=Modifier.height(16.dp))
                Row(modifier=Modifier.wrapContentSize()){
                    Text("DOB:", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(modifier=Modifier.width(4.dp))
                    Text(user.dob, fontWeight = FontWeight.Medium, fontSize = 14.sp,
                        modifier=Modifier.offset(0.dp,2.dp))
                }


                Spacer(modifier=Modifier.height(16.dp))

                Row(modifier=Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.Center){
                    Text("Occupation:", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(modifier=Modifier.width(4.dp))
                    Text(user.occupation, fontWeight = FontWeight.Medium, fontSize = 14.sp,
                        modifier=Modifier.offset(0.dp,2.dp))
                }
                Spacer(modifier=Modifier.height(16.dp))
                Row(modifier=Modifier.wrapContentSize()){
                    Text("Phone:", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(modifier=Modifier.width(4.dp))
                    Text(user.phone, fontWeight = FontWeight.Medium, fontSize = 14.sp,
                        modifier=Modifier.offset(0.dp,2.dp))
                }
            }



        }
    }
    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { sheetState.hide() }
            },
            sheetState = sheetState,
            dragHandle = { /* you can supply a custom handle here */ },
        ) {
            // Put whatever content you want in the sheet
            // :

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 24.dp)
            ) {
                Spacer(modifier=Modifier.height(12.dp))
                Text(
                    "Wanted skills",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    items(user.skills) { skill ->
                        Box(
                            modifier = Modifier
                                .border(if(skill==wanted){
                                    2.dp
                                }else 1.dp, if(skill==wanted){
                                    Color(76,61,61,255)

                                }
                                else{
                                    Color.Gray
                                }, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(12.dp).clickable(onClick = {
                                    wanted=skill
                                })
                        ) {
                            Text(skill)
                        }
                    }
                }
                Text(
                    "Offering Skills",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    items(curruser.skills) { skill ->


                        Box(
                            modifier = Modifier
                                .border(if(skill==offering){
                                    2.dp
                                }else 1.dp, if(skill==offering){
                                    Color.Black

                                }
                                else{
                                    Color.Gray
                                }, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(12.dp).clickable(onClick = {offering=skill})
                        ) {
                            Text(skill)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Your Availability",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    items(availabilityList) { slot ->
                        val isChosen = slot in selectedAvailabilities
                        Box(
                            modifier = Modifier
                                .border(
                                    width = if (isChosen) 2.dp else 1.dp,
                                    color = if (isChosen) Color(0xFF4C3D3D) else Color.Gray,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable {
                                    if (isChosen) selectedAvailabilities.remove(slot)
                                    else selectedAvailabilities.add(slot)
                                }
                        ) {
                            Text(slot)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        scope.launch { sheetState.hide() }
                    }) {
                        Text("Cancel",color=Color(76,61,61,255))
                    }
                    TextButton(onClick = {
                        // TODO: perform your swap logic here
                        scope.launch {
                            firestoreHelper.swapSkills(curruid,user.uid,selectedAvailabilities,context)
                            sheetState.hide() }
                    }) {
                        Text("Confirm",color=Color(76,61,61,255))
                    }
                }
            }
        }
    }

}

