package com.example.iba.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.iba.R
import com.example.iba.firebase.BankingRepository
import com.example.iba.firebase.FirestoreClass

class PaymentConfirmationActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_confirmation)
        setupActionBar()

        var recipient: String? = intent.getStringExtra("recipientEmail")
        var recipientEmail = findViewById<TextView>(R.id.tv_toEmail_activity_Confirmation)
        val enterAmount = findViewById<EditText>(R.id.editTextNumber_payment_confirm)
        val sendMoneyButton = findViewById<Button>(R.id.btn_send_money_payment_confirm)


        //for printing email
        BankingRepository().getField(FirestoreClass().getCurrentUserID(), "email") { email ->
            if (email != null) {
                val tv_email = findViewById<TextView>(R.id.tv_fromEmail_activity_Confirmation)
                tv_email.text = email.toString()
            } else {
                println("Failed to retrieve the email.")
            }
        }
        recipientEmail.setText(recipient)

        sendMoneyButton.setOnClickListener {
            val amountText = enterAmount.text.toString()

            if (amountText.isNotEmpty()) {
                val amount = amountText.toDouble()

                // Call the transferTransaction() function directly.
                if (recipient != null) {
                    BankingRepository().transferTransaction(recipient, amount)
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)

                }
                Toast.makeText(this, "Money sent successfully!", Toast.LENGTH_SHORT).show()


            }


        }
    }

    private fun setupActionBar() {
        val toolbarSendActivity = findViewById<Toolbar>(R.id.toolbar_payment_confirmation)
        setSupportActionBar(toolbarSendActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "Confirm Payment"
        }

        toolbarSendActivity.setNavigationOnClickListener { onBackPressed() }
    }
}