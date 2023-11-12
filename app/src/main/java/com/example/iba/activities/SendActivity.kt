package com.example.iba.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iba.R
import com.example.iba.firebase.BankingRepository
import com.example.iba.models.Transaction

class SendActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var latestTransactionsAdapter: TransactionAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        setupActionBar()

        val recipientEmailEditText = findViewById<EditText>(R.id.et_email_send_money)
        val sendMoneyButton = findViewById<Button>(R.id.btn_send_money)
      //  val enterAmount = findViewById<EditText>(R.id.et_amount_send_money)

        recyclerView = findViewById(R.id.RV_activity_send)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        // Initialize and set up the RecyclerView for displaying the latest transactions
        latestTransactionsAdapter = TransactionAdapter(emptyList()) { transaction ->
            // Handle the click event (e.g., print email and amount)
            Toast.makeText(
                this@SendActivity,
                "Email: ${transaction.receiverEmail}, Amount: ${transaction.amount}",
                Toast.LENGTH_SHORT
            ).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = latestTransactionsAdapter

        updateLatestTransactions()

        sendMoneyButton.setOnClickListener {
            val recipientEmail = recipientEmailEditText.text.toString()
            val intent = Intent(this,PaymentConfirmationActivity::class.java)
            intent.putExtra("recipientEmail",recipientEmail)
            startActivity(intent);
            /*
            val amountText = enterAmount.text.toString()

            if (amountText.isNotEmpty()) {
                val amount = amountText.toDouble()

                // Call the transferTransaction() function directly.
                BankingRepository().transferTransaction(recipientEmail, amount)
                Toast.makeText(this, "Money sent successfully!", Toast.LENGTH_SHORT).show()


             */
              //  updateLatestTransactions()
           // }
            }
        }



    private fun updateLatestTransactions() {
        BankingRepository().getLatestSenderTransactions { latestTransactions ->
            if (latestTransactions != null) {
                // Update the RecyclerView with the latest transactions
                latestTransactionsAdapter.updateData(latestTransactions)
            }
        }
    }

    private fun setupActionBar() {
        val toolbarSendActivity = findViewById<Toolbar>(R.id.toolbar_send_activity)
        setSupportActionBar(toolbarSendActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "Send Money"
        }

        toolbarSendActivity.setNavigationOnClickListener { onBackPressed() }
    }
}

