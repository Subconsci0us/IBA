package com.example.iba.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.iba.R
import com.example.iba.models.Transaction

class TransactionAdapter(private var transactions:List<Transaction>, private val onItemClick: (Transaction) -> Unit) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Transaction_successful_to: TextView = itemView.findViewById(R.id.tv_email_transactionHistory)
        val TransactionType: TextView = itemView.findViewById(R.id.tv_transactionType)
        val Transactionamount: TextView = itemView.findViewById(R.id.tv_transaction_amount)
        val TransactionDate: TextView = itemView.findViewById(R.id.tv_Date)

        init {
            // Set up click listener for the card view
            itemView.findViewById<CardView>(R.id.list_item_card).setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(transactions[position])
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_history, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction: Transaction = transactions[position]
        holder.Transaction_successful_to.text="${transaction.receiverEmail}"
        holder.TransactionType.text = "${transaction.transactionType}"
        holder.Transactionamount.text = "${transaction.amount}"
        holder.TransactionDate.text="${transaction.transactionDate}"
    }

    override fun getItemCount(): Int {
        return transactions.size
    }
    fun updateData(newTransactions: List<Transaction>?) {
        transactions = newTransactions ?: emptyList()
        notifyDataSetChanged()
    }
}
