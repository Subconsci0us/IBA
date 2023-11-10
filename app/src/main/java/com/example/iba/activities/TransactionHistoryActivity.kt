package com.example.iba.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
                val transactionHistory = account?.get("transactionHistory") as? List<Map<String, Any>> ?: emptyList()

                Log.d("Checking", "DocumentSnapshot data: ${documentSnapshot.data}")
                Log.d("Checking", "transactionHistory ${transactionHistory}")

                // Log the size and details of the transactionHistory list
                Log.d("TransactionHistory", "Transaction History Size: ${transactionHistory.size}")
           /*
                transactionHistory.forEachIndexed { index, transaction ->
                    Log.d("TransactionHistory321", "Transaction $index: $transaction")
                }

            */

                val transactions = transactionHistory.map { transactionMap ->
                    val transactionTypeString = transactionMap["transactionType"].toString()
                    val amount = (transactionMap["amount"] as? Double) ?: 0.0
                    val transactionDate = (transactionMap["transactionDate"] as? Date) ?: Date()

                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    sdf.timeZone = TimeZone.getTimeZone("Asia/Karachi") // Set the time zone to Pakistan Standard Time (PST)

                    Transaction(
                        transactionType = TransactionType.valueOf(transactionTypeString),
                        amount = amount,
                        transactionDate = sdf.format(transactionDate), // Format the date
                        // Add other properties as needed
                    )
                }

                // Use the safe call operator to check for null
                val adapter = TransactionAdapter(transactions)
                recyclerView.adapter = adapter
            } else {
                Toast.makeText(this, "Document snapshot doesn't exist", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

