package com.example.skillswaps.backend

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.skillswaps.dataclasses.CommunityPost
import com.example.skillswaps.dataclasses.User

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions

import kotlinx.coroutines.tasks.await

class FirestoreHelper {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    private val usersCollection = db.collection("data")
    suspend fun getFriendRequestsList(): List<String> {
        val currentUid = auth.currentUser?.uid ?: return emptyList()
        val snap = db.collection("data")
            .document(currentUid)
            .get()
            .await()
        @Suppress("UNCHECKED_CAST")
        return snap.get("friendRequest") as? List<String> ?: emptyList()
    }
    fun acceptFriendRequest(otherUid: String, context: Context, onDone: () -> Unit = {}) {
        val currentUid = auth.currentUser?.uid ?: return
        val meRef = usersCollection.document(currentUid)
        val youRef = usersCollection.document(otherUid)

        db.runTransaction { tx ->
            // Read current state
            val meSnap = tx.get(meRef)
            val youSnap = tx.get(youRef)

            // Build updated lists
            val incoming = (meSnap.get("friendRequest") as? List<String>).orEmpty().toMutableList()
            val myFriends = (meSnap.get("friends") as? List<String>).orEmpty().toMutableList()
            val yourFriends = (youSnap.get("friends") as? List<String>).orEmpty().toMutableList()

            // 1) Remove the request
            incoming.remove(otherUid)
            // 2) Add each other to friends
            if (!myFriends.contains(otherUid)) myFriends.add(otherUid)
            if (!yourFriends.contains(currentUid)) yourFriends.add(currentUid)

            // Queue updates
            tx.update(
                meRef, mapOf(
                    "friendRequest" to incoming,
                    "friends" to myFriends
                )
            )
            tx.update(
                youRef, mapOf(
                    "friends" to yourFriends
                )
            )
            null
        }
            .addOnSuccessListener {
                Toast.makeText(context, "Friend request accepted!", Toast.LENGTH_SHORT).show()
                onDone()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
        fun updateField(
        collection: String,
        docId: String,
        field: String,
        value: Any,
        onComplete: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        db.collection(collection)
            .document(docId)
            .update(field, value)
            .addOnSuccessListener { onComplete?.invoke() }
            .addOnFailureListener { e -> onError?.invoke(e) }
    }

    fun storeUserData(user: User, context: Context) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val docRef = db.collection("data").document(userId)

            // Use set with merge to avoid losing previous data
            docRef.set(user, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(context, "Successfully added/updated user", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error in adding/updating user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
    suspend fun getUserData(): User? {


        return if (userId != null) {
            try {
                val snapshot = db.collection("data").document(userId).get().await()
                if (snapshot.exists()) {
                    snapshot.toObject(User::class.java)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    fun updateUserFields(
        updates: Map<String, Any>,
        context: Context
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("data")
            .document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "User fields updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating fields: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    fun logoutUser(context: Context,navController: NavController) {
        val auth = FirebaseAuth.getInstance()
        try {
            auth.signOut()

            // Optional: Provider-specific sign-out
            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            navController.navigate("signin"){
                popUpTo("home"){inclusive=true}
            }



        } catch (e: Exception) {
            Log.e("Logout", "Failed to sign out", e)
            // Show error to user
        }
    }
    //Paging through all the user by sorting them on the basis of the
    suspend fun getUsersPage(
        pageSize: Long,
        lastSnapshot: DocumentSnapshot?
    ): Pair<List<User>, DocumentSnapshot?> {
        return try {
            var query = usersCollection
                .orderBy("upvotes", Query.Direction.DESCENDING)
                .limit(pageSize)

            if (lastSnapshot != null) {
                query = query.startAfter(lastSnapshot)
            }

            val snapshot = query.get().await()
            val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            val last = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
            Pair(users, last)
        } catch (e: Exception) {
            // In case of error, return empty list and no lastSnapshot
            Pair(emptyList(), null)
        }
    }
    // For Liking and Disliking User

    suspend fun setUpvote(targetUid: String, shouldUpvote: Boolean) {
        // Get current user UID; if absent, do nothing
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return

        // Prevent self-upvote (optional)
        if (currentUid == targetUid) return

        val currentRef = usersCollection.document(currentUid)
        val targetRef  = usersCollection.document(targetUid)

        db.runTransaction { tr ->
            // Read current user's likedUsers array and target's existing upvotes
            val currentSnap = tr.get(currentRef)
            val likedList = currentSnap.get("likedUsers") as? List<*> ?: emptyList<Any>()
            val alreadyLiked = likedList.contains(targetUid)

            if (shouldUpvote) {
                if (!alreadyLiked) {
                    // Add to likedUsers and increment target upvotes
                    tr.update(currentRef, "likedUsers", FieldValue.arrayUnion(targetUid))
                    tr.update(targetRef,  "upvotes",     FieldValue.increment(1))
                }
                // else: already upvoted, do nothing
            } else {
                // should remove upvote
                if (alreadyLiked) {
                    tr.update(currentRef, "likedUsers", FieldValue.arrayRemove(targetUid))
                    tr.update(targetRef,  "upvotes",     FieldValue.increment(-1))
                }
                // else: not upvoted already, do nothing
            }
        }.await()
    }
    suspend fun getUserByUid(uid: String): User? {
        return try {
            // Try to get the document snapshot
            val docSnap = db.collection("data")
                .document(uid)
                .get()
                .await()

            // If it exists, convert to User; otherwise return null
            if (docSnap.exists()) {
                docSnap.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreHelper", "Error fetching user $uid", e)
            null
        }
    }
    suspend fun setDownvote(targetUid: String, shouldDownvote: Boolean) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (currentUid == targetUid) return

        val currentRef = usersCollection.document(currentUid)
        val targetRef  = usersCollection.document(targetUid)

        db.runTransaction { tr ->
            val currentSnap = tr.get(currentRef)
            // Get existing lists (or empty)
            val dislikedList = currentSnap.get("dislikedUsers") as? List<*> ?: emptyList<Any>()
            val likedList    = currentSnap.get("likedUsers")    as? List<*> ?: emptyList<Any>()
            val alreadyDown  = dislikedList.contains(targetUid)
            val alreadyUp    = likedList.contains(targetUid)

            if (shouldDownvote) {
                if (!alreadyDown) {
                    // Add to dislikedUsers and increment downvotes
                    tr.update(currentRef, "dislikedUsers", FieldValue.arrayUnion(targetUid))
                    tr.update(targetRef,  "downvotes",     FieldValue.increment(1))
                    // If previously upvoted, remove upvote
                    if (alreadyUp) {
                        tr.update(currentRef, "likedUsers", FieldValue.arrayRemove(targetUid))
                        tr.update(targetRef,  "upvotes",     FieldValue.increment(-1))
                    }
                }
                // else already downvoted: do nothing
            } else {
                // Remove downvote if existed
                if (alreadyDown) {
                    tr.update(currentRef, "dislikedUsers", FieldValue.arrayRemove(targetUid))
                    tr.update(targetRef,  "downvotes",     FieldValue.increment(-1))
                }
                // else not downvoted: do nothing
            }
        }.await()
    }
    //Liked User fetching
    suspend fun getCurrentUserLikedIds(): List<String> {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("data")
                .document(currentUid)
                .get()
                .await()
            // Assuming the field in Firestore is named "likedUsers" and is an array of strings
            @Suppress("UNCHECKED_CAST")
            snapshot.get("likedUsers") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            // log if desired
            emptyList()
        }
    }
    fun swapSkills(
        currUserId: String,
        targetUserId: String,
        availabilitySlots: List<String>,
        context: Context
    ) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("data")

        val currUserRef   = usersCollection.document(currUserId)
        val targetUserRef = usersCollection.document(targetUserId)

        db.runBatch { batch ->
            // 1) Add current user to 'requestReceived' of target
            batch.update(
                targetUserRef,
                "requestReceived",
                FieldValue.arrayUnion(currUserId)
            )

            // 2) Add target user to 'requestSent' of current
            batch.update(
                currUserRef,
                "requestSent",
                FieldValue.arrayUnion(targetUserId)
            )

            // 3) Increment swap count of current user
            batch.update(
                currUserRef,
                "swapCount",
                FieldValue.increment(1)
            )

            // 4) Replace current user's availability with the new List<String>
            //    This will overwrite the entire 'availability' array in Firestore.
            batch.update(
                currUserRef,
                "availability",
                availabilitySlots
            )
        }
            .addOnSuccessListener {
                Toast.makeText(context, "Swap requested & availability updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }



    suspend fun getUsersByIds(uids: List<String>): List<User> {
        if (uids.isEmpty()) return emptyList()
        return try {
            val querySnapshot: QuerySnapshot = db.collection("data")
                .whereIn(FieldPath.documentId(), uids)
                .get()
                .await()
            // Map to User data class. Make sure User has a no-arg constructor or uses @PropertyName etc.
            querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)
            }
        } catch (e: Exception) {
            // log if desired
            emptyList()
        }
    }

    suspend fun createCommunityPost(post: CommunityPost): String? {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return null

        val userDoc = db.collection("data").document(currentUid).get().await()
        val firstName = userDoc.getString("firstName") ?: ""
        val lastName  = userDoc.getString("lastName") ?: ""
        val authorName = "$firstName $lastName".trim()
        val authorImageUrl = userDoc.getString("imageUrl") ?: ""

        val data = hashMapOf<String, Any>(
            "authorId"        to currentUid,
            "authorName"      to authorName,
            "authorImageUrl"  to authorImageUrl,
            "type"            to post.type,
            "category"        to post.category,
            "title"           to post.title,
            "content"         to post.content,
            "tags"            to post.tags,
            "location"        to post.location,
            "timestamp" to FieldValue.serverTimestamp(),
            "likeCount"       to 0,
            "commentCount"    to 0
        )

        if (post.type == "exchange") {
            post.offer?.let { data["offer"] = it }
            post.want?.let { data["want"] = it }
        }

        val docRef = db.collection("communityPosts").document()
        data["id"] = docRef.id
        docRef.set(data).await()
        return docRef.id
    }



    suspend fun getCommunityPostsByTypePage(
        type: String,
        pageSize: Int,
        lastDocument: DocumentSnapshot?
    ): Pair<List<CommunityPost>, DocumentSnapshot?> {
        return try {
            // Build query: where type == ..., orderBy timestamp desc
            var q: Query = db.collection("communityPosts")
                .whereEqualTo("type", type)
                .orderBy("timestamp", Query.Direction.DESCENDING)

            if (lastDocument != null) {
                q = q.startAfter(lastDocument)
            }
            q = q.limit(pageSize.toLong())

            val snap = q.get().await()
            val posts = snap.documents.mapNotNull { doc ->
                // toObject must match your data class fields exactly
                doc.toObject(CommunityPost::class.java)?.copy(id = doc.id)
            }
            Pair(posts, snap.documents.lastOrNull())
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                // Composite index missing or still building
                Log.w("FirestoreHelper", "Index for (type+timestamp) missing/building", e)
                Pair(emptyList(), null)
            } else {
                throw e
            }
        }
    }

    /**
     * Fetch a page of CommunityPost filtered by both `type` and `category`, ordered by timestamp descending.
     */
    suspend fun getCommunityPostsByTypeAndCategoryPage(
        type: String,
        category: String,
        pageSize: Int,
        lastDocument: DocumentSnapshot?
    ): Pair<List<CommunityPost>, DocumentSnapshot?> {
        return try {
            var q: Query = db.collection("communityPosts")
                .whereEqualTo("type", type)
                .whereEqualTo("category", category)
                .orderBy("timestamp", Query.Direction.DESCENDING)

            if (lastDocument != null) {
                q = q.startAfter(lastDocument)
            }
            q = q.limit(pageSize.toLong())

            val snap = q.get().await()
            val posts = snap.documents.mapNotNull { doc ->
                doc.toObject(CommunityPost::class.java)?.copy(id = doc.id)
            }
            Pair(posts, snap.documents.lastOrNull())
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                Log.w("FirestoreHelper", "Index for (type+category+timestamp) missing/building", e)
                Pair(emptyList(), null)
            } else {
                throw e
            }
        }
    }






    /* ----------  READ (single)  ---------- */
    suspend fun getCommunityPostById(postId: String): CommunityPost? {
        val doc = db.collection("communityPosts").document(postId).get().await()
        return doc.toObject(CommunityPost::class.java)?.copy(id = doc.id)
    }


    suspend fun getRequestReceivedList(): List<String> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snap = db.collection("data")
            .document(uid)
            .get()
            .await()

        @Suppress("UNCHECKED_CAST")
        return snap.get("requestReceived") as? List<String> ?: emptyList()
    }
    suspend fun getSwapDoneList(): List<String> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snap = db.collection("data")
            .document(uid)
            .get()
            .await()

        @Suppress("UNCHECKED_CAST")
        return snap.get("swapDone") as? List<String> ?: emptyList()
    }
    // 2. Batch‐fetch other users by DOCUMENT ID, not by a field
    suspend fun getUsersFromIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()

        val all = mutableListOf<User>()
        userIds
            .chunked(10)
            .forEach { chunk ->
                val snap = db.collection("data")
                    // this matches the _document_ ID, not a field inside
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                snap.documents
                    .mapNotNull { it.toObject(User::class.java) }
                    .also(all::addAll)
            }
        return all
    }

    fun acceptSwapRequest(

        otherUserId: String
    ): Task<Void> {
        val currentUserId = auth.currentUser?.uid ?: return Tasks.forCanceled()
        val dataCol = db.collection("data")
        val currentUserRef = dataCol.document(currentUserId)
        val otherUserRef   = dataCol.document(otherUserId)

        return db.runTransaction { tx ->
            // 1) Read both documents
            val currentSnap = tx.get(currentUserRef)
            val otherSnap   = tx.get(otherUserRef)

            // 2) Extract and mutate lists
            val currentRequests: MutableList<String> =
                (currentSnap.get("requestReceived") as? List<String>).orEmpty().toMutableList()
            val currentSwaps: MutableList<String> =
                (currentSnap.get("swapDone") as? List<String>).orEmpty().toMutableList()

            val otherRequests: MutableList<String> =
                (otherSnap.get("requestSent") as? List<String>).orEmpty().toMutableList()
            val otherSwaps: MutableList<String> =
                (otherSnap.get("swapDone") as? List<String>).orEmpty().toMutableList()

            // Remove each other from pending lists
            currentRequests.remove(otherUserId)
            otherRequests.remove(currentUserId)

            // Add each other to completed swaps
            currentSwaps.add(otherUserId)
            otherSwaps.add(currentUserId)

            // 3) Queue the updates
            tx.update(currentUserRef, mapOf(
                "requestReceived" to currentRequests,
                "swapDone"       to currentSwaps
            ))
            tx.update(otherUserRef, mapOf(
                "requestSent" to otherRequests,
                "swapDone"    to otherSwaps
            ))

            // transaction returns null on success
            null
        }
    }

}