package com.example.skillswaps.Screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHomeScreen(navController: NavController) {
    // Tabs: Exchange, Request, Discussion
    val tabs = listOf("Exchange", "Request", "Discussion")
    var selectedTab by remember { mutableStateOf(0) }

    // Category filter state
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }
    // Include an “All” option if you want to show all categories:
    val categories = listOf("All", "Programming", "Design", "Music", "Public Speaking", "Career", "Other")

    Column(modifier = Modifier.fillMaxSize().background(Color(255, 247, 212))) {
        Row (modifier = Modifier.fillMaxWidth().height(35.dp).background(Color(184, 150, 83, 255))){  }
        // 1) TabRow for post type
        TabRow(selectedTabIndex = selectedTab,
            containerColor = Color(184, 150, 83, 255)) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = (selectedTab == index),
                    onClick = {
                        selectedTab = index
                        // Optionally reset category filter when switching tab:
                        // selectedCategory = null
                    },
                    text = { Text(title, color = Color.White, fontSize = 15.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2) "New Post" button
        val postType = when (selectedTab) {
            0 -> "exchange"
            1 -> "request"
            else -> "discussion"
        }
        Button(
            onClick = {
                navController.navigate("createPost/$postType")
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(76,61,61,255))
        ) {
            Text(text = "New ${tabs[selectedTab]} Post", color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(modifier = Modifier.height(6.dp))


//        ExposedDropdownMenuBox(
//            expanded = expandedCategory,
//            onExpandedChange = { expandedCategory = !expandedCategory },
//            modifier = Modifier
//                .padding(horizontal = 16.dp)
//                .fillMaxWidth()
//        ) {
//            OutlinedTextField(
//                value = selectedCategory ?: "",
//                onValueChange = { /* readOnly */ },
//                readOnly = true,
//                label = { Text("Category Filter") },
//                trailingIcon = {
//                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                 .menuAnchor() // if using newer Compose versions
//            )
//            ExposedDropdownMenu(
//                expanded = expandedCategory,
//                onDismissRequest = { expandedCategory = false }
//            ) {
//                categories.forEach { cat ->
//                    DropdownMenuItem(
//                        text = { Text(cat) },
//                        onClick = {
//                            selectedCategory = if (cat == "All") null else cat
//                            expandedCategory = false
//                        }
//                    )
//                }
//            }
//        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4) Feed for selected type + category filter
        //    We assume your FeedScreen can accept an optional selectedCategory parameter
        //    If your FeedScreen signature is: FeedScreen(navController: NavController, postType: String, selectedCategory: String?)
        //    adapt accordingly. Below is an example call.
        FeedScreen(
            navController = navController,
            postType = postType,



            )
    }
}