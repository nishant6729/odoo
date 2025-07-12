package com.example.skillswaps.Screens

import androidx.compose.runtime.Composable


import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState


import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(
    navController: NavController? = null  // pass NavController if you want to navigate after save
) {
    var publicId by remember { mutableStateOf(true) } // true = Public, false = Private

    val context = LocalContext.current
    val firestoreHelper = remember { FirestoreHelper() }
    val scope = rememberCoroutineScope()

    // States for user fields
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var skillsText by remember { mutableStateOf("") }   // comma-separated
    var location by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }          // formatted string "dd-MM-yyyy"
    var description by remember { mutableStateOf("") }
    var achievements by remember { mutableStateOf("") }
    var workLink by remember { mutableStateOf("") }

    // For date picker
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val calendarState = rememberUseCaseState()
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    // Hold the loaded User object (to get uid and any fields not shown here)
    var loadedUser by remember { mutableStateOf<User?>(null) }

    // Load user data once on composition
    LaunchedEffect(Unit) {
        try {
            val user = firestoreHelper.getUserData()  // assume this is a suspend or blocking call returning User?
            if (user != null) {
                loadedUser = user
                // Populate states
                firstName = user.firstName
                lastName = user.lastName
                phone = user.phone
                occupation = user.occupation
                // skills: join list to comma-separated string
                skillsText = user.skills.joinToString(", ")
                location = user.location
                experience = user.experience
                dob = user.dob
                // If dob non-blank, parse into selectedDate if possible
                if (user.dob.isNotBlank()) {
                    runCatching {
                        val parsed = LocalDate.parse(user.dob, dateFormatter)
                        selectedDate = parsed
                    }
                }
                description = user.description
                achievements = user.achievements
                workLink = user.worklink
                publicId = user.publicId

            } else {
                Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(255, 247, 212, 255))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier=Modifier.height(32.dp))

        // First Name
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Last Name
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Phone
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Occupation
        OutlinedTextField(
            value = occupation,
            onValueChange = { occupation = it },
            label = { Text("Occupation") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Skills (comma-separated)
        OutlinedTextField(
            value = skillsText,
            onValueChange = { skillsText = it },
            label = { Text("Skills (comma-separated)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Location
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Experience
        OutlinedTextField(
            value = experience,
            onValueChange = { experience = it },
            label = { Text("Experience (e.g., 2 years)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Date of Birth (clickable opens calendar)
        OutlinedTextField(
            value = dob,
            onValueChange = { /* no-op, read-only */ },
            label = { Text("Date of Birth") },
            placeholder = { Text("Select date") },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { calendarState.show() }
        )
        Spacer(modifier = Modifier.height(12.dp))

        // CalendarDialog for DOB
        CalendarDialog(
            state = calendarState,
            selection = CalendarSelection.Date { dateHere ->
                selectedDate = dateHere
                dob = dateFormatter.format(dateHere)
            }
        )

        // Description (multi-line)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            placeholder = { Text("Tell us about yourself") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Achievements
        OutlinedTextField(
            value = achievements,
            onValueChange = { achievements = it },
            label = { Text("Achievements") },
            placeholder = { Text("Your achievements") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Work Link
        OutlinedTextField(
            value = workLink,
            onValueChange = { workLink = it },
            label = { Text("Work Link (e.g., LinkedIn URL)") },
            placeholder = { Text("Enter your profile URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding( 8.dp)
        ) {
            Text(
                text = if (publicId) "Public ID" else "Private ID",
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = publicId,
                onCheckedChange = { publicId = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF4C8B82),
                    uncheckedThumbColor = Color.Gray
                )
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Save button
        Button(
            onClick = {
                // Validate mandatory fields if desired
                if (loadedUser == null) {
                    Toast.makeText(context, "User data not loaded yet", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val uid = loadedUser!!.uid
                // Convert skillsText into List<String>
                val skillsList = skillsText
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                // Build updated User object; keep fields not edited if desired
                val updatedUser = loadedUser!!.copy(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    phone = phone.trim(),
                    occupation = occupation.trim(),
                    skills = skillsList,
                    location = location.trim(),
                    experience = experience.trim(),
                    dob = dob.trim(),
                    description = description.trim(),
                    achievements = achievements.trim(),
                    worklink = workLink.trim(),
                    publicId = publicId                )
                // Save to Firestore
                scope.launch {
                    try {
                        firestoreHelper.storeUserData(updatedUser, context)
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                        // Optionally navigate back:
                        navController?.popBackStack()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(50.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = Color(76, 61, 61, 255)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Save", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
