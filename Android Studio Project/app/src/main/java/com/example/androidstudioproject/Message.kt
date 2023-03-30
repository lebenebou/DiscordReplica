package com.example.androidstudioproject

data class Message(
    val username: String,
    val message: String,
    val time: String
)

// DataRepository.kt
object DataRepository {
    val messageList = mutableListOf<Message>(
        Message("John Doe", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "3:25 PM"),
        Message("Jane Smith", "Sed ut perspiciatis unde omnis iste natus error sit voluptatem", "3:27 PM"),
        Message("Bob Williams", "At vero eos et accusamus et iusto odio dignissimos ducimus", "3:29 PM"),
        Message("Sarah Johnson", "Nam libero tempore, cum soluta nobis est eligendi optio cumque", "3:30 PM"),
    )
}

