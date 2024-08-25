package com.example.sharefi.ui.login

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sharefi.MainActivity
import com.example.sharefi.R
import com.example.sharefi.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
//    private lateinit var oneTapClient: SignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val googleSignIn = binding.signInButton
//        oneTapClient = Identity.getSignInClient(this)
        val googlesigninbutton = binding.signInButton1
        auth = Firebase.auth


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE


                var email = username.text.toString()
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    email += "@gmail.com"
                }

                val pwdText = password.text.toString()
                auth.signInWithEmailAndPassword(email, pwdText)
                    .addOnCompleteListener(this@LoginActivity) { task ->
                        if (task.isSuccessful) {
                            // User already registered, sign in successful
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
//                            updateUI(user)
                        } else {
                            // If sign in fails, the user might not be registered, so register them
                            Log.w(TAG, "signInWithEmail:failure", task.exception)

                            if (task.exception is FirebaseAuthInvalidUserException || task.exception is FirebaseAuthInvalidCredentialsException) {
                                // User doesn't exist, proceed with registration
                                auth.createUserWithEmailAndPassword(email, pwdText)
                                    .addOnCompleteListener(this@LoginActivity) { registerTask ->
                                        if (registerTask.isSuccessful) {
                                            // Registration successful
                                            Log.d(TAG, "createUserWithEmail:success")
                                            val user = auth.currentUser
//                                            updateUI(user)
                                        } else {
                                            // Registration failed, display a message to the user
                                            Log.w(TAG, "createUserWithEmail:failure", registerTask.exception)
                                            Toast.makeText(
                                                this@LoginActivity,
                                                "Authentication failed.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            updateUI(null)
                                        }
                                    }
                            } else {
                                // Sign in failed for another reason (e.g., wrong password)
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Authentication failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                updateUI(null)
                            }
                        }
                        loginViewModel.login(username.text.toString(), password.text.toString())
                        loading.visibility = View.GONE
                    }
            }
        }

        googleSignIn?.setOnClickListener {
            val RC_SIGN_IN = 9001
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        googlesigninbutton?.setOnClickListener {
            val RC_SIGN_IN = 9001
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { firebaseAuthWithGoogle(it) }
//                updateUI(account)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }

    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    updateUI(user)
                } else {
                    showLoginFailed(task.hashCode())
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // User is signed in, update UI accordingly
            Toast.makeText(this, "Welcome, ${user.email}!", Toast.LENGTH_SHORT).show()

            // You can navigate to another activity, display user info, etc.
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // User is signed out, or registration failed, update UI accordingly
            Toast.makeText(this, "Please sign in or register.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        var currentUser = auth.getCurrentUser()
        updateUI(currentUser);
    }

}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}