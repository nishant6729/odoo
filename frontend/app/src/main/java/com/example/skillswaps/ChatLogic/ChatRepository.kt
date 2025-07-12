
package com.example.skillswaps.ChatLogic

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.android.gms.tasks.Task

/**
 * A repository for sending and observing chat messages between two users.
 */
object ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val chats = db.collection("chats")

    // Deterministically combine two UIDs to form a chat document ID
    private fun chatId(uid1: String, uid2: String): String =
        listOf(uid1, uid2).sorted().joinToString("_")

    /**
     * Listen in real time to the message stream between currentUser and otherUser.
     * Returns the ListenerRegistration so you can remove it when no longer needed.
     */
    fun observeChat(
        currentUid: String,
        otherUid: String,
        onMessages: (List<ChatMessage>) -> Unit
    ): ListenerRegistration {
        val id = chatId(currentUid, otherUid)
        return chats
            .document(id)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                val msgs = snap.documents.mapNotNull { it.toChatMessage() }
                onMessages(msgs)
            }
    }

    /**
     * Sends a message and returns its Task<DocumentReference> once the write has been enqueued.
     * Can be called directly from any thread (e.g. UI onClick).
     */
    fun sendMessage(
        currentUid: String,
        otherUid: String,
        text: String
    ): Task<DocumentReference> {
        val id = chatId(currentUid, otherUid)
        val msg = ChatMessage(
            senderId = currentUid,
            text = text,
            timestamp = FieldValue.serverTimestamp()
        )
        return chats
            .document(id)
            .collection("messages")
            .add(msg)
    }
}

/**
 * Simple data class representing a chat message.
 */
data class ChatMessage(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Any? = null
)

/**
 * Extension to convert Firestore DocumentSnapshot into ChatMessage.
 */
fun DocumentSnapshot.toChatMessage(): ChatMessage? {
    return try {
        ChatMessage(
            senderId = getString("senderId") ?: return null,
            text = getString("text") ?: return null,
            timestamp = get("timestamp")
        )
    } catch (e: Exception) {
        null
    }
}

