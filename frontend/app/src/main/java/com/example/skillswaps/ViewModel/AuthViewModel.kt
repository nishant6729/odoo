package com.example.skillswaps.ViewModel

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel

import androidx.navigation.NavController

import com.example.skillswaps.backend.FirestoreHelper
import com.example.skillswaps.dataclasses.User

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AuthViewModel : ViewModel() {
    // State for authentication status
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    var firestoreHelper= FirestoreHelper()







    // Firebase Email Sign-up
    fun signUpWithEmail(email: String, password: String, context: Context, navController: NavController, fname: String, lname:String, phone:String) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Email or password cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        _authState.value = AuthState.Loading

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success("Sign-up successful!")

                    Toast.makeText(context, "Sign-up successful!", Toast.LENGTH_SHORT).show()
                    val firebaseAuth= FirebaseAuth.getInstance()
                    var uid=firebaseAuth.currentUser?:"-1"
                    var user=User(firstName = fname, lastName = lname, phone = phone, email = email, uid = uid.toString())
                    var firestorehelper= FirestoreHelper()
                    firestorehelper.storeUserData(user,context)
                    navController.navigate("pdetails1"){
                        {
                            popUpTo("signup") { inclusive = true }
                        }
                    } // Navigate to main page
                } else {
                    val exception = task.exception
                    _authState.value = AuthState.Error(exception?.localizedMessage ?: "Unknown error occurred")
                    Toast.makeText(context, "Sign-up failed: ${exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Firebase Email Sign-in
    fun signInWithEmail(email: String, password: String, activity: Activity, onResult: (Boolean) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "signInWithEmail: success")
                    onResult(true) // Sign-in success
                } else {
                    Log.w("Auth", "signInWithEmail: failure", task.exception)
                    onResult(false) // Sign-in failure
                }
            }
    }

    // Authentication states
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val message: String) : AuthState()
        data class Error(val error: String) : AuthState()
    }

}