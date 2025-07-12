package com.example.skillswaps.Auth

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient

import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth

import com.google.firebase.ktx.Firebase

@Composable
fun rememberGoogleSignInLauncher(
    activity: Activity,
    navController: NavController,
    client: GoogleSignInClient
): () -> Unit {
    val firebaseAuth = Firebase.auth
    val context = LocalContext.current
    val user=User(firstName = "", lastName = "", phone = "")
    val firestoreHelper= FirestoreHelper()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(Exception::class.java)
            val email = account?.email ?: return@rememberLauncherForActivityResult
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            user.email=email
            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener {
                    firestoreHelper.storeUserData(user,context)

                    // pass the email as part of the route:
                    navController.navigate("pdetails1") {
                        popUpTo("signin") { inclusive = true }
                    }
                }
                .addOnFailureListener { ex ->
                    Toast.makeText(context, "Sign-up failed: ${ex?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }

    return {
        launcher.launch(client.signInIntent)
    }
}
//Steps
//1.Turn ON the Google Auth from authentication section of Firebase
//2.Add dependencies BOM and Google services as added in the dependencies in this app ktx
//3.Make Client and activity variable in the MainActivity
//4.Pass this client and activity as the argument in the SignIn Page
//5.Pass this argument in this bada wala function which perfoms major task.

//Additional point the client_id is in the new google-services.json
// dd that in the client_id of the client made in thw main activiy
