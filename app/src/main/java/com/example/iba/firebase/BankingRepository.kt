package com.example.iba.firebase


import android.util.Log
import com.example.iba.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
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
    /*
    fun transferTransaction(receiverEmail: String, amount: Double) {
        val db = FirebaseFirestore.getInstance()
        val senderDocRef = db.collection("users").document(getCurrentUserID())
        val receiverQuery = db.collection("users").whereEqualTo("email", receiverEmail).limit(1)

        senderDocRef.get().addOnSuccessListener { senderDocumentSnapshot ->
            if (senderDocumentSnapshot.exists()) {
                val senderAccount = senderDocumentSnapshot.data?.get("account") as? Map<*, *>
                val senderBalance = senderAccount?.get("balance") as? Double
                Log.d("Checking","senderBalance $senderBalance")

                receiverQuery.get().addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        val receiverDocRef = db.collection("users").document(document.id)
                        val receiverDocument = document.data
                        val receiverAccountBalance = receiverDocument?.get("account") as? Map<*, *>
                        val receiverBalance = receiverAccountBalance?.get("balance") as? Double
                        Log.d("Checking","receiverBalance $receiverBalance")


                        if (senderBalance != null && receiverBalance != null) {
                            if (senderBalance >= amount && receiverBalance >= amount) {
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

                                    // Create a transaction document in the "Transactions" collection
                                    val transactionsCollection = db.collection("Transactions")
                                    val transactionData = hashMapOf(
                                        "transactionDate" to FieldValue.serverTimestamp(),
                                        "transactionType" to "transfer",
                                        "senderId" to senderDocRef.id,
                                        "receiverId" to receiverDocRef.id,
                                        "amount" to amount
                                    )

                                    val newTransactionDoc = transactionsCollection.document()
                                    transaction.set(newTransactionDoc, transactionData)

                                    // Update sender's transaction history
                                    val senderTransactionHistory = senderAccount?.get("transactionHistory") as? ArrayList<HashMap<String, Any>> ?: ArrayList()
                                    val senderTransactionEntry = hashMapOf(
                                        "transactionDate" to FieldValue.serverTimestamp(),
                                        "amount" to amount
                                    )
                                    senderTransactionHistory.add(senderTransactionEntry)
                                    transaction.update(senderDocRef, "account.transactionHistory", senderTransactionHistory)

                                    // Update receiver's transaction history
                                    val receiverTransactionHistory = receiverAccountBalance?.get("transactionHistory") as? ArrayList<HashMap<String, Any>> ?: ArrayList()
                                    val receiverTransactionEntry = hashMapOf(
                                        "transactionDate" to FieldValue.serverTimestamp(),
                                        "amount" to amount
                                    )
                                    receiverTransactionHistory.add(receiverTransactionEntry)
                                    transaction.update(receiverDocRef, "account.transactionHistory", receiverTransactionHistory)
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

     */


    fun transferTransaction(receiverEmail: String, amount: Double) {
        val db = FirebaseFirestore.getInstance()
        val senderDocRef = db.collection("users").document(getCurrentUserID())
        val receiverQuery = db.collection("users").whereEqualTo("email", receiverEmail).limit(1)

        senderDocRef.get().addOnSuccessListener { senderDocumentSnapshot ->
            Log.d("Firestore", "Sender Document Snapshot: $senderDocumentSnapshot")
            if (senderDocumentSnapshot.exists()) {
                val senderAccount = senderDocumentSnapshot.data?.get("account") as? Map<*, *>
                Log.d("Firestore", "Sender Account: $senderAccount")

                val senderBalance = senderAccount?.get("balance") as? Long
                Log.d("Firestore","senderBalance $senderBalance")


                receiverQuery.get().addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        val receiverDocRef = db.collection("users").document(document.id)
                        val receiverDocument = document.data
                        Log.d("Checking","receiverDocument $receiverDocument")
                        val receiverAccountBalance = receiverDocument?.get("account") as? Map<*, *>
                        Log.d("Checking","receiverAccountBalance $receiverAccountBalance")

                        val receiverBalance = receiverAccountBalance?.get("balance") as? Double
                        Log.d("Checking","receiverBalance $receiverBalance")

                        if (senderBalance != null && receiverBalance != null) {
                            if (senderBalance >= amount && receiverBalance >= amount) {
                                db.runTransaction { transaction ->
                                    transaction.update(
                                        senderDocRef,
                                        "account.balance",
                                        senderBalance - amount
                                    )

                                    transaction.update(
                                        receiverDocRef,
                                        "account.balance",
                                        receiverBalance + amount)
                                }
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









