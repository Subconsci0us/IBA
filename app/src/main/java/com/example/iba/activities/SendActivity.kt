package com.example.iba.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.iba.R
import com.example.iba.firebase.BankingRepository

class SendActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)


        val recipientEmailEditText = findViewById<EditText>(R.id.et_email_send_money)
        val sendMoneyButton = findViewById<Button>(R.id.btn_send_money)
        val enterAmount = findViewById<EditText>(R.id.et_amount_send_money)

        sendMoneyButton.setOnClickListener {
            val recipientEmail = recipientEmailEditText.text.toString()
            val amountText = enterAmount.text.toString()

            if (amountText.isNotEmpty()) {
                val amount = amountText.toDouble()

                // Call the transferTransaction() function directly.
                BankingRepository().transferTransaction(recipientEmail, amount)
                Toast.makeText(this, "Money sent successfully!", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}