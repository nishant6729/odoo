package com.example.skillswaps.Screens

import androidx.compose.foundation.text.KeyboardOptions

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization

import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.skillswaps.backend.FirestoreHelper

import com.example.skillswaps.dataclasses.CommunityPost

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    postType: String  // "exchange", "request", or "discussion"
) {
    val firestoreHelper = remember { FirestoreHelper() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Form states
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }
    val categories = listOf("Programming", "Design", "Music", "Public Speaking", "Career", "Other")

    var title by remember { mutableStateOf("") }
    var offer by remember { mutableStateOf("") }  // for exchange
    var want by remember { mutableStateOf("") }   // for exchange
    var content by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier=Modifier.height(32.dp))
        Text(
            text = when (postType) {
                "exchange" -> "New Skill Exchange"
                "request" -> "New Request/Offer"
                else -> "New Discussion"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Category dropdown
        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory ?: "",
                onValueChange = { /* readOnly */ },
                readOnly = true,
                label = { Text("Category Filter") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor() // if using newer Compose versions
            )
            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            selectedCategory = if (cat == "All") null else cat
                            expandedCategory = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization =KeyboardCapitalization.Sentences)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Exchange-specific fields
        if (postType == "exchange") {
            OutlinedTextField(
                value = offer,
                onValueChange = { offer = it },
                label = { Text("Offer (e.g. I can teach Java)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization =KeyboardCapitalization.Sentences)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = want,
                onValueChange = { want = it },
                label = { Text("Want (e.g. Looking to learn UI/UX)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization =KeyboardCapitalization.Sentences)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Content/description
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Tags
        OutlinedTextField(
            value = tagsText,
            onValueChange = { tagsText = it },
            label = { Text("Tags (comma-separated)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Location
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location )") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization =KeyboardCapitalization.Sentences)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Submit button
        Button(
            onClick = {
                // Validate inputs
                if (selectedCategory.isNullOrBlank()) {
                    Toast.makeText(context, "Select a category", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (title.isBlank()) {
                    Toast.makeText(context, "Enter a title", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                // Build CommunityPost instance
                val tagsList = tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                val post = CommunityPost(
                    id = "", // FirestoreHelper should assign the ID
                    authorId = "",       // FirestoreHelper.createCommunityPost should fill current userId
                    authorName = "",     // and authorName/imageUrl as needed
                    authorImageUrl = "",
                    type = postType,
                    category = selectedCategory!!,
                    title = title.trim(),
                    offer = if (postType == "exchange" && offer.isNotBlank()) offer.trim() else null,
                    want = if (postType == "exchange" && want.isNotBlank()) want.trim() else null,
                    content = content.trim(),
                    tags = tagsList,
                    location = location.trim(),

                    )
                // Create post in Firestore
                scope.launch {
                    val newId = firestoreHelper.createCommunityPost(post)
                    if (newId != null) {
                        Toast.makeText(context, "Post created", Toast.LENGTH_SHORT).show()
                        navController.navigate("imageupload/$newId")
                    } else {
                        Toast.makeText(context, "Failed to create post", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Submit")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
