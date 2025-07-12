package com.example.skillswaps

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.skillswaps.Screens.CommunityHomeScreen

import com.example.skillswaps.backend.FirestoreHelper

import com.example.skillswaps.FirstLaunchManager
import com.example.skillswaps.R
import com.example.skillswaps.DelayClass
import com.example.skillswaps.Screens.CardDetailsPage
import com.example.skillswaps.Screens.ChatPage
import com.example.skillswaps.Screens.CreatePostScreen
import com.example.skillswaps.Screens.FriendRequestsPage
import com.example.skillswaps.Screens.ImageUploadScreen
import com.example.skillswaps.Screens.LikedUsersScreen
import com.example.skillswaps.Screens.OnBoardingScreen
import com.example.skillswaps.Screens.PersonalDetails
import com.example.skillswaps.Screens.PersonalDetailsScreen
import com.example.skillswaps.Screens.PostDetailScreen
import com.example.skillswaps.Screens.RequestPage
import com.example.skillswaps.Screens.SPersonalDetails
import com.example.skillswaps.Screens.SignInPage
import com.example.skillswaps.Screens.SignUpPage
import com.example.skillswaps.Screens.SwapDone
import com.example.skillswaps.Screens.UploadPictureScreen
import com.example.skillswaps.Screens.UserListScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    //Step 2 in Google Auth
    private val googleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso)
    }
    //Step 2 Ends






    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        val viewModel by viewModels<DelayClass>()
        super.onCreate(savedInstanceState)
        val firstLaunchManager = FirstLaunchManager(this)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isResult.value
            }
        }

        //For Google Auth Make this variable Step 3
        val isUserAuthenticated = FirebaseAuth.getInstance().currentUser != null
        val activityRef: Activity = this
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            // Collect the first‑launch flag as state
            val isFirstLaunch by firstLaunchManager.isFirstLaunch.collectAsState(initial = true)
            val startDestination = if (isUserAuthenticated) "home" else "signin"
            val context= LocalContext.current

            NavHost(
                navController = navController,
                startDestination = if (isFirstLaunch) "onboarding" else startDestination
            ) {
                composable("onboarding") {
                    OnBoardingScreen(
                        onContinue = {
                            // mark as seen, then navigate to home
                            lifecycleScope.launch {
                                firstLaunchManager.setLaunched()
                                navController.navigate("signin") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        }
                    )
                }
                composable("signup") {
                    SignUpPage(navController)  // your real app
                }
                composable("signin") {
                    SignInPage(navController,googleSignInClient,activityRef)  // your real app
                }
                composable("mainpage") {

                    MainPage(navController)  // your real app
                }
                composable("pdetails1") {
                    PersonalDetails(navController)  // your real app
                }
                composable("pdetails2") {
                    SPersonalDetails(navController)  // your real app
                }
                composable("home") {
                    UserListScreen(navController,4,{
                        navController.navigate("mainpage")
                    })
                }
                composable("UploadPhoto") {
                    UploadPictureScreen(navController)
                }
                composable("PersonalDetails") {
                    PersonalDetailsScreen(navController)
                }
                composable("Community") {

                }
                composable("Premium") {

                }
                composable("TimeTable") {

                }
                composable("History") {

                }
                composable("Portfolio") {

                }
                composable("PersonalCoaching") {

                }
                composable("likedUser") {
                    LikedUsersScreen(navController = navController){
                        navController.navigate("mainpage")
                    }
                }
                composable("communityHome") {
                    CommunityHomeScreen(navController)
                }
                composable("createPost/{type}") { backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type") ?: "exchange"
                    CreatePostScreen(navController, postType = type)
                }
                // If you want a separate feed screen route:
                // composable("feed/{type}") { backStackEntry -> ... }
                composable("postDetail/{postId}") { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId") ?: ""
                    PostDetailScreen(postId = postId, navController = navController)
                }
                composable("imageupload/{postId}") {
                    val postId = it.arguments?.getString("postId") ?: ""
                    ImageUploadScreen(navController,postId)  // your real app
                }
                composable("detailsPage/{uid}"){
                    val uid=it.arguments?.getString("uid") ?: ""
                    CardDetailsPage(navController,uid)
                }
                composable("requestPage"){
                    RequestPage(navController)
                }
                composable("requestMemberDetails/{uid}"){
                    val uid=it.arguments?.getString("uid") ?: ""
                    CardDetailsPage(navController,uid,false)
                }
                composable("SwapDonePage"){
                    SwapDone(navController)
                }
                composable("ChatPage/{uid}"){
                    val uid=it.arguments?.getString("uid") ?: ""
                    ChatPage(uid,navController)
                }
                composable("FriendRequest"){
                    FriendRequestsPage(navController)
                }
            }
        }
    }
}





@Composable
fun MainPage(navController: NavController) {
    // ... your normal app UI

    val firestoreHelper= FirestoreHelper()
    val context=LocalContext.current
    Column(verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier=Modifier.fillMaxSize()) {
        Button(onClick = {
            firestoreHelper.logoutUser(context,navController)
        }) {
            Text("Logout")
        }
    }
    @Composable
    fun HomePage(navController: NavController) {
        // ... your normal app UI


    }

}
