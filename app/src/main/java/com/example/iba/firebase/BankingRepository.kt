package com.example.iba.firebase


import android.content.ContentValues.TAG
import android.util.Log

import com.example.iba.models.User
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID


class BankingRepository : FirestoreClass() {
    val db = Firebase.firestore

    private lateinit var mUserDetails: User
    fun getBalance(id: String, callback: (Number?) -> Unit) {
        val docRef = db.collection("users").document(id)
        docRef.addSnapshotListener { documentSnapshot, e ->
            if (e != null) {
                Log.e("Firestore", "Error fetching document", e)
                return@addSnapshotListener
            }

            val data = documentSnapshot?.data
            val accountMap = data?.get("account") as? Map<*, *>
            val balance = accountMap?.get("balance")

            if (balance is Number) {
                callback(balance)
            } else {
                Log.d("Firestore", "Field 'balance' is not a valid number")
                callback(null)
            }
        }
    }

    fun getField(id: String, fieldName: String, callback: (String?) -> Unit) {
        val docRef = db.collection("users").document(id)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val data = documentSnapshot.data
                val fieldValue = data?.get(fieldName)

                if (fieldValue is String) {
                    callback(fieldValue)
                } else {
                    Log.d("Firestore", "Field '$fieldName' is not a valid string")
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching document", e)
                callback(null)
            }
    }

    class InsufficientBalanceException : Exception("Insufficient funds")
    class UserNotFoundException : Exception("User not found")

    private val firestore = FirebaseFirestore.getInstance()
    suspend fun transferTransaction(receiverEmail: String, amount: Double) {
        // Replace this with the actual way you obtain the user's email securely.
        val senderEmail =

        // Initialize Firestore
        val firestore = FirebaseFirestore.getInstance()

        // Use a Kotlin coroutine scope.
        try {
            // Start a Firestore transaction.
            firestore.runTransaction { transaction ->
                // Query the sender's account based on their email.
                val senderQuery = firestore.collection("users").whereEqualTo("email", senderEmail).limit(1)
                val senderQuerySnapshot = transaction.get(senderQuery).documents

                // Ensure that the sender's account exists.
                if (senderQuerySnapshot.isNotEmpty()) {
                    val senderAccountDocument = senderQuerySnapshot[0].reference

                    // Get the receiver's account document.
                    val receiverAccountQuery = firestore.collection("users").whereEqualTo("email", receiverEmail).limit(1)
                    val receiverAccountQuerySnapshot = transaction.get(receiverAccountQuery).documents

                    // Ensure that the receiver's account exists.
                    if (receiverAccountQuerySnapshot.isNotEmpty()) {
                        val receiverAccountDocument = receiverAccountQuerySnapshot[0].reference

                        // Get the sender's account balance.
                        val senderAccountData = transaction.get(senderAccountDocument)
                        val senderAccountBalance = senderAccountData.getDouble("account.balance") ?: 0.0

                        // Get the receiver's account balance.
                        val receiverAccountData = transaction.get(receiverAccountDocument)
                        val receiverAccountBalance = receiverAccountData.getDouble("account.balance") ?: 0.0

                        // Check if the sender has enough balance.
                        if (senderAccountBalance >= amount) {
                            // Update the sender's account balance.
                            transaction.update(senderAccountDocument, "account.balance", senderAccountBalance - amount)

                            // Update the receiver's account balance.
                            transaction.update(receiverAccountDocument, "account.balance", receiverAccountBalance + amount)

                            // Create a transaction document.
                            val transactionDocument = firestore.collection("transactions").document()

                            // Set the transaction details.
                            val transactionData = hashMapOf(
                                "transactionId" to transactionDocument.id,
                                "transactionDate" to Date(),
                                "transactionType" to "transfer",
                                "senderUuid" to senderEmail,
                                "receiverUuid" to receiverEmail,
                                "amount" to amount
                            )

                            transaction.set(transactionDocument, transactionData)

                            // Update the sender's account transaction history.
                            val senderTransactionHistory = senderAccountData.get("account.transactionHistory") as MutableList<String>
                            senderTransactionHistory.add(transactionDocument.id)
                            transaction.update(senderAccountDocument, "account.transactionHistory", senderTransactionHistory)

                            // Update the receiver's account transaction history.
                            val receiverTransactionHistory = receiverAccountData.get("account.transactionHistory") as MutableList<String>
                            receiverTransactionHistory.add(transactionDocument.id)
                            transaction.update(receiverAccountDocument, "account.transactionHistory", receiverTransactionHistory)
                        } else {
                            // Throw an exception for insufficient balance.
                            throw InsufficientBalanceException()
                        }
                    } else {
                        // Handle the case where the receiver's account does not exist.
                        throw ReceiverAccountNotFoundException()
                    }
                } else {
                    // Handle the case where the sender's account does not exist.
                    throw SenderAccountNotFoundException()
                }
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., InsufficientBalanceException, SenderAccountNotFoundException, ReceiverAccountNotFoundException)
        }
    }
}
