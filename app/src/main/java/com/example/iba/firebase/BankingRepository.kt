package com.example.iba.firebase


import android.util.Log
import com.example.iba.models.Transaction
import com.example.iba.models.TransactionType
import com.example.iba.models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class BankingRepository : FirestoreClass() {


    private lateinit var mUserDetails: User
    fun getBalance(id: String, callback: (Number?) -> Unit) {
        val db = Firebase.firestore

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
        val db = Firebase.firestore

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
        val db = FirebaseFirestore.getInstance()
        val senderDocRef = db.collection("users").document(getCurrentUserID())
        val receiverQuery = db.collection("users").whereEqualTo("email", receiverEmail).limit(1)

        senderDocRef.get().addOnSuccessListener { senderDocumentSnapshot ->
            Log.d("Firestore", "Sender Document Snapshot: $senderDocumentSnapshot")
            if (senderDocumentSnapshot.exists()) {
                val senderAccount = senderDocumentSnapshot.data?.get("account") as? Map<*, *>
                Log.d("Firestore", "Sender Account: $senderAccount")

                val senderBalance = senderAccount?.get("balance") as? Double
                Log.d("Firestore", "senderBalance $senderBalance")

                receiverQuery.get().addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        val receiverDocRef = db.collection("users").document(document.id)
                        val receiverDocument = document.data
                        Log.d("Checking", "receiverDocument $receiverDocument")
                        val receiverAccount = receiverDocument?.get("account") as? Map<*, *>
                        Log.d("Checking", "receiverAccountBalance $receiverAccount")

                        val receiverBalance = receiverAccount?.get("balance") as? Double
                        Log.d("Checking", "receiverBalance $receiverBalance")

                        if (senderBalance != null && receiverBalance != null) {
                            if (senderBalance >= amount && receiverBalance >= amount) {
                                // Create a Transaction object
                                val transaction = Transaction(
                                    transactionType = TransactionType.TRANSFER,
                                    amount = amount,
                                    senderUuid = getCurrentUserID(),
                                    receiverUuid = document.id
                                )

                                // Store the Transaction object in Firestore
                                val transactionsCollection = db.collection("transactions")
                                transactionsCollection.add(transaction)
                                    .addOnSuccessListener { documentReference ->
                                        val transactionId = documentReference.id
                                        Log.d("Firestore", "Transaction added with ID: $transactionId")

                                        // Perform the transaction by updating sender and receiver balances
                                        db.runTransaction { transaction ->
                                            transaction.update(
                                                senderDocRef,
                                                "account.balance",
                                                senderBalance - amount
                                            )

                                            transaction.update(
                                                receiverDocRef,
                                                "account.balance",
                                                receiverBalance + amount
                                            )
                                        }

                                        // Add the transaction to the sender's transactionHistory
                                        val senderTransactionHistory = senderAccount["transactionHistory"] as? MutableList<Map<String, Any>> ?: mutableListOf()
                                        val senderTransactionRecord = mapOf(
                                            "transactionId" to transactionId,
                                            "transactionType" to TransactionType.TRANSFER.toString(),
                                            "amount" to amount
                                        )
                                        senderTransactionHistory.add(senderTransactionRecord)
                                        senderDocRef.update("account.transactionHistory", senderTransactionHistory)

                                        // Add the transaction to the receiver's transactionHistory
                                        val receiverTransactionHistory = receiverAccount["transactionHistory"] as? MutableList<Map<String, Any>> ?: mutableListOf()
                                        val receiverTransactionRecord = mapOf(
                                            "transactionId" to transactionId,
                                            "transactionType" to TransactionType.DEPOSIT.toString(),
                                            "amount" to amount
                                        )
                                        receiverTransactionHistory.add(receiverTransactionRecord)
                                        receiverDocRef.update("account.transactionHistory", receiverTransactionHistory)
                                    }
                            } else {
                                println("Insufficient balance")
                            }
                        }
                    }
                }
            }
        }
    }
}









