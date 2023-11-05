package com.example.iba.firebase

import android.app.Activity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.iba.R
import com.example.iba.activities.MainActivity
import com.example.iba.activities.MyProfileActivity
import com.example.iba.activities.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.iba.models.User
import com.example.iba.utils.Constants
import com.example.iba.activities.SignInActivity
import com.example.iba.models.Account
import com.example.iba.models.Transaction


import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await


// additional work for transactions

open class FirestoreClass {

    // Create a instance of Firebase Firestore
    val mFireStore = FirebaseFirestore.getInstance()

    //  Create a function to make an entry of the registered user in the firestore database.
    /**
     * A function to make an entry of the registered user in the firestore database.
     */
    fun registerUser(activity: SignUpActivity, userInfo: User, transactioninfo: Transaction) {
        val useraccount = mFireStore.collection(Constants.USERS).document(getCurrentUserID())

        // Set user information
        useraccount.set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                // User information is updated successfully
                val transactionsCollection = mFireStore.collection(Constants.TRANSACTIONS)
                val transactionDocRef = transactionsCollection.document(getCurrentUserID())

                // Set the transaction information
                transactionDocRef.set(transactioninfo, SetOptions.merge())
                    .addOnSuccessListener {
                        // Transaction information is updated successfully
                        activity.userRegisteredSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error writing transaction document",
                            e
                        )
                    }
            }
            .addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing user document",
                    e
                )
            }
    }

    fun loadUserData(activity: Activity) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())

                // Here we have received the document snapshot which is converted into the User Data model object.
                val loggedInUser = document.toObject(User::class.java)!!

                //  Modify the parameter and check the instance of activity and send the success result to it.

                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }

                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser)
                    }

                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)

                    }
                }

            }
            .addOnFailureListener { e ->
                // Hide the progress dialog in failure function based on instance of activity.

                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }

                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }

                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }

    //  Create a function for getting the user id of current logged user.

    /**
     * A function for getting the user id of current logged user.
     */
    fun getCurrentUserID(): String {
        //  Return the user id if he is already logged in before or else it will be blank

        // An Instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID

    }

    /**
     * A function to update the user profile data into the database.
     */
    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS) // Collection Name
            .document(getCurrentUserID()) // Document ID
            .update(userHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                // Profile data is updated successfully.
                Log.e(activity.javaClass.simpleName, "Profile Data updated successfully!")

                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                // Notify the success result.
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }



}