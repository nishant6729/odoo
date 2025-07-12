package com.example.skillswaps.Screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.skillswaps.ImageBackend.ImageHandler
import com.example.skillswaps.R
import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ImageUploadScreen(navController: NavController,id:String) {

    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val scope = rememberCoroutineScope()

    // Step 1: Keep track of the picked image URI
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var user by remember { mutableStateOf<User?>(null) }

    val firestoreHelper = FirestoreHelper()
    val imageHandler = ImageHandler()

    // Step 2: When the Composable first launches, fetch the user from Firestore
    LaunchedEffect(Unit) {
        user = firestoreHelper.getUserData()
    }

    // Step 3: Launcher to pick an image from the gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(255, 247, 212))
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(80.dp))

        Text(
            text = "Upload your Photo",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Personalise your account with a profile picture upload",
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 0.4.sp,
            lineHeight = 22.sp,
            modifier = Modifier.heightIn(min = 60.dp)
        )
        Spacer(Modifier.height(100.dp))

        // Step 4: A box the user taps to launch the gallery
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GalleryDialog(imageUri, galleryLauncher)
        }

        Spacer(Modifier.height(100.dp))

        // Step 5: "Next" button
        Button(
            onClick = {
                // Always get the *current* FirebaseAuth UID here, inside onClick:
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUid.isNullOrEmpty()) {
                    Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (imageUri == null) {
                    Toast.makeText(context, "Please select an image first", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Launch a coroutine to upload & update Firestore
                scope.launch {
                    imageHandler.uploadImageToCloudinaryAndSaveToFirestore2(
                        uri = imageUri!!,
                        userId = id,
                        contentResolver = contentResolver,
                        onSuccess = {
                            // Navigate only on success (on main thread)
                            navController.navigate("communityHome") {
                                popUpTo("imageupload") { inclusive = true }
                            }
                        },
                        onFailure = { errorMsg ->
                            // Show the error on the MAIN thread
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(76, 61, 61)),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(50.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Next",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

