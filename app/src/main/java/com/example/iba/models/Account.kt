package com.example.iba.models

import android.os.Parcel
import android.os.Parcelable

data class Account(

    var balance: Double = 0.0,
    var transactionHistory: MutableList<Transaction> = mutableListOf()
) : Parcelable {
    constructor(source: Parcel) : this(

        source.readDouble(),
        source.readParcelable(Transaction::class.java.classLoader)!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {

        writeDouble(balance)
        writeParcelable(transactionHistory[0], flags)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Account> = object : Parcelable.Creator<Account> {
            override fun createFromParcel(source: Parcel): Account = Account(source)
            override fun newArray(size: Int): Array<Account?> = arrayOfNulls(size)
        }
    }
}
