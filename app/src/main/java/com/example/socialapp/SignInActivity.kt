package com.example.socialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.example.socialapp.daos.UserDao
import com.example.socialapp.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {

    private val RC_SIGN_IN: Int = 123
    private lateinit var googleSignInClient: GoogleSignInClient
    private val TAG = "SignInActivity Tag"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        //Configure google sign in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        auth = Firebase.auth

        val signInButton : SignInButton = findViewById(R.id.signInButton)

        signInButton.setOnClickListener {
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        Toast.makeText(applicationContext, "$currentUser", Toast.LENGTH_SHORT).show()
        updateUI(currentUser)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if(requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthwithGoogle(account.idToken!!)
        } catch (e:ApiException) {
            Log.w(TAG, "signInResult:failed code = " + e.statusCode)
        }
    }

    private fun firebaseAuthwithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        findViewById<SignInButton>(R.id.signInButton).visibility = View.GONE
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE

        GlobalScope.launch(Dispatchers.IO) {
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user

            withContext(Dispatchers.Main) {
                updateUI(firebaseUser)
                Toast.makeText(applicationContext, "$firebaseUser", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if(firebaseUser != null) {

            val user = User(firebaseUser.uid,firebaseUser.displayName,"$firebaseUser.photoUrl")
            val usersDao = UserDao()
            usersDao.addUser(user)

            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        } else {
            findViewById<SignInButton>(R.id.signInButton).visibility = View.VISIBLE
            findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
        }
    }
}

