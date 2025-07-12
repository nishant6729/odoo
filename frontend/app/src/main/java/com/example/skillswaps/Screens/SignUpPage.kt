package com.example.skillswaps.Screens


import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.skillswaps.R
import com.example.skillswaps.ViewModel.AuthViewModel

@Composable
fun SignUpPage(navController: NavController) {
    var Email by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var confmpassword by remember { mutableStateOf("") }
    val viewModel: AuthViewModel = viewModel()
    var visible by remember { mutableStateOf(false) }
    var visible2 by remember { mutableStateOf(false) }
    var fname by remember { mutableStateOf("") }
    var lname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as? Activity
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Gray,
        unfocusedBorderColor = Color.Gray,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        disabledBorderColor = Color.Gray,
        disabledTextColor = Color.Black,
        disabledLabelColor = Color.Gray
    )
    var isPublicId by remember { mutableStateOf(true) }

    BackHandler {
        Toast.makeText(context, "You can't go back at this stage", Toast.LENGTH_SHORT).show()
    }
    Column(
        modifier = Modifier
            .fillMaxSize().background(Color(255, 247, 212, 255))
            .padding(horizontal = 16.dp), // Reduced horizontal padding for efficient space usage
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Spacer(modifier = Modifier.height(48.dp))

        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(90.dp)
                .clip(RoundedCornerShape(16.dp)) // Reduced logo size slightly
        )
        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            Text(
                "Create account", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Create an account and enjoy the world of learning and connections",
                minLines = 2, fontSize = 14.sp, fontWeight = FontWeight.Light,
                letterSpacing = 0.2.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = fname,
            singleLine = true,
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            onValueChange = { fname = it },
            label = { Text(text = "First Name") },

            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lname,
            singleLine = true,
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            onValueChange = { lname = it },
            label = { Text(text = "Last Name") },

            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            singleLine = true,
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = { phone = it },
            label = { Text(text = "Phone") },

            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Email input
        OutlinedTextField(
            value = Email,
            singleLine = true,
            colors = textFieldColors,
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

        Spacer(modifier = Modifier.height(8.dp))

        // Password input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
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
                        tint = Color(0xFF4C8B82)
                    )
                }
            },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm password input
        OutlinedTextField(
            value = confmpassword,
            onValueChange = {
                confmpassword = it
                if (Email.isNotEmpty() && password.length >= 8 && confmpassword.length >= 8) {
                    isValid = true
                }
            },

            label = { Text(text = "Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { visible2 = !visible2 }) {
                    val image = if (visible2) {
                        R.drawable.baseline_visibility_24
                    } else {
                        R.drawable.baseline_visibility_off_24
                    }
                    Icon(
                        painter = painterResource(id = image),
                        contentDescription = null,
                        tint = Color(0xFF4C8B82)
                    )
                }
            },
            visualTransformation = if (visible2) VisualTransformation.None else PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // "Already a member" text with Sign In button
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Already a Member?")
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(onClick = {
                navController.navigate("signin") {
                    {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            }, modifier = Modifier.offset(-8.dp, -15.dp)) {
                Text(text = "Sign In", fontWeight = FontWeight.Medium, color = Color(0xFF4C8B82))
            }
        }

        Spacer(modifier = Modifier.height(0.dp))

            // Sign up button
            Button(
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    if (confmpassword != password) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    } else {
                        if (activity != null) {
                            viewModel.signUpWithEmail(
                                Email,
                                password,
                                activity,
                                navController,
                                fname,
                                lname,
                                phone
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "Activity is not available.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isValid) {
                        Color(0xFF4C8B82) // Active color
                    } else {
                        Color.LightGray // Disabled color
                    }
                ),
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(4.dp)) // Adjusted height for space efficiency
            ) {
                Text(
                    text = "Register",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
    }
}


