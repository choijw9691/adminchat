package com.project.adminchat.common

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.firestore.FirebaseFirestore
import com.project.adminchat.model.UserEntity

import com.google.android.gms.tasks.Tasks
import com.project.adminchat.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Context.getUser(uid: String,db: FirebaseFirestore): UserEntity? {
    LoadingDialog.show(this)
    return try {
        val userRef = db.collection("users").document(uid)
        val documentSnapshot = Tasks.await(userRef.get())
        if (documentSnapshot.exists()) {
            documentSnapshot.toObject(UserEntity::class.java)
        } else {
            null
        }
    } catch (exception: Exception) {
        // Handle any errors that occurred
        null
    } finally {
        LoadingDialog.dismiss()
    }
}
fun LifecycleOwner.repeatCollectOnStarted(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED, block)
    }
}
fun getProfileImage(image:Int):Int{
  return when(image){
        0->{
            R.drawable.background_circle
        }
        1->{
            R.drawable.background_circle
        }
        2->{
            R.drawable.background_circle
        }
        else->{
            R.drawable.background_circle
        }
    }
}





