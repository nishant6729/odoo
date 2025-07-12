package com.example.skillswaps.Screens

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.skillswaps.Auth.rememberGoogleSignInLauncher
import com.example.skillswaps.R
import com.example.skillswaps.ViewModel.AuthViewModel
import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient

import com.google.firebase.auth.GoogleAuthProvider


@Composable
fun SignInPage(navController: NavController,googleSignInClient: GoogleSignInClient,acti: Activity){

    var Email by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    val viewModel: AuthViewModel = viewModel()
    var visible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val firestoreHelper= FirestoreHelper()

    //Step 5 for Google Auth
    val startGoogleSignIn = rememberGoogleSignInLauncher(
        acti,
        navController = navController,
        client = googleSignInClient
    )

    // Handling back press explicitly
    BackHandler {
        Toast.makeText(context, "You can't go back at this stage", Toast.LENGTH_SHORT).show()
    }
    Column(modifier=Modifier.fillMaxSize().background(
        Color(255,247,212,255)
    )) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Distributes content to fit screen
        ) {
            // Top Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.on_boarding_bg),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp), // Reduce height to fit content
                    contentScale = ContentScale.FillWidth
                )

                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)) // Adjust size for balance
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Share What You Know, Learn What You Love.", fontWeight = FontWeight.Medium, fontSize = 16.sp)




            }

            // Middle Content
            Column(
                modifier = Modifier.fillMaxWidth().offset(0.dp,-0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = Email,
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(76,61,61,255),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedLabelColor = Color(76,61,61,255)
                    ),
                    onValueChange = { Email = it },
                    label = { Text(text = "Email ID") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_email_24),
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it
                        if(password.length>=8 && Email.isNotEmpty()){
                            isValid=true
                        }},
                    label = { Text(text= "Password") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            val image = if (visible) {
                                R.drawable.baseline_visibility_24
                            } else {
                                R.drawable.baseline_visibility_off_24
                            }
                            Icon(
                                painter = painterResource(id = image),
                                contentDescription = null,
                                tint = Color(76,61,61,255)
                            )
                        }
                    },
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation()
                )
            }

            // Bottom Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,modifier=Modifier.offset(0.dp,-0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("New Member?")
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = { navController.navigate("signup") }, modifier = Modifier.offset(-8.dp, -16.dp)) {
                        Text(text = "Sign Up", fontWeight = FontWeight.Medium, color =Color(0xFF4C8B82)
                        )
                    }
                }


                OrSeparator()
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier=Modifier.fillMaxWidth().offset(0.dp,-10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {startGoogleSignIn()},modifier=Modifier.size(60.dp)) {
                        Image(painter = painterResource(R.drawable.google), contentDescription = null)
                    }
                    Spacer(modifier=Modifier.padding(8.dp))
                    IconButton(onClick = {},modifier=Modifier.size(50.dp)) {
                        Image(painter = painterResource(R.drawable.facebook), contentDescription = null)
                    }
                    Spacer(modifier=Modifier.padding(8.dp))
                    IconButton(onClick = {},modifier=Modifier.size(50.dp)) {
                        Image(painter = painterResource(R.drawable.apple), contentDescription = null)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (activity != null) {
                            viewModel.signInWithEmail(Email, password, activity) { success ->
                                if (success) {
                                    val user=User(email = Email)
                                    val updates: Map<String, Any> = mapOf(
                                        "email" to Email
                                    )

                                    firestoreHelper.updateUserFields(updates,context)
                                    navController.navigate("home"){{
                                        popUpTo("signin") { inclusive = true }
                                    }

                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Authentication failed. Please check your credentials.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        } else {
                            Toast.makeText(context, "Activity is not available.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(76,61,61,255)
                    ),

                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp).offset(0.dp,-16.dp), // Adjusted for better appearance
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Login",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }

}
@Composable
fun OrSeparator(
    text: String = "or",
    lineColor: Color = Color.Black,
    thickness: Dp = 1.dp,
    textPadding: Dp = 8.dp,
    modifier: Modifier = Modifier.fillMaxWidth().offset(0.dp,-10.dp)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Divider(
            color = lineColor,
            thickness = thickness,
            modifier = Modifier
                .weight(1f)
        )
        Text(
            text = text,
            color = lineColor,
            modifier = Modifier
                .padding(horizontal = textPadding)
        )
        Divider(
            color = lineColor,
            thickness = thickness,
            modifier = Modifier
                .weight(1f)
        )
    }
}
