package com.example.iba.firebase


import android.util.Log
import com.example.iba.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore

val db = Firebase.firestore
class BankingRepository : FirestoreClass() {


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



    fun transferTransaction(receiverEmail: String, amount: Double) {


        db.runTransaction { transaction ->
            val senderDocRef = db.collection("users").document(getCurrentUserID())

            val senderDocument = transaction.get(senderDocRef)

            val receiverQuery = db.collection("users").whereEqualTo("email", receiverEmail).limit(1)

            receiverQuery.get().addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result

                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]

                        val itemData = document.data
                        val receiverAccountBalance = itemData?.get("balance") as? Double

                        if (receiverAccountBalance != null && receiverAccountBalance > amount) {
                            // Update the sender's account balance
                            var double = senderDocument.getDouble("balance")

                            transaction.update(senderDocRef, "balance",
                            )

                            println(senderDocument.data)

                            // Update the receiver's account balance
                            transaction.update(document.reference, "balance", receiverAccountBalance + amount)
                        } else {
                            println("shitt")
                            // Handle the case of insufficient balance
                        }
                    }
                } else {
                    println("bruhmomet")
                    // Handle the case where the receiver's account was not found
                }
            })
        }
    }


}
/*
     fun transferTransaction(receiverEmail: String, amount: Double) {
        // Replace this with the actual way you obtain the user's email securely.
        val senderEmail = "current_user_email@example.com"



        withContext(Dispatchers.IO) {
            // Start a Firestore transaction.
            firestore.runTransaction { transaction ->
                // Query the sender's account based on their email.
                val senderQuery =
                    firestore.collection("users").whereEqualTo("email", senderEmail).limit(1)
                val senderQuerySnapshot = senderQuery.get().await()


                val senderAccountDocument = senderQuerySnapshot.documents[0].reference

                // Query the receiver's account based on their email.
                val receiverQuery =
                    firestore.collection("users").whereEqualTo("email", receiverEmail).limit(1)
                val receiverQuerySnapshot = receiverQuery.get().await()


                val receiverAccountDocument = receiverQuerySnapshot.documents[0].reference

                // Get the sender's account balance.
                val senderAccountBalance =
                    senderQuerySnapshot.documents[0].getDouble("account.balance") ?: 0.0

                // Get the receiver's account balance.
                val receiverAccountBalance =
                    receiverQuerySnapshot.documents[0].getDouble("account.balance") ?: 0.0

                // Check if the sender has enough balance.
                if (senderAccountBalance >= amount) {
                    // Update the sender's account balance.
                    transaction.update(
                        senderAccountDocument,
                        "account.balance",
                        senderAccountBalance - amount
                    )

                    // Update the receiver's account balance.
                    transaction.update(
                        receiverAccountDocument,
                        "account.balance",
                        receiverAccountBalance + amount
                    )

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
                    val senderTransactionHistory =
                        senderQuerySnapshot.documents[0].get("account.transactionHistory") as MutableList<String>
                    senderTransactionHistory.add(transactionDocument.id)
                    transaction.update(
                        senderAccountDocument,
                        "account.transactionHistory",
                        senderTransactionHistory
                    )

                    // Update the receiver's account transaction history.
                    val receiverTransactionHistory =
                        receiverQuerySnapshot.documents[0].get("account.transactionHistory") as MutableList<String>
                    receiverTransactionHistory.add(transactionDocument.id)
                    transaction.update(
                        receiverAccountDocument,
                        "account.transactionHistory",
                        receiverTransactionHistory
                    )
                } else {
                    // Throw an exception for insufficient balance.
                    throw InsufficientBalanceException()
                }
            }

            // Handle exceptions (e.g., InsufficientBalanceException, SenderAccountNotFoundException, ReceiverAccountNotFoundException)

        }
    }

 */






