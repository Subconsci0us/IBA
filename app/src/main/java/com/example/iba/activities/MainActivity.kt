package com.example.iba.activities


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.iba.R
import com.example.iba.databinding.ActivityMainBinding
import com.example.iba.databinding.NavHeaderMainBinding
import com.example.iba.firebase.BankingRepository
import com.example.iba.firebase.FirestoreClass
import com.example.iba.models.User
import com.example.iba.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


// Implement the NavigationView.OnNavigationItemSelectedListener and add the implement members of it.
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        //A unique code for starting the activity for result
        const val MY_PROFILE_REQUEST_CODE: Int = 11
    }


    //for banking
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser
    val userId = currentUser?.uid


    private lateinit var binding: ActivityMainBinding // Use View Binding for the activity's layout
    private lateinit var headerBinding: NavHeaderMainBinding // Use View Binding for the header

    // Initialize mdrawer_layout in onCreate
    private lateinit var mdrawerLayout: DrawerLayout

    // for testing banking transaction
    val bankingRepository = FirestoreClass()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding for the activity's layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize the binding for the header
        val navView: NavigationView = binding.navView
        headerBinding = NavHeaderMainBinding.bind(navView.getHeaderView(0))

        // Initialize the mdrawer_layout
        mdrawerLayout = binding.drawerLayout

        setupActionBar()
        //

        navView.setNavigationItemSelectedListener(this)
        // Get the current logged in user details.
        FirestoreClass().loadUserData(this@MainActivity)


        //for printing email
        BankingRepository().getField(getCurrentUserID(), "email") { email ->
            if (email != null) {
                val tv_email = findViewById<TextView>(R.id.tv_main_email)
                tv_email.text = "Email = " + email.toString()
            } else {
                println("Failed to retrieve the email.")
            }
        }


        //for printing name
        BankingRepository().getField(getCurrentUserID(), "name") { name ->
            if (name != null) {
                val tv_name = findViewById<TextView>(R.id.tv_main_name)
                tv_name.text = "Name = " +name.toString()
            } else {
                println("Failed to retrieve the name.")
            }
        }



        //for balance
        getBalance()


        //sending money button function
        var sending_money_btn = findViewById<Button>(R.id.Send_Money)
        sending_money_btn.setOnClickListener {
            val intent = Intent(this, SendActivity::class.java)
            startActivity(intent)

        }

    }


    fun getBalance() {
        val banking = BankingRepository() // Create an instance of the Banking class
        val userId = getCurrentUserID()// Replace with the actual user ID
        banking.getBalance(userId) { balance ->
            if (balance != null) {
                val tv_balance = findViewById<TextView>(R.id.tv_balance)
                tv_balance.text = "Balance = "+balance.toString()
            } else {
                println("Failed to retrieve the balance.")
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()

        if (mdrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mdrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        //  Add the click events of navigation menu items

        when (menuItem.itemId) {
            R.id.nav_my_profile -> {

                //  Launch the my profile activity for Result

                startActivityForResult(
                    Intent(this@MainActivity, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
            }

            R.id.nav_sign_out -> {
                // Here sign outs the user from firebase in this device.
                FirebaseAuth.getInstance().signOut()

                // Send the user to the intro screen of the application.
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        mdrawerLayout.closeDrawer(GravityCompat.START)

        return true
    }


    private fun setupActionBar() {
        val mtoolbar_main_activity =
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_main_activity)

        setSupportActionBar(mtoolbar_main_activity)
        mtoolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        //  Add click event for navigation in the action bar and call the toggleDrawer function.)

        mtoolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }

    }

    private fun toggleDrawer() {
        if (mdrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mdrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            mdrawerLayout.openDrawer(GravityCompat.START)
        }
    }

    fun updateNavigationUserDetails(user: User) {
        val navigation_view = findViewById<NavigationView>(R.id.nav_view)
        // The instance of the header view of the navigation view.
        val headerView = navigation_view.getHeaderView(0)

        // The instance of the user image of the navigation view.
        val navUserImage = headerView.findViewById<ImageView>(R.id.iv_user_image)

        // Load the user image in the ImageView.
        Glide
            .with(this@MainActivity)
            .load(user.image) // URL of the image
            .centerCrop() // Scale type of the image.
            .placeholder(R.drawable.ic_user_place_holder) // A default place holder
            .into(navUserImage) // the view in which the image will be loaded.

        // The instance of the user name TextView of the navigation view.
        val navUsername = headerView.findViewById<TextView>(R.id.tv_username)
        // Set the user name
        navUsername.text = user.name
    }
    // Add the onActivityResult function and check the result of the activity for which we expect the result.)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE
        ) {
            // Get the user updated details.
            FirestoreClass().loadUserData(this@MainActivity)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

}

