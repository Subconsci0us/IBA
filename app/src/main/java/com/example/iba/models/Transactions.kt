package com.example.iba.models


import android.os.Parcel
import android.os.Parcelable
import java.util.Date

// for Transfer val transaction = Transaction(transactionType = TransactionType.TRANSFER)

data class Transaction(
    val transactionId: String = "",
    val transactionDate: Date = Date(),
    val transactionType: TransactionType = TransactionType.DEPOSIT,
    val amount: Double = 0.0,
    val senderUuid: String = "",
    val receiverUuid: String = ""
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readSerializable() as Date,
        TransactionType.valueOf(source.readString()!!),
        source.readDouble(),
        source.readString()!!,
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(transactionId)
        writeSerializable(transactionDate)
        writeString(transactionType.name)
        writeDouble(amount)
        writeString(senderUuid)
        writeString(receiverUuid)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Transaction> = object : Parcelable.Creator<Transaction> {
            override fun createFromParcel(source: Parcel): Transaction = Transaction(source)
            override fun newArray(size: Int): Array<Transaction?> = arrayOfNulls(size)
        }
    }
}
enum class TransactionType {
    DEPOSIT,
    TRANSFER
}





