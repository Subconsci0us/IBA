package com.example.iba.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iba.R
import com.example.iba.firebase.FirestoreClass
import com.example.iba.models.Transaction
import com.example.iba.models.TransactionType
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)
        setupActionBar()

        recyclerView = findViewById(R.id.RV_history)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("users").document(FirestoreClass().getCurrentUserID())

        Log.d("Checking", "documentReference ${documentReference.id}")

        documentReference.get().addOnSuccessListener { documentSnapshot ->
            Log.d("Checking", "documentSnapshot ${documentSnapshot}")

            if (documentSnapshot.exists()) {
                val account = documentSnapshot.data?.get("account") as? Map<String, Any>
                val transactionHistory =
                    account?.get("transactionHistory") as? List<Map<String, Any>> ?: emptyList()

                Log.d("Checking", "DocumentSnapshot data: ${documentSnapshot.data}")
                Log.d("Checking", "transactionHistory ${transactionHistory}")

                // Log the size and details of the transactionHistory list
                Log.d("TransactionHistory", "Transaction History Size: ${transactionHistory.size}")

                val transactions = mutableListOf<Transaction>()

// Iterate through each transaction ID and fetch details
                transactionHistory.forEach { transactionMap ->
                    val transactionId = transactionMap["transactionId"].toString()

                    // Fetch the corresponding transaction details from the "transactions" collection
                    fetchTransactionDetails(transactionId) { transactionDetails ->
                        // Create a Transaction object and add it to the list
                        val transaction = transactionDetails?.let {
                            Transaction(
                                receiverEmail = it["receiverEmail"].toString(),
                                transactionDate = it["transactionDate"].toString(),
                                transactionType = TransactionType.valueOf(it["transactionType"].toString()),
                                amount = it["amount"] as? Double ?: 0.0,
                            )
                        }
                        transaction?.let { transactions.add(it) }

                        // Check if the list is complete and update the RecyclerView adapter
                        if (transactions.size == transactionHistory.size) {
                            // Sort transactions in descending order based on transactionDate
                            val sortedTransactions =
                                transactions.sortedByDescending { it.transactionDate }

                            // Use the safe call operator to check for null
                            val adapter = TransactionAdapter(sortedTransactions)
                            recyclerView.adapter = adapter
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Document snapshot doesn't exist", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchTransactionDetails(
        transactionId: String,
        onTransactionDetailsFetched: (Map<String, Any>?) -> Unit
    ) {
        db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("transactions").document(transactionId)

        documentReference.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val transactionDetails = documentSnapshot.data as Map<String, Any>?
                onTransactionDetailsFetched(transactionDetails)
            } else {
                onTransactionDetailsFetched(null)
            }
        }.addOnFailureListener { e ->
            onTransactionDetailsFetched(null)
        }
    }

    private fun setupActionBar() {
        val toolbarTransactionHistory = findViewById<Toolbar>(R.id.toolbar_transaction_History)
        setSupportActionBar(toolbarTransactionHistory)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "View Transaction History"
        }

        toolbarTransactionHistory.setNavigationOnClickListener { onBackPressed() }
    }
}

