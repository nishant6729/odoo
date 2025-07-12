package com.example.skillswaps.dataclasses

import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    var phone: String="",
    var email: String = "",
    var skills: List<String> = emptyList(),
    var occupation: String = "",
    var location: String = "",
    var experience: String = "",
    var dob: String = "",
    var description: String = "",
    var achievements: String = "",
    var worklink: String = "",
    val imageUrl:String="",
    val likedUsers: List<String> = emptyList(),   // list of user IDs this user has liked
    val dislikedUsers: List<String> = emptyList(), // optional
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    var requestReceived: List<String> = emptyList(),
    var requestSent: List<String> = emptyList(),
    var swapDone: List<String> = emptyList(),
    var swapCount: Long=0,
    var availability: List<String> = emptyList(),
    var publicId:Boolean=true,
    var friendRequest: List<String> = emptyList(),
    var friends: List<String> =emptyList()
)
data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),  // always 2 UIDs
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L
)
data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val messageType: String = "text"  // can be "text", "image", etc.
)
data class CommunityPost(
    // Document ID in Firestore will be assigned automatically or you can set it as the field "id"
    var id: String = "",        // will set after creation (doc.id)
    val authorId: String = "",  // UID of poster
    val authorName: String = "",// optional: store display name at creation time
    val authorImageUrl: String = "", // optional: store profile image URL at creation
    val imageUrl: String="",
    val type: String = "",      // "exchange", "request", "discussion"
    val category: String = "",  // e.g. "Programming", "Design", etc.
    val title: String = "",     // short summary or question

    // For "exchange" posts:
    val offer: String? = null,  // e.g., "I can teach Java"
    val want: String? = null,   // e.g., "Looking to learn UI/UX"

    // For "request"/"offer" feed: you can use `title` + `content` or reuse offer/want fields
    val content: String = "",   // longer description

    val tags: List<String> = emptyList(), // e.g. ["Java", "UIUX", "BeginnerFriendly"]

    // Filters:
    val location: String = "",        // e.g. city or "Remote"
    val commentCount: Int = 0, // ✅ ADD THIS FIELD if Firestore has it

    @ServerTimestamp
    val timestamp: Timestamp? = null


)
