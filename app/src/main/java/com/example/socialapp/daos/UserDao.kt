package com.example.socialapp.daos

import com.example.socialapp.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao {
    val db = FirebaseFirestore.getInstance()
    val usersCollection = db.collection("users")

    fun addUser(user: User?) {
        GlobalScope.launch(Dispatchers.IO) {
            user?.let {
                usersCollection.document("$user.uid").set(it)
            }
        }
    }
}