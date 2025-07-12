package com.example.skillswaps.Screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User

@Composable
fun SPersonalDetails(navController: NavController){
    val context=LocalContext.current
    var worklink by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var achivements by remember { mutableStateOf("") }
    val wordCount = description
        .trim()
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }
        .size
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Gray,
        unfocusedBorderColor = Color.Gray,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        disabledBorderColor = Color.Gray,
        disabledTextColor = Color.Black,
        disabledLabelColor = Color.Gray
    )
    var user=User()
    val firestorehelper= FirestoreHelper()
    var phone by remember{
        mutableStateOf("")
    }
    LaunchedEffect(Unit) {
        user=firestorehelper.getUserData()?: User()
    }
    Column(modifier=Modifier
        .fillMaxSize()
        .background(Color(255, 247, 212, 255))
        .padding(16.dp)){
        Spacer(modifier=Modifier.height(32.dp))
        Column(modifier=Modifier
            .fillMaxWidth()
            .wrapContentHeight()) {
            Column(modifier=Modifier.padding(8.dp)){
                Text(
                    "Personal Details", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Provide your personal details to enhance your Skill Swap experience and connect with like-minded individuals",
                    minLines = 3, fontSize = 14.sp, fontWeight = FontWeight.Light,
                    letterSpacing = 0.2.sp
                )
            }
            Spacer(modifier=Modifier.height(16.dp))
            if(phone==""){
                OutlinedTextField(
                    value = phone,
                    singleLine = true,
                    colors = textFieldColors,
                    onValueChange = { phone = it },
                    label = { Text(text = "Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), placeholder = {Text("Enter your phone number")}

                )
                Spacer(modifier=Modifier.height(16.dp))
            }
            OutlinedTextField(
                value = worklink,
                singleLine = true,
                colors = textFieldColors,
                onValueChange = { worklink = it },
                label = { Text(text = "Work Link") },

                modifier = Modifier.fillMaxWidth(), placeholder = {Text("E.x.LinkedIn Profile:[Enter your LinkedIn profile URL ", minLines = 2)}

            )
            Spacer(modifier=Modifier.height(8.dp))
            OutlinedTextField(
                supportingText = {
                    Text("$wordCount/100 words", fontWeight = FontWeight.Bold)
                },
                value = description,
                singleLine = false,
                colors = textFieldColors,
                onValueChange = { val words = it.trim().split("\\s+".toRegex())
                    if (words.size <= 100) {
                        description = it
                    } else {
                        Toast.makeText(context, "Maximum 100 words allowed", Toast.LENGTH_SHORT).show()
                    }
                },
                label = { Text(text = "Description") },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = {Text("Add something about yourself")
                }

            )
            Spacer(modifier=Modifier.height(8.dp))
            OutlinedTextField(
                value = achivements,
                singleLine = true,
                colors = textFieldColors,
                onValueChange = { achivements = it
                },
                label = { Text(text = "Achivements") },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), placeholder = {Text("Add your achivements")}

            )
            Spacer(modifier=Modifier.height(32.dp))
            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier=Modifier.fillMaxWidth()){
                Button(
                    onClick = {
                        when {
                            wordCount < 50 -> {
                                Toast.makeText(
                                    context,
                                    "Please enter at least 50 words in description",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            wordCount > 100 -> {
                                Toast.makeText(
                                    context,
                                    "Maximum 100 words allowed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {

                                val updates: Map<String, Any>
                                if(phone==""){
                                    updates = mapOf(
                                        "description" to description,              // new occupation string
                                        "achievements" to achivements,
                                        "worklink" to worklink,

                                        )
                                } else{
                                    updates= mapOf(
                                        "description" to description,              // new occupation string
                                        "achievements" to achivements,
                                        "worklink" to worklink,
                                        "phone" to phone
                                    )
                                }
                                firestorehelper.updateUserFields(updates, context)
                                navController.navigate("UploadPhoto")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(76, 61, 61, 255)
                    ),

                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp)
                        .offset(0.dp, -16.dp), // Adjusted for better appearance
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

    }
}