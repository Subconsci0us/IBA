package com.example.iba.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.example.iba.R
import com.example.iba.databinding.ActivitySignUpBinding
import com.example.iba.firebase.FirestoreClass
import com.example.iba.models.Transaction
import com.example.iba.models.TransactionType

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.iba.models.User
import java.util.Date

class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding // Binding object

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        // Set the root view of the activity to the root of the inflated binding layout
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarSignUpActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding.toolbarSignUpActivity.setNavigationOnClickListener { onBackPressed() }
    }

    // A function to register a new user to the app.

    /**
     * A function to register a user to our app using the Firebase.
     */
    private fun registerUser() {
        val mname = findViewById<EditText>(R.id.et_name)
        val memail = findViewById<EditText>(R.id.et_email)
        val mpassword = findViewById<EditText>(R.id.et_password)


        val name: String = mname.text.toString().trim { it <= ' ' }
        val email: String = memail.text.toString().trim { it <= ' ' }
        val password: String = mpassword.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        // If the registration is successfully done
                        if (task.isSuccessful) {

                            // Firebase registered user
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            // Registered Email
                            val registeredEmail = firebaseUser.email!!

                            val user = User(
                                firebaseUser.uid, name, registeredEmail
                            )
                            val transaction = Transaction(
                                transactionId = firebaseUser.uid,
                                transactionDate = Date().toString(),  // You can customize the date as needed
                                transactionType = TransactionType.TRANSFER,  // Set the transaction type as needed
                                amount = 0.0,  // Set the amount as needed
                                senderUuid = "sender-uuid",  // Set the sender UUID as needed
                                receiverUuid = "receiver-uuid"  // Set the receiver UUID as needed
                            )
                            // call the registerUser function of FirestoreClass to make an entry in the database.
                            FirestoreClass().registerUser(this@SignUpActivity, user,transaction)
                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
                                task.exception!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
        }
    }




    //A function to validate the entries of a new user
    /**
     * A function to validate the entries of a new user.
     */
    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter name.")
                false
            }

            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.")
                false
            }

            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter password.")
                false
            }

            else -> {
                true
            }
        }
    }


    fun userRegisteredSuccess() {

        Toast.makeText(
            this@SignUpActivity,
            "You have successfully registered.",
            Toast.LENGTH_SHORT
        ).show()
        // Hide the progress dialog
        hideProgressDialog()

        /**
         * Here the new user registered is automatically signed-in so we just sign-out the user from firebase
         * and send him to Intro Screen for Sign-In
         */
        FirebaseAuth.getInstance().signOut()
        // Finish the Sign-Up Screen
        finish()
    }


}
