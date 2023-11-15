package com.example.iba.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.iba.R
import com.example.iba.firebase.FirestoreClass
import com.example.iba.models.User
import com.example.iba.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    companion object {
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }


    private var mSelectedImageFileUri: Uri? = null

    // Add the global variables for UserDetails and Profile Image URL

    // A global variable for user details.
    private lateinit var mUserDetails: User

    // A global variable for a user profile image URL
    private var mProfileImageURL: String = ""

    private lateinit var miv_user_image: ImageView

    @SuppressLint("MissingInflatedId")
    override  fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()
        miv_user_image = findViewById(R.id.iv_profile_user_image)

        FirestoreClass().loadUserData(this)

        miv_user_image.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showImageChooser()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        val btnupdate = findViewById<Button>(R.id.btn_update)
        btnupdate.setOnClickListener {

            // Here if the image is not selected then update the other details of user.
            if (mSelectedImageFileUri != null) {

                uploadUserImage()
            } else {

                showProgressDialog(resources.getString(R.string.please_wait))

                // Call a function to update user details in the database.
                updateUserProfileData()
            }
        }

        val btn_sign_out = findViewById<Button>(R.id.btn_sign_out_myprofile_activity)

        btn_sign_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            // Send the user to the intro screen of the application.
            val intent = Intent(this, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

/*
        R.id.nav_sign_out -> {
            // Here sign outs the user from firebase in this device.
            FirebaseAuth.getInstance().signOut()

            // Send the user to the intro screen of the application.
            val intent = Intent(this, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

 */
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                showImageChooser()
            } else {
                Toast.makeText(
                    this,
                    "Permission for storage was denied. You can enable it from settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showImageChooser() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data?.data != null) {
            mSelectedImageFileUri = data.data
            try {
                Glide.with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(miv_user_image)
            } catch (e: IOException) {
                e.printStackTrace()
                // Handle the exception, e.g., show a message to the user.
            }
        }
    }

    private fun setupActionBar() {
        val toolbarMyProfileActivity = findViewById<Toolbar>(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolbarMyProfileActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile)
        }

        toolbarMyProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataInUI(user: User) {
        mUserDetails=user

        val name = findViewById<EditText>(R.id.et_name)
        val email = findViewById<EditText>(R.id.et_email)
        val mobile = findViewById<EditText>(R.id.et_mobile)

        Glide.with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(miv_user_image)

        name.setText(user.name)
        email.setText(user.email)
        if (user.mobile != 0L) {
            mobile.setText(user.mobile.toString())
        }
    }
    private fun uploadUserImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {

            //getting the storage reference
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(
                    mSelectedImageFileUri
                )
            )

            //adding the file to reference
            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success
                    Log.e(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    // Get the downloadable url from the task snapshot
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.e("Downloadable Image URL", uri.toString())

                            // assign the image url to the variable.
                            mProfileImageURL = uri.toString()

                            // Call a function to update user details in the database.
                            updateUserProfileData()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@MyProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }
    }
    /**
     * A function to update the user profile details into the database.
     */
    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()
        val et_name = findViewById<EditText>(R.id.et_name)
        val et_mobile = findViewById<EditText>(R.id.et_mobile)

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (et_name.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = et_name.text.toString()
        }

        // Check if et_mobile is not empty before converting to Long
        val mobileString = et_mobile.text.toString()
        if (mobileString.isNotEmpty() && mobileString != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = mobileString.toLong()
        }

        // Update the data in the database.
        FirestoreClass().updateUserProfileData(this@MyProfileActivity, userHashMap)
    }


    // A function to notify the user profile is updated successfully.
    fun profileUpdateSuccess() {

        hideProgressDialog()

        // Send the success result to the Base Activity.

        setResult(Activity.RESULT_OK)

        finish()
    }

    private fun getFileExtension(uri: Uri?): String? {
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         *
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         *
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         *
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }
    // END
}
