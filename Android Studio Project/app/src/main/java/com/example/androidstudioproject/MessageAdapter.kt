package com.example.androidstudioproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: MutableList<Message>) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val userProfilePicture: ImageView = itemView.findViewById(R.id.user_profile_picture)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]

        holder.usernameTextView.text = message.username
        holder.messageTextView.text = message.message
        holder.timeTextView.text = message.time
    }

    override fun getItemCount() = messages.size
}
