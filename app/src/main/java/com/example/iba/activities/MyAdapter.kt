package com.example.iba.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iba.R
import com.example.iba.models.Transaction

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val TransactionType: TextView = itemView.findViewById(R.id.tv_transactionType)
        val Transactionamount: TextView = itemView.findViewById(R.id.tv_transaction_amount)
        val TransactionDate: TextView = itemView.findViewById(R.id.tv_Date)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_history, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction: Transaction = transactions[position]

        holder.TransactionType.text = "${transaction.transactionType}"
        holder.Transactionamount.text = "${transaction.amount}"
        holder.TransactionDate.text="${transaction.transactionDate}"
    }

    override fun getItemCount(): Int {
        return transactions.size
    }
}
