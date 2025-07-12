package com.example.skillswaps.Screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User

import com.google.firebase.auth.FirebaseAuth
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState

import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarSelection



import kotlinx.coroutines.launch
import java.security.Key
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetails(navController: NavController) {
    // -- State holders --
    var occupation by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val calendarState = rememberUseCaseState()
    var skills by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    var formattedDate by remember { mutableStateOf(dateFormatter.format(selectedDate)) }
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Gray,
        unfocusedBorderColor = Color.Gray,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        disabledBorderColor = Color.Gray,
        disabledTextColor = Color.Black,
        disabledLabelColor = Color.Gray
    )
    var user:User=User()
    var userId by remember { mutableStateOf("") }
    val firestorehelper= FirestoreHelper()
    val context=LocalContext.current
    var coroutineScope=rememberCoroutineScope()
    LaunchedEffect(Unit) {
        user=firestorehelper.getUserData()?: User()
        coroutineScope.launch {
            var uid=FirebaseAuth.getInstance().currentUser?.uid?:""
            var updates=mapOf("uid" to uid)
            FirestoreHelper().updateUserFields(updates,context)
        }
    }
    var fname by remember{
        mutableStateOf("")
    }
    var lname by remember{
        mutableStateOf("")
    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(255, 247, 212))
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            text = "Personal Details",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Provide your personal details to enhance your Skill Swap experience and connect with like-minded individuals",
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 0.4.sp,
            lineHeight = 22.sp,
            modifier = Modifier.heightIn(min = 60.dp)
        )
        Spacer(Modifier.height(16.dp))
        if(user.firstName==""){
            OutlinedTextField(
                value = fname,
                onValueChange = { fname = it },
                label = { Text("First Name") },
                placeholder = { Text("Enter your First Name") },
                singleLine = true,
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = lname,
                onValueChange = { lname = it },
                label = { Text("Last Name") },
                placeholder = { Text("Enter your Last Name") },
                singleLine = true,
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            // Occupation
            Spacer(Modifier.height(16.dp))

        }
        OutlinedTextField(
            value = occupation,
            onValueChange = { occupation = it },
            label = { Text("Occupation") },
            placeholder = { Text("Enter your Occupation") },
            singleLine = true,
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        Spacer(Modifier.height(16.dp))

        // Skills
        OutlinedTextField(
            value = skills,
            onValueChange = { skills = it },
            label = { Text("Skills") },
            placeholder = { Text("Enter the skills you own") },
            singleLine = true,
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // Experience
        OutlinedTextField(
            value = experience,
            onValueChange = { experience = it },
            label = { Text("Experience") },
            placeholder = { Text("Enter your experience duration") },
            singleLine = true,
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(16.dp))

        // Location
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            placeholder = { Text("Enter your location") },
            singleLine = true,
            colors =textFieldColors,
            modifier = Modifier.fillMaxWidth(),keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        Spacer(Modifier.height(16.dp))

        // Date of Birth (read‑only, opens sheet)
        OutlinedTextField(
            value = date,
            onValueChange = { /* no-op */ },
            label = { Text("Date of Birth") },
            placeholder = { Text("Select your date of birth") },

            enabled = false,
            colors = textFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { calendarState.show() }
        )

        // The bottom‑sheet date picker
        CalendarDialog(
            state = calendarState,
            selection = CalendarSelection.Date { dateHere ->
                selectedDate = dateHere
                formattedDate = dateFormatter.format(selectedDate)
                date = formattedDate
            }
        )

        Spacer(Modifier.height(64.dp))

        // Next button
        Button(
            onClick = {
                val updates:Map<String,Any>
                if(fname==""){
                    updates= mapOf(
                        "occupation" to occupation,              // new occupation string
                        "skills" to skills.split(",").map { it.trim() },
                        "experience" to experience,
                        "location" to location,
                        "dob" to date,

                        )
                }
                else{
                    updates=mapOf(
                        "firstName" to fname,
                        "lastName" to lname,
                        "occupation" to occupation,              // new occupation string
                        "skills" to skills.split(",").map { it.trim() },
                        "experience" to experience,
                        "location" to location,
                        "dob" to date,

                        )
                }


                firestorehelper.updateUserFields(updates, context)
                navController.navigate("pdetails2") {
                    popUpTo("pdetails1") { inclusive = true }
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
