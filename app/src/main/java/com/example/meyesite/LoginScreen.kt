package com.example.meyesite

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_login_screen.*


class LoginScreen : AppCompatActivity() {

    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code:Int=123
    private var firebaseAuth= FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        FirebaseApp.initializeApp(this)

        // Configure Google Sign In inside onCreate mentod
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_google_web_client_id))
            .requestEmail()
            .build()
        // getting the value of gso inside the GoogleSigninClient
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)
        // initialize the firebaseAuth variable

        firebaseAuth = FirebaseAuth.getInstance()

        Signin.setOnClickListener{ view: View? ->
            Toast.makeText(this,"Logging In", Toast.LENGTH_SHORT).show()
            signInGoogle()
        }

    }
    private  fun signInGoogle(){

        val signInIntent: Intent =mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent,Req_Code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==Req_Code){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
//            firebaseAuthWithGoogle(account!!)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>){
        try {
            val account: GoogleSignInAccount? =completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e: ApiException){
            Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun UpdateUI(account: GoogleSignInAccount){
        val credential= GoogleAuthProvider.getCredential(account.idToken,null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {task->
            if(task.isSuccessful) {
                SavedPreference.setEmail(this,account.email.toString())
                SavedPreference.setUsername(this,account.displayName.toString())
                updateFirebaseUserDisplayName()
            }
        }
    }

    private fun updateFirebaseUserDisplayName() {

        FirebaseAuth.getInstance().currentUser?.apply {
            val profileUpdates : UserProfileChangeRequest = UserProfileChangeRequest.Builder().setDisplayName(
                firebaseAuth.currentUser?.email
            ).build()
            updateProfile(profileUpdates)?.addOnCompleteListener(OnCompleteListener {
                when(it.isSuccessful) {
                    true -> apply {
                        Intent(this@LoginScreen, MainActivity::class.java).apply {
                            startActivity(this)
                            finish()
                        }
                    }
                    false -> Toast.makeText(this@LoginScreen, "Login has failed", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        if(GoogleSignIn.getLastSignedInAccount(this)!=null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }



}