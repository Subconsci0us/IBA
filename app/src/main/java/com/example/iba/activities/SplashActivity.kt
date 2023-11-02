package com.example.iba.activities
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.iba.R
import com.example.iba.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_splash)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )



        // Adding the handler to after the a task after some delay.
        Handler().postDelayed({

            // TODO (Step 2: Check if the current user id is not blank then send the user to MainActivity as he have logged in
            //  before or else send him to Intro Screen as earlier.)
            // START
            // Here if the user is signed in once and not signed out again from the app. So next time while coming into the app
            // we will redirect him to MainScreen or else to the Intro Screen as it was before.

            // Get the current user id
            val currentUserID = FirestoreClass().getCurrentUserID()
            // Start the Intro Activity

            if (currentUserID.isNotEmpty()) {
                // Start the Main Activity
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                // Start the Intro Activity
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            }
            finish() // Call this when your activity is done and should be closed.
            // END
        }, 1500) // Here we pass the delay time in milliSeconds after which the splash activity will disappear.
    }
}