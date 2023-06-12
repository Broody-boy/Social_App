package com.example.socialapp.daos

import android.util.Log
import android.widget.Toast
import com.example.socialapp.models.Post
import com.example.socialapp.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao {
    val db = FirebaseFirestore.getInstance()
    val postsCollections = db.collection("posts")
    val auth = Firebase.auth

    fun addPost(text: String) {

        val currentUserId = auth.currentUser!!.uid

        GlobalScope.launch {
            val userDao = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!

            val currentTime = System.currentTimeMillis()
            val post = Post(text, user, currentTime)
            postsCollections.document().set(post)
        }
    }
}